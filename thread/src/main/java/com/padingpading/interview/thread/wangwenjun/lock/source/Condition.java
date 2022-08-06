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

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;


/**Condition:条件队列。
 * 1、类似Object的wait、notify,notfiyAll,让每一个锁都拥有多个等待集合,让线程可以进入不同的等待集合(synchronizd只有一个等待集合)
 * 2、让线程挂起,直到被另一个线程通知,对共享状态访问是多线程。
 * 3、condition和lock是绑定的 多对一。
 * 4、condition和monitor的wait、notify 没有关系。
 * 5、condition.wait 应该放置到一个while循环中,避免假唤醒。
 * 6、被唤醒和执行时两个概念。
 */
public interface Condition {

    /**导致当前线程处于等待,知道被signalled 或者 interrupted.
     * 关联的lock将会释放,直到出现四种情况
     * 1、其他线程调用signalled方法,正好选择当前线程唤醒。
     * 2、其他线程调用signalAll方法,当前Condition上等待的线程都会被唤醒。
     * 3、其他线程中断(interrupt)了当前线程.
     * 4、假唤醒。
     * 方法能够返回:被唤醒之后,如果再次获取到lock。
     * 中断:
     * 1、如果线程进入方法之前已经被中断了。?
     * 2、线程被挂起的时候中断了。
     * 抛出中断异常、中断状态被清理掉。
     */
    void await() throws InterruptedException;

    /**不可中断的await,当前线程会等待,直到调用signal
     * 1、其他线程调用signalled方法,正好选择当前线程唤醒。
     * 2、其他线程调用signalAll方法,当前Condition上等待的线程都会被唤醒。
     * 3、假唤醒。
     */
    void awaitUninterruptibly();

    /**基于等待时间await
     * 1、其他线程调用signalled方法,正好选择当前线程唤醒。
     * 2、其他线程调用signalAll方法,当前Condition上等待的线程都会被唤醒。
     * 3、其他线程中断(interrupt)了当前线程.
     * 4、知道等待时间还没有被single,会自动释放,返回之后也要持有锁。
     * 5、假唤醒。
     *
     * return:返回剩余时间nanos 200
     * 最长500
     * 实际300
     * 剩余200
     * 小于0:超时了。
     * 返回纳秒,等待时间可能有误差,使用纳秒更加精确。到
     */
    long awaitNanos(long nanosTimeout) throws InterruptedException;

    /**基于等待时间await。可以指定时间格式。
     */
    boolean await(long time, TimeUnit unit) throws InterruptedException;

    /**基于截止时间await。
     */
    boolean awaitUntil(Date deadline) throws InterruptedException;

    /**唤醒一个等待的线程(子类决定是否唤醒哪一个),如果condition有多个等待的线程,由具体实现决定唤醒哪一个。
     * 线程调用必须持有condition绑定的那个锁,没有持有会抛出异常。
     */
    void signal();

    /**唤醒所有处于等待的线程
     */
    void signalAll();
}
