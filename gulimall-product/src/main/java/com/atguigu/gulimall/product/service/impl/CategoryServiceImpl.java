package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //baseMapper就是ServiceImpl<CategoryDao, CategoryEntity>中的CategoryDao
        //查询所有分类
        List<CategoryEntity> list = baseMapper.selectList(null);
        List<CategoryEntity> topCategory = list.stream()
                //查出一级分类
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                //映射方法，改变对象结构
                .map((menu) -> {
                    menu.setChildren(getAllChildren(menu, list));
                    return menu;
                })
                //根据sort字段排序
                .sorted(Comparator.comparingInt((menu) -> menu.getSort() != null ? menu.getSort() : 0))
                //搜集
                .collect(Collectors.toList());

        return topCategory;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 检查当前删除的菜单是否被别的地方引用

        //逻辑删除(show_status作为标志位，置为0表示删除)
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();

        List<Long> parentPath = findParentPath(catelogId, paths);
        //先加入节点id后再递归求解其父分类，所有求出的完整路径是反的，需要转置一下
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[0]);
    }

    /**
     * 级联更新所有的数据
     *@CacheEvict: 失效模式
     *@CachePut(value = {"category"}, key = "'level1Categories'") : 双写模式
     * @Caching: 批量操作
     * @CacheEvict(value = {"category"},allEntries = true) 删除该分区的所有数据
     * 存储同一类型的数据，都可以指定成同一个分区.分区名默认就是缓存的前缀
     * @param category
     */
    //@Caching(evict = {
    //        @CacheEvict(value = {"category"}, key = "'level1Categories'"),
    //        @CacheEvict(value = {"category"}, key = "'getCatalogJson'")
    //})
    @CacheEvict(value = {"category"},allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category);
    }

    /**
     * 1、每一个需要缓存的数据我们都来指定要放到那个名字的缓存。[ 缓存的分区(按照业务类型分)]
     * 2、@Cacheable({"category"})
     *   代表当前方法的结果需要缓存，如果缓存中有，方法不用调用。
     *   如果缓存中没有，会调用方法，最后将方法的结果放入缓存
     * 3、默认行为
     *   1)、如果缓存中有，方法不用调用。
     *   2)、key默认自动生成;缓存的名字::SimpleKey []( 自主生成的key值)
     *   3)、缓存的value的值。默认使用jdk序列化机制，将序列化后的数据存到redis
     * 4)、默认ttl时间-1; .
     * 自定义:
     *   1)、指定生成的缓存使用的key:
     *     key属性指定，接受一个SpEL
     *     SpEL的详细https ://docs.spring. io/spring/docs/5.1.12. REL EASE/spring-framework-re.
     *   2)、指定缓存的数据的存活时间:配置文件中 修改ttL
     *   3)、将数据保存为json格式
     **/

    @Cacheable(value = {"category"}, key = "'level1Categories'",sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categories() {
        System.out.println("getLevel1Categories...");
        LambdaQueryWrapper<CategoryEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(CategoryEntity::getParentCid, 0);
        lambdaQueryWrapper.select(CategoryEntity::getCatId, CategoryEntity::getName);
        //long start = System.currentTimeMillis();
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(lambdaQueryWrapper);
        //long end = System.currentTimeMillis();
        //System.out.println("消耗时间："+(end-start));
        return categoryEntities;
    }

    @Cacheable(value = "category",key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        System.out.println("查询了数据库......");
        //一次查询所有
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);
        //1、查出所有一级分类
        List<CategoryEntity> level1Categories = this.getLevel1Categories();
        Map<String, List<Catelog2Vo>> result = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), l1 -> {
            //2、该一级分类的所有二级分类
            List<CategoryEntity> category2Entities = getCategoryEntities(categoryEntities, l1);
            List<Catelog2Vo> catelog2VoList = null;
            if (category2Entities != null) {
                catelog2VoList = category2Entities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo();
                    catelog2Vo.setCatalog1Id(l1.getCatId().toString());
                    catelog2Vo.setId(l2.getCatId().toString());
                    catelog2Vo.setName(l2.getName());
                    //3、当前二级分类的所有三级分类
                    List<CategoryEntity> category3Entities = getCategoryEntities(categoryEntities, l2);
                    List<Catelog2Vo.Catelog3Vo> catelog3VoList = null;
                    if (category3Entities != null) {
                        catelog3VoList = category3Entities.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo();
                            catelog3Vo.setId(l3.getCatId().toString());
                            catelog3Vo.setName(l3.getName());
                            catelog3Vo.setCatalog2Id(l2.getCatId().toString());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                    }
                    catelog2Vo.setCatalog3List(catelog3VoList);
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2VoList;
        }));
        return result;
    }

    /**
     * 1、空结果缓存:解诀缓存穿透
     * 2、设置过期时间(加随机值) :解诀缓存雪崩
     * 3、加锁:解决缓存击穿
     */
    public Map<String, List<Catelog2Vo>> getCatalogJson2() {
        //1、加入缓存逻辑,缓存中存的数据是json字符串。
        //JSON跨语言，跨平台兼容。
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String catalogJson = ops.get("catalogJson");
        if (!StringUtils.hasText(catalogJson)) {
            //2、缓存中没有,查询数据库
            System.out.println("缓存不命中...查询数据库...");
            Map<String, List<Catelog2Vo>> catalogJsonForDb = getCatalogJsonForDbWithRedisson();
            return catalogJsonForDb;
        }
        System.out.println("缓存命中...直接返回");
        TypeReference<Map<String, List<Catelog2Vo>>> typeReference = new TypeReference<Map<String, List<Catelog2Vo>>>() {
        };
        return JSON.parseObject(catalogJson, typeReference);
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonForDbWithRedisson() {
        //1、锁的名字。锁的粒度， 越细越快。
        //锁的粒度:具体缓存的是某个数据，11-号商品; product- 11-lock product-12-lock
        RLock lock = redissonClient.getLock("catalogJson-lock");
        lock.lock();
        Map<String, List<Catelog2Vo>> catalogJsonForDb;
        try {
            catalogJsonForDb = getCatalogJsonForDb();
        } finally {
            lock.unlock();
        }
        return catalogJsonForDb;
    }


    public Map<String, List<Catelog2Vo>> getCatalogJsonForDbWithRedisLock() {
        //获取redis锁
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);
        if (lock) {
            Map<String, List<Catelog2Vo>> catalogJsonForDb;
            try {
                catalogJsonForDb = getCatalogJsonForDb();
            } finally {
                //删除锁
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then  return redis.call('del',KEYS[1]) else return 0 end";
                // KEYS[1] 为 Arrays.asList("lock") ；ARGV[1] 为 uuid
                Long lockValue = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class)
                        , Arrays.asList("lock"), uuid);
                if (lockValue != null && lockValue == 1) {
                    System.out.println("删除成功...");
                } else {
                    System.out.println("删除失败...");
                }
            }
            return catalogJsonForDb;
        } else {
            //加锁失败，休眠100ms后重试，synchronized
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //自旋锁
            return getCatalogJsonForDbWithRedisLock();
        }
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonForDbWithLocalLock() {
        //得到锁以后，我们应该再去缓存中确定一 次，如果没有才需要继续查询(双检锁)
        //只要是同一把锁，就能锁住需要这个锁的所有线程
        //synchronized (this): SpringBoot所有的组件在容器中都是单例的。
        //TODO 本地锁: synchronized, JUC(Lock) 在分布式情况下，想要锁住所有，必须使用分布式锁
        synchronized (this) {
            return getCatalogJsonForDb();
        }
    }

    private Map<String, List<Catelog2Vo>> getCatalogJsonForDb() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String catalogJson = ops.get("catalogJson");
        if (StringUtils.hasText(catalogJson)) {
            TypeReference<Map<String, List<Catelog2Vo>>> typeReference = new TypeReference<Map<String, List<Catelog2Vo>>>() {
            };
            return JSON.parseObject(catalogJson, typeReference);
        }
        System.out.println("查询了数据库......");
        //一次查询所有
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);
        //1、查出所有一级分类
        List<CategoryEntity> level1Categories = this.getLevel1Categories();
        Map<String, List<Catelog2Vo>> result = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), l1 -> {
            //2、该一级分类的所有二级分类
            List<CategoryEntity> category2Entities = getCategoryEntities(categoryEntities, l1);
            List<Catelog2Vo> catelog2VoList = null;
            if (category2Entities != null) {
                catelog2VoList = category2Entities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo();
                    catelog2Vo.setCatalog1Id(l1.getCatId().toString());
                    catelog2Vo.setId(l2.getCatId().toString());
                    catelog2Vo.setName(l2.getName());
                    //3、当前二级分类的所有三级分类
                    List<CategoryEntity> category3Entities = getCategoryEntities(categoryEntities, l2);
                    List<Catelog2Vo.Catelog3Vo> catelog3VoList = null;
                    if (category3Entities != null) {
                        catelog3VoList = category3Entities.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo();
                            catelog3Vo.setId(l3.getCatId().toString());
                            catelog3Vo.setName(l3.getName());
                            catelog3Vo.setCatalog2Id(l2.getCatId().toString());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                    }
                    catelog2Vo.setCatalog3List(catelog3VoList);
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2VoList;
        }));
        //3.查到的数据再放入缓存，将对象转为json放在缓存中
        String s = JSON.toJSONString(result);
        ops.set("catalogJson", s);
        return result;
    }

    private List<CategoryEntity> getCategoryEntities(List<CategoryEntity> categoryEntities, CategoryEntity l) {
        //LambdaQueryWrapper<CategoryEntity> category2QueryWrapper = new LambdaQueryWrapper<>();
        //category2QueryWrapper.eq(CategoryEntity::getParentCid, l1.getCatId());
        //return this.baseMapper.selectList(category2QueryWrapper);
        List<CategoryEntity> collect = categoryEntities.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(l.getCatId());
        }).collect(Collectors.toList());
        return collect;
    }


    /**
     * 例如：[413,50,5]
     * 根据最后一级分类的id递归求解完整分类id(最后一级分类的所有父分类id+最后一级分类id)
     *
     * @param catelogId 当前分类id
     * @param paths     分类id数组
     * @return 完整分类id
     */
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        Long parentCid = categoryEntity.getParentCid();
        if (parentCid != 0) {
            findParentPath(parentCid, paths);
        }
        return paths;
    }


    /**
     * 从list集合中获得当前菜单的子菜单
     *
     * @param root 当前菜单
     * @param list 菜单集合
     * @return
     */
    private List<CategoryEntity> getAllChildren(CategoryEntity root, List<CategoryEntity> list) {
        List<CategoryEntity> collect = list.stream()
                .filter(categoryEntity -> root.getCatId().equals(categoryEntity.getParentCid()))
                //
                .map((menu) -> {
                    //递归求解其子菜单
                    menu.setChildren(getAllChildren(menu, list));
                    return menu;
                })
                //根据sort字段排序
                .sorted(Comparator.comparingInt((menu) -> menu.getSort() != null ? menu.getSort() : 0))
                .collect(Collectors.toList());
        return collect;

    }

}