/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.padingpading.interview.thread.wangwenjun.lock.source;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 非阻塞的方式获取锁:相对于sunchronized
 * 1、tryLock():尝试获取锁。
 * 2、lockInterruptibly():可中断的。
 * 3、tryLock(long time, TimeUnit unit):超时跳出获取锁。
 *
 * lock是一个普通的对象,可以用作synchronized的monitor,两者锁并没有什么关系。
 */
public interface Lock {

    /**阻塞式获取.获取不到锁,线程无法被调度。
     *
     */
    void lock();

    /**可中断获取锁,
     * 1、获取不到锁,进入睡眠。
     * 2、可中断, 其他额线程中断了当前线程,跳出睡眠。interrupt()
     * 抛出interruptException,标志位设置为false。
     */
    void lockInterruptibly() throws InterruptedException;

    /**尝试获取锁。
    * 获取不到锁,返回false。
     */
    boolean tryLock();

    /**基于时间的尝试获取
     * 1、给定的时间内,获取锁,返回true。
     * 2、其他线程中断了当前线程。
     * 3、超过等待的时间,线程也会醒来,返回false。
     */
    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;

    /**释放锁
     *
     */
    void unlock();

    /**返回绑定lock上的新的Condition实例。
     * lock和condition  一对多。
     * 1、在该条件等待之前,必须先获取到锁。
     * 2、调用Condition.await方法之前，自动释放锁。
     */
    Condition newCondition();
}
