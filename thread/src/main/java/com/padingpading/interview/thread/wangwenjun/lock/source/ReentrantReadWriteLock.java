//
//
//package com.padingpading.interview.thread.wangwenjun.lock.source;
//
//import java.util.Collection;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.locks.AbstractQueuedSynchronizer;
//import java.util.concurrent.locks.Condition;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReadWriteLock;
//import java.util.concurrent.locks.ReentrantLock;
//
///**
// * @author Doug Lea
// */
//public class ReentrantReadWriteLock
//        implements ReadWriteLock, java.io.Serializable {
//    private static final long serialVersionUID = -6992448646407690164L;
//
//    private final ReadLock readerLock;
//
//    private final WriteLock writerLock;
//
//    final Sync sync;
//
//
//    /*=================================================构造方法========================================================*/
//    /**默认非公平
//     */
//    public ReentrantReadWriteLock() {
//        this(false);
//    }
//
//    /**最终构造方法
//     */
//    public ReentrantReadWriteLock(boolean fair) {
//        //创建公平或者非公平
//        sync = fair ? new FairSync() : new NonfairSync();
//        //初始化读锁
//        readerLock = new ReadLock(this);
//        //初始化写锁
//        writerLock = new WriteLock(this);
//    }
//
//    //读写锁获取
//    public WriteLock writeLock() { return writerLock; }
//    public ReadLock  readLock()  { return readerLock; }
//
//    /**
//     * Synchronization implementation for ReentrantReadWriteLock.
//     * Subclassed into fair and nonfair versions.
//     */
//    abstract static class Sync extends java.util.concurrent.locks.AbstractQueuedSynchronizer {
//        private static final long serialVersionUID = 6317671515068378041L;
//
//        //写锁和读锁的位分割
//        static final int SHARED_SHIFT   = 16;
//        static final int SHARED_UNIT    = (1 << SHARED_SHIFT);
//        static final int MAX_COUNT      = (1 << SHARED_SHIFT) - 1;
//        static final int EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1;
//
//        //共享锁(读锁)
//        static int sharedCount(int c)    { return c >>> SHARED_SHIFT; }
//        //独占锁(写锁)
//        static int exclusiveCount(int c) { return c & EXCLUSIVE_MASK; }
//
//        /**
//         * A counter for per-thread read hold counts.
//         * Maintained as a ThreadLocal; cached in cachedHoldCounter
//         */
//        static final class HoldCounter {
//            int count = 0;
//            // Use id, not reference, to avoid garbage retention
//            final long tid = getThreadId(Thread.currentThread());
//        }
//
//        /**
//         * ThreadLocal subclass. Easiest to explicitly define for sake
//         * of deserialization mechanics.
//         */
//        static final class ThreadLocalHoldCounter
//            extends ThreadLocal<HoldCounter> {
//            public HoldCounter initialValue() {
//                return new HoldCounter();
//            }
//        }
//
//        /**
//         * The number of reentrant read locks held by current thread.
//         * Initialized only in constructor and readObject.
//         * Removed whenever a thread's read hold count drops to 0.
//         */
//        private transient ThreadLocalHoldCounter readHolds;
//
//        /**
//         * The hold count of the last thread to successfully acquire
//         * readLock. This saves ThreadLocal lookup in the common case
//         * where the next thread to release is the last one to
//         * acquire. This is non-volatile since it is just used
//         * as a heuristic, and would be great for threads to cache.
//         *
//         * <p>Can outlive the Thread for which it is caching the read
//         * hold count, but avoids garbage retention by not retaining a
//         * reference to the Thread.
//         *
//         * <p>Accessed via a benign data race; relies on the memory
//         * model's final field and out-of-thin-air guarantees.
//         */
//        private transient HoldCounter cachedHoldCounter;
//
//        /**第一个获取读锁的线程,
//         *
//         */
//        private transient Thread firstReader = null;
//
//        /**
//         * 第一个读线程的重入次数
//         */
//        private transient int firstReaderHoldCount;
//
//        Sync() {
//            readHolds = new ThreadLocalHoldCounter();
//            setState(getState()); // ensures visibility of readHolds
//        }
//
//        /*
//         * Acquires and releases use the same code for fair and
//         * nonfair locks, but differ in whether/how they allow barging
//         * when queues are non-empty.
//         */
//
//        /**
//         * Returns true if the current thread, when trying to acquire
//         * the read lock, and otherwise eligible to do so, should block
//         * because of policy for overtaking other waiting threads.
//         */
//        abstract boolean readerShouldBlock();
//
//        /**
//         * Returns true if the current thread, when trying to acquire
//         * the write lock, and otherwise eligible to do so, should block
//         * because of policy for overtaking other waiting threads.
//         */
//        abstract boolean writerShouldBlock();
//
//        /*写锁的释放
//         */
//        protected final boolean tryRelease(int releases) {
//            //判断是否是获取写锁的线程。
//            if (!isHeldExclusively())
//                throw new IllegalMonitorStateException();
//            //
//            int nextc = getState() - releases;
//            //判断减后的state是否是全部释放。
//            boolean free = exclusiveCount(nextc) == 0;
//            if (free)
//                //设置线程为null
//                setExclusiveOwnerThread(null);
//            //设置state
//            setState(nextc);
//            return free;
//        }
//
//
//        /**排他锁(写锁)的获取
//         */
//        protected final boolean tryAcquire(int acquires) {
//            //获取到当前对象
//            Thread current = Thread.currentThread();
//            //state
//            int c = getState();
//            //写锁数量
//            int w = exclusiveCount(c);
//            //当前有线程占有锁,不管是读锁还是写锁 线程。
//            if (c != 0) {
//                // (Note: if c != 0 and w == 0 then shared count != 0)
//                //读锁线程！=0,并且不是重入
//                if (w == 0 || current != getExclusiveOwnerThread())
//                    return false;
//                //可重入锁:当前数量+获取数量>最大值
//                if (w + exclusiveCount(acquires) > MAX_COUNT)
//                    throw new Error("Maximum lock count exceeded");
//                // Reentrant acquire
//                //设置值。
//                setState(c + acquires);
//                return true;
//            }
//            //没有线程获取
//            //校验当前线程是否要到队列里去。
//            //   公平:判断head节点后head后的节点是共享锁。
//            //   非公平:直接去队列
//            //CAS原型性更新。
//            if (writerShouldBlock() ||
//                !compareAndSetState(c, c + acquires))
//                return false;
//            //设置排他锁线程。
//            setExclusiveOwnerThread(current);
//            return true;
//        }
//
//        /**共享锁(读锁),释放
//         *
//         */
//        protected final boolean tryReleaseShared(int unused) {
//            Thread current = Thread.currentThread();
//            //第一个读锁的线程
//            if (firstReader == current) {
//                //重入次数=1,设置线程为null
//                if (firstReaderHoldCount == 1)
//                    firstReader = null;
//                else
//                    //重入次数--,
//                    firstReaderHoldCount--;
//            } else {
//                //非第一个读线程,可重入次数保存到Threadlocal中。
//                HoldCounter rh = cachedHoldCounter;
//                if (rh == null || rh.tid != getThreadId(current))
//                    rh = readHolds.get();
//                //持有读锁的数量。
//                int count = rh.count;
//                if (count <= 1) {
//                    readHolds.remove();
//                    if (count <= 0)
//                        throw unmatchedUnlockException();
//                }
//                //持有读锁--
//                --rh.count;
//            }
//            //
//            for (;;) {
//                int c = getState();
//                //state-1
//                int nextc = c - SHARED_UNIT;
//                //CAS原子更新
//                if (compareAndSetState(c, nextc))
//                    // 如果读锁数亮=0,写锁有机会获取。
//                    return nextc == 0;
//            }
//        }
//
//        private IllegalMonitorStateException unmatchedUnlockException() {
//            return new IllegalMonitorStateException(
//                "attempt to unlock read lock, not locked by current thread");
//        }
//
//
//        /**共享锁的获取
//         *
//         */
//        protected final int tryAcquireShared(int unused) {
//            //获取当前线程
//            Thread current = Thread.currentThread();
//            //获取state
//            int c = getState();
//            //写锁
//            if (exclusiveCount(c) != 0 && getExclusiveOwnerThread() != current)
//                //1、state低16位为0,没有独占线程
//                //2、独占线程是不是当前线程？血锁内部有调用了读锁？
//                return -1;
//            //读锁,共享线程数量
//            int r = sharedCount(c);
//            //readerShouldBlock()可能是判断是否可以去争夺锁
//            // 公平:直接排队。
//            // 非公平:查看队列后面是否为共享节点,去抢鄋。
//            //r < MAX_COUNT:小于最大线程数量,
//            //Cas原子更新。
//            if (!readerShouldBlock() && r < MAX_COUNT && compareAndSetState(c, c + SHARED_UNIT)) {
//                //占有锁的读线程
//                if (r == 0) {
//                    //设置当前的读线程
//                    firstReader = current;
//                    //设置当前的读线程数量
//                    firstReaderHoldCount = 1;
//                } else if (firstReader == current) {
//                    //
//                    firstReaderHoldCount++;
//                } else {
//                    HoldCounter rh = cachedHoldCounter;
//                    if (rh == null || rh.tid != getThreadId(current))
//                        cachedHoldCounter = rh = readHolds.get();
//                    else if (rh.count == 0)
//                        readHolds.set(rh);
//                    rh.count++;
//                }
//                return 1;
//            }
//            return fullTryAcquireShared(current);
//        }
//
//        /**
//         * Full version of acquire for reads, that handles CAS misses
//         * and reentrant reads not dealt with in tryAcquireShared.
//         */
//        final int fullTryAcquireShared(Thread current) {
//            /*
//             * This code is in part redundant with that in
//             * tryAcquireShared but is simpler overall by not
//             * complicating tryAcquireShared with interactions between
//             * retries and lazily reading hold counts.
//             */
//            HoldCounter rh = null;
//            for (;;) {
//                int c = getState();
//                if (exclusiveCount(c) != 0) {
//                    if (getExclusiveOwnerThread() != current)
//                        return -1;
//                    // else we hold the exclusive lock; blocking here
//                    // would cause deadlock.
//                } else if (readerShouldBlock()) {
//                    // Make sure we're not acquiring read lock reentrantly
//                    if (firstReader == current) {
//                        // assert firstReaderHoldCount > 0;
//                    } else {
//                        if (rh == null) {
//                            rh = cachedHoldCounter;
//                            if (rh == null || rh.tid != getThreadId(current)) {
//                                rh = readHolds.get();
//                                if (rh.count == 0)
//                                    readHolds.remove();
//                            }
//                        }
//                        if (rh.count == 0)
//                            return -1;
//                    }
//                }
//                if (sharedCount(c) == MAX_COUNT)
//                    throw new Error("Maximum lock count exceeded");
//                if (compareAndSetState(c, c + SHARED_UNIT)) {
//                    if (sharedCount(c) == 0) {
//                        firstReader = current;
//                        firstReaderHoldCount = 1;
//                    } else if (firstReader == current) {
//                        firstReaderHoldCount++;
//                    } else {
//                        if (rh == null)
//                            rh = cachedHoldCounter;
//                        if (rh == null || rh.tid != getThreadId(current))
//                            rh = readHolds.get();
//                        else if (rh.count == 0)
//                            readHolds.set(rh);
//                        rh.count++;
//                        cachedHoldCounter = rh; // cache for release
//                    }
//                    return 1;
//                }
//            }
//        }
//
//        /**
//         * Performs tryLock for write, enabling barging in both modes.
//         * This is identical in effect to tryAcquire except for lack
//         * of calls to writerShouldBlock.
//         */
//        final boolean tryWriteLock() {
//            Thread current = Thread.currentThread();
//            int c = getState();
//            if (c != 0) {
//                int w = exclusiveCount(c);
//                if (w == 0 || current != getExclusiveOwnerThread())
//                    return false;
//                if (w == MAX_COUNT)
//                    throw new Error("Maximum lock count exceeded");
//            }
//            if (!compareAndSetState(c, c + 1))
//                return false;
//            setExclusiveOwnerThread(current);
//            return true;
//        }
//
//        /**
//         * Performs tryLock for read, enabling barging in both modes.
//         * This is identical in effect to tryAcquireShared except for
//         * lack of calls to readerShouldBlock.
//         */
//        final boolean tryReadLock() {
//            Thread current = Thread.currentThread();
//            for (;;) {
//                int c = getState();
//                if (exclusiveCount(c) != 0 &&
//                    getExclusiveOwnerThread() != current)
//                    return false;
//                int r = sharedCount(c);
//                if (r == MAX_COUNT)
//                    throw new Error("Maximum lock count exceeded");
//                if (compareAndSetState(c, c + SHARED_UNIT)) {
//                    if (r == 0) {
//                        firstReader = current;
//                        firstReaderHoldCount = 1;
//                    } else if (firstReader == current) {
//                        firstReaderHoldCount++;
//                    } else {
//                        HoldCounter rh = cachedHoldCounter;
//                        if (rh == null || rh.tid != getThreadId(current))
//                            cachedHoldCounter = rh = readHolds.get();
//                        else if (rh.count == 0)
//                            readHolds.set(rh);
//                        rh.count++;
//                    }
//                    return true;
//                }
//            }
//        }
//
//        protected final boolean isHeldExclusively() {
//            // While we must in general read state before owner,
//            // we don't need to do so to check if current thread is owner
//            return getExclusiveOwnerThread() == Thread.currentThread();
//        }
//
//        // Methods relayed to outer class
//
//        final ConditionObject newCondition() {
//            return new ConditionObject();
//        }
//
//        final Thread getOwner() {
//            // Must read state before owner to ensure memory consistency
//            return ((exclusiveCount(getState()) == 0) ?
//                    null :
//                    getExclusiveOwnerThread());
//        }
//
//        final int getReadLockCount() {
//            return sharedCount(getState());
//        }
//
//        final boolean isWriteLocked() {
//            return exclusiveCount(getState()) != 0;
//        }
//
//        final int getWriteHoldCount() {
//            return isHeldExclusively() ? exclusiveCount(getState()) : 0;
//        }
//
//        final int getReadHoldCount() {
//            if (getReadLockCount() == 0)
//                return 0;
//
//            Thread current = Thread.currentThread();
//            if (firstReader == current)
//                return firstReaderHoldCount;
//
//            HoldCounter rh = cachedHoldCounter;
//            if (rh != null && rh.tid == getThreadId(current))
//                return rh.count;
//
//            int count = readHolds.get().count;
//            if (count == 0) readHolds.remove();
//            return count;
//        }
//
//        /**
//         * Reconstitutes the instance from a stream (that is, deserializes it).
//         */
//        private void readObject(java.io.ObjectInputStream s)
//            throws java.io.IOException, ClassNotFoundException {
//            s.defaultReadObject();
//            readHolds = new ThreadLocalHoldCounter();
//            setState(0); // reset to unlocked state
//        }
//
//        final int getCount() { return getState(); }
//    }
//
//    /**
//     * Nonfair version of Sync
//     */
//    static final class NonfairSync extends Sync {
//        private static final long serialVersionUID = -8159625535654395037L;
//        final boolean writerShouldBlock() {
//            return false; // writers can always barge
//        }
//        final boolean readerShouldBlock() {
//            /* As a heuristic to avoid indefinite writer starvation,
//             * block if the thread that momentarily appears to be head
//             * of queue, if one exists, is a waiting writer.  This is
//             * only a probabilistic effect since a new reader will not
//             * block if there is a waiting writer behind other enabled
//             * readers that have not yet drained from the queue.
//             */
//            return apparentlyFirstQueuedIsExclusive();
//        }
//    }
//
//    /**
//     * Fair version of Sync
//     */
//    static final class FairSync extends Sync {
//        private static final long serialVersionUID = -2274990926593161451L;
//        final boolean writerShouldBlock() {
//            return hasQueuedPredecessors();
//        }
//        final boolean readerShouldBlock() {
//            return hasQueuedPredecessors();
//        }
//    }
//
//    /**读锁
//     */
//    public static class ReadLock implements java.util.concurrent.locks.Lock, java.io.Serializable {
//        private static final long serialVersionUID = -5992448646407690164L;
//        private final Sync sync;
//
//        /**读锁构造
//         */
//        protected ReadLock(ReentrantReadWriteLock lock) {
//            sync = lock.sync;
//        }
//
//        /**读锁
//         */
//        public void lock() {
//            sync.acquireShared(1);
//        }
//
//        /**
//         * Acquires the read lock unless the current thread is
//         * {@linkplain Thread#interrupt interrupted}.
//         *
//         * <p>Acquires the read lock if the write lock is not held
//         * by another thread and returns immediately.
//         *
//         * <p>If the write lock is held by another thread then the
//         * current thread becomes disabled for thread scheduling
//         * purposes and lies dormant until one of two things happens:
//         *
//         * <ul>
//         *
//         * <li>The read lock is acquired by the current thread; or
//         *
//         * <li>Some other thread {@linkplain Thread#interrupt interrupts}
//         * the current thread.
//         *
//         * </ul>
//         *
//         * <p>If the current thread:
//         *
//         * <ul>
//         *
//         * <li>has its interrupted status set on entry to this method; or
//         *
//         * <li>is {@linkplain Thread#interrupt interrupted} while
//         * acquiring the read lock,
//         *
//         * </ul>
//         *
//         * then {@link InterruptedException} is thrown and the current
//         * thread's interrupted status is cleared.
//         *
//         * <p>In this implementation, as this method is an explicit
//         * interruption point, preference is given to responding to
//         * the interrupt over normal or reentrant acquisition of the
//         * lock.
//         *
//         * @throws InterruptedException if the current thread is interrupted
//         */
//        public void lockInterruptibly() throws InterruptedException {
//            sync.acquireSharedInterruptibly(1);
//        }
//
//        /**
//         * Acquires the read lock only if the write lock is not held by
//         * another thread at the time of invocation.
//         *
//         * <p>Acquires the read lock if the write lock is not held by
//         * another thread and returns immediately with the value
//         * {@code true}. Even when this lock has been set to use a
//         * fair ordering policy, a call to {@code tryLock()}
//         * <em>will</em> immediately acquire the read lock if it is
//         * available, whether or not other threads are currently
//         * waiting for the read lock.  This &quot;barging&quot; behavior
//         * can be useful in certain circumstances, even though it
//         * breaks fairness. If you want to honor the fairness setting
//         * for this lock, then use {@link #tryLock(long, TimeUnit)
//         * tryLock(0, TimeUnit.SECONDS) } which is almost equivalent
//         * (it also detects interruption).
//         *
//         * <p>If the write lock is held by another thread then
//         * this method will return immediately with the value
//         * {@code false}.
//         *
//         * @return {@code true} if the read lock was acquired
//         */
//        public boolean tryLock() {
//            return sync.tryReadLock();
//        }
//
//        /**
//         * Acquires the read lock if the write lock is not held by
//         * another thread within the given waiting time and the
//         * current thread has not been {@linkplain Thread#interrupt
//         * interrupted}.
//         *
//         * <p>Acquires the read lock if the write lock is not held by
//         * another thread and returns immediately with the value
//         * {@code true}. If this lock has been set to use a fair
//         * ordering policy then an available lock <em>will not</em> be
//         * acquired if any other threads are waiting for the
//         * lock. This is in contrast to the {@link #tryLock()}
//         * method. If you want a timed {@code tryLock} that does
//         * permit barging on a fair lock then combine the timed and
//         * un-timed forms together:
//         *
//         *  <pre> {@code
//         * if (lock.tryLock() ||
//         *     lock.tryLock(timeout, unit)) {
//         *   ...
//         * }}</pre>
//         *
//         * <p>If the write lock is held by another thread then the
//         * current thread becomes disabled for thread scheduling
//         * purposes and lies dormant until one of three things happens:
//         *
//         * <ul>
//         *
//         * <li>The read lock is acquired by the current thread; or
//         *
//         * <li>Some other thread {@linkplain Thread#interrupt interrupts}
//         * the current thread; or
//         *
//         * <li>The specified waiting time elapses.
//         *
//         * </ul>
//         *
//         * <p>If the read lock is acquired then the value {@code true} is
//         * returned.
//         *
//         * <p>If the current thread:
//         *
//         * <ul>
//         *
//         * <li>has its interrupted status set on entry to this method; or
//         *
//         * <li>is {@linkplain Thread#interrupt interrupted} while
//         * acquiring the read lock,
//         *
//         * </ul> then {@link InterruptedException} is thrown and the
//         * current thread's interrupted status is cleared.
//         *
//         * <p>If the specified waiting time elapses then the value
//         * {@code false} is returned.  If the time is less than or
//         * equal to zero, the method will not wait at all.
//         *
//         * <p>In this implementation, as this method is an explicit
//         * interruption point, preference is given to responding to
//         * the interrupt over normal or reentrant acquisition of the
//         * lock, and over reporting the elapse of the waiting time.
//         *
//         * @param timeout the time to wait for the read lock
//         * @param unit the time unit of the timeout argument
//         * @return {@code true} if the read lock was acquired
//         * @throws InterruptedException if the current thread is interrupted
//         * @throws NullPointerException if the time unit is null
//         */
//        public boolean tryLock(long timeout, TimeUnit unit)
//                throws InterruptedException {
//            return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
//        }
//
//        /**
//         * Attempts to release this lock.
//         *
//         * <p>If the number of readers is now zero then the lock
//         * is made available for write lock attempts.
//         */
//        public void unlock() {
//            sync.releaseShared(1);
//        }
//
//        /**
//         * Throws {@code UnsupportedOperationException} because
//         * {@code ReadLocks} do not support conditions.
//         *
//         * @throws UnsupportedOperationException always
//         */
//        public java.util.concurrent.locks.Condition newCondition() {
//            throw new UnsupportedOperationException();
//        }
//
//        /**
//         * Returns a string identifying this lock, as well as its lock state.
//         * The state, in brackets, includes the String {@code "Read locks ="}
//         * followed by the number of held read locks.
//         *
//         * @return a string identifying this lock, as well as its lock state
//         */
//        public String toString() {
//            int r = sync.getReadLockCount();
//            return super.toString() +
//                "[Read locks = " + r + "]";
//        }
//    }
//
//    /**
//     * The lock returned by method {@link ReentrantReadWriteLock#writeLock}.
//     */
//    public static class WriteLock implements java.util.concurrent.locks.Lock, java.io.Serializable {
//        private static final long serialVersionUID = -4992448646407690164L;
//        private final Sync sync;
//
//        /**
//         * Constructor for use by subclasses
//         *
//         * @param lock the outer lock object
//         * @throws NullPointerException if the lock is null
//         */
//        protected WriteLock(ReentrantReadWriteLock lock) {
//            sync = lock.sync;
//        }
//
//        /**
//         * Acquires the write lock.
//         *
//         * <p>Acquires the write lock if neither the read nor write lock
//         * are held by another thread
//         * and returns immediately, setting the write lock hold count to
//         * one.
//         *
//         * <p>If the current thread already holds the write lock then the
//         * hold count is incremented by one and the method returns
//         * immediately.
//         *
//         * <p>If the lock is held by another thread then the current
//         * thread becomes disabled for thread scheduling purposes and
//         * lies dormant until the write lock has been acquired, at which
//         * time the write lock hold count is set to one.
//         */
//        public void lock() {
//            sync.acquire(1);
//        }
//
//        /**
//         * Acquires the write lock unless the current thread is
//         * {@linkplain Thread#interrupt interrupted}.
//         *
//         * <p>Acquires the write lock if neither the read nor write lock
//         * are held by another thread
//         * and returns immediately, setting the write lock hold count to
//         * one.
//         *
//         * <p>If the current thread already holds this lock then the
//         * hold count is incremented by one and the method returns
//         * immediately.
//         *
//         * <p>If the lock is held by another thread then the current
//         * thread becomes disabled for thread scheduling purposes and
//         * lies dormant until one of two things happens:
//         *
//         * <ul>
//         *
//         * <li>The write lock is acquired by the current thread; or
//         *
//         * <li>Some other thread {@linkplain Thread#interrupt interrupts}
//         * the current thread.
//         *
//         * </ul>
//         *
//         * <p>If the write lock is acquired by the current thread then the
//         * lock hold count is set to one.
//         *
//         * <p>If the current thread:
//         *
//         * <ul>
//         *
//         * <li>has its interrupted status set on entry to this method;
//         * or
//         *
//         * <li>is {@linkplain Thread#interrupt interrupted} while
//         * acquiring the write lock,
//         *
//         * </ul>
//         *
//         * then {@link InterruptedException} is thrown and the current
//         * thread's interrupted status is cleared.
//         *
//         * <p>In this implementation, as this method is an explicit
//         * interruption point, preference is given to responding to
//         * the interrupt over normal or reentrant acquisition of the
//         * lock.
//         *
//         * @throws InterruptedException if the current thread is interrupted
//         */
//        public void lockInterruptibly() throws InterruptedException {
//            sync.acquireInterruptibly(1);
//        }
//
//        /**
//         * Acquires the write lock only if it is not held by another thread
//         * at the time of invocation.
//         *
//         * <p>Acquires the write lock if neither the read nor write lock
//         * are held by another thread
//         * and returns immediately with the value {@code true},
//         * setting the write lock hold count to one. Even when this lock has
//         * been set to use a fair ordering policy, a call to
//         * {@code tryLock()} <em>will</em> immediately acquire the
//         * lock if it is available, whether or not other threads are
//         * currently waiting for the write lock.  This &quot;barging&quot;
//         * behavior can be useful in certain circumstances, even
//         * though it breaks fairness. If you want to honor the
//         * fairness setting for this lock, then use {@link
//         * #tryLock(long, TimeUnit) tryLock(0, TimeUnit.SECONDS) }
//         * which is almost equivalent (it also detects interruption).
//         *
//         * <p>If the current thread already holds this lock then the
//         * hold count is incremented by one and the method returns
//         * {@code true}.
//         *
//         * <p>If the lock is held by another thread then this method
//         * will return immediately with the value {@code false}.
//         *
//         * @return {@code true} if the lock was free and was acquired
//         * by the current thread, or the write lock was already held
//         * by the current thread; and {@code false} otherwise.
//         */
//        public boolean tryLock( ) {
//            return sync.tryWriteLock();
//        }
//
//        /**
//         * Acquires the write lock if it is not held by another thread
//         * within the given waiting time and the current thread has
//         * not been {@linkplain Thread#interrupt interrupted}.
//         *
//         * <p>Acquires the write lock if neither the read nor write lock
//         * are held by another thread
//         * and returns immediately with the value {@code true},
//         * setting the write lock hold count to one. If this lock has been
//         * set to use a fair ordering policy then an available lock
//         * <em>will not</em> be acquired if any other threads are
//         * waiting for the write lock. This is in contrast to the {@link
//         * #tryLock()} method. If you want a timed {@code tryLock}
//         * that does permit barging on a fair lock then combine the
//         * timed and un-timed forms together:
//         *
//         *  <pre> {@code
//         * if (lock.tryLock() ||
//         *     lock.tryLock(timeout, unit)) {
//         *   ...
//         * }}</pre>
//         *
//         * <p>If the current thread already holds this lock then the
//         * hold count is incremented by one and the method returns
//         * {@code true}.
//         *
//         * <p>If the lock is held by another thread then the current
//         * thread becomes disabled for thread scheduling purposes and
//         * lies dormant until one of three things happens:
//         *
//         * <ul>
//         *
//         * <li>The write lock is acquired by the current thread; or
//         *
//         * <li>Some other thread {@linkplain Thread#interrupt interrupts}
//         * the current thread; or
//         *
//         * <li>The specified waiting time elapses
//         *
//         * </ul>
//         *
//         * <p>If the write lock is acquired then the value {@code true} is
//         * returned and the write lock hold count is set to one.
//         *
//         * <p>If the current thread:
//         *
//         * <ul>
//         *
//         * <li>has its interrupted status set on entry to this method;
//         * or
//         *
//         * <li>is {@linkplain Thread#interrupt interrupted} while
//         * acquiring the write lock,
//         *
//         * </ul>
//         *
//         * then {@link InterruptedException} is thrown and the current
//         * thread's interrupted status is cleared.
//         *
//         * <p>If the specified waiting time elapses then the value
//         * {@code false} is returned.  If the time is less than or
//         * equal to zero, the method will not wait at all.
//         *
//         * <p>In this implementation, as this method is an explicit
//         * interruption point, preference is given to responding to
//         * the interrupt over normal or reentrant acquisition of the
//         * lock, and over reporting the elapse of the waiting time.
//         *
//         * @param timeout the time to wait for the write lock
//         * @param unit the time unit of the timeout argument
//         *
//         * @return {@code true} if the lock was free and was acquired
//         * by the current thread, or the write lock was already held by the
//         * current thread; and {@code false} if the waiting time
//         * elapsed before the lock could be acquired.
//         *
//         * @throws InterruptedException if the current thread is interrupted
//         * @throws NullPointerException if the time unit is null
//         */
//        public boolean tryLock(long timeout, TimeUnit unit)
//                throws InterruptedException {
//            return sync.tryAcquireNanos(1, unit.toNanos(timeout));
//        }
//
//        /**
//         * Attempts to release this lock.
//         *
//         * <p>If the current thread is the holder of this lock then
//         * the hold count is decremented. If the hold count is now
//         * zero then the lock is released.  If the current thread is
//         * not the holder of this lock then {@link
//         * IllegalMonitorStateException} is thrown.
//         *
//         * @throws IllegalMonitorStateException if the current thread does not
//         * hold this lock
//         */
//        public void unlock() {
//            sync.release(1);
//        }
//
//        /**
//         * Returns a {@link java.util.concurrent.locks.Condition} instance for use with this
//         * {@link Lock} instance.
//         * <p>The returned {@link java.util.concurrent.locks.Condition} instance supports the same
//         * usages as do the {@link Object} monitor methods ({@link
//         * Object#wait() wait}, {@link Object#notify notify}, and {@link
//         * Object#notifyAll notifyAll}) when used with the built-in
//         * monitor lock.
//         *
//         * <ul>
//         *
//         * <li>If this write lock is not held when any {@link
//         * java.util.concurrent.locks.Condition} method is called then an {@link
//         * IllegalMonitorStateException} is thrown.  (Read locks are
//         * held independently of write locks, so are not checked or
//         * affected. However it is essentially always an error to
//         * invoke a condition waiting method when the current thread
//         * has also acquired read locks, since other threads that
//         * could unblock it will not be able to acquire the write
//         * lock.)
//         *
//         * <li>When the condition {@linkplain java.util.concurrent.locks.Condition#await() waiting}
//         * methods are called the write lock is released and, before
//         * they return, the write lock is reacquired and the lock hold
//         * count restored to what it was when the method was called.
//         *
//         * <li>If a thread is {@linkplain Thread#interrupt interrupted} while
//         * waiting then the wait will terminate, an {@link
//         * InterruptedException} will be thrown, and the thread's
//         * interrupted status will be cleared.
//         *
//         * <li> Waiting threads are signalled in FIFO order.
//         *
//         * <li>The ordering of lock reacquisition for threads returning
//         * from waiting methods is the same as for threads initially
//         * acquiring the lock, which is in the default case not specified,
//         * but for <em>fair</em> locks favors those threads that have been
//         * waiting the longest.
//         *
//         * </ul>
//         *
//         * @return the Condition object
//         */
//        public java.util.concurrent.locks.Condition newCondition() {
//            return sync.newCondition();
//        }
//
//        /**
//         * Returns a string identifying this lock, as well as its lock
//         * state.  The state, in brackets includes either the String
//         * {@code "Unlocked"} or the String {@code "Locked by"}
//         * followed by the {@linkplain Thread#getName name} of the owning thread.
//         *
//         * @return a string identifying this lock, as well as its lock state
//         */
//        public String toString() {
//            Thread o = sync.getOwner();
//            return super.toString() + ((o == null) ?
//                                       "[Unlocked]" :
//                                       "[Locked by thread " + o.getName() + "]");
//        }
//
//        /**
//         * Queries if this write lock is held by the current thread.
//         * Identical in effect to {@link
//         * ReentrantReadWriteLock#isWriteLockedByCurrentThread}.
//         *
//         * @return {@code true} if the current thread holds this lock and
//         *         {@code false} otherwise
//         * @since 1.6
//         */
//        public boolean isHeldByCurrentThread() {
//            return sync.isHeldExclusively();
//        }
//
//        /**
//         * Queries the number of holds on this write lock by the current
//         * thread.  A thread has a hold on a lock for each lock action
//         * that is not matched by an unlock action.  Identical in effect
//         * to {@link ReentrantReadWriteLock#getWriteHoldCount}.
//         *
//         * @return the number of holds on this lock by the current thread,
//         *         or zero if this lock is not held by the current thread
//         * @since 1.6
//         */
//        public int getHoldCount() {
//            return sync.getWriteHoldCount();
//        }
//    }
//
//    // Instrumentation and status
//
//    /**
//     * Returns {@code true} if this lock has fairness set true.
//     *
//     * @return {@code true} if this lock has fairness set true
//     */
//    public final boolean isFair() {
//        return sync instanceof FairSync;
//    }
//
//    /**
//     * Returns the thread that currently owns the write lock, or
//     * {@code null} if not owned. When this method is called by a
//     * thread that is not the owner, the return value reflects a
//     * best-effort approximation of current lock status. For example,
//     * the owner may be momentarily {@code null} even if there are
//     * threads trying to acquire the lock but have not yet done so.
//     * This method is designed to facilitate construction of
//     * subclasses that provide more extensive lock monitoring
//     * facilities.
//     *
//     * @return the owner, or {@code null} if not owned
//     */
//    protected Thread getOwner() {
//        return sync.getOwner();
//    }
//
//    /**
//     * Queries the number of read locks held for this lock. This
//     * method is designed for use in monitoring system state, not for
//     * synchronization control.
//     * @return the number of read locks held
//     */
//    public int getReadLockCount() {
//        return sync.getReadLockCount();
//    }
//
//    /**
//     * Queries if the write lock is held by any thread. This method is
//     * designed for use in monitoring system state, not for
//     * synchronization control.
//     *
//     * @return {@code true} if any thread holds the write lock and
//     *         {@code false} otherwise
//     */
//    public boolean isWriteLocked() {
//        return sync.isWriteLocked();
//    }
//
//    /**
//     * Queries if the write lock is held by the current thread.
//     *
//     * @return {@code true} if the current thread holds the write lock and
//     *         {@code false} otherwise
//     */
//    public boolean isWriteLockedByCurrentThread() {
//        return sync.isHeldExclusively();
//    }
//
//    /**
//     * Queries the number of reentrant write holds on this lock by the
//     * current thread.  A writer thread has a hold on a lock for
//     * each lock action that is not matched by an unlock action.
//     *
//     * @return the number of holds on the write lock by the current thread,
//     *         or zero if the write lock is not held by the current thread
//     */
//    public int getWriteHoldCount() {
//        return sync.getWriteHoldCount();
//    }
//
//    /**
//     * Queries the number of reentrant read holds on this lock by the
//     * current thread.  A reader thread has a hold on a lock for
//     * each lock action that is not matched by an unlock action.
//     *
//     * @return the number of holds on the read lock by the current thread,
//     *         or zero if the read lock is not held by the current thread
//     * @since 1.6
//     */
//    public int getReadHoldCount() {
//        return sync.getReadHoldCount();
//    }
//
//    /**
//     * Returns a collection containing threads that may be waiting to
//     * acquire the write lock.  Because the actual set of threads may
//     * change dynamically while constructing this result, the returned
//     * collection is only a best-effort estimate.  The elements of the
//     * returned collection are in no particular order.  This method is
//     * designed to facilitate construction of subclasses that provide
//     * more extensive lock monitoring facilities.
//     *
//     * @return the collection of threads
//     */
//    protected Collection<Thread> getQueuedWriterThreads() {
//        return sync.getExclusiveQueuedThreads();
//    }
//
//    /**
//     * Returns a collection containing threads that may be waiting to
//     * acquire the read lock.  Because the actual set of threads may
//     * change dynamically while constructing this result, the returned
//     * collection is only a best-effort estimate.  The elements of the
//     * returned collection are in no particular order.  This method is
//     * designed to facilitate construction of subclasses that provide
//     * more extensive lock monitoring facilities.
//     *
//     * @return the collection of threads
//     */
//    protected Collection<Thread> getQueuedReaderThreads() {
//        return sync.getSharedQueuedThreads();
//    }
//
//    /**
//     * Queries whether any threads are waiting to acquire the read or
//     * write lock. Note that because cancellations may occur at any
//     * time, a {@code true} return does not guarantee that any other
//     * thread will ever acquire a lock.  This method is designed
//     * primarily for use in monitoring of the system state.
//     *
//     * @return {@code true} if there may be other threads waiting to
//     *         acquire the lock
//     */
//    public final boolean hasQueuedThreads() {
//        return sync.hasQueuedThreads();
//    }
//
//    /**
//     * Queries whether the given thread is waiting to acquire either
//     * the read or write lock. Note that because cancellations may
//     * occur at any time, a {@code true} return does not guarantee
//     * that this thread will ever acquire a lock.  This method is
//     * designed primarily for use in monitoring of the system state.
//     *
//     * @param thread the thread
//     * @return {@code true} if the given thread is queued waiting for this lock
//     * @throws NullPointerException if the thread is null
//     */
//    public final boolean hasQueuedThread(Thread thread) {
//        return sync.isQueued(thread);
//    }
//
//    /**
//     * Returns an estimate of the number of threads waiting to acquire
//     * either the read or write lock.  The value is only an estimate
//     * because the number of threads may change dynamically while this
//     * method traverses internal data structures.  This method is
//     * designed for use in monitoring of the system state, not for
//     * synchronization control.
//     *
//     * @return the estimated number of threads waiting for this lock
//     */
//    public final int getQueueLength() {
//        return sync.getQueueLength();
//    }
//
//    /**
//     * Returns a collection containing threads that may be waiting to
//     * acquire either the read or write lock.  Because the actual set
//     * of threads may change dynamically while constructing this
//     * result, the returned collection is only a best-effort estimate.
//     * The elements of the returned collection are in no particular
//     * order.  This method is designed to facilitate construction of
//     * subclasses that provide more extensive monitoring facilities.
//     *
//     * @return the collection of threads
//     */
//    protected Collection<Thread> getQueuedThreads() {
//        return sync.getQueuedThreads();
//    }
//
//    /**
//     * Queries whether any threads are waiting on the given condition
//     * associated with the write lock. Note that because timeouts and
//     * interrupts may occur at any time, a {@code true} return does
//     * not guarantee that a future {@code signal} will awaken any
//     * threads.  This method is designed primarily for use in
//     * monitoring of the system state.
//     *
//     * @param condition the condition
//     * @return {@code true} if there are any waiting threads
//     * @throws IllegalMonitorStateException if this lock is not held
//     * @throws IllegalArgumentException if the given condition is
//     *         not associated with this lock
//     * @throws NullPointerException if the condition is null
//     */
//    public boolean hasWaiters(java.util.concurrent.locks.Condition condition) {
//        if (condition == null)
//            throw new NullPointerException();
//        if (!(condition instanceof java.util.concurrent.locks.AbstractQueuedSynchronizer.ConditionObject))
//            throw new IllegalArgumentException("not owner");
//        return sync.hasWaiters((java.util.concurrent.locks.AbstractQueuedSynchronizer.ConditionObject)condition);
//    }
//
//    /**
//     * Returns an estimate of the number of threads waiting on the
//     * given condition associated with the write lock. Note that because
//     * timeouts and interrupts may occur at any time, the estimate
//     * serves only as an upper bound on the actual number of waiters.
//     * This method is designed for use in monitoring of the system
//     * state, not for synchronization control.
//     *
//     * @param condition the condition
//     * @return the estimated number of waiting threads
//     * @throws IllegalMonitorStateException if this lock is not held
//     * @throws IllegalArgumentException if the given condition is
//     *         not associated with this lock
//     * @throws NullPointerException if the condition is null
//     */
//    public int getWaitQueueLength(java.util.concurrent.locks.Condition condition) {
//        if (condition == null)
//            throw new NullPointerException();
//        if (!(condition instanceof java.util.concurrent.locks.AbstractQueuedSynchronizer.ConditionObject))
//            throw new IllegalArgumentException("not owner");
//        return sync.getWaitQueueLength((java.util.concurrent.locks.AbstractQueuedSynchronizer.ConditionObject)condition);
//    }
//
//    /**
//     * Returns a collection containing those threads that may be
//     * waiting on the given condition associated with the write lock.
//     * Because the actual set of threads may change dynamically while
//     * constructing this result, the returned collection is only a
//     * best-effort estimate. The elements of the returned collection
//     * are in no particular order.  This method is designed to
//     * facilitate construction of subclasses that provide more
//     * extensive condition monitoring facilities.
//     *
//     * @param condition the condition
//     * @return the collection of threads
//     * @throws IllegalMonitorStateException if this lock is not held
//     * @throws IllegalArgumentException if the given condition is
//     *         not associated with this lock
//     * @throws NullPointerException if the condition is null
//     */
//    protected Collection<Thread> getWaitingThreads(Condition condition) {
//        if (condition == null)
//            throw new NullPointerException();
//        if (!(condition instanceof java.util.concurrent.locks.AbstractQueuedSynchronizer.ConditionObject))
//            throw new IllegalArgumentException("not owner");
//        return sync.getWaitingThreads((AbstractQueuedSynchronizer.ConditionObject)condition);
//    }
//
//    /**
//     * Returns a string identifying this lock, as well as its lock state.
//     * The state, in brackets, includes the String {@code "Write locks ="}
//     * followed by the number of reentrantly held write locks, and the
//     * String {@code "Read locks ="} followed by the number of held
//     * read locks.
//     *
//     * @return a string identifying this lock, as well as its lock state
//     */
//    public String toString() {
//        int c = sync.getCount();
//        int w = Sync.exclusiveCount(c);
//        int r = Sync.sharedCount(c);
//
//        return super.toString() +
//            "[Write locks = " + w + ", Read locks = " + r + "]";
//    }
//
//    /**
//     * Returns the thread id for the given thread.  We must access
//     * this directly rather than via method Thread.getId() because
//     * getId() is not final, and has been known to be overridden in
//     * ways that do not preserve unique mappings.
//     */
//    static final long getThreadId(Thread thread) {
//        return UNSAFE.getLongVolatile(thread, TID_OFFSET);
//    }
//
//    // Unsafe mechanics
//    private static final sun.misc.Unsafe UNSAFE;
//    private static final long TID_OFFSET;
//    static {
//        try {
//            UNSAFE = sun.misc.Unsafe.getUnsafe();
//            Class<?> tk = Thread.class;
//            TID_OFFSET = UNSAFE.objectFieldOffset
//                (tk.getDeclaredField("tid"));
//        } catch (Exception e) {
//            throw new Error(e);
//        }
//    }
//
//}