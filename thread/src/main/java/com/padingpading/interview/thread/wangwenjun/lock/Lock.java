package com.padingpading.interview.thread.wangwenjun.lock;

import java.util.Collection;

public interface Lock {

    //上锁,允许中断
    void lock() throws InterruptedException;

    /**上锁
     * @param mills 超时时间
     * @throws InterruptedException
     * @throws TimeOutException
     */
    void lock(long mills) throws InterruptedException, TimeOutException;

    /**
     * 释放锁
     */
    void unlock();

    /**获取阻塞的线程
     * @return
     */
    Collection<Thread> getBlockedThread();

    /**获取阻塞的线程数量
     * @return
     */
    int getBlockedSize();


    class TimeOutException extends Exception {

        public TimeOutException(String message) {
            super(message);
        }
    }
}