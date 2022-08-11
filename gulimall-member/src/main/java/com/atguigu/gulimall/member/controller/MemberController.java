package com.atguigu.gulimall.member.controller;

import com.atguigu.common.exception.BizCodeException;
import com.atguigu.common.to.Oauth2GiteeLoginTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.feign.CouponFeignService;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 会员
 *
 * @author 无名氏
 * @email 2185180175@qq.com
 * @date 2022-04-18 22:06:32
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo vo){
        try {
            memberService.regist(vo);
        } catch (UsernameExistException e) {
            return R.error(BizCodeException.USER_EXIST_EXCEPTION);
        }catch (PhoneExistException e){
            return R.error(BizCodeException.PHONE_EXIST_EXCEPTION);
        }
        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo){
        MemberEntity entity = memberService.login(vo);
        if (entity!=null) {
            return R.ok().put("data", entity);
        }else {
            return R.error(BizCodeException.ACCOUNT_PASSWORD_INVALID_EXCEPTION);
        }
    }

    @PostMapping("/giteeLogin")
    public R giteeLogin(@RequestBody Oauth2GiteeLoginTo to){
        MemberEntity entity = memberService.giteeLogin(to);
        if (entity!=null) {
            return R.ok().put("data", entity);
        }else {
            return R.error(BizCodeException.GITEE_LOGIN_EXCEPTION);
        }
    }

    /**
     * Openfeign消费方测试
     * @return
     */
    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");

        R memberCoupons = couponFeignService.memberCoupons();

        Object coupons = memberCoupons.get("coupons");
        return R.ok().put("member",memberEntity).put("coupons",coupons);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
        public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
        public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
        public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
        public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
        public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
