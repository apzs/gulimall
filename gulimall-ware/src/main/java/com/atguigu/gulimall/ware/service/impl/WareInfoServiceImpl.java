package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.constant.FreightConstant;
import com.atguigu.gulimall.ware.dao.WareInfoDao;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;
import com.atguigu.gulimall.ware.feign.MemberFeignService;
import com.atguigu.gulimall.ware.service.WareInfoService;
import com.atguigu.gulimall.ware.vo.FareVo;
import com.atguigu.gulimall.ware.vo.MemberAddressVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    FreightConstant freightConstant;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<WareInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        String key = (String) params.get("key");
        if (StringUtils.hasLength(key)){
            lambdaQueryWrapper.eq(WareInfoEntity::getId,key)
                    .or().like(WareInfoEntity::getName,key)
                    .or().like(WareInfoEntity::getAddress,key)
                    .or().like(WareInfoEntity::getAreacode,key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                lambdaQueryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 根据收货地址计算运费
     * 如果收货地址所在的省份/直辖市 有仓库就按本地运费计算
     * 如果收货地址所在的省份/直辖市 没有有仓库就按外地运费计算
     * @param addrId
     * @return
     */
    @Override
    public FareVo getFare(Long addrId) {
        R r = memberFeignService.addrInfo(addrId);
        Object data = r.get("memberReceiveAddress");
        if (data==null){
            return null;
        }
        FareVo fareVo = new FareVo();
        String s = JSON.toJSONString(data);
        MemberAddressVo memberAddressVo = JSON.parseObject(s, MemberAddressVo.class);
        fareVo.setMemberAddressVo(memberAddressVo);
        //获取用户该收货地址 省份/直辖市
        String city = memberAddressVo.getProvince();

        LambdaQueryWrapper<WareInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<WareInfoEntity> eq = lambdaQueryWrapper.eq(WareInfoEntity::getAddress, city);
        WareInfoEntity wareInfoEntity = this.baseMapper.selectOne(eq);
        BigDecimal fare = null;
        if (wareInfoEntity!=null){
            //用户收货地址有仓库
           fareVo.setFare(freightConstant.getLocalFreight());
        }else {
            fareVo.setFare(freightConstant.getOutlandFreight());
        }
        return fareVo;
    }

}