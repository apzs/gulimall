package com.atguigu.gulimall.thirdparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableConfigurationProperties
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallThirdPartyApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimallThirdPartyApplication.class, args);
	}

}
