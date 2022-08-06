package com.padingpading.interview.thread.wangwenjun.lock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;


public class BooleanLock implements Lock {

    //true:锁已经被占用
    //false:锁没有被占用
    private boolean initValue;

    private Collection<Thread> blockedThreadCollection = new ArrayList<>();

    private Thread currentThread;

    public BooleanLock() {
        this.initValue = false;
    }

    /**获取锁
     * 1、如果锁被占用,将当前线程放入到集合。
     * 2、获取到锁,将当前线程从集合remove。
     * 3、将标志位设置为true
     */
    @Override
    public synchronized void lock() throws InterruptedException {
        while (initValue) {
            blockedThreadCollection.add(Thread.currentThread());
            this.wait();
        }

        blockedThreadCollection.remove(Thread.currentThread());
        this.initValue = true;
        this.currentThread = Thread.currentThread();
    }

    /**
     * @param mills 超时时间
     * @throws InterruptedException
     * @throws TimeOutException
     */
    @Override
    public synchronized void lock(long mills) throws InterruptedException, TimeOutException {
        if (mills <= 0)
            lock();

        long hasRemaining = mills;
        long endTime = System.currentTimeMillis() + mills;
        while (initValue) {
            if (hasRemaining <= 0)
                throw new TimeOutException("Time out");
            blockedThreadCollection.add(Thread.currentThread());
            this.wait(mills);
            hasRemaining = endTime - System.currentTimeMillis();
        }

        this.initValue = true;
        this.currentThread = Thread.currentThread();

    }

    /**
     * 释放锁:
     * 1、判断是否是当前线程。
     * 2、调用notifyAll，唤醒其他的线程。
     */
    @Override
    public synchronized void unlock() {
        if (Thread.currentThread() == currentThread) {
            this.initValue = false;
            Optional.of(Thread.currentThread().getName() + " release the lock monitor.")
                    .ifPresent(System.out::println);
            this.notifyAll();
        }
    }

    /**获取阻塞的线程
     * @return
     */
    @Override
    public Collection<Thread> getBlockedThread() {
        //这里返回的集合是不能为修改的。
        return Collections.unmodifiableCollection(blockedThreadCollection);
    }

    @Override
    public int getBlockedSize() {
        return blockedThreadCollection.size();
    }
}
