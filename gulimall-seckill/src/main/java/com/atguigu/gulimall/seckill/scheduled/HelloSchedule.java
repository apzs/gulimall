package com.atguigu.gulimall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author 无名氏
 * @date 2022/8/23
 * @Description:
 * @EnableScheduling 开启定时任务
 * @Scheduled        开启一个定时任务
 * 定时任务
 *   1、@EnableScheduling开启定时任务
 *   2、@Scheduled开启一 个定时任务
 *   3、自动配置类：TaskSchedulingAutoConfiguration 属性绑定在：TaskSchedulingProperties
 * 异步任务
 *   1、@EnableAsync开启异步任务功能
 *   2、@Async给希望异步执行的方法上标注
 *   3、自动配置类：TaskExecutionAutoConfiguration 属性绑定在：TaskExecutionProperties
 */
@Slf4j
@Component
public class HelloSchedule {

    /**
     * 在Spring中的不同
     * 1、cron由6位组成，不允许第7位的年
     * 2、在周几的位置，1-7代表周一到周日; MON- SUN
     * 3、定时任务不应该阻塞。默认是阻塞的
     *   1)、可以让业务运行以异步的方式，自己提交到线程池
     *     CompletableFuture.runAsync(()->{
     *       xxxxService.hello();
     *     }, executor);
     *   2)、支持定时任务线程池: TaskSchedulingProperties 该配置不生效
     *     spring.task.scheduling.pool.size=5
     *   3）异步任务：启动类加上@EnableAsync 在方法上加上@Async
     *   解决:使用异步+定时任务来完成定时任务不阻塞的功能;
     *
     */
    //@Async
    //@Scheduled(cron = "* * * ? * *")
    //public void hello() throws InterruptedException {
    //    log.info("hello...");
    //    TimeUnit.SECONDS.sleep(3);
    //}


}
