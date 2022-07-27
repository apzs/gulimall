package com.atguigu.gulimall.search.controller;

import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author 无名氏
 * @date 2022/7/22
 * @Description:
 */
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    /**
     * 自动将页面提交过来的所有请求查询参数封装成指定的对象
     * @return
     */
    @RequestMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model){

        SearchResult result = mallSearchService.search(searchParam);
        model.addAttribute("result",result);
        return "list";
    }
}
