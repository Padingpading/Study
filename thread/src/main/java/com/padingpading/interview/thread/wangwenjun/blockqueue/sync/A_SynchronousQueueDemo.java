package com.padingpading.interview.thread.wangwenjun.blockqueue.sync;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author libin
 * @description
 * @date 2022-05-22
 */
public class A_SynchronousQueueDemo {
    
    public static void main(String[] args) {
        SynchronousQueue<String> synchronousQueue = new SynchronousQueue<>();
        new Thread(() -> {
            try {
                System.out.println(Thread.currentThread().getName() + "\t 入队列 1");
                synchronousQueue.put("1");
                System.out.println(Thread.currentThread().getName() + "\t 入队列 2");
                synchronousQueue.put("2");
                System.out.println(Thread.currentThread().getName() + "\t 入队列 3");
                synchronousQueue.put("3");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "AAAAA").start();
        
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
                System.out.println(Thread.currentThread().getName() + "\t 出队列 " + synchronousQueue.take());
                TimeUnit.SECONDS.sleep(5);
                System.out.println(Thread.currentThread().getName() + "\t 出队列 " + synchronousQueue.take());
                TimeUnit.SECONDS.sleep(5);
                System.out.println(Thread.currentThread().getName() + "\t 出队列 " + synchronousQueue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "BBBBB").start();
    }
    
}
