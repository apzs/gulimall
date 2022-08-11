package com.atguigu.gulimall.search.thread;

import lombok.SneakyThrows;

import java.util.concurrent.*;

/**
 * @author 无名氏
 * @date 2022/7/31
 * @Description: 初始化线程的4种方式
 * 1）继承 Thread
 * 2）实现 Runnable 接口
 * 3）实现 Callable 接口 + FutureTask （可以拿到返回结果，可以处理异常）
 * 4）线程池
 */
public class ThreadTest {

    //一个固定数量的线程池
    public static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("==========main start==========");

        CompletableFuture<Object> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务1运行结果：" + i);
            return i;
        }, executorService);

        CompletableFuture<Object> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2线程：" + Thread.currentThread().getId());
            String hello = "hello2";
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务2运行结果：" + hello);
            return hello;
        }, executorService);

        CompletableFuture<Object> future3 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务3线程：" + Thread.currentThread().getId());
            String hello = "hello3";
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务3运行结果：" + hello);
            return hello;
        }, executorService);

        CompletableFuture<Object> future = CompletableFuture.anyOf(future1, future2, future3);
        long start = System.currentTimeMillis();
        System.out.println("3个任务的返回结果：" + future.get());
        long end = System.currentTimeMillis();
        System.out.println("阻塞式等待所消耗：" + (end - start) + "s");
        System.out.println("==========main end============");
    }


    /**
     * 创建没有返回值异步对象
     * ==========main start==========
     * ==========main end============
     * 当前线程：12
     * 运行结果：5
     */
    public static void future1_runAsync_创建没有返回值异步对象(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("==========main start==========");
        CompletableFuture.runAsync(()->{
            System.out.println("当前线程："+Thread.currentThread().getId());
            int i = 10 /2 ;
            System.out.println("运行结果：" + i);
        },executorService);


        System.out.println("==========main end============");
    }

    /**
     * 创建有返回值异步对象
     *==========main start==========
     * 开启线程之后，获取线程执行结果之前...
     * 当前线程：12
     * 运行结果：5
     * 5
     * ==========main end============
     */
    public static void future1_supplyAsync_创建有返回值异步对象(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("==========main start==========");
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
            return i;
        }, executorService);
        System.out.println("开启线程之后，获取线程执行结果之前...");
        //阻塞式等待整个线程执行完成，获取返回结果
        System.out.println(future.get());
        System.out.println("==========main end============");
    }

    /**
     * 感知异常 并 处理异常
     *==========main start==========
     * 当前线程：12
     * 异步任务完成后的返回结果：null
     * 异步任务抛出的异常：java.util.concurrent.CompletionException: java.lang.ArithmeticException: / by zero
     * 线程返回的结果：10
     * ==========main end============
     */
    public static void future2_whenComplete_exceptionally_感知异常_处理异常(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("==========main start==========");
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 0;
            System.out.println("运行结果：" + i);
            return i;
        }, executorService).whenComplete((result,exception)->{
            //虽然能得到异常信息，但是没法修改返回数据。
            System.out.println("异步任务完成后的返回结果：" + result);
            System.out.println("异步任务抛出的异常：" + exception);
        }).exceptionally(throwable -> {
            //可以感知异常，同时返回默认值
            return 10;
        });
        System.out.println("线程返回的结果："+future.get());
        System.out.println("==========main end============");
    }


    /**
     * 如果线程执行失败就处理异常，如果线程执行成功就处理返回值
     * ==========main start==========
     * 当前线程：12
     * 运行结果：5
     * 线程返回的结果：10
     * ==========main end============
     */
    public static void future3_handle_处理异常或返回值(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("==========main start==========");
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
            return i;
        }, executorService).handle((result,throwable)->{
            if (result!=null){
                return result*2;
            }
            if (throwable!=null){
                return 0;
            }
            return 0;
        });
        System.out.println("线程返回的结果："+future.get());
        System.out.println("==========main end============");
    }

    /**
     * A完成后，B再执行，B不需要A的返回值
     * ==========main start==========
     * 当前线程：12
     * ==========main end============
     * 运行结果：5
     * 任务2启动了...
     */
    public static void future4_thenRunAsync_B不需要A的返回值(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("==========main start==========");
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("运行结果：" + i);
            return i;
        }, executorService).thenRun(() -> {
            System.out.println("任务2启动了...");
        });
        System.out.println("==========main end============");
    }

    /**
     * A完成后，B再执行，B需要A的返回值
     * ==========main start==========
     * 当前线程：12
     * ==========main end============
     * 运行结果：5
     * 任务2启动了...
     * 获取到了上一步的返回结果：5
     */
    public static void future4_thenAcceptAsync_B需要A的返回值(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("==========main start==========");

        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("运行结果：" + i);
            return i;
        }, executorService).thenAccept((result) -> {
            System.out.println("任务2启动了...");
            System.out.println("获取到了上一步的返回结果：" + result);
        });
        System.out.println("==========main end============");
    }

    /**
     * A完成后，B再执行，B需要A的返回值，且B有返回值
     * ==========main start==========
     * 当前线程：12
     * 任务1运行结果：5
     * 任务2启动了...
     * 任务2获取到了任务1的返回结果：5
     * 任务都完成后返回结果：Hello 5
     * ==========main end============
     */
    public static void future4_thenApplyAsync_AB都返回结果(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("==========main start==========");

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务1运行结果：" + i);
            return i;
        }, executorService).thenApply((result) -> {
            System.out.println("任务2启动了...");
            System.out.println("任务2获取到了任务1的返回结果：" + result);
            return "Hello " + result;
        });
        System.out.println("任务都完成后返回结果：" + future.get());
        System.out.println("==========main end============");
    }

    /**
     * A、B任务完成后，C再执行，C不需要A、B的返回值
     * ==========main start==========
     * 任务1线程：12
     * 任务2线程：13
     * ==========main end============
     * 任务1运行结果：5
     * 任务2运行结果：hello2
     * 任务3线程：15
     * 任务3运行结果：hello3
     */
    public static void future5_runAfterBothAsync(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("==========main start==========");

        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务1运行结果：" + i);
            return i;
        }, executorService);

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2线程：" + Thread.currentThread().getId());
            String hello = "hello2";
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务2运行结果：" + hello);
            return hello;
        }, executorService);

        future1.runAfterBothAsync(future2,()->{
            System.out.println("任务3线程：" + Thread.currentThread().getId());
            String hello = "hello3";
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务3运行结果：" + hello);
        },executorService);
        System.out.println("==========main end============");
    }

    /**
     * A、B任务完成后，C再执行，C需要A、B的返回值
     * ==========main start==========
     * 任务1线程：12
     * 任务2线程：13
     * ==========main end============
     * 任务1运行结果：5
     * 任务2运行结果：hello2
     * 任务3线程：15
     * 任务3获取到的任务1结果：5
     * 任务3获取到的任务2结果：hello2
     * 任务3运行结果：hello3
     */
    public static void future5_thenAcceptBothAsync(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("==========main start==========");

        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务1运行结果：" + i);
            return i;
        }, executorService);

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2线程：" + Thread.currentThread().getId());
            String hello = "hello2";
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务2运行结果：" + hello);
            return hello;
        }, executorService);

        future1.thenAcceptBothAsync(future2,(f1,f2)->{
            System.out.println("任务3线程：" + Thread.currentThread().getId());
            String hello = "hello3";
            System.out.println("任务3获取到的任务1结果：" + f1);
            System.out.println("任务3获取到的任务2结果：" + f2);
            System.out.println("任务3运行结果：" + hello);
        },executorService);
        System.out.println("==========main end============");
    }

    /**
     * A、B任务完成后，C再执行，C需要A、B的返回值，且C有返回值
     * ==========main start==========
     * 任务1线程：12
     * 任务2线程：13
     * 任务2运行结果：hello2
     * 任务1运行结果：5
     * 任务3线程：14
     * 任务3获取到的任务1结果：5
     * 任务3获取到的任务2结果：hello2
     * 任务3运行结果：hello3
     * 三个任务执行完的返回结果：5 => hello2 => hello3
     * ==========main end============
     */
    public static void future5_thenCombineAsync(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("==========main start==========");

        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务1运行结果：" + i);
            return i;
        }, executorService);

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2线程：" + Thread.currentThread().getId());
            String hello = "hello2";
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务2运行结果：" + hello);
            return hello;
        }, executorService);

        CompletableFuture<String> future3 = future1.thenCombineAsync(future2, (f1, f2) -> {
            System.out.println("任务3线程：" + Thread.currentThread().getId());
            String hello = "hello3";
            System.out.println("任务3获取到的任务1结果：" + f1);
            System.out.println("任务3获取到的任务2结果：" + f2);
            System.out.println("任务3运行结果：" + hello);
            return f1 + " => " + f2 + " => " + hello;
        }, executorService);
        System.out.println("三个任务执行完的返回结果：" + future3.get());
        System.out.println("==========main end============");
    }

    /**
     * A或B其中任何一个任务完成后，C再执行，C不需要A或B的返回值
     * ==========main start==========
     * 任务1线程：12
     * 任务2线程：13
     * ==========main end============
     * 任务1运行结果：5
     * 任务3线程：15
     * 任务3运行结果：hello3
     * 任务2运行结果：hello2
     */
    public static void future6_runAfterEitherAsync(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("==========main start==========");

        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务1运行结果：" + i);
            return i;
        }, executorService);

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2线程：" + Thread.currentThread().getId());
            String hello = "hello2";
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务2运行结果：" + hello);
            return hello;
        }, executorService);

        future1.runAfterEitherAsync(future2, () -> {
            System.out.println("任务3线程：" + Thread.currentThread().getId());
            String hello = "hello3";
            System.out.println("任务3运行结果：" + hello);
        }, executorService);
        System.out.println("==========main end============");
    }

    /**
     * A或B其中任何一个任务完成后，C再执行，C需要已经执行成功的那个任务的返回值
     * ==========main start==========
     * 任务1线程：12
     * 任务2线程：13
     * ==========main end============
     * 任务1运行结果：5
     * 任务3线程：15
     * 任务3获取到的前两个任务其中一个执行完的返回值5
     * 任务3运行结果：hello3
     * 任务2运行结果：hello2
     */
    public static void future6_acceptEitherAsync(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("==========main start==========");

        CompletableFuture<Object> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务1运行结果：" + i);
            return i;
        }, executorService);

        CompletableFuture<Object> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2线程：" + Thread.currentThread().getId());
            String hello = "hello2";
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务2运行结果：" + hello);
            return hello;
        }, executorService);

        future1.acceptEitherAsync(future2, (result) -> {
            System.out.println("任务3线程：" + Thread.currentThread().getId());
            System.out.println("任务3获取到的前两个任务其中一个执行完的返回值"+result);
            String hello = "hello3";
            System.out.println("任务3运行结果：" + hello);
        }, executorService);
        System.out.println("==========main end============");
    }


    /**
     * A或B其中任何一个任务完成后，C再执行，C需要已经执行成功的那个任务的返回值，且C有返回值
     * ==========main start==========
     * 任务1线程：12
     * 任务2线程：13
     * 任务1运行结果：5
     * 任务3线程：14
     * 任务3获取到的前两个任务其中一个执行完的返回值5
     * 任务3运行结果：hello3
     * 任务3执行完后的返回结果:5 => hello3
     * ==========main end============
     * 任务2运行结果：hello2
     */
    public static void future6_applyToEitherAsync(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("==========main start==========");

        CompletableFuture<Object> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务1运行结果：" + i);
            return i;
        }, executorService);

        CompletableFuture<Object> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2线程：" + Thread.currentThread().getId());
            String hello = "hello2";
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务2运行结果：" + hello);
            return hello;
        }, executorService);

        CompletableFuture<String> future = future1.applyToEitherAsync(future2, (result) -> {
            System.out.println("任务3线程：" + Thread.currentThread().getId());
            System.out.println("任务3获取到的前两个任务其中一个执行完的返回值" + result);
            String hello = "hello3";
            System.out.println("任务3运行结果：" + hello);
            return result + " => " + hello;
        }, executorService);
        System.out.println("任务3执行完后的返回结果:" + future.get());
        System.out.println("==========main end============");
    }


    /**
     * A、B、C所有任务都要完成
     * ==========main start==========
     * 任务1线程：12
     * 任务2线程：13
     * 任务3线程：14
     * 任务1运行结果：5
     * 任务3运行结果：hello3
     * 任务2运行结果：hello2
     * 3个任务的返回结果：null
     * 阻塞式等待所消耗：3000s
     * ==========main end============
     */
    public static void future7_allOf_都要完成(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("==========main start==========");

        CompletableFuture<Object> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务1运行结果：" + i);
            return i;
        }, executorService);

        CompletableFuture<Object> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2线程：" + Thread.currentThread().getId());
            String hello = "hello2";
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务2运行结果：" + hello);
            return hello;
        }, executorService);

        CompletableFuture<Object> future3 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务3线程：" + Thread.currentThread().getId());
            String hello = "hello3";
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务3运行结果：" + hello);
            return hello;
        }, executorService);

        CompletableFuture<Void> future = CompletableFuture.allOf(future1, future2, future3);
        long start = System.currentTimeMillis();
        System.out.println("3个任务的返回结果：" + future.get());
        long end = System.currentTimeMillis();
        System.out.println("阻塞式等待所消耗：" + (end - start) + "s");
        System.out.println("==========main end============");
    }

    /**
     * A、B、C任务中有一个完成
     *==========main start==========
     * 任务1线程：12
     * 任务2线程：13
     * 任务3线程：14
     * 任务1运行结果：5
     * 3个任务的返回结果：5
     * 阻塞式等待所消耗：1000s
     * ==========main end============
     * 任务3运行结果：hello3
     * 任务2运行结果：hello2
     */
    public static void future7_anyOf_任何一个完成(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("==========main start==========");

        CompletableFuture<Object> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务1运行结果：" + i);
            return i;
        }, executorService);

        CompletableFuture<Object> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2线程：" + Thread.currentThread().getId());
            String hello = "hello2";
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务2运行结果：" + hello);
            return hello;
        }, executorService);

        CompletableFuture<Object> future3 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务3线程：" + Thread.currentThread().getId());
            String hello = "hello3";
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务3运行结果：" + hello);
            return hello;
        }, executorService);

        CompletableFuture<Object> future = CompletableFuture.anyOf(future1, future2, future3);
        long start = System.currentTimeMillis();
        System.out.println("3个任务的返回结果：" + future.get());
        long end = System.currentTimeMillis();
        System.out.println("阻塞式等待所消耗：" + (end - start) + "s");
        System.out.println("==========main end============");
    }

    public static class Thread01 extends Thread{
        @SneakyThrows
        @Override
        public void run() {
            System.out.println("当前线程："+Thread.currentThread().getId());
            Thread.sleep(2000);
            int i = 10 /2 ;
            System.out.println("运行结果：" + i);
        }
    }

    /**
     * 继承`Thread`类
     * ==========main start==========
     * ==========main end============
     * 当前线程：12
     * 运行结果：5
     * @param args
     */
    public static void thread1_继承Thread类(String[] args){
        System.out.println("==========main start==========");
        Thread01 thread01 = new Thread01();
        //启动线程
        thread01.start();
        System.out.println("==========main end============");
    }

    public static class Runnable02 implements Runnable{
        @SneakyThrows
        @Override
        public void run() {
            System.out.println("当前线程："+Thread.currentThread().getId());
            Thread.sleep(2000);
            int i = 10 /2 ;
            System.out.println("运行结果：" + i);
        }
    }

    /**
     * 实现`Runnable`接口
     * ==========main start==========
     * ==========main end============
     * 当前线程：12
     * 运行结果：5
     * @param args
     */
    public static void thread2_实现Runnable接口(String[] args){
        System.out.println("==========main start==========");
        Runnable02 runnable02 = new Runnable02();
        Thread thread02 = new Thread(runnable02);
        thread02.start();
        System.out.println("==========main end============");
    }

    public static class Callable03 implements Callable<Integer>{
        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程："+Thread.currentThread().getId());
            Thread.sleep(2000);
            int i = 10 /2 ;
            System.out.println("运行结果：" + i);
            return i;
        }
    }

    /**
     * 实现`Callable`接口
     * ==========main start==========
     * 开启线程之后，获取线程执行结果之前...
     * 当前线程：12
     * 运行结果：5
     * 返回的结果：5
     * ==========main end============
     * @param args
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void thread3_实现Callable接口(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("==========main start==========");
        Callable03 callable03 = new Callable03();
        FutureTask<Integer> futureTask03 = new FutureTask<>(callable03);
        Thread thread03 = new Thread(futureTask03);
        thread03.start();
        System.out.println("开启线程之后，获取线程执行结果之前...");
        //阻塞式等待整个线程执行完成，获取返回结果
        Integer integer = futureTask03.get();
        System.out.println("返回的结果：" + integer);
        System.out.println("==========main end============");
    }

    /**
     *通过线程池
     * ==========main start==========
     * ==========main end============
     * 当前线程：12
     * 运行结果：5
     * 该线程一直存在，控制台可以看出还在一直运行
     * @param args
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void thread4_线程池(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("==========main start==========");
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.execute(new Runnable02());
        System.out.println("==========main end============");
    }


    /**
     * 七大参数：
     * 1、int corePoolSize    [5]核心线程数; 线程池，创建好以后就准备就一直存在【除非设置(allowCoreThreadTimeOut)】
     *                        5个 Thread thread = new Thread(); thread.start();
     * 2、int maximumPoolSize 最大线程数量，控制资源
     * 3、long keepAliveTime  存活时间。如果当前的线程数量大于core数量，并且线程空闲时间大于指定的存活时间，就释放空闲的线程(最少保留corePoolSize个)。
     * 4、TimeUnit unit       时间单位
     * 5、BlockingQueue<Runnable> workQueue 阻塞队列。如果任务有很多，就会将目前多的任务放在队列里面。
     *                                      只要有线程空用，就会去队列里面取出新的任务继续执行。
     * 6、ThreadFactory threadFactory 线程的创建工厂
     * 7、RejectedExecutionHandler handler 拒绝策略 如果队列满了，按照我们指定的拒绝策略拒绝执行任务
     *
     * 运行流程：
     * 1、线程池创建，准备好 core  数量的核心线程，准备接受任务
     * 2、新的任务进来，用 core 准备好的空闲线程执行。
     *      (1)、core 满了，就将再进来的任务放入阻塞队列中。空闲的 core 就会自己去阻塞队列获取任务执行
     *      (2)、阻塞队列满了，就直接开新线程执行，最大只能开到 max  指定的数量
     *      (3)、max 都执行好了。max-core 数量空闲的线程会在 keepAliveTime 指定的时间后自动销毁。最终保持到 core 大小
     *      (4)、如果线程数开到了 max 的数量，还有新任务进来，就会使用 reject 指定的拒绝策略进行处理
     * 3、所有的线程创建都是由指定的 factory 创建的。
     */
    public static void thread5_线程池七大参数(String[] args){
        System.out.println("==========main start==========");

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5,
                200,
                10,
                TimeUnit.SECONDS,
                //new LinkedBlockingDeque<>() 时一定要设置容量，默认是Integer.MAX_VALUE，会导致内存不够
                new LinkedBlockingDeque<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
        /**
         *
         * 一个线程池core 7;max 20; queue 50， 100并发进来怎么分配的;
         * 答：7个会立即得到执行，50个会进入队列，再开13个进行执行。剩下的30个就使用拒绝策略。
         */

        Executors.newCachedThreadPool(); //core是0，所有都可回收
        Executors.newFixedThreadPool(5); //固定大小，core=max; 都不可回收
        Executors.newScheduledThreadPool(5);    //定时任务的线程池 DelayedWorkQueue
        Executors.newSingleThreadExecutor(); //单线程的线程池，后台从队列里面获取任务，挨个执行

        System.out.println("==========main end============");
    }

}

