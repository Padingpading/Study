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
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 *
 * 代Generation:由于CyclicBarrier是可重复使用的，我们把每一个新的barrier称为一“代”。这个怎么理解呢，打个比方：一个过山车有10个座位，景区常常需要等够10个人了，才会去开动过山车。于是我们常常在栏杆（barrier）外面等，等凑够了10个人，
 * 工作人员就把栏杆打开，让10个人通过；然后再将栏杆归位，后面新来的人还是要在栏杆外等待。这里，前面已经通过的人就是一“代”，后面再继续等待的一波人就是另外一“代”，栏杆每打开关闭一次，就产生新一的“代”
 */
public class CyclicBarrier {
    
    private Generation generation = new Generation();
    /** The lock for guarding barrier entry */
    private final ReentrantLock lock = new ReentrantLock();
    /** Condition to wait on until tripped */
    private final Condition trip = lock.newCondition();

    //Runnable对象，代表了一个任务。当所有线程都到齐后，在它们一同通过barrier之前，
    // 就会执行这个对象的run方法，因此，它有点类似于一个钩子方法。
    private final Runnable barrierCommand;
    
   // 这两个属性都是用来表征线程的数量，parties代表了参与线程的总数，即需要一同通过barrier的线程数，它是final类型的，由构造函数初始化，在类被创建后就一直不变了；
   // count属性和CountDownLatch中的count一样，代表还需要等待的线程数，初始值为parties，每当一个线程到来就减一，如果该值为0，则说明所有的线程都到齐了，大家可以一起通过barrier了。
    //参与线程的总数
    private final int parties;
    /**需要等待的线程数
     */
    private int count;
    
    public CyclicBarrier(int parties, Runnable barrierAction) {
        if (parties <= 0) throw new IllegalArgumentException();
        this.parties = parties;
        this.count = parties;
        this.barrierCommand = barrierAction;
    }
    
    public CyclicBarrier(int parties) {
        this(parties, null);
    }
    

    /**通常是被最后一个调用await方法的线程调用。在该方法中，
     * 我们的主要工作就是唤醒当前这一代中所有等待在条件队列里的线程
     * ，将count的值恢复为parties，以及开启新的一代。
     */
    private void nextGeneration() {
        // 唤醒当前这一代中所有等待在条件队列里的线程
        trip.signalAll();
        // 恢复count值，开启新的一代
        count = parties;
        generation = new Generation();
    }

    /**打破现有的栅栏，让所有线程通过：
     * 有时候某个时间段，景区的人比较少，等待过山车的人数凑不够10个人，眼看后面迟迟没有人再来，这个时候有的工作人员也会打开栅栏，让正在等待的人进来坐过山车。
     * 这里工作人员的行为就是breakBarrier，由于并不是在凑够10个人的情况下就开启了栅栏，我们就把这一代的broken状态标记为true。
     */
    private void breakBarrier() {
        generation.broken = true;
        count = parties;
        trip.signalAll();
    }

    /**集“countDown”和“阻塞等待”于一体的方法。
     */
    private int dowait(boolean timed, long nanos)
        throws InterruptedException, BrokenBarrierException, TimeoutException {
        final ReentrantLock lock = this.lock;
        // 所有执行await方法的线程必须是已经持有了锁，所以这里必须先获取锁
        lock.lock();
        try {
            final Generation g = generation;
            // 前面说过，调用breakBarrier会将当前“代”的broken属性设为true
            // 如果一个正在await的线程发现barrier已经被break了，则将直接抛出BrokenBarrierException异常
            if (g.broken)
                throw new BrokenBarrierException();
            
            // 如果当前线程被中断了，则先将栅栏打破，再抛出InterruptedException
            // 这么做的原因是，所以等待在barrier的线程都是相互等待的，如果其中一个被中断了，那其他的就不用等了。
            if (Thread.interrupted()) {
                breakBarrier();
                throw new InterruptedException();
            }
            // 当前线程已经来到了栅栏前，先将等待的线程数减一
            // 则可以唤醒所有等待的线程，让大家一起通过栅栏，并重置栅栏
            int index = --count;
            if (index == 0) {  // tripped
                boolean ranAction = false;
                try {
                    // 如果创建CyclicBarrier时传入了barrierCommand
                    final Runnable command = barrierCommand;
                    // 说明通过栅栏前有一些额外的工作要做
                    if (command != null)
                        command.run();
                    ranAction = true;
                    // 唤醒所有线程，开启新一代
                    // 释放队列的
                    nextGeneration();
                    return 0;
                } finally {
                    //run可能出现异常。
                    if (!ranAction)
                        breakBarrier();
                }
            }
    
            // 如果count数不为0，就将当前线程挂起，直到所有的线程到齐，或者超时，或者中断发生
            for (;;) {
                try {
                    // 如果没有设定超时机制，则直接调用condition的await方法
                    if (!timed)
                        // 当前线程在这里被挂起
                        trip.await();
                    else if (nanos > 0L)
                        // 如果设了超时，则等待指定的时间
                        nanos = trip.awaitNanos(nanos);
                } catch (InterruptedException ie) { // 当前线程在这里被挂起，超时时间到了就会自动唤醒
                    // 执行到这里说明线程被中断了
                    // 如果线程被中断时还处于当前这一“代”，并且当前这一代还没有被broken,则先打破栅栏
                    if (g == generation && ! g.broken) {
                        breakBarrier();
                        throw ie;
                    } else {
                        // 注意来到这里有两种情况
                        // 一种是g!=generation，说明新的一代已经产生了，所以我们没有必要处理这个中断，只要再自我中断一下就好，交给后续的人处理
                        // 一种是g.broken = true, 说明中断前栅栏已经被打破了，既然中断发生时栅栏已经被打破了，也没有必要再处理这个中断了
                        Thread.currentThread().interrupt();
                    }
                }
                // 注意，执行到这里是对应于线程从await状态被唤醒了
                // 这里先检测broken状态，能使broken状态变为true的，只有breakBarrier()方法，到这里对应的场景是
                // 1. 其他执行await方法的线程在挂起前就被中断了
                // 2. 其他执行await方法的线程在还处于等待中时被中断了
                // 2. 最后一个到达的线程在执行barrierCommand的时候发生了错误
                // 4. reset()方法被调用
                if (g.broken)
                    throw new BrokenBarrierException();
                // 如果线程被唤醒时，新一代已经被开启了，说明一切正常，直接返回
                if (g != generation)
                    return index;
                
                // 如果是因为超时时间到了被唤醒，则打破栅栏，返回TimeoutException
                if (timed && nanos <= 0L) {
                    breakBarrier();
                    throw new TimeoutException();
                }
            }
        } finally {
            lock.unlock();
        }
    }
    /**集“countDown”和“阻塞等待”于一体的方法。
     */
    public int await() throws InterruptedException, BrokenBarrierException {
        try {
            return dowait(false, 0L);
        } catch (TimeoutException toe) {
            throw new Error(toe); // cannot happen
        }
    }

    /**
     * Waits until all {@linkplain #getParties parties} have invoked
     * {@code await} on this barrier, or the specified waiting time elapses.
     *
     * <p>If the current thread is not the last to arrive then it is
     * disabled for thread scheduling purposes and lies dormant until
     * one of the following things happens:
     * <ul>
     * <li>The last thread arrives; or
     * <li>The specified timeout elapses; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * one of the other waiting threads; or
     * <li>Some other thread times out while waiting for barrier; or
     * <li>Some other thread invokes {@link #reset} on this barrier.
     * </ul>
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p>If the specified waiting time elapses then {@link TimeoutException}
     * is thrown. If the time is less than or equal to zero, the
     * method will not wait at all.
     *
     * <p>If the barrier is {@link #reset} while any thread is waiting,
     * or if the barrier {@linkplain #isBroken is broken} when
     * {@code await} is invoked, or while any thread is waiting, then
     * {@link BrokenBarrierException} is thrown.
     *
     * <p>If any thread is {@linkplain Thread#interrupt interrupted} while
     * waiting, then all other waiting threads will throw {@link
     * BrokenBarrierException} and the barrier is placed in the broken
     * state.
     *
     * <p>If the current thread is the last thread to arrive, and a
     * non-null barrier action was supplied in the constructor, then the
     * current thread runs the action before allowing the other threads to
     * continue.
     * If an exception occurs during the barrier action then that exception
     * will be propagated in the current thread and the barrier is placed in
     * the broken state.
     *
     * @param timeout the time to wait for the barrier
     * @param unit the time unit of the timeout parameter
     * @return the arrival index of the current thread, where index
     *         {@code getParties() - 1} indicates the first
     *         to arrive and zero indicates the last to arrive
     * @throws InterruptedException if the current thread was interrupted
     *         while waiting
     * @throws TimeoutException if the specified timeout elapses.
     *         In this case the barrier will be broken.
     * @throws BrokenBarrierException if <em>another</em> thread was
     *         interrupted or timed out while the current thread was
     *         waiting, or the barrier was reset, or the barrier was broken
     *         when {@code await} was called, or the barrier action (if
     *         present) failed due to an exception
     */
    public int await(long timeout, TimeUnit unit)
        throws InterruptedException,
               BrokenBarrierException,
               TimeoutException {
        return dowait(true, unit.toNanos(timeout));
    }

    /**
     * Queries if this barrier is in a broken state.
     *
     * @return {@code true} if one or more parties broke out of this
     *         barrier due to interruption or timeout since
     *         construction or the last reset, or a barrier action
     *         failed due to an exception; {@code false} otherwise.
     */
    public boolean isBroken() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return generation.broken;
        } finally {
            lock.unlock();
        }
    }

    /**reset方法用于将barrier恢复成初始的状态，它的内部就是简单地调用了breakBarrier方法和nextGeneration方法。
     */
    public void reset() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            breakBarrier();   // break the current generation
            nextGeneration(); // start a new generation
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the number of parties currently waiting at the barrier.
     * This method is primarily useful for debugging and assertions.
     *
     * @return the number of parties currently blocked in {@link #await}
     */
    public int getNumberWaiting() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return parties - count;
        } finally {
            lock.unlock();
        }
    }
    
    
    private static class Generation {
        boolean broken = false;
    }
    
    
    public int getParties() {
        return parties;
    }
}
