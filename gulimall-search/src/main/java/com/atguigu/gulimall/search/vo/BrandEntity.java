package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 品牌
 *
 * @author 无名氏
 * @email 2185180175@qq.com
 * @date 2022-04-17 18:19:58
 */
@Data
public class BrandEntity implements Serializable {

    private Long brandId;

    private String name;

    private String logo;

    private String descript;

    private Integer showStatus;

    private String firstLetter;

    private Integer sort;



}