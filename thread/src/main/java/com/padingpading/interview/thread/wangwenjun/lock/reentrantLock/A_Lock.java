package com.padingpading.interview.thread.wangwenjun.lock.reentrantLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/**获取锁
 *
 */
public class A_Lock {
    
    private  static final Lock lock = new ReentrantLock();
    
    public static void main(String[] args) {
        IntStream.range(0,10).forEach(i->{
            new Thread(){
                @Override
                public void run() {
                    method();
                }
            }.start();
        });
    }
    
    public static  void needLock(){
        try {
            //获取锁
            lock.lock();
            System.out.println("thread-"+Thread.currentThread().getName()+"get the lock and ds");
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("thread-"+Thread.currentThread().getName()+"return the lock");
            //释放锁
            lock.unlock();
        }
    }
    
    public static void method(){
        try {
            //获取锁
            lock.lock();
            Thread.sleep(1000);
            System.out.println("method");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //释放锁
            lock.unlock();
        }
    }
    
    
}
