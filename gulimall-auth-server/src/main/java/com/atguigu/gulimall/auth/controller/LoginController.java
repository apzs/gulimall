package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.constant.auth.AuthServerConstant;
import com.atguigu.common.exception.BizCodeException;
import com.atguigu.common.to.MemberEntityTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.feign.ThirdPartyFeignService;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 无名氏
 * @date 2022/8/3
 * @Description:
 */
@Controller
public class LoginController {

    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 给指定手机号发送验证码
     * @param phone
     * @return
     */
    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone){
        //TODO 接口防刷
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        //如果redis有该手机号的验证码，如果有则判断是否过了60s。如果没有证明没有发送过验证码，直接发送验证码
        if (StringUtils.hasText(redisCode)){
            String[] s = redisCode.split("_");
            if (s.length==2 && StringUtils.hasText(s[0])){
                long startTime = Long.parseLong(s[1]);
                if (System.currentTimeMillis() - startTime < 60*1000){
                    //同一手机号获取验证码频率太高
                    return R.error(BizCodeException.SMS_CODE_EXCEPTION);
                }
            }else {
                return R.error();
            }
        }
        String code = UUID.randomUUID().toString().substring(0, 5);
        //在code后添加当前系统时间，判断是否过了一分钟，防止同一个phone在60秒内再次发送验证码(用户刷新页面、拿接口直接发)
        String redisValue = code +"_" +System.currentTimeMillis();
        System.out.println(code);
        //redis中缓存验证码再次校验  sms:code:17512080612 -> 45678_系统时间

        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,redisValue,
                10, TimeUnit.MINUTES);
        thirdPartyFeignService.sendCode(phone,code);
        return R.ok();
    }

    /**
     * TODO 重定向携带数据，利用session原理。将数据放在session中。只要跳到下一个页面取出这个数据以后，session里面的数据就会删掉
     * @param userRegisterVo
     * @param bindingResult 校验失败的错误信息
     * @param redirectAttributes 重定向携带数据
     * @return
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegisterVo userRegisterVo, BindingResult bindingResult, RedirectAttributes redirectAttributes){
        if (bindingResult.hasErrors()){
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            //添加一个一闪而过的属性(只需要取一次)
            redirectAttributes.addFlashAttribute("errors",errors);
            //  /regist为Post方式，转发将请求原封不动的发给了/reg.html，而/reg.html的路径映射的请求方式为GET
            return "redirect:http://auth.gulimall.com/reg.html";
            //return "reg";
        }
        //调用远程服务注册
        String code = userRegisterVo.getCode();
        String key = AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegisterVo.getPhone();
        String redisCode = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.hasText(redisCode)){
            if (redisCode.contains("_") && code.equals(redisCode.split("_")[0])){
                stringRedisTemplate.delete(key);
                R r = memberFeignService.regist(userRegisterVo);
                if (r.getCode()==0){
                    //注册成功，回到登录页
                    // (需要经过GulimallWebConfig类 registry.addViewController("/login.html").setViewName("login");)
                    //再跳转到login的视图
                    return "redirect:http://auth.gulimall.com/login.html";
                }else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg",r.getMsg());
                    redirectAttributes.addFlashAttribute("errors",errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }

            }
        }
        Map<String, String> errors = new HashMap<>();
        errors.put("code","验证码错误");
        redirectAttributes.addFlashAttribute("errors",errors);
        return "redirect:http://auth.gulimall.com/reg.html";
    }

    /**
     * 页面传递 k，v不加 @RequestBody
     * @param vo
     * @return
     */
    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session){
        R r = memberFeignService.login(vo);
        if (r.getCode()==0){
            Object data = r.get("data");
            String json = JSON.toJSONString(data);
            MemberEntityTo memberEntityTo = JSON.parseObject(json, MemberEntityTo.class);
            session.setAttribute(AuthServerConstant.LOGIN_USER,memberEntityTo);
            return "redirect:http://gulimall.com";
        }else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg",r.getMsg());
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
        Object hasLogin = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (hasLogin!=null){
            return "redirect:http://gulimall.com";
        }
        return "login";
    }

    //@GetMapping("/reg.html")
    //public String regPage(){
    //    return "reg";
    //}

}
