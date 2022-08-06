package com.padingpading.interview.thread.wangwenjun.lock.reentrantLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/**
 *可中断锁
 */
public class B_Interupty {
    
    private  static final Lock lock = new ReentrantLock();
    
    public static void main(String[] args) throws InterruptedException {
        Thread thread1 = new Thread(() -> testUnInterruptibly());
        thread1.start();
        TimeUnit.SECONDS.sleep(1);
//        Thread thread2 = new Thread(() -> testUnInterruptibly());
//        thread2.start();
        TimeUnit.SECONDS.sleep(2);
        //可中断
        thread1.interrupt();
    }
    
    public static void testUnInterruptibly(){
        try {
            lock.lockInterruptibly();
            System.out.println("thread-"+Thread.currentThread().getName()+" get the lock and ds");
            while (true){
            
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
}
