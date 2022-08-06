package com.padingpading.interview.thread.wangwenjun.lock.reentrantLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/**
 *可中断锁
 */
public class C_TryLock {
    private  static final Lock lock = new ReentrantLock();
    
    
    public static void main(String[] args) {
        IntStream.range(0,2).forEach(i->{
            new Thread(){
                @Override
                public void run() {
                    testTryLock();
                }
            }.start();
        });
    
    }
    public static void testTryLock(){
        //获取锁
        if(lock.tryLock()){
            try {
                System.out.println("thread-"+Thread.currentThread().getName()+" get the lock and ds");
                while (true){
                
                }
            }finally {
                lock.unlock();
            }
        }else {
            System.out.println("thread-"+Thread.currentThread().getName()+" not get the lock");
        }
    }
}
