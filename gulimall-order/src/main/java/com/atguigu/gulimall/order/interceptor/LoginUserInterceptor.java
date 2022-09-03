package com.atguigu.gulimall.order.interceptor;

import com.atguigu.common.constant.auth.AuthServerConstant;
import com.atguigu.common.to.MemberEntityTo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 无名氏
 * @date 2022/8/12
 * @Description: 添加拦截器
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberEntityTo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match = antPathMatcher.match("/order/order/status/**", uri);
        boolean match2 = antPathMatcher.match("/payed/notify", uri);
        if (match || match2){
            return true;
        }

        Object attribute = request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute!=null){
            MemberEntityTo memberEntityTo= (MemberEntityTo) attribute;
            loginUser.set(memberEntityTo);
            return true;
        }else {
            request.getSession().setAttribute("msg","请先进行登录");
            //没登陆就重定向到登录页面
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }
}
