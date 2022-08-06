package com.padingpading.interview.thread.wangwenjun.mult;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author libin
 * @description 线程池方式实现
 * @date 2021-07-12
 */
public class MyThreadPool {

    //线程池
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        while (true) {
            threadPool.execute(new Runnable() { // 提交多个线程任务，并执行
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName() + " is running ..");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}

