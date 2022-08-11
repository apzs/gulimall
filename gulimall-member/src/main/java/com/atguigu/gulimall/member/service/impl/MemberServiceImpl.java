package com.atguigu.gulimall.member.service.impl;

import com.atguigu.common.to.Oauth2GiteeLoginTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.member.constant.SourceType;
import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.dao.MemberLevelDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.entity.OauthGiteeEntity;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.gulimall.member.service.OauthGiteeService;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;
    @Autowired
    OauthGiteeService oauthGiteeService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        MemberDao baseMapper = this.baseMapper;
        MemberEntity memberEntity = new MemberEntity();

        MemberLevelEntity memberLevelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(memberLevelEntity.getId());

        //检查手机号和用户名是否唯一，使用异常机制
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUsername());

        memberEntity.setMobile(vo.getPhone());

        memberEntity.setUsername(vo.getUsername());
        //默认用户名也是昵称
        memberEntity.setNickname(vo.getUsername());
        //盐值加密
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

        baseMapper.insert(memberEntity);
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException{
        LambdaQueryWrapper<MemberEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MemberEntity::getUsername, username);
        Integer count = this.baseMapper.selectCount(lambdaQueryWrapper);
        if (count > 0) {
            throw new UsernameExistException();
        }
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException{
        LambdaQueryWrapper<MemberEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MemberEntity::getMobile, phone);
        Integer count = this.baseMapper.selectCount(lambdaQueryWrapper);
        if (count > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        LambdaQueryWrapper<MemberEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MemberEntity::getUsername,vo.getLoginAccount())
                .or().eq(MemberEntity::getMobile,vo.getLoginAccount())
                .or().eq(MemberEntity::getEmail,vo.getLoginAccount());
        MemberEntity memberEntity = this.baseMapper.selectOne(lambdaQueryWrapper);
        if (memberEntity==null){
            return null;
        }
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        boolean matches = bCryptPasswordEncoder.matches(vo.getPassword(), memberEntity.getPassword());
        if (matches){
            return memberEntity;
        }else {
            return null;
        }
    }

    /**
     * gitee登录
     * @param to
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public MemberEntity giteeLogin(Oauth2GiteeLoginTo to) {
        MemberEntity memberEntity = null;
        Long memberId = oauthGiteeService.getMemberId(to.getId());
        //没有注册
        if (memberId==null){
            memberEntity = new MemberEntity();
            memberEntity.setSourceType(SourceType.GITEE_LOGIN);
            memberEntity.setNickname(to.getName());
            memberEntity.setCreateTime(new Date());
            MemberLevelEntity memberLevelEntity = memberLevelDao.getDefaultLevel();
            memberEntity.setLevelId(memberLevelEntity.getId());
            memberEntity.setHeader(to.getAvatarUrl());
            this.save(memberEntity);

            OauthGiteeEntity oauthGiteeEntity = new OauthGiteeEntity();
            BeanUtils.copyProperties(to,oauthGiteeEntity);
            oauthGiteeEntity.setMemberId(memberEntity.getId());
            oauthGiteeService.save(oauthGiteeEntity);
        }else {
            memberEntity = this.getById(memberId);
        }
        return memberEntity;
    }
}