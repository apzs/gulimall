package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.product.AttrEnum;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    CategoryDao categoryDao;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    CategoryService categoryService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        //将attr中的数据复制到attrEntity对应的字段里
        BeanUtils.copyProperties(attr, attrEntity);
        //1、保存基本数据
        this.save(attrEntity);
        //2、如果是基本属性,则还要保存关联关系
        if (attr.getAttrType() == AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId()!=null) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            //调用this.save(attrEntity);方法后，会将数据库生成的attrId封装到AttrEntity里面
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long categoryId, String attrType) {
        LambdaQueryWrapper<AttrEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //判断是"基本属性"还是"销售属性"
        lambdaQueryWrapper.eq(AttrEntity::getAttrType,
                "base".equalsIgnoreCase(attrType) ?
                        AttrEnum.ATTR_TYPE_BASE.getCode() :
                        AttrEnum.ATTR_TYPES_SALE.getCode());
        if (categoryId != 0) {
            //如果有categoryId，则查该categoryId的数据
            lambdaQueryWrapper.eq(AttrEntity::getCatelogId, categoryId);
        }

        String key = (String) params.get("key");
        if (StringUtils.hasLength(key)) {
            lambdaQueryWrapper.and((obj) -> {
                //如果有查询条件，则判断该条件是否 与attrId相等 或 名字包含该条件
                obj.eq(AttrEntity::getAttrId, key).or().like(AttrEntity::getAttrName, key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), lambdaQueryWrapper);

        //查询categoryName字段和groupName字段
        List<AttrEntity> list = page.getRecords();
        List<AttrRespVo> respVos = list.stream().map(attrEntity -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            //根据attrId到attr和attrgroup的中间表查询 attrgroupId
            LambdaQueryWrapper<AttrAttrgroupRelationEntity> attrAttrgroupRelationQueryWrapper = new LambdaQueryWrapper<>();
            attrAttrgroupRelationQueryWrapper.eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId());
            //根据attrgroupId查询中间表的该行数据，并封装到对象
            if ("base".equalsIgnoreCase(attrType)) {
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(attrAttrgroupRelationQueryWrapper);
                if (attrAttrgroupRelationEntity != null && attrAttrgroupRelationEntity.getAttrGroupId()!=null) {
                    //如果查到attrgroupId，则根据attrgroupId查询attrgroupName，并添加到attrRespVo中
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            LambdaQueryWrapper<CategoryEntity> categoryQueryWrapper = new LambdaQueryWrapper<>();
            categoryQueryWrapper.eq(CategoryEntity::getCatId, attrEntity.getCatelogId());
            //根据catelogId查询该行数据，并封装到对象
            CategoryEntity categoryEntity = categoryDao.selectOne(categoryQueryWrapper);
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            return attrRespVo;
        }).collect(Collectors.toList());

        PageUtils pageUtils = new PageUtils(page);
        //重新给数据
        pageUtils.setList(respVos);
        return pageUtils;
    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrRespVo attrRespVo = new AttrRespVo();
        //根据attrId到attr表中查该行数据
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrRespVo);

        //如果是基本属性，需要设置分组信息
        if (attrEntity.getAttrType() == AttrEnum.ATTR_TYPE_BASE.getCode()) {
            LambdaQueryWrapper<AttrAttrgroupRelationEntity> attrAttrgroupRelationQueryWrapper = new LambdaQueryWrapper<>();
            attrAttrgroupRelationQueryWrapper.eq(AttrAttrgroupRelationEntity::getAttrId, attrId);
            //根据attrId到attrAttrgroupRelation关联关系表里查attrGroupId
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(attrAttrgroupRelationQueryWrapper);
            if (attrAttrgroupRelationEntity != null) {
                attrRespVo.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());
                //根据attrGroupId到attrGroup表里查groupName
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                if (attrGroupEntity != null) {
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }

        //根据最后一级分类id到categoryService中查询完整三级分类id
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        if (catelogPath != null) {
            attrRespVo.setCatelogPath(catelogPath);
        }
        return attrRespVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);

        //如果是基本属性，就更新或添加关联关系
        if (attr.getAttrType() == AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId()!=null) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());

            LambdaQueryWrapper<AttrAttrgroupRelationEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId());
            Integer count = attrAttrgroupRelationDao.selectCount(lambdaQueryWrapper);
            //如果有attr和attrgroup的关联关系就修改该，没有就新增
            if (count > 0) {
                //根据attr_id修改 pms_attr_attrgroup_relation 里的attr_group_id 字段
                attrAttrgroupRelationDao.update(attrAttrgroupRelationEntity, lambdaQueryWrapper);
            } else {
                //添加attr和attrgroup的关联关系
                attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());
                attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
            }
        }
    }

    /**
     * 根据分组的id查找关联的所有基本属性
     * @param attrgroupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        LambdaQueryWrapper<AttrAttrgroupRelationEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AttrAttrgroupRelationEntity::getAttrGroupId,attrgroupId);
        List<AttrAttrgroupRelationEntity> entities = attrAttrgroupRelationDao.selectList(lambdaQueryWrapper);

        if (entities.size()<=0){
            return null;
        }

        Set<Long> attrIds = entities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toSet());

        Collection<AttrEntity> attrEntities = this.listByIds(attrIds);

        return (List<AttrEntity>) attrEntities;
    }

    /**
     * delete from gulimall_pms.pms_attr_attrgroup_relation
     * where (attr_id = 1 and attr_group_id=1)
     * or (attr_id = 2 and attr_group_id=2)
     * or (attr_id = 3 and attr_group_id=3);
     * @param attrGroupRelationVos
     */
    @Override
    public void deleteRelation(AttrGroupRelationVo[] attrGroupRelationVos) {
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = Arrays.stream(attrGroupRelationVos).map((attrGroupRelationVo -> {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(attrGroupRelationVo, attrAttrgroupRelationEntity);
            return attrAttrgroupRelationEntity;
        })).collect(Collectors.toList());

        attrAttrgroupRelationDao.deleteBatchRelation(attrAttrgroupRelationEntities);
    }

    /**
     * 查询本分类下，没有被其他分组关联的属性
     * (比方说"主机"属性分组查询 "手机/手机通讯/手机"下的未被其他属性分组关联的基本属性)
     * localhost:88/api/product/attrgroup/1/noattr/relation?t=1652878342763&page=1&limit=10&key=
     * @param attrgroupId 属性分组id
     * @param params      分页参数
     * @return            分页对象
     */
    @Override
    public PageUtils getNoRelationAttr(Long attrgroupId, Map<String, Object> params) {
        //1、查询该attrgroupId的catelogId(当前分组只能关联自己所属的分类里面的所有属性)
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        //2、当前分组只能关联本分类下的其他分组没有引用的属性
        //2.1)、当前分类下的所有分组(包括本分组，因为本分组已经关联的属性，不能再次关联)
        LambdaQueryWrapper<AttrGroupEntity> attrGroupQueryWrapper = new LambdaQueryWrapper<>();
        //ne：not equal
        //attrGroupQueryWrapper.eq(AttrGroupEntity::getCatelogId,catelogId).ne(AttrGroupEntity::getAttrGroupId,attrgroupId);
        attrGroupQueryWrapper.eq(AttrGroupEntity::getCatelogId,catelogId);
        List<AttrGroupEntity> otherGroups = attrGroupDao.selectList(attrGroupQueryWrapper);
        List<Long> otherAttrGroupIds = otherGroups.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
        LambdaQueryWrapper<AttrEntity> attrQueryWrapper = new LambdaQueryWrapper<>();
        attrQueryWrapper.eq(AttrEntity::getCatelogId, catelogId);
        attrQueryWrapper.eq(AttrEntity::getAttrType, AttrEnum.ATTR_TYPE_BASE.getCode());
        //2.2)、如果有其他分组，则查询这些分组关联的属性
        //otherAttrGroupIds!=null && 有些多余
        if (otherAttrGroupIds!=null && otherAttrGroupIds.size()>0) {
            LambdaQueryWrapper<AttrAttrgroupRelationEntity> attrAttrgroupRelationQueryWrapper = new LambdaQueryWrapper<>();
            attrAttrgroupRelationQueryWrapper.in(AttrAttrgroupRelationEntity::getAttrGroupId, otherAttrGroupIds);
            List<AttrAttrgroupRelationEntity> otherAttrAttrgroupRelations = attrAttrgroupRelationDao.selectList(attrAttrgroupRelationQueryWrapper);
            List<Long> otherAttrIds = otherAttrAttrgroupRelations.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
            //2.3)、如果有已被关联的属性，则从当前分类的所有属性中移除这些已被关联的属性;
            if (otherAttrIds!=null && otherAttrIds.size()>0) {
                attrQueryWrapper.notIn(AttrEntity::getAttrId, otherAttrIds);
            }
        }
        //如果有查询条件，则添加查询条件
        String key = (String) params.get("key");
        if (StringUtils.hasLength(key)){
            attrQueryWrapper.and(item->{
                item.eq(AttrEntity::getAttrId,key).or().like(AttrEntity::getAttrName,key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), attrQueryWrapper);
        return new PageUtils(page);
    }

}