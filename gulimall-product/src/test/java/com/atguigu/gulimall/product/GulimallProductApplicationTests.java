package com.atguigu.gulimall.product;


import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryBrandRelationDao;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

	///**
	// * 对象存储OSS测试
	// * @throws FileNotFoundException
	// */
/*	@Test
	public void testUpload() throws FileNotFoundException {
		// Endpoint以杭州为例，其它Region请按实际情况填写。
		String endpoint = "oss-cn-beijing.aliyuncs.com";
		// 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
		String accessKeyId = "LTAl4FwfjSycd1APnuG9bjj";
		String accessKeySecret = "O6xaxyiWfSlitcOkSuK27ju4hXT5HI";

		// 创建OSSClient实例。
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

		// 上传文件流。
		InputStream inputStream = new FileInputStream("C:\\1.png");

		ossClient.putObject("gulimall-anonymous", "1.png", inputStream);

		// 关闭OSSClient。
		ossClient.shutdown();
		System.out.println("上传成功。。。");
	}

	@Autowired
	OSSClient ossClient;

	@Test
	public void testUpload2() throws FileNotFoundException {

		// 上传文件流。
		InputStream inputStream = new FileInputStream("C:\\2.png");

		ossClient.putObject("gulimall-anonymous", "2.png", inputStream);

		// 关闭OSSClient。
		ossClient.shutdown();
		System.out.println("上传成功。。。");
	}*/


	@Autowired
	CategoryService categoryService;

	@Test
	public void test(){
		Long[] catelogPath = categoryService.findCatelogPath(413L);
		System.out.println(Arrays.toString(catelogPath));
	}

	@Autowired
	CategoryBrandRelationDao categoryBrandRelationDao;
	@Test
	public void testCategoryBrandRelationDao(){
		CategoryEntity categoryEntity = new CategoryEntity();
		categoryEntity.setCatId(225L);
		categoryEntity.setName("手机2");
		categoryBrandRelationDao.updateCategory(categoryEntity);
	}

	@Autowired
	public AttrGroupDao attrGroupDao;
	@Test
	public void streamTest(){
		LambdaQueryWrapper<AttrGroupEntity> attrGroupQueryWrapper = new LambdaQueryWrapper<>();
		attrGroupQueryWrapper.eq(AttrGroupEntity::getCatelogId,2000L);
		List<AttrGroupEntity> otherGroups = attrGroupDao.selectList(attrGroupQueryWrapper);
		System.out.println(otherGroups);
		List<Long> otherAttrGroupIds = otherGroups.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
		System.out.println(otherAttrGroupIds);
	}

	@Test
	public void DateTimeFormatterTest(){
		DateTimeFormatter formatter1 = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		LocalDateTime localDateTime1 = LocalDateTime.now();//获取当前时间
		String str1 = formatter1.format(localDateTime1);
		System.out.println(localDateTime1);//2022-01-24T11:06:34.473
		System.out.println(str1);

		System.out.println("==========================");
		DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime localDateTime2 = LocalDateTime.now();//获取当前时间
		String str2 = formatter2.format(localDateTime2);
		System.out.println(localDateTime2);
		System.out.println(str2);

	}


	@Autowired
	StringRedisTemplate stringRedisTemplate;
	@Test
	public void stringRedisTemplateTest(){
		ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
		//保存
		ops.set("hello","world"+ UUID.randomUUID());
		//查询
		String hello = ops.get("hello");
		System.out.println("之前保存的数据是："+ hello);
	}

	@Autowired
	RedissonClient redissonClient;
	@Test
	public void redissonTest(){
		System.out.println(redissonClient);
	}

}
