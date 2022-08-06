package com.padingpading.interview.thread.wangwenjun.lock.countdown;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 让所有的其他线程执行完毕后，执行主线程
 */
public class A_CountDownDemo {
    //定义计数器
    private static final CountDownLatch countDownLatch = new CountDownLatch(10);
    
    private static ExecutorService executor = Executors.newFixedThreadPool(2);
    
    public static void main(String[] args) throws InterruptedException {
        final int[] data = query();
        for (int i = 0; i < data.length; i++) {
            executor.execute(new SimpleRunable(data, i, countDownLatch));
        }
        //等到所有的线程执行完毕后再执行后面的东西
        countDownLatch.await();
        System.out.println("all of work done");
        executor.shutdown();
    }
    
    private static int[] query() {
        return new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    }
    
    static class SimpleRunable implements Runnable {
        
        private final int[] data;
        
        private final int index;
        
        private final CountDownLatch countDownLatch;
        
        public SimpleRunable(int[] data, int index, CountDownLatch countDownLatch) {
            this.data = data;
            this.index = index;
            this.countDownLatch = countDownLatch;
        }
        
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                int value = data[index];
                if (value % 2 == 0) {
                    data[index] = value * 2;
                } else {
                    data[index] = value * 10;
                }
                System.out.println(Thread.currentThread().getName() + "finshed");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                //要写在finally中。
                countDownLatch.countDown();
            }

        }
    }
}

