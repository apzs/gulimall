package com.atguigu.gulimall.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

//@EnableRabbit  只向`Rabblt MQ`发送消息，不需要在主类上添加`@EnableRabbit`注解

/**
 * 1、整合Sentinel
 * 	1)、导入依赖spring-cloud-starter-al ibaba-sentinel
 * 	2)、下载sentinel的控制台
 * 	3)、配置sentinel控制台地址信息
 * 	4)、在控制台调整参数。[默认所有的流控设置保存在内存中，重启失效]
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GulimallSeckillApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimallSeckillApplication.class, args);
	}

}
