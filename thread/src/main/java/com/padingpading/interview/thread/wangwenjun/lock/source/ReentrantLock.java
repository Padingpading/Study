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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**接口:
 *  Lock:获取锁和释放锁。
 *
 *内部的类
 *   Sync:排他锁的释放,校验是否是排它锁
 *   NonfairSync:非公平锁,获取锁的逻辑是非公平
 *   FairSync:公平锁,获取所的逻辑是公平的。
 *
 */
public class ReentrantLock implements java.util.concurrent.locks.Lock, java.io.Serializable {
    private static final long serialVersionUID = 7373984872572414699L;
    
    /**同步器
     *
     * NonfairSync：
     */
    private final Sync sync;
    
    /*===================================================构造方法===================================================*/
    /**默认为非公平锁。
     */
    public ReentrantLock() {
        sync = new NonfairSync();
    }
    
    /**设置非公平还是公平锁
     */
    public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }
    
    /**
     */
    abstract static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -5179523762034025860L;

        /**
         * Performs {@link java.util.concurrent.locks.Lock#lock}. The main reason for subclassing
         * is to allow fast path for nonfair version.
         */
        abstract void lock();

        /**非公平的获取
         *
         */
        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            //没有线程持有锁
            if (c == 0) {
                //直接抢,不会判断有没有等待的线程
                if (compareAndSetState(0, acquires)) {
                    //抢夺成功
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            //可重入判断
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    
        /**排他锁的释放
         * 非公平和公平锁的释放没有区别。
         */
        @Override
        protected final boolean tryRelease(int releases) {
            // 首先将当前持有锁的线程个数减1(回溯到调用源头sync.release(1)可知, releases的值为1)
            // 这里的操作主要是针对可重入锁的情况下, c可能大于1
            int c = getState() - releases;
            // 释放锁的线程当前必须是持有锁的线程
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            
            // 如果c为0了, 说明锁已经完全释放了
            boolean free = false;
            //没有线程持有这把锁。
            if (c == 0) {
                //返回调用者true,标识没有线程持有以lock
                //也可能是重入锁。
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }
    
        /**是否是独占
         */
        @Override
        protected final boolean isHeldExclusively() {
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        final ConditionObject newCondition() {
            return new ConditionObject();
        }

        final Thread getOwner() {
            return getState() == 0 ? null : getExclusiveOwnerThread();
        }

        final int getHoldCount() {
            return isHeldExclusively() ? getState() : 0;
        }

        final boolean isLocked() {
            return getState() != 0;
        }

        /**
         * Reconstitutes the instance from a stream (that is, deserializes it).
         */
        private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            setState(0); // reset to unlocked state
        }
    }

    /**
     * Sync object for non-fair locks
     */
    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = 7316153563782823691L;

        /**非公平锁获取
         * 1、第一次获取锁,不管有没有等待的线程,直接去抢。
         * 2、
         */
        @Override
        final void lock() {
            //尝试争抢锁,争抢失败,添加到阻塞队列。
            if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }
        @Override
        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }

    /**
     * Sync object for fair locks
     */
    static final class FairSync extends Sync {
        private static final long serialVersionUID = -3000897897090466540L;

        final void lock() {
            //直接添加到阻塞队列,这里不做争抢
            acquire(1);
        }

        /**公平锁
         * 1、队列中没有等待的线程并且state=0,可以获取。
         * 2、如果当前占有锁的线程是自己,可以获取。
         */
        protected final boolean tryAcquire(int acquires) {
            //获取所的线程
            final Thread current = Thread.currentThread();
            // 首先获取当前锁的状态
            int c = getState();
            if (c == 0) {
                // c=0 说明当前锁是avaiable的, 没有被任何线程占用, 可以尝试获取
                // 因为是实现公平锁, 所以在抢占之前首先看看队列中有没有排在自己前面的Node
                // 如果没有人在排队, 则通过CAS方式获取锁, 就可以直接退出了
                if (!hasQueuedPredecessors() &&
                        // 将当前线程设置为占用锁的线程
                    compareAndSetState(0, acquires)) {
                    //获取到锁,设置独占线程。
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            // 如果 c>0 说明锁已经被占用了
            // 对于可重入锁, 这个时候检查占用锁的线程是不是就是当前线程,是的话,说明已经拿到了锁, 直接重入就行
            else if (current == getExclusiveOwnerThread()) {
                //可重入,添加获取数量
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                //设置state,
                setState(nextc);
                return true;
            }
            // 到这里说明有人占用了锁, 并且占用锁的不是当前线程, 则获取锁失败
            return false;
        }
    }
    
    
    /**获取锁
     */
    public void lock() {
        sync.lock();
    }

    /**可中断获取锁。
     */
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    /**尝试获取锁
     * 获取不到锁,返回false。
     */
    public boolean tryLock() {
        return sync.nonfairTryAcquire(1);
    }

    /**基于时间的尝试获取锁。
     */
    public boolean tryLock(long timeout, TimeUnit unit)
            throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }

    /**释放锁
    
     */
    public void unlock() {
        sync.release(1);
    }

    /**创建条件对象。
     *
     */
    public Condition newCondition() {
        return sync.newCondition();
    }

    /**
  
     */
    public boolean isLocked() {
        return sync.isLocked();
    }

    /**
   
     */
    public final boolean isFair() {
        return sync instanceof FairSync;
    }

    /**
  
     */
    protected Thread getOwner() {
        return sync.getOwner();
    }
    
    /*==================================================同步队列=========================================================*/
    
    /**同步队列是否有及诶单。
   
     */
    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    /**队列中是否含有当前线程
   
     */
    public final boolean hasQueuedThread(Thread thread) {
        return sync.isQueued(thread);
    }

    /**同步队列长度。
     */
    public final int getQueueLength() {
        return sync.getQueueLength();
    }

    /**
    
     */
    protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }
    
    
    /*==================================================条件队列=========================================================*/

    /**条件队列是否有节点。
    
     */
    public boolean hasWaiters(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.hasWaiters((AbstractQueuedSynchronizer.ConditionObject)condition);
    }
    
    /**条件队列长度。。
     
     */
    public int getWaitQueueLength(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitQueueLength((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    /**获取条件队列的线程
   
     */
    protected Collection<Thread> getWaitingThreads(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitingThreads((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    /*========================================================普通方法=================================================*/
    
    
    
    /**
    
     */
    public String toString() {
        Thread o = sync.getOwner();
        return super.toString() + ((o == null) ?
                                   "[Unlocked]" :
                                   "[Locked by thread " + o.getName() + "]");
    }
}
