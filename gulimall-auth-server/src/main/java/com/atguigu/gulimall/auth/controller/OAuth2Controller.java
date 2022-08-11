package com.atguigu.gulimall.auth.controller;

import com.atguigu.common.constant.auth.AuthServerConstant;
import com.atguigu.common.to.MemberEntityTo;
import com.atguigu.gulimall.auth.service.OAuth2Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author 无名氏
 * @date 2022/8/6
 * @Description: 社交登录
 */
@Controller
@Slf4j
@RequestMapping("oauth2.0")
public class OAuth2Controller {


    @Autowired
    OAuth2Service oAuth2Service;

    @GetMapping("/gitee/success")
    public String giteeRegister(@RequestParam String code, HttpSession session){

        try {
            MemberEntityTo memberEntityTo = oAuth2Service.giteeRegister(code);
            session.setAttribute(AuthServerConstant.LOGIN_USER,memberEntityTo);
            //Cookie cookie = new Cookie("data",memberEntityTo.toString());
            //cookie.setPath("/");
            //response.addCookie(cookie);
            return "redirect:http://gulimall.com";
        }catch (Exception e){
            log.error("第三方登录失败 :{}",e.getMessage());
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

    public void getSession(HttpServletRequest request){
        HttpSession session = request.getSession();
    }

}
