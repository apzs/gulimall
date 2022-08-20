package com.atguigu.gulimall.ware.controller;

import com.atguigu.common.exception.BizCodeException;
import com.atguigu.common.to.SkuHasStockTo;
import com.atguigu.common.to.ware.WareSkuLockTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.common.utils.RS;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 商品库存
 *
 * @author 无名氏
 * @email 2185180175@qq.com
 * @date 2022-04-18 22:22:59
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 下订单后。锁库存
     */
    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockTo wareSkuLockTo){
        try {
            Boolean stock = wareSkuService.orderLockStock(wareSkuLockTo);
            return R.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(BizCodeException.NO_STOCK_EXCEPTION);
        }
    }

    /**
     * 查询sku是否有库存
     *
     * @return
     */
    @PostMapping("/hasStock")
    public RS<List<SkuHasStockTo>> getSkuHasStock(@RequestBody List<Long> skuIds) {
        List<SkuHasStockTo> skuHasStocks = wareSkuService.getSkuHasStock(skuIds);

        RS<List<SkuHasStockTo>> rs = new RS<List<SkuHasStockTo>>();
        return rs.ok().setData(skuHasStocks);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
