package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 品牌分类关联
 *
 * @author 无名氏
 * @email 2185180175@qq.com
 * @date 2022-04-17 18:19:58
 */
@Mapper
@Repository
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    /**
     *使用MyBatis如果有2个及以上参数 推荐使用@Param("name")为每一个参数起一个自己的名字，否则会很麻烦
     * 1、顺序传参法(非常不推荐)：
     *      public User selectUser(String name, int deptId);
     *     <select id="selectUser" resultMap="UserResultMap">
     *          select * from user
     *          where user_name = #{0} and dept_id = #{1}
     *      </select>
     * 2、@Param注解传参法：
     *      public User selectUser(@Param("userName") String name, int @Param("deptId") deptId);
     *
     *      <select id="selectUser" resultMap="UserResultMap">
     *          select * from user
     *          where user_name = #{userName} and dept_id = #{deptId}
     * </select>
     * 3、Map传参法:
     *      public User selectUser(Map<String, Object> params);
     *
     *      <select id="selectUser" parameterType="java.util.Map" resultMap="UserResultMap">
     *          select * from user
     *          where user_name = #{userName} and dept_id = #{deptId}
     *      </select>
     * 4、Java Bean传参法：
     *      public User selectUser(User params);
     *
     *      <select id="selectUser" parameterType="com.test.User" resultMap="UserResultMap">
     *          select * from user
     *          where user_name = #{userName} and dept_id = #{deptId}
     *      </select>
     * @param category 分类名修改后，品牌和三级分类的关联表的分类名也要修改
     */
    void updateCategory(CategoryEntity category);
}
