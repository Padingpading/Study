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
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Serializable:可序列化
 *
 */
public class Semaphore implements java.io.Serializable {
    private static final long serialVersionUID = -3222578661600680210L;
    
    
    
    /*========================================================构造器===================================================*/
    public Semaphore(int permits) {
        sync = new NonfairSync(permits);
    }
    
    public Semaphore(int permits, boolean fair) {
        sync = fair ? new FairSync(permits) : new NonfairSync(permits);
    }
    
    
    /**同步器
     *
     */
    private final Sync sync;

    abstract static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 1192457210091910933L;

        Sync(int permits) {
            setState(permits);
        }

        final int getPermits() {
            return getState();
        }
    
        /**非公平获取锁
         */
        final int nonfairTryAcquireShared(int acquires) {
            for (;;) {
                //获取允许通过的线程数
                int available = getState();
                //获取剩余量
                int remaining = available - acquires;
                //自旋方式
                //Semaphore的tryAcquire逻辑是一个自旋操作，因为Semaphore是共享锁，
                // 同一时刻可能有多个线程来修改这个值，所以我们必须使用自旋 + CAS来避免线程冲突。
                if (remaining < 0 ||
                    compareAndSetState(available, remaining))
                    //如果该值小于0，则代表当前线程获取共享锁失败
                    //如果该值大于0，则代表当前线程获取共享锁成功，并且接下来其他线程尝试获取共享锁的行为很可能成功
                    //如果该值等于0，则代表当前线程获取共享锁成功，但是接下来其他线程尝试获取共享锁的行为会失败
                    return remaining;
            }
        }
    
        /**释放共享锁(公平和非公平)
         *
         */
        protected final boolean tryReleaseShared(int releases) {
            for (;;) {
                int current = getState();
                //添加信号量。
                int next = current + releases;
                if (next < current) // overflow
                    throw new Error("Maximum permit count exceeded");
                //Cas操作。
                if (compareAndSetState(current, next))
                    return true;
            }
        }

        final void reducePermits(int reductions) {
            for (;;) {
                int current = getState();
                int next = current - reductions;
                if (next > current) // underflow
                    throw new Error("Permit count underflow");
                if (compareAndSetState(current, next))
                    return;
            }
        }

        final int drainPermits() {
            for (;;) {
                int current = getState();
                if (current == 0 || compareAndSetState(current, 0))
                    return current;
            }
        }
    }

    /**公平
     */
    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = -2694183684443567898L;

        NonfairSync(int permits) {
            super(permits);
        }
    
        /**非公平锁获取
         */
        protected int tryAcquireShared(int acquires) {
            return nonfairTryAcquireShared(acquires);
        }
    }

    /**非公平
     */
    static final class FairSync extends Sync {
        private static final long serialVersionUID = 2014338818796000944L;

        FairSync(int permits) {
            super(permits);
        }
    
        /**公平锁获取
         */
        protected int tryAcquireShared(int acquires) {
            for (;;) {
                //即在获取共享锁之前，头结点后边是否还有其他线程节点.
                //如果有,返回获取所失败。
                if (hasQueuedPredecessors())
                    return -1;
                int available = getState();
                int remaining = available - acquires;
                if (remaining < 0 ||
                    compareAndSetState(available, remaining))
                    return remaining;
            }
        }
    }



    /**获取信号量
     */
    public void acquire() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**不可中断获取
     */
    public void acquireUninterruptibly() {
        sync.acquireShared(1);
    }
    
    /**
     */
    public void acquire(int permits) throws InterruptedException {
        if (permits < 0) throw new IllegalArgumentException();
        sync.acquireSharedInterruptibly(permits);
    }
    
    /**
     
     */
    public void acquireUninterruptibly(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        sync.acquireShared(permits);
    }
    
    /**释放信号量
     */
    public void release() {
        sync.releaseShared(1);
    }
    
    
    /**
     */
    public void release(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        sync.releaseShared(permits);
    }
    
    
    /*====================================================Semaphore的tryAcquire========================================*/
    //tryAcquire和acquire的区别是:
    //acquire:返回空,获取不到会进入同步队列。
    //tryAcquire:返回true或false,获取不到不会进入同步队列中。
    /**尝试获取
     */
    public boolean tryAcquire() {
        return sync.nonfairTryAcquireShared(1) >= 0;
    }

    public boolean tryAcquire(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        return sync.nonfairTryAcquireShared(permits) >= 0;
    }

    /**超时机制
     */
    public boolean tryAcquire(int permits, long timeout, TimeUnit unit)
        throws InterruptedException {
        if (permits < 0) throw new IllegalArgumentException();
        return sync.tryAcquireSharedNanos(permits, unit.toNanos(timeout));
    }
    
    /**
     */
    public boolean tryAcquire(long timeout, TimeUnit unit)
            throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    /*==================================================工具方法=======================================================*/
    /**获取当前 存留的信号量数。
     */
    public int availablePermits() {
        return sync.getPermits();
    }

    /**
     *
     */
    public int drainPermits() {
        return sync.drainPermits();
    }
    
    
    /**减少信号量的总数
     */
    protected void reducePermits(int reduction) {
        if (reduction < 0) throw new IllegalArgumentException();
        sync.reducePermits(reduction);
    }

  
    public boolean isFair() {
        return sync instanceof FairSync;
    }

  
    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

   
    public final int getQueueLength() {
        return sync.getQueueLength();
    }


    protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }
    
    /*=========================================================其他====================================================*/

    public String toString() {
        return super.toString() + "[Permits = " + sync.getPermits() + "]";
    }
}
