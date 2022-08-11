package com.atguigu.gulimall.cart.interceptor;

import com.atguigu.common.constant.auth.AuthServerConstant;
import com.atguigu.common.constant.cart.CartConstant;
import com.atguigu.common.to.MemberEntityTo;
import com.atguigu.common.to.UserInfoTo;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @author 无名氏
 * @date 2022/8/9
 * @Description:
 */
public class CartInterceptor implements HandlerInterceptor {

    /**
     * 把UserInfoTo的信息放到ThreadLocal里
     */
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * 在执行目标方法之前，判断用户的登录状态。并封装传递给controller目标请求
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)){
            return true;
        }
        UserInfoTo userInfoTo = new UserInfoTo();
        //根据session判断当前用户是否登录，如果登录了就把用户id赋值给userInfoTo.userId
        HttpSession session = request.getSession();
        MemberEntityTo member = (MemberEntityTo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (member != null) {
            //用户登录
            userInfoTo.setUserId(member.getId());
        }
        //在cookies里寻找key为user-key的cookie，把该cookie的value放到userInfoTo.userKey里
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (CartConstant.TEMP_USER_COOKIE_NAME.equals(cookie.getName())) {
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setHasTempUserCookie(true);
                    break;
                }
            }
        }
        //如果在cookies里没有找到key为user-key的cookie，就证明是第一次来到系统，或删除了cookie
        //如果没有临时用户，分配一个临时用户
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }
        //目标方法执行之前
        threadLocal.set(userInfoTo);
        return true;
    }

    /**
     * 业务执行完后，如果当前用户的cookies里没有user-key为键的cookie，就存放该cookie
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (!(handler instanceof HandlerMethod)){
            return;
        }
        UserInfoTo userInfoTo = threadLocal.get();
        if (!userInfoTo.isHasTempUserCookie()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setPath("gulimall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
        //删除ThreadLocal，防止线程复用，获取到别的用户信息
        threadLocal.remove();
    }
}
