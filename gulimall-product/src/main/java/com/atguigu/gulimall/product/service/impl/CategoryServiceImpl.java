package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sun.xml.internal.bind.v2.TODO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {


    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

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
                .map((menu)->{
                    menu.setChildren(getAllChildren(menu,list));
                    return menu;
                })
                //根据sort字段排序
                .sorted(Comparator.comparingInt((menu)->menu.getSort()!=null?menu.getSort():0))
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

        List<Long> parentPath = findParentPath(catelogId,paths);
        //先加入节点id后再递归求解其父分类，所有求出的完整路径是反的，需要转置一下
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[0]);
    }

    /**
     * 级联更新所有的数据
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category);
    }

    /**
     * 例如：[413,50,5]
     * 根据最后一级分类的id递归求解完整分类id(最后一级分类的所有父分类id+最后一级分类id)
     * @param catelogId 当前分类id
     * @param paths 分类id数组
     * @return  完整分类id
     */
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        Long parentCid = categoryEntity.getParentCid();
        if (parentCid!=0){
            findParentPath(parentCid,paths);
        }
        return paths;
    }


    /**
     * 从list集合中获得当前菜单的子菜单
     * @param root 当前菜单
     * @param list  菜单集合
     * @return
     */
    private List<CategoryEntity> getAllChildren(CategoryEntity root, List<CategoryEntity> list) {
        List<CategoryEntity> collect = list.stream()
                .filter(categoryEntity -> root.getCatId().equals(categoryEntity.getParentCid()))
                //
                .map((menu)->{
                    //递归求解其子菜单
                     menu.setChildren(getAllChildren(menu,list));
                     return menu;
                })
                //根据sort字段排序
                .sorted(Comparator.comparingInt((menu)->menu.getSort()!=null?menu.getSort():0))
                .collect(Collectors.toList());
        return collect;

    }

}