package com.atguigu.gulimall.ssoclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 无名氏
 * @date 2022/8/8
 * @Description:
 */
@Controller
public class HelloController {

    @Value("${sso.server.url}")
    String serverUrl;

    /**
     * 不登录就可以访问
     * @return
     */
    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }

    /**
     * 必须登录才可访问
     * 只要带了token，就认为这次是在ssoserver登录成功跳回来的。(当然应该在redis里查一下，这里就不做了)
     * @param model
     * @return
     */
    @GetMapping("/employees")
    public String employees(Model model, HttpSession session,
                            @RequestParam(value = "token",required = false)String token){
        Object loginUser = session.getAttribute("loginUser");
        if (StringUtils.hasText(token)){
            //去ssoserver登录成功跳回来就会带.上
            //TODO 1、去ssoserver获取当前token真正对应的用户信息
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> entity = restTemplate.getForEntity(
                    "http://ssoserver.com:8080/userInfo?token={token}", String.class, token);
            String body = entity.getBody();
            session.setAttribute("loginUser",body);
            loginUser = "张三";
        }
        if (loginUser==null){
            //通过该方法获取到的是： http://127.0.0.1:8081/employees
            //HttpServletRequest request=>System.out.println(request.getRequestURL());
            //如果没登录，就跳转到登录服务器进行登录
            return "redirect:" + serverUrl+"?redirect_url=http://client1.com:8081/employees";
        }
        List<String> emps = new ArrayList<>();
        emps.add("张三");
        emps.add("李四");
        model.addAttribute("emps",emps);
        return "list";
    }
}
