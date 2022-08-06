package com.padingpading.interview.thread.wangwenjun.lock.rwlock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**读写锁
 */
public class A_RD_Demo {
    //读写锁
    private  static final ReentrantReadWriteLock readWritelock  = new ReentrantReadWriteLock(true);
    //读锁
    private static final Lock readLock = readWritelock.readLock();
    //写锁
    private static final Lock writeLock = readWritelock.writeLock();
    
    //共享变量
    private static final List<Long> data = new ArrayList<>();
    static {
        data.add(System.currentTimeMillis());
    }
    
    public static void main(String[] args) throws InterruptedException {
        //两个线程只能同时读，不能同时写，同时读写操作
        Thread t1 = new Thread(() -> read());
        t1.start();
        Thread t2 = new Thread(() -> read());
        t2.start();
    }
    
    //写操作
    public static void write(){
        try {
            writeLock.lock();
            data.add(System.currentTimeMillis());
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    }
    
    //读操作
    public static void read(){
        try {
            readLock.lock();
            System.out.println(data.size());
            for (Long datum : data) {
                System.out.println(datum);
            }
        }  finally {
            readLock.unlock();
        }
    }
    
    
}
