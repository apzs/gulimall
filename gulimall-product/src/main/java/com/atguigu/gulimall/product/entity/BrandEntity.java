package com.atguigu.gulimall.product.entity;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.ListValue;
import com.atguigu.common.valid.UpdateGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 *
 * @author 无名氏
 * @email 2185180175@qq.com
 * @date 2022-04-17 18:19:58
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 * UpdateGroup不能指定品牌
	 * AddGroup必须指定品牌id
	 */
	@Null(message = "添加不能指定品牌id",groups = {AddGroup.class})
	@NotNull(message = "修改必须指定品牌id",groups = {UpdateGroup.class})
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 * @NotBlank: 不能为空，必须包含一个非空白字符
	 */
	@NotBlank(message = "品牌名不能为空",groups = AddGroup.class)
	private String name;
	/**
	 * 品牌logo地址
	 * @NotEmpty ：The annotated element must not be {@code null} nor empty
	 *              可以接收 CharSequence、Collection、Map、Array 类型
	 * @URL ：必须为一个合法的url地址
	 *
	 * 添加品牌时logo不能为空，并且需要是一个URL
	 * 修改可以为空，但如果不为空则必须为一个URL
	 */
	@NotEmpty(groups = AddGroup.class)
	@URL(message = "Logo必须为一个合法的url地址",groups = {AddGroup.class,UpdateGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 *
	 * @Pattern 不能用在Integer和Long上   因为java中的正则是针对字符串的,只有String才有Patter相关的方法
	 * No validator could be found for constraint 'javax.validation.constraints.Pattern' validating type 'java.lang.Integer'
	 * 因此不能使用该注解来校验: @Pattern(regexp = "^[01]$",message = "显示状态异常",groups = AddGroup.class)
	 *
	 * 不过可以使用这三个注解完成这个功能
	 * @NotNull(groups = AddGroup.class)
	 * @Max(value = 1,groups = {AddGroup.class,UpdateGroup.class})
	 * @Min(value = 0,groups = {AddGroup.class,UpdateGroup.class})
	 */
	@NotNull(groups = AddGroup.class)
	@ListValue(vals = {0,1},groups = {AddGroup.class,UpdateGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotEmpty(groups = AddGroup.class)
	@Pattern(regexp = "^[a-zA-Z]$",message = "首字母必须为单个的大写或小写英文字母",groups = {AddGroup.class,UpdateGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 * @NotNull : The annotated element must not be {@code null}.
	 *        * Accepts any type.
	 */
	@NotNull(groups = AddGroup.class)
	@Min(value = 0,message = "排序字段必须为一个大于0的整数",groups = {AddGroup.class,UpdateGroup.class})
	private Integer sort;



}