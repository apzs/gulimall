package com.atguigu.gulimall.ssoserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @author 无名氏
 * @date 2022/8/8
 * @Description:
 */
@Controller
public class LoginController {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @GetMapping("/login.html")
    public String loginPage(@RequestParam("redirect_url") String url, Model model,
                            @CookieValue(value = "sso_token",required = false) String ssoToken) {
        if (StringUtils.hasText(ssoToken)){
            //说明之前有人登录过，浏览器留下了痕迹
            return "redirect:" + url+"?token="+ssoToken;
        }
        model.addAttribute("url", url);
        return "login";
    }

    @PostMapping("/doLogin")
    public String doLogin(String username, String password, String url, HttpServletResponse response) {
        if (StringUtils.hasText(username) && StringUtils.hasText(password) && StringUtils.hasText(url)) {
            //如果username、password、url都不为空，就返回之前页
            String uuid = UUID.randomUUID().toString().replace("-", "");
            stringRedisTemplate.opsForValue().set(uuid,username);
            Cookie cookie = new Cookie("sso_token", uuid);
            response.addCookie(cookie);
            return "redirect:" + url+"?token="+uuid;
        }
        //如果其中一个为空，就跳转到登录页
        return "login";
    }

    @ResponseBody
    @GetMapping("/userInfo")
    public String userInfo(@RequestParam("token") String token){
        return stringRedisTemplate.opsForValue().get(token);
    }
}
