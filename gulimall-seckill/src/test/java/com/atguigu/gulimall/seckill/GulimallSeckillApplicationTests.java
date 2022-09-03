package com.atguigu.gulimall.seckill;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSeckillApplicationTests {

	@Test
	public void contextLoads() {
		// UTF-8
		System.out.println(StandardCharsets.UTF_8.name());
		// application/json;charset=UTF-8
		System.out.println(MediaType.APPLICATION_JSON_UTF8_VALUE);
		// application/json
		System.out.println(MediaType.APPLICATION_JSON_VALUE);
		// application
		System.out.println(MediaType.APPLICATION_JSON.getType());
		// json
		System.out.println(MediaType.APPLICATION_JSON.getSubtype());
	}

}