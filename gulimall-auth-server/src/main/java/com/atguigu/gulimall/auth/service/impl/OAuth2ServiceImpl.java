package com.atguigu.gulimall.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.MemberEntityTo;
import com.atguigu.common.to.Oauth2GiteeLoginTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.config.Oauth2FormGitee;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.service.OAuth2Service;
import com.atguigu.gulimall.auth.vo.GiteeCodeResponseVo;
import com.atguigu.gulimall.auth.vo.GiteeTokenResponseVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 无名氏
 * @date 2022/8/6
 * @Description:
 */
@Service
public class OAuth2ServiceImpl implements OAuth2Service {

    @Autowired
    Oauth2FormGitee oauth2FormGitee;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public MemberEntityTo giteeRegister(String code) {
        GiteeCodeResponseVo giteeCodeResponseVo = getToken(code);
        if (giteeCodeResponseVo==null || !StringUtils.hasText(giteeCodeResponseVo.getAccessToken())){
            throw new RuntimeException("通过gitee获取用户token失败");
        }
        GiteeTokenResponseVo giteeTokenResponseVo = getGiteeUserInfo(giteeCodeResponseVo.getAccessToken());
        if (giteeTokenResponseVo==null || giteeTokenResponseVo.getId()==0){
            throw new RuntimeException("通过gitee获取用户基本失败");
        }
        Oauth2GiteeLoginTo to = new Oauth2GiteeLoginTo();
        BeanUtils.copyProperties(giteeCodeResponseVo,to);
        to.setId(giteeTokenResponseVo.getId());
        to.setAvatarUrl(giteeTokenResponseVo.getAvatar_url());
        to.setName(giteeTokenResponseVo.getName());
        try {
            R r = memberFeignService.giteeLogin(to);
            if (r.getCode()==0){
                Object data = r.get("data");
                String jsonString = JSON.toJSONString(data);
                return JSON.parseObject(jsonString, MemberEntityTo.class);
            }else {
                throw new RuntimeException("社交登录失败");
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("社交登录失败");
        }
    }

    private GiteeCodeResponseVo getToken(String code){
        GiteeCodeResponseVo vo = null;
        String url = "https://gitee.com/oauth/token?grant_type=authorization_code&code={code}&client_id={client_id}" +
                "&redirect_uri={redirect_uri}&client_secret={client_secret}";
        Map<String,String> map = new HashMap<>();
        map.put("code",code);
        map.put("client_id",oauth2FormGitee.getClientId());
        map.put("redirect_uri",oauth2FormGitee.getRedirectUri());
        map.put("client_secret",oauth2FormGitee.getClientSecret());
        try {
            ResponseEntity<GiteeCodeResponseVo> response = restTemplate.postForEntity(url,null,GiteeCodeResponseVo.class,map);
            if (response.getStatusCodeValue()==200){
                vo = response.getBody();
            }else {
                throw new RuntimeException("连接gitee获取token状态异常");
            }
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            throw new RuntimeException("连接gitee获取token响应参数异常");
        }
        catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("连接gitee获取token异常");
        }
        return vo;
    }

    private GiteeTokenResponseVo getGiteeUserInfo(String accessToken){
        GiteeTokenResponseVo vo = null;
        String url = "https://gitee.com/api/v5/user?access_token={access_token}";
        try {
            ResponseEntity<GiteeTokenResponseVo> response = restTemplate.getForEntity(url, GiteeTokenResponseVo.class, accessToken);
            if (response.getStatusCodeValue()==200){
                vo = response.getBody();
            }else {
                throw new RuntimeException("连接gitee获取token状态异常");
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("连接gitee获取用户信息异常");
        }
        return vo;
    }
}
