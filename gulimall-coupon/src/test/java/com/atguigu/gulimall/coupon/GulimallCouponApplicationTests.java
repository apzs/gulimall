package com.atguigu.gulimall.coupon;

import org.junit.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = GulimallCouponApplication.class)
public class GulimallCouponApplicationTests {

	@Test
	public void contextLoads() {
		LocalDate now = LocalDate.now();
		LocalDate plus2 = now.plusDays(2);
		System.out.println(now);
		System.out.println(plus2);
		System.out.println("=============================");

		LocalTime min = LocalTime.MIN;
		LocalTime max = LocalTime.MAX;
		System.out.println(min);
		System.out.println(max);
		System.out.println("=============================");

		LocalDateTime nowDateTime = LocalDateTime.of(now, min);
		LocalDateTime plus2DateTime = LocalDateTime.of(plus2, max);
		System.out.println(nowDateTime);
		System.out.println(plus2DateTime);
		System.out.println("=============================");
		System.out.println(startTime());
		System.out.println(endTime());
	}

	private String startTime(){
		LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
		return start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

	private String endTime(){
		LocalDateTime endTime = LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.MAX);
		return endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

}