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

import sun.misc.Unsafe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractOwnableSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

/**
 */
public abstract class AbstractQueuedSynchronizer
    extends AbstractOwnableSynchronizer
    implements java.io.Serializable {

    private static final long serialVersionUID = 7373984972572414691L;

    protected AbstractQueuedSynchronizer() { }
    
    /**state head tail  wiatStatus  next 会被同时访问。
     */
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long stateOffset;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long waitStatusOffset;
    private static final long nextOffset;
    
    static {
        try {
            stateOffset = unsafe.objectFieldOffset
                    (AbstractQueuedSynchronizer.class.getDeclaredField("state"));
            headOffset = unsafe.objectFieldOffset
                    (AbstractQueuedSynchronizer.class.getDeclaredField("head"));
            tailOffset = unsafe.objectFieldOffset
                    (AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
            waitStatusOffset = unsafe.objectFieldOffset
                    (Node.class.getDeclaredField("waitStatus"));
            nextOffset = unsafe.objectFieldOffset
                    (Node.class.getDeclaredField("next"));
            
        } catch (Exception ex) { throw new Error(ex); }
    }
    
    /*=======================================================属性====================================================*/
    
    // 头结点，不代表任何线程，是一个哑结点
    //head节点永远是一个哑结点（dummy node),
    // 它不代表任何线程（某些情况下可以看做是代表了当前持有锁的线程）
    //因此head所指向的Node的thread属性永远是null。
    private transient volatile Node head;
    
    // 尾节点，每一个请求锁的线程会加到队尾
    private transient volatile Node tail;

    /**同步状态:
     * 独占锁:非0表示被独占。
     * 可重入锁:个数代表锁被重入的个数。
     * 读写锁:
     *   高16位,读锁数量
     *   低16位,写锁数量
     */
    private volatile int state;


    /**
     * Atomically sets synchronization state to the given updated
     * value if the current state value equals the expected value.
     * This operation has memory semantics of a {@code volatile} read
     * and write.
     *
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful. False return indicates that the actual
     *         value was not equal to the expected value.
     */
    protected final boolean compareAndSetState(int expect, int update) {
        // See below for intrinsics setup to support this
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
    }

    // Queuing utilities

    /**
     * The number of nanoseconds for which it is faster to spin
     * rather than to use timed park. A rough estimate suffices
     * to improve responsiveness with very short timeouts.
     */
    static final long spinForTimeoutThreshold = 1000L;

    /**能执行到这个方法，说明当前线程获取锁已经失败了，我们已经把它包装成一个Node,准备把它扔到等待队列中去，但是在这一步又失败了。这个失败的原因可能是以下两种之一：
      * 1、等待队列现在是空的，没有线程在等待。
      * 2、其他线程在当前线程入队的过程中率先完成了入队，导致尾节点的值已经改变了，CAS操作失败。
     */
    private Node enq(final Node node) {
        //自旋方式将节点插入队列
        for (;;) {
            Node t = tail;
            // 如果是空队列, 首先进行初始化
            // 这里也可以看出, 队列不是在构造的时候初始化的, 而是延迟到需要用的时候再初始化, 以提升性能
            if (t == null) { // Must initialize
                // 注意，初始化时使用new Node()方法新建了一个dummy节点
                if (compareAndSetHead(new Node()))
                    // 这里仅仅是将尾节点指向dummy节点，并没有返回
                    tail = head;
            } else {
                // 到这里说明队列已经不是空的了, 这个时候再继续尝试将节点加到队尾
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }

    /**如果执行到此方法, 说明前面尝试获取锁的tryAcquire已经失败了, 既然获取锁已经失败了,
     * 就要将当前线程包装成Node，加到等待锁的队列中去, 因为是FIFO队列, 所以自然是直接加在队尾
     * 独占锁:Node.EXCLUSIVE 是个变量名字叫 EXCLUSIVE
     */
    private Node addWaiter(Node mode) {
        //独占锁:因为传入的mode值为Node.EXCLUSIVE，所以节点的nextWaiter属性被设为null
        Node node = new Node(Thread.currentThread(), mode);
        Node pred = tail;
        // 如果队列不为空, 则用CAS方式将当前节点设为尾节点
        if (pred != null) {
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred. next= node;
                return node;
            }
        }
        // 代码会执行到这里, 只有两种情况:
        //    1. 队列为空,去做队列的初始化。
        //    2. CAS失败
        // 注意, 这里是并发条件下, 所以什么都有可能发生, 尤其注意CAS失败后也会来到这里
        enq(node);
        return node;
    }

    /**
     * Sets head of queue to be node, thus dequeuing. Called only by
     * acquire methods.  Also nulls out unused fields for sake of GC
     * and to suppress unnecessary signals and traversals.
     *
     * @param node the node
     */
    private void setHead(Node node) {
        head = node;
        node.thread = null;
        node.prev = null;
    }

    /**值得注意的是, 在成功释放锁之后(tryRelease 返回 true之后),
     * 唤醒后继节点只是一个 "附加操作", 无论该操作结果怎样, 最后 release操作都会返回 true.
     */
    private void unparkSuccessor(Node node) {
        // 如果head节点的ws比0小, 则直接将它设为0
        int ws = node.waitStatus;
        if (ws < 0)
            compareAndSetWaitStatus(node, ws, 0);
    
        // 通常情况下, 要唤醒的节点就是自己的后继节点
        // 如果后继节点存在且也在等待锁, 那就直接唤醒它
        // 但是有可能存在 后继节点取消等待锁 的情况
        // 此时从尾节点开始向前找起, 直到找到距离head节点最近的ws<=0的节点
        Node s = node.next;
        if (s == null || s.waitStatus > 0) {
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev)
                if (t.waitStatus <= 0)
                    // 注意! 这里找到了之并有return, 而是继续向前找
                    s = t;
        }
        // 如果找到了还在等待锁的节点,则唤醒它
        if (s != null)
            //唤醒线程
            LockSupport.unpark(s.thread);
    }

    /**共享锁已经释放完成。唤醒后边的线程
     *  该方法有几处调用
     *  1、该方法有两处调用，一处在acquireShared方法的末尾，当线程成功获取到共享锁后，在一定条件下调用
     *  2、一处在releaseShared方法中，当线程释放共享锁的时候调用。
     *  该方法的调用者:
     *  1、在独占锁中，只有获取了锁的线程才能调用release释放锁，因此调用unparkSuccessor(h)唤醒后继节点的必然是持有锁的线程，
     *  该线程可看做是当前的头节点(虽然在setHead方法中已经将头节点的thread属性设为了null，但是这个头节点曾经代表的就是这个线程)
     *  2、在共享锁中，持有共享锁的线程可以有多个，这些线程都可以调用releaseShared方法释放锁；而这些线程想要获得共享锁，则它们必然曾经成为过头节点，或者就是现在的头节点。
     *  因此，如果是在releaseShared方法中调用的doReleaseShared，可能此时调用方法的线程已经不是头节点所代表的线程了，头节点可能已经被易主好几次了。
     *  调用该方法的目的是什么？
     *  调用该方法的目的是什么
     *  无论是在acquireShared中调用，还是在releaseShared方法中调用，该方法的目的都是在当前共享锁是可获取的状态时，唤醒head节点的下一个节点。这一点看上去和独占锁似乎一样，但是它们的一个重要的差别是——在共享锁中，当头节点发生变化时，
     *  是会回到循环中再立即唤醒head节点的下一个节点的。也就是说，在当前节点完成唤醒后继节点的任务之后将要退出时，
     *  如果发现被唤醒后继节点已经成为了新的头节点，则会立即触发唤醒head节点的下一个节点的操作，如此周而复始。
     *  退出该方法的条件是什么
     */
    private void doReleaseShared() {
        //自旋
        for (;;) {
            Node h = head;
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                if (ws == Node.SIGNAL) {
                    //如果当前ws值为Node.SIGNAL，则说明后继节点需要唤醒，这里采用CAS操作先将Node.SIGNAL状态改为0，
                    // 这是因为前面讲过，可能有大量的doReleaseShared方法在同时执行，
                    // 我们只需要其中一个执行unparkSuccessor(h)操作就行了，这里通过CAS操作保证了unparkSuccessor(h)只被执行一次。
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                        continue;            // loop to recheck cases
                    unparkSuccessor(h);
                }
                else if (ws == 0 &&
                         !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    continue;                // loop on failed CAS
            }
            if (h == head)                   // loop if head changed
                break;
        }
    }

    /**
     * Sets head of queue, and checks if successor may be waiting
     * in shared mode, if so propagating if either propagate > 0 or
     * PROPAGATE status was set.
     *
     * @param node the node
     * @param propagate the return value from a tryAcquireShared
     */
    private void setHeadAndPropagate(Node node, int propagate) {
        Node h = head;
        setHead(node);
        //在该方法内部我们不仅调用了setHead(node)，还在一定条件下调用了doReleaseShared()来唤醒后继的节点。
        //这是因为在共享锁模式下，锁可以被多个线程所共同持有，既然当前线程已经拿到共享锁了，
        //那么就可以直接通知后继节点来拿锁，而不必等待锁被释放的时候再通知。
        if (propagate > 0 || h == null || h.waitStatus < 0 ||
            (h = head) == null || h.waitStatus < 0) {
            Node s = node.next;
            if (s == null || s.isShared())
                doReleaseShared();
        }
    }

    // Utilities for various versions of acquire

    /**
     * Cancels an ongoing attempt to acquire.
     *
     * @param node the node
     */
    private void cancelAcquire(Node node) {
        if (node == null)
            return;

        node.thread = null;

        // Skip cancelled predecessors
        Node pred = node.prev;
        while (pred.waitStatus > 0)
            node.prev = pred = pred.prev;

        // predNext is the apparent node to unsplice. CASes below will
        // fail if not, in which case, we lost race vs another cancel
        // or signal, so no further action is necessary.
        Node predNext = pred.next;

        // Can use unconditional write instead of CAS here.
        // After this atomic step, other Nodes can skip past us.
        // Before, we are free of interference from other threads.
        node.waitStatus = Node.CANCELLED;

        // If we are the tail, remove ourselves.
        if (node == tail && compareAndSetTail(node, pred)) {
            compareAndSetNext(pred, predNext, null);
        } else {
            // If successor needs signal, try to set pred's next-link
            // so it will get one. Otherwise wake it up to propagate.
            int ws;
            if (pred != head &&
                ((ws = pred.waitStatus) == Node.SIGNAL ||
                 (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
                pred.thread != null) {
                Node next = node.next;
                if (next != null && next.waitStatus <= 0)
                    compareAndSetNext(pred, predNext, next);
            } else {
                unparkSuccessor(node);
            }

            node.next = node; // help GC
        }
    }

    /**1、如果为前驱节点的waitStatus值为 Node.SIGNAL 则直接返回 true
       2、如果为前驱节点的waitStatus值为 Node.CANCELLED (ws > 0), 则跳过那些节点, 重新寻找正常等待中的前驱节点，然后排在它后面，返回false
       3、其他情况, 将前驱节点的状态改为 Node.SIGNAL, 返回false
     */
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        // 获得前驱节点的ws
        int ws = pred.waitStatus;
        if (ws == Node.SIGNAL)
        // 前驱节点的状态已经是SIGNAL了，说明闹钟已经设了，可以直接睡了
        return true;
        if (ws > 0) {
            // 当前节点的 ws > 0, 则为 Node.CANCELLED 说明前驱节点已经取消了等待锁(由于超时或者中断等原因)
            // 既然前驱节点不等了, 那就继续往前找, 直到找到一个还在等待锁的节点
            // 然后我们跨过这些不等待锁的节点, 直接排在等待锁的节点的后面 (是不是很开心!!!)
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            // 前驱节点的状态既不是SIGNAL，也不是CANCELLED
            // 用CAS设置前驱节点的ws为 Node.SIGNAL，给自己定一个闹钟
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }

    /**
     * Convenience method to interrupt current thread.
     */
    static void selfInterrupt() {
        Thread.currentThread().interrupt();
    }

    /**线程的挂起。
     */
    private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        return Thread.interrupted();
    }


    /**(1) 能执行到该方法, 说明addWaiter 方法已经成功将包装了当前Thread的节点添加到了等待队列的队尾
     * (2) 该方法中将再次尝试去获取锁
     *(3) 在再次尝试获取锁失败后, 判断是否需要把当前线程挂起
     */
    final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                // 在当前节点的前驱就是HEAD节点时, 再次尝试获取锁
                if (p == head && tryAcquire(arg)) {
                    //sethead不会产生竞争
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return interrupted;
                }
                //在获取锁失败后, 判断是否需要把当前线程挂起
                if (shouldParkAfterFailedAcquire(p, node) &&
                    // 线程被挂起，停在这里不再往下执行了
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            //跳出for循环并不一定是拿到了锁，也可能是tryAcquire(arg)抛出了异常，因为是子类重写tryAcquire(arg)，我们并不知道子类如何去实现他，一旦子类获取锁抛出异常，就必须要走finally的cancelAcquire(node);方法，将本身节点从同步队列中移除。
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * Acquires in exclusive interruptible mode.
     * @param arg the acquire argument
     */
    private void doAcquireInterruptibly(int arg)
        throws InterruptedException {
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * Acquires in exclusive timed mode.
     *
     * @param arg the acquire argument
     * @param nanosTimeout max wait time
     * @return {@code true} if acquired
     */
    private boolean doAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return true;
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)
                    return false;
                if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**共享节点添加到队列。
     */
    private void doAcquireShared(int arg) {
        //添加共享节点到同步队列。
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    //根据返回值判断
//                    如果该值小于0，则代表当前线程获取共享锁失败
//                    如果该值大于0，则代表当前线程获取共享锁成功，并且接下来其他线程尝试获取共享锁的行为很可能成功
//                    如果该值等于0，则代表当前线程获取共享锁成功，但是接下来其他线程尝试获取共享锁的行为会失败
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        if (interrupted)
                            selfInterrupt();
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * Acquires in shared interruptible mode.
     * @param arg the acquire argument
     */
    private void doAcquireSharedInterruptibly(int arg)
        throws InterruptedException {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * Acquires in shared timed mode.
     *
     * @param arg the acquire argument
     * @param nanosTimeout max wait time
     * @return {@code true} if acquired
     */
    private boolean doAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return true;
                    }
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)
                    return false;
                if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**排他锁的获取,子类实现.
     */
    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
    }

    /**排它锁,释放
     */
    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }

    /**共享锁获取,子类实现获取逻辑。
     */
    protected int tryAcquireShared(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to set the state to reflect a release in shared mode.
     *
     * <p>This method is always invoked by the thread performing release.
     *
     * <p>The default implementation throws
     * {@link UnsupportedOperationException}.
     *
     * @param arg the release argument. This value is always the one
     *        passed to a release method, or the current state value upon
     *        entry to a condition wait.  The value is otherwise
     *        uninterpreted and can represent anything you like.
     * @return {@code true} if this release of shared mode may permit a
     *         waiting acquire (shared or exclusive) to succeed; and
     *         {@code false} otherwise
     * @throws IllegalMonitorStateException if releasing would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     * @throws UnsupportedOperationException if shared mode is not supported
     */
    protected boolean tryReleaseShared(int arg) {
        throw new UnsupportedOperationException();
    }

    /**判断是排他和共享锁,子类实现
     */
    protected boolean isHeldExclusively() {
        throw new UnsupportedOperationException();
    }

    /**排他锁获取逻辑
     *
     */
    public final void acquire(int arg) {
        //1、tryAcquire由子类去实现,决定非公平还是公平
        //  非公平:不会判断是否队列中有等待的线程
        //  公平:会判断其他线程是否占有锁(state)并且队列中没有等待的线程。
        //2、放置到阻塞队列。
        if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg)){
            //该方法由AQS实现, 用于中断当前线程。由于在整个抢锁过程中，我们都是不响应中断的。
            // 那如果在抢锁的过程中发生了中断怎么办呢，总不能假装没看见呀。AQS的做法简单的记录有没有有发生过中断，
            // 如果返回的时候发现曾经发生过中断，则在退出acquire方法之前，就调用selfInterrupt自我中断一下，
            // 就好像将这个发生在抢锁过程中的中断“推迟”到抢锁结束以后再发生一样。
            selfInterrupt();
        }
    }

    /**
     * Acquires in exclusive mode, aborting if interrupted.
     * Implemented by first checking interrupt status, then invoking
     * at least once {@link #tryAcquire}, returning on
     * success.  Otherwise the thread is queued, possibly repeatedly
     * blocking and unblocking, invoking {@link #tryAcquire}
     * until success or the thread is interrupted.  This method can be
     * used to implement method {@link Lock#lockInterruptibly}.
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquire} but is otherwise uninterpreted and
     *        can represent anything you like.
     * @throws InterruptedException if the current thread is interrupted
     */
    public final void acquireInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (!tryAcquire(arg))
            doAcquireInterruptibly(arg);
    }

    /**
     * Attempts to acquire in exclusive mode, aborting if interrupted,
     * and failing if the given timeout elapses.  Implemented by first
     * checking interrupt status, then invoking at least once {@link
     * #tryAcquire}, returning on success.  Otherwise, the thread is
     * queued, possibly repeatedly blocking and unblocking, invoking
     * {@link #tryAcquire} until success or the thread is interrupted
     * or the timeout elapses.  This method can be used to implement
     * method {@link Lock#tryLock(long, TimeUnit)}.
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquire} but is otherwise uninterpreted and
     *        can represent anything you like.
     * @param nanosTimeout the maximum number of nanoseconds to wait
     * @return {@code true} if acquired; {@code false} if timed out
     * @throws InterruptedException if the current thread is interrupted
     */
    public final boolean tryAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquire(arg) ||
            doAcquireNanos(arg, nanosTimeout);
    }

    /**排他模式下,释放锁。
     */
    public final boolean release(int arg) {
        //子类实现去释放锁逻辑
        //true:锁已经释放干净了
        //false:可重入锁,没有释放干净
        if (tryRelease(arg)) {
            //排他锁已经释放完了。
            Node h = head;
            if (h != null && h.waitStatus != 0)
                //唤醒后继节点的线程
                unparkSuccessor(h);
            return true;
        }
        return false;
    }

    /**共享锁获取
     * 获取思路:线程在获取的时候,不是只有一个线程独占的逻辑。
     */
    public final void acquireShared(int arg) {
        //如果该值小于0，则代表当前线程获取共享锁失败
        //如果该值大于0，则代表当前线程获取共享锁成功，并且接下来其他线程尝试获取共享锁的行为很可能成功
        //如果该值等于0，则代表当前线程获取共享锁成功，但是接下来其他线程尝试获取共享锁的行为会失败
        if (tryAcquireShared(arg) < 0)
            doAcquireShared(arg);
    }

    /**
     * Acquires in shared mode, aborting if interrupted.  Implemented
     * by first checking interrupt status, then invoking at least once
     * {@link #tryAcquireShared}, returning on success.  Otherwise the
     * thread is queued, possibly repeatedly blocking and unblocking,
     * invoking {@link #tryAcquireShared} until success or the thread
     * is interrupted.
     * @param arg the acquire argument.
     * This value is conveyed to {@link #tryAcquireShared} but is
     * otherwise uninterpreted and can represent anything
     * you like.
     * @throws InterruptedException if the current thread is interrupted
     */
    public final void acquireSharedInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (tryAcquireShared(arg) < 0)
            doAcquireSharedInterruptibly(arg);
    }

    /**
     * Attempts to acquire in shared mode, aborting if interrupted, and
     * failing if the given timeout elapses.  Implemented by first
     * checking interrupt status, then invoking at least once {@link
     * #tryAcquireShared}, returning on success.  Otherwise, the
     * thread is queued, possibly repeatedly blocking and unblocking,
     * invoking {@link #tryAcquireShared} until success or the thread
     * is interrupted or the timeout elapses.
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquireShared} but is otherwise uninterpreted
     *        and can represent anything you like.
     * @param nanosTimeout the maximum number of nanoseconds to wait
     * @return {@code true} if acquired; {@code false} if timed out
     * @throws InterruptedException if the current thread is interrupted
     */
    public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquireShared(arg) >= 0 ||
            doAcquireSharedNanos(arg, nanosTimeout);
    }

    /**共享锁的释放,取消对线程的阻塞。
     *
     */
    public final boolean releaseShared(int arg) {
        if (tryReleaseShared(arg)) {
            doReleaseShared();
            return true;
        }
        return false;
    }

    // Queue inspection methods
    /*==================================================同步队列方法===========================================================*/

    /**队列是否有节点
     */
    public final boolean hasQueuedThreads() {
        return head != tail;
    }

    /**
     * Queries whether any threads have ever contended to acquire this
     * synchronizer; that is if an acquire method has ever blocked.
     *
     * <p>In this implementation, this operation returns in
     * constant time.
     *
     * @return {@code true} if there has ever been contention
     */
    public final boolean hasContended() {
        return head != null;
    }

    /**
     * Returns the first (longest-waiting) thread in the queue, or
     * {@code null} if no threads are currently queued.
     *
     * <p>In this implementation, this operation normally returns in
     * constant time, but may iterate upon contention if other threads are
     * concurrently modifying the queue.
     *
     * @return the first (longest-waiting) thread in the queue, or
     *         {@code null} if no threads are currently queued
     */
    public final Thread getFirstQueuedThread() {
        // handle only fast path, else relay
        return (head == tail) ? null : fullGetFirstQueuedThread();
    }

    /**
     * Version of getFirstQueuedThread called when fastpath fails
     */
    private Thread fullGetFirstQueuedThread() {
        /*
         * The first node is normally head.next. Try to get its
         * thread field, ensuring consistent reads: If thread
         * field is nulled out or s.prev is no longer head, then
         * some other thread(s) concurrently performed setHead in
         * between some of our reads. We try this twice before
         * resorting to traversal.
         */
        Node h, s;
        Thread st;
        if (((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null) ||
            ((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null))
            return st;

        /*
         * Head's next field might not have been set yet, or may have
         * been unset after setHead. So we must check to see if tail
         * is actually first node. If not, we continue on, safely
         * traversing from tail back to head to find first,
         * guaranteeing termination.
         */

        Node t = tail;
        Thread firstThread = null;
        while (t != null && t != head) {
            Thread tt = t.thread;
            if (tt != null)
                firstThread = tt;
            t = t.prev;
        }
        return firstThread;
    }

    /**线程是否在同步队列中。
     */
    public final boolean isQueued(Thread thread) {
        if (thread == null)
            throw new NullPointerException();
        for (Node p = tail; p != null; p = p.prev)
            if (p.thread == thread)
                return true;
        return false;
    }

    /**
     * Returns {@code true} if the apparent first queued thread, if one
     * exists, is waiting in exclusive mode.  If this method returns
     * {@code true}, and the current thread is attempting to acquire in
     * shared mode (that is, this method is invoked from {@link
     * #tryAcquireShared}) then it is guaranteed that the current thread
     * is not the first queued thread.  Used only as a heuristic in
     * ReentrantReadWriteLock.
     */
    final boolean apparentlyFirstQueuedIsExclusive() {
        Node h, s;
        return (h = head) != null &&
            (s = h.next)  != null &&
            !s.isShared()         &&
            s.thread != null;
    }

    /**
      * 为了阅读方便, hasQueuedPredecessors源码就直接贴在这里了, 这个方法的本质实际上是
     * 检测自己是不是head节点的后继节点，即处在阻塞队列第一位的节点
     */
    public final boolean hasQueuedPredecessors() {
        Node t = tail; // Read fields in reverse initialization order
        Node h = head;
        Node s;
        //h != t 队列不为空
        //(s = h.next) == null || s.thread != Thread.currentThread():头结点后继节点存在,并且不是
        return h != t &&
            ((s = h.next) == null || s.thread != Thread.currentThread());
    }


    // Instrumentation and monitoring methods

    /**同步队列的长度。
     */
    public final int getQueueLength() {
        int n = 0;
        for (Node p = tail; p != null; p = p.prev) {
            if (p.thread != null)
                ++n;
        }
        return n;
    }

    /**获取同步队列中所有的线程。
     */
    public final Collection<Thread> getQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            Thread t = p.thread;
            if (t != null)
                list.add(t);
        }
        return list;
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire in exclusive mode. This has the same properties
     * as {@link #getQueuedThreads} except that it only returns
     * those threads waiting due to an exclusive acquire.
     *
     * @return the collection of threads
     */
    public final Collection<Thread> getExclusiveQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (!p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire in shared mode. This has the same properties
     * as {@link #getQueuedThreads} except that it only returns
     * those threads waiting due to a shared acquire.
     *
     * @return the collection of threads
     */
    public final Collection<Thread> getSharedQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    /**
     * Returns a string identifying this synchronizer, as well as its state.
     * The state, in brackets, includes the String {@code "State ="}
     * followed by the current value of {@link #getState}, and either
     * {@code "nonempty"} or {@code "empty"} depending on whether the
     * queue is empty.
     *
     * @return a string identifying this synchronizer, as well as its state
     */
    public String toString() {
        int s = getState();
        String q  = hasQueuedThreads() ? "non" : "";
        return super.toString() +
            "[State = " + s + ", " + q + "empty queue]";
    }


    // Internal support methods for Conditions

    /**判断是否在通读队列上。
     */
    final boolean isOnSyncQueue(Node node) {
        if (node.waitStatus == Node.CONDITION || node.prev == null)
            return false;
        if (node.next != null) // If has successor, it must be on queue
            return true;
        return findNodeFromTail(node);
    }

    /**
     * Returns true if node is on sync queue by searching backwards from tail.
     * Called only when needed by isOnSyncQueue.
     * @return true if present
     */
    private boolean findNodeFromTail(Node node) {
        Node t = tail;
        for (;;) {
            if (t == node)
                return true;
            if (t == null)
                return false;
            t = t.prev;
        }
    }

    /**条件队列->同步队列。
     */
    final boolean transferForSignal(Node node) {
        // 如果该节点在调用signal方法前已经被取消了，则直接跳过这个节点
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
            return false;
        // 如果该节点在条件队列中正常等待，则利用enq方法将该节点添加至sync queue队列的尾部
        Node p = enq(node);
        int ws = p.waitStatus;
        //设置前一个节点的状态为SIGNALSIGNAL
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
            //唤起线程。
            LockSupport.unpark(node.thread);
        return true;
    }

  
    final boolean transferAfterCancelledWait(Node node) {
        //还在提提交队列,应该是线程在阻塞的时候被中断抛出异常。
        if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
            enq(node);
            return true;
        }
        // 由于中断发生前，线程已经被signal了，则这里只需要等待线程成功进入sync queue即可
        while (!isOnSyncQueue(node))
            Thread.yield();
        return false;
    }

    /**释放当前线程所占用的锁
     */
    final int fullyRelease(Node node) {
        boolean failed = true;
        try {
            int savedState = getState();
            if (release(savedState)) {
                failed = false;
                return savedState;
            } else {
                throw new IllegalMonitorStateException();
            }
        } finally {
            //调用await的节点可能不是当前持有锁的线程。
            if (failed)
                node.waitStatus = Node.CANCELLED;
        }
    }

    // Instrumentation methods for conditions

    /**
     * Queries whether the given ConditionObject
     * uses this synchronizer as its lock.
     *
     * @param condition the condition
     * @return {@code true} if owned
     * @throws NullPointerException if the condition is null
     */
    public final boolean owns(ConditionObject condition) {
        return condition.isOwnedBy(this);
    }

    /**条件队列是否有节点。
     */
    public final boolean hasWaiters(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.hasWaiters();
    }

    /**条件队列长度

     */
    public final int getWaitQueueLength(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitQueueLength();
    }

    /**
     * Returns a collection containing those threads that may be
     * waiting on the given condition associated with this
     * synchronizer.  Because the actual set of threads may change
     * dynamically while constructing this result, the returned
     * collection is only a best-effort estimate. The elements of the
     * returned collection are in no particular order.
     *
     * @param condition the condition
     * @return the collection of threads
     * @throws IllegalMonitorStateException if exclusive synchronization
     *         is not held
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this synchronizer
     * @throws NullPointerException if the condition is null
     */
    public final Collection<Thread> getWaitingThreads(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitingThreads();
    }

    /** 等待队列。
     *
     */
    public class ConditionObject implements Condition, java.io.Serializable {
        private static final long serialVersionUID = 1173984872572414699L;
        //队头
        private transient Node firstWaiter;
        //队尾
        private transient Node lastWaiter;

        /**
         * Creates a new {@code ConditionObject} instance.
         */
        public ConditionObject() { }


        /**当前线程封装成Node扔进条件队列中
         */
        private Node addConditionWaiter() {
            Node t = lastWaiter;
            // 如果尾节点被cancel了，则先遍历整个链表，清除所有被cancel的节点
            if (t != null && t.waitStatus != Node.CONDITION) {
                unlinkCancelledWaiters();
                t = lastWaiter;
            }
            // 将当前线程包装成Node扔进条件队列
            Node node = new Node(Thread.currentThread(), Node.CONDITION);
            if (t == null)
                firstWaiter = node;
            else
                t.nextWaiter = node;
            lastWaiter = node;
            return node;
        }

        /**o-while循环，目的是遍历整个条件队列，找到第一个没有被cancelled的节点，
         * 并将它添加到条件队列的末尾。如果条件队列里面已经没有节点了，则将条件队列清空
         */
        private void doSignal(Node first) {
            do {
                // 将firstWaiter指向条件队列队头的下一个节点
                if ( (firstWaiter = first.nextWaiter) == null)
                    lastWaiter = null;
                first.nextWaiter = null;
                // 将条件队列原来的队头从条件队列中断开，则此时该节点成为一个孤立的节点
            } while (!transferForSignal(first) &&
                     (first = firstWaiter) != null);
        }

        /**
         * Removes and transfers all nodes.
         * @param first (non-null) the first node on condition queue
         */
        private void doSignalAll(Node first) {
            //将整个条件队列清空
            lastWaiter = firstWaiter = null;
            do {
                //然后通过一个do-while循环，将原先的条件队列里面的节点一个一个拿出来(令nextWaiter = null)
                Node next = first.nextWaiter;
                first.nextWaiter = null;
                //通过transferForSignal方法一个一个添加到sync queue的末尾：
                transferForSignal(first);
                first = next;
            } while (first != null);
        }

        /**
         * 方法将从头节点开始遍历整个队列，剔除其中waitStatus不为Node.CONDTION的节点，这里使用了两个指针firstWaiter
         * 和trail来分别记录第一个和最后一个waitStatus不为Node.CONDTION的节点
         */
        private void unlinkCancelledWaiters() {
            Node t = firstWaiter;
            Node trail = null;
            while (t != null) {
                Node next = t.nextWaiter;
                if (t.waitStatus != Node.CONDITION) {
                    t.nextWaiter = null;
                    if (trail == null)
                        firstWaiter = next;
                    else
                        trail.nextWaiter = next;
                    if (next == null)
                        lastWaiter = trail;
                }
                else
                    trail = t;
                t = next;
            }
        }

        // public methods

        /**等待时间最长的节点(头结点),
         * 公平锁:放入阻塞队列。
         * 非公平锁:先抢占,然后放到阻塞队列。
         */
        public final void signal() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignal(first);
        }

        /**
         * Moves all threads from the wait queue for this condition to
         * the wait queue for the owning lock.
         *
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        public final void signalAll() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignalAll(first);
        }

        /**
         * Implements uninterruptible condition wait.
         * <ol>
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled.
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         * </ol>
         */
        public final void awaitUninterruptibly() {
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean interrupted = false;
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if (Thread.interrupted())
                    interrupted = true;
            }
            if (acquireQueued(node, savedState) || interrupted)
                selfInterrupt();
        }

        /*
         * For interruptible waits, we need to track whether to throw
         * InterruptedException, if interrupted while blocked on
         * condition, versus reinterrupt current thread, if
         * interrupted while blocked waiting to re-acquire.
         */

        /** Mode meaning to reinterrupt on exit from wait */
        private static final int REINTERRUPT =  1;
        /** Mode meaning to throw InterruptedException on exit from wait */
        private static final int THROW_IE    = -1;

        /**
         * Checks for interrupt, returning THROW_IE if interrupted
         * before signalled, REINTERRUPT if after signalled, or
         * 0 if not interrupted.
         */
        private int checkInterruptWhileWaiting(Node node) {
            //0 ： 代表整个过程中一直没有中断发生。
            //THROW_IE ： 表示退出await()方法时需要抛出InterruptedException，这种模式对应于中断发生在signal之前
            //REINTERRUPT ： 表示退出await()方法时只需要再自我中断以下，这种模式对应于中断发生在signal之后，即中断来的太晚了。
            return Thread.interrupted() ?
                (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) :
                0;
        }

        /**
         * Throws InterruptedException, reinterrupts current thread, or
         * does nothing, depending on mode.
         */
        private void reportInterruptAfterWait(int interruptMode)
            throws InterruptedException {
            if (interruptMode == THROW_IE)
                throw new InterruptedException();
            else if (interruptMode == REINTERRUPT)
                selfInterrupt();
        }
    
        /**进入等待队列。
         */
        public final void await() throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
            // 将当前线程封装成Node添加到条件队列
            Node node = addConditionWaiter();
            // 释放当前线程所占用的锁，保存当前的锁状态
            int savedState = fullyRelease(node);
            int interruptMode = 0;
            // 如果当前队列不在同步队列中，说明刚刚被await, 还没有人调用signal方法，则直接将当前线程挂起
            //isOnSyncQueue:被release唤醒,会放入同步队列中。
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                // 能执行到这里说明要么是signal方法被调用了，要么是线程被中断了
                // 所以检查下线程被唤醒的原因，如果是因为中断被唤醒，则跳出while循环
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                   // 0 ： 代表整个过程中一直没有中断发生。
                  //THROW_IE ： 表示退出await()方法时需要抛出InterruptedException，这种模式对应于中断发生在signal之前
                  //REINTERRUPT ： 表示退出await()方法时只需要再自我中断以下，这种模式对应于中断发生在signal之后，即中断来的太晚了。
                    break;
            }
            //acquireQueued(node, savedState)进行争锁，注意，这里传入的需要获取锁的重入数量是savedState，即之前释放了多少，这里就需要再次获取多少：
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null) // clean up if cancelled
                //移除节点。
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                //汇报中断状态
                reportInterruptAfterWait(interruptMode);
        }

        /**
         * Implements timed condition wait.
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled, interrupted, or timed out.
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * </ol>
         */
        public final long awaitNanos(long nanosTimeout)
                throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    transferAfterCancelledWait(node);
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return deadline - System.nanoTime();
        }

        /**
         * Implements absolute timed condition wait.
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled, interrupted, or timed out.
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * <li> If timed out while blocked in step 4, return false, else true.
         * </ol>
         */
        public final boolean awaitUntil(Date deadline)
                throws InterruptedException {
            long abstime = deadline.getTime();
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (System.currentTimeMillis() > abstime) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                LockSupport.parkUntil(this, abstime);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        /**
         * Implements timed condition wait.
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled, interrupted, or timed out.
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * <li> If timed out while blocked in step 4, return false, else true.
         * </ol>
         */
        public final boolean await(long time, TimeUnit unit)
                throws InterruptedException {
            long nanosTimeout = unit.toNanos(time);
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout;
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        //  support for instrumentation

        /**
         * Returns true if this condition was created by the given
         * synchronization object.
         *
         * @return {@code true} if owned
         */
        final boolean isOwnedBy(AbstractQueuedSynchronizer sync) {
            return sync == AbstractQueuedSynchronizer.this;
        }

        /**
         * Queries whether any threads are waiting on this condition.
         * Implements {@link AbstractQueuedSynchronizer#hasWaiters(ConditionObject)}.
         *
         * @return {@code true} if there are any waiting threads
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        protected final boolean hasWaiters() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    return true;
            }
            return false;
        }

        /**
         * Returns an estimate of the number of threads waiting on
         * this condition.
         * Implements {@link AbstractQueuedSynchronizer#getWaitQueueLength(ConditionObject)}.
         *
         * @return the estimated number of waiting threads
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        protected final int getWaitQueueLength() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            int n = 0;
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    ++n;
            }
            return n;
        }

        /**
         * Returns a collection containing those threads that may be
         * waiting on this Condition.
         * Implements {@link AbstractQueuedSynchronizer#getWaitingThreads(ConditionObject)}.
         *
         * @return the collection of threads
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        protected final Collection<Thread> getWaitingThreads() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            ArrayList<Thread> list = new ArrayList<Thread>();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION) {
                    Thread t = w.thread;
                    if (t != null)
                        list.add(t);
                }
            }
            return list;
        }
    }



    /**
     * CAS head field. Used only by enq.
     */
    private final boolean compareAndSetHead(Node update) {
        return unsafe.compareAndSwapObject(this, headOffset, null, update);
    }

    /**
     * CAS tail field. Used only by enq.
     */
    private final boolean compareAndSetTail(Node expect, Node update) {
        return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
    }

    /**
     * CAS waitStatus field of a node.
     */
    private static final boolean compareAndSetWaitStatus(Node node,
                                                         int expect,
                                                         int update) {
        return unsafe.compareAndSwapInt(node, waitStatusOffset,
                                        expect, update);
    }

    /**
     * CAS next field of a node.
     */
    private static final boolean compareAndSetNext(Node node,
                                                   Node expect,
                                                   Node update) {
        return unsafe.compareAndSwapObject(node, nextOffset, expect, update);
    }

    /*==========================================================================================*/

    /**获取同步状态
     */
    protected final int getState() {
        return state;
    }

    /**设置同步状态
     */
    protected final void setState(int newState) {
        state = newState;
    }
    
    
    /*====================================================类===========================================================*/
    
    /**链表节点信息
     */
    static final class Node {
        //标识改节点是共享节点。
        static final Node SHARED = new Node();
        //独占
        static final Node EXCLUSIVE = null;
        
        // 线程所处的等待锁的状态，初始化时，该值为0
        static final int CANCELLED =  1;  //表示Node所代表的当前线程已经取消了排队，即放弃获取锁了。
        //SIGNAL这个状态就有点意思了，它不是表征当前节点的状态，而是当前节点的下一个节点的状态。
        //当一个节点的waitStatus被置为SIGNAL，就说明它的下一个节点（即它的后继节点）已经被挂起了（或者马上就要被挂起了），
        // 因此在当前节点释放了锁或者放弃获取锁时，如果它的waitStatus属性为SIGNAL，它还要完成一个额外的操作——唤醒它的后继节点。
        //SIGNAL这个状态的设置常常不是节点自己给自己设的，而是后继节点设置的，这里给大家打个比方：
        static final int SIGNAL    = -1;
        //条件队列中的节点状态为CONDITION,只要不是CONDITION,那么久需要移出条件队列中。
        static final int CONDITION = -2;
        static final int PROPAGATE = -3;
        
        //当前Node所代表的线程的等待锁的状态
        //独占锁模式下，我们只需要关注CANCELLED SIGNAL
        volatile int waitStatus;
        
        //节点所代表的线程
        volatile Thread thread;
        
        // 双向链表，每个节点需要保存自己的前驱节点和后继节点的引用
        volatile Node prev;
        volatile Node next;
        
        // 该属性用于条件队列或者共享锁
        //独占锁模式下永远为null，仅仅起到一个标记作用
        //共享模式:指向SHARED
        //条件队列:向后的指正。
        Node nextWaiter;
        
        /**
         * Returns true if node is waiting in shared mode.
         */
        final boolean isShared() {
            return nextWaiter == SHARED;
        }
        
        /**返回当前节点的前节点。
         */
        final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null)
                throw new NullPointerException();
            else
                return p;
        }
        
        Node() {    // Used to establish initial head or SHARED marker
        }
        
        Node(Thread thread, Node mode) {     // Used by addWaiter
            this.nextWaiter = mode;
            this.thread = thread;
        }
        
        Node(Thread thread, int waitStatus) { // Used by Condition
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }
}
