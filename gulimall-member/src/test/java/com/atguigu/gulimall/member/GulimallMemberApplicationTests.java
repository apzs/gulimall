package com.atguigu.gulimall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallMemberApplicationTests {

	@Test
	public void contextLoads() {
		//e10adc3949ba59abbe56e057f20f883e
		//抗修改性:彩虹表。 123456->>xx
		//1234567- >dddd
		String s1 = DigestUtils.md5Hex("123456");
		System.out.println("s1=>" + s1);
		//MD5不能直接进行密码的加密存储;
		//"123456 "+System.currentTimeMillis();
		//盐值加密;随机值加盐: $1$+8位字符
		//$1$q4yw9ojS$YQk9WvivLoEWT04q/Fr2q1
		String s2 = Md5Crypt.md5Crypt("123456".getBytes());
		System.out.println("s2=>"+s2);
		//$1$qqqqqqqq$AZofg3QwurbxV3KEOzwuI1
		//验证: 123456进行盐值(去数据库查)加密
		String s3 = Md5Crypt.md5Crypt ( "123456".getBytes(),"$1$qqqqqqqq");
		System. out.println("s3=>"+s3);
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		//$2a$10$4li09amFs0Tfof8Y/0PjKe0ZWngU5tMHuAYNUkyGiM/2FuJ25oeBi
		String encode = passwordEncoder.encode("123456");
		System.out.println(encode);
		boolean matches = passwordEncoder.matches("123456",
				"$2a$10$4li09amFs0Tfof8Y/0PjKe0ZWngU5tMHuAYNUkyGiM/2FuJ25oeBi");
		System.out.println(matches);
	}

}
