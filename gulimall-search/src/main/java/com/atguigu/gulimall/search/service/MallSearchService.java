package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

/**
 * @author 无名氏
 * @date 2022/7/22
 * @Description:
 */
public interface MallSearchService {

    /**
     * 查询商品
     * @param searchParam 检索的参数
     * @return 根据检索的参数查询到的结果，包含页面需要的所有信息
     */
    public SearchResult search(SearchParam searchParam);
}
