package com.atguigu.gulimall.product;


import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

	@Autowired
	BrandService brandService;
	@Test
	public void save() {
		BrandEntity brand = new BrandEntity();
		brand.setDescript("111");
		brandService.save(brand);
		System.out.println("保存成功...");
	}

	@Test
	public void updateById() {
		BrandEntity brand = new BrandEntity();
		brand.setBrandId(1L);
		brand.setDescript("222");
		brandService.updateById(brand);
		System.out.println("修改成功...");
	}

	@Test
	public void query() {
		LambdaQueryWrapper<BrandEntity> lambdaQueryWrapper = new LambdaQueryWrapper<BrandEntity>();
		lambdaQueryWrapper.eq(BrandEntity::getBrandId,1);
		List<BrandEntity> list = brandService.list(lambdaQueryWrapper);
		list.forEach(System.out::println);
	}
}
