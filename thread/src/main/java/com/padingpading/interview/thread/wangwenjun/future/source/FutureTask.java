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

package com.padingpading.interview.thread.wangwenjun.future.source;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

/**
 * Future:异步计算的结果
 * Runnable:线程任务执行逻辑。
 * RunnableFuture:继承future和runable
 * FutureTask:具体执行的任务。
 */
public class FutureTask<V> implements RunnableFuture<V> {

    //初始状态
    private static final int NEW          = 0;
    //中间状态，，任务的中间状态是一个瞬态，它非常的短暂。而且任务的中间态并不代表任务正在执行，而是任务已经执行完了，正在设置最终的返回结果，
    //正在设置任务结果
    private static final int COMPLETING   = 1;
    //正在中断运行任务的线程
    private static final int INTERRUPTING = 5;
    //终止状态
    //任务正常执行完毕
    private static final int NORMAL       = 2;
    //任务执行过程中发生异常
    private static final int EXCEPTIONAL  = 3;
    //任务被取消
    private static final int CANCELLED    = 4;
    //任务被中断
    private static final int INTERRUPTED  = 6;
    
    /*
     *
     * Possible state transitions:
     * NEW -> COMPLETING -> NORMAL
     * NEW -> COMPLETING -> EXCEPTIONAL
     * NEW -> CANCELLED
     * NEW -> INTERRUPTING -> INTERRUPTED
     */
    private volatile int state;
    
    //执行的任务
    private Callable<V> callable;
    //执行后的任务结果。
    private Object outcome;
    //任务的执行线程。
    private volatile Thread runner;
    
    //等待返回结果的线程栈:指向栈顶的指针。
    private volatile WaitNode waiters;

  
    @SuppressWarnings("unchecked")
    private V report(int s) throws ExecutionException {
        Object x = outcome;
        if (s == NORMAL)
            return (V)x;
        if (s >= CANCELLED)
            throw new CancellationException();
        throw new ExecutionException((Throwable)x);
    }
    
    /*==========================================================构造函数=================================================*/
    /**执行任务,但是结果是需要的。
     */
    public FutureTask(Callable<V> callable) {
        if (callable == null)
            throw new NullPointerException();
        this.callable = callable;
        //设置任务的状态
        this.state = NEW;
    }

    /**执行任务,但是结果是不需要的。
     */
    public FutureTask(Runnable runnable, V result) {
        //将runAble转换为callAble,返回RunnableAdapter,是callAble的适配器
        this.callable = Executors.callable(runnable, result);
        //设置任务的状态
        this.state = NEW;
    }
    /*==========================================================主要方法=================================================*/
    
    /**jvm调用的执行任务方法。
     *
     */
    public void run() {
        //校验任务状态
        //设置runner为当前线程。
        if (state != NEW ||
                !UNSAFE.compareAndSwapObject(this, runnerOffset,
                        null, Thread.currentThread()))
            return;
        try {
            //获取任务
            Callable<V> c = callable;
            if (c != null && state == NEW) {
                V result;
                boolean ran;
                try {
                    //调用方法执行
                    result = c.call();
                    ran = true;
                } catch (Throwable ex) {
                    //处理任务执行过程中的异常,但并不包括对该线程的中断。
                    result = null;
                    ran = false;
                    setException(ex);
                }
                //执行成功,设置返回值。
                if (ran)
                    //原子性设置返回值，
                    set(result);
            }
        } finally {
            //设置执行线程
            runner = null;
            int s = state;
            //可能其他线程已经修改任务的状态
            if (s >= INTERRUPTING)
                //检查是否有遗漏的中断，如果有，等待中断状态完成。
                handlePossibleCancellationInterrupt(s);
        }
    }
    
    /**设置返回值
     */
    protected void set(V v) {
        //设置任务状态为COMPLETING,表示正在设置任务的结果。注:state状态必须是new,否则无法设置value
        //任务可能会被取消。
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            outcome = v;
            //设置任务状态为NORMAL
            UNSAFE.putOrderedInt(this, stateOffset, NORMAL);
            //执行
            finishCompletion();
        }
    }
    
    /**异常后设置值
     */
    protected void setException(Throwable t) {
        //设置任务状态为COMPLETING,表示正在设置任务的结果为异常对象
        //任务被cancel之后,不会进入里面,cancel会将状态修改为INTERRUPTED
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            outcome = t;
            //设置任务状态为EXCEPTIONAL
            UNSAFE.putOrderedInt(this, stateOffset, EXCEPTIONAL);
            finishCompletion();
        }
    }
    private void finishCompletion() {
        // assert state > COMPLETING;
        for (WaitNode q; (q = waiters) != null;) {
            //设置对栈顶的引用为null
            if (UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)) {
                //遍历链表
                for (;;) {
                    
                    Thread t = q.thread;
                    if (t != null) {
                        q.thread = null;
                        //唤醒节点。
                        LockSupport.unpark(t);
                    }
                    //下一个节点。
                    WaitNode next = q.next;
                    if (next == null)
                        break;
                    q.next = null; // unlink to help gc
                    q = next;
                }
                break;
            }
        }
        //子类复
        done();
        //设置为null
        callable = null;        // to reduce footprint
    }
    
    
    /**取消任务
     * mayInterruptIfRunning
     * true:中断
     *cancel方法所造成的中断最大的意义不是为了对中断进行处理，而是简单的停止任务线程的执行，节省CPU资源。
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
        //只要state不为NEW，说明任务已经执行完毕了
        //true:当前在执行的任务会被中断
        //false:可以允许正在执行的任务继续运行，直到它执行完
        if (!(state == NEW &&
                UNSAFE.compareAndSwapInt(this, stateOffset, NEW,
                        mayInterruptIfRunning ? INTERRUPTING : CANCELLED)))
            //直接返回失败,中断失败,任务已经执行完了
            return false;
        try {
            //当前任务会被中断。
            if (mayInterruptIfRunning) {
                try {
                    Thread t = runner;
                    if (t != null)
                        //中断线程,是否中断取决于callable的方法。
                        t.interrupt();
                } finally {
                    //设置中断状态
                    UNSAFE.putOrderedInt(this, stateOffset, INTERRUPTED);
                }
            }
        } finally {
            //唤起所有的线程
            finishCompletion();
        }
        return true;
    }
    
    /**判断是否cancel状态
     * state >= CANCELLED 包含了那些状态呢，它包括了: CANCELLED INTERRUPTING INTERRUPTED
     */
    public boolean isCancelled() {
        return state >= CANCELLED;
    }
    
    /**判断任务是否执行完毕
     * 非new状态
     */
    public boolean isDone() {
        return state != NEW;
    }


    /**获取任务结果。
     */
    public V get() throws InterruptedException, ExecutionException {
        int s = state;
        if (s <= COMPLETING)
            //返回值是任务的状态，而不是任务的结果
            s = awaitDone(false, 0L);
        return report(s);
    }

   
    public V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        if (unit == null)
            throw new NullPointerException();
        int s = state;
        if (s <= COMPLETING &&
            (s = awaitDone(true, unit.toNanos(timeout))) <= COMPLETING)
            throw new TimeoutException();
        return report(s);
    }

    /**
     * Protected method invoked when this task transitions to state
     * {@code isDone} (whether normally or via cancellation). The
     * default implementation does nothing.  Subclasses may override
     * this method to invoke completion callbacks or perform
     * bookkeeping. Note that you can query status inside the
     * implementation of this method to determine whether this task
     * has been cancelled.
     */
    protected void done() { }


   

    /**
     * Executes the computation without setting its result, and then
     * resets this future to initial state, failing to do so if the
     * computation encounters an exception or is cancelled.  This is
     * designed for use with tasks that intrinsically execute more
     * than once.
     *
     * @return {@code true} if successfully run and reset
     */
    protected boolean runAndReset() {
        if (state != NEW ||
            !UNSAFE.compareAndSwapObject(this, runnerOffset,
                                         null, Thread.currentThread()))
            return false;
        boolean ran = false;
        int s = state;
        try {
            Callable<V> c = callable;
            if (c != null && s == NEW) {
                try {
                    c.call(); // don't set result
                    ran = true;
                } catch (Throwable ex) {
                    setException(ex);
                }
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner = null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
        return ran && s == NEW;
    }

    /**检查是否有遗漏的中断，如果有，等待中断状态完成。
     */
    private void handlePossibleCancellationInterrupt(int s) {
        //如果任务的状态是INTERRUPTING
        if (s == INTERRUPTING)
            //原地自旋，直到state状态转换成终止态。
            while (state == INTERRUPTING)
                Thread.yield(); // wait out pending interrupt
    }

    /**单向链表:所有等待任务执行完毕的线程的集合
     */
    static final class WaitNode {
        volatile Thread thread;
        volatile WaitNode next;
        WaitNode() { thread = Thread.currentThread(); }
    }


    /**
     */
    private int awaitDone(boolean timed, long nanos)
        throws InterruptedException {
        
        final long deadline = timed ? System.nanoTime() + nanos : 0L;
        WaitNode q = null;
        boolean queued = false;
        //自旋
        for (;;) {
            //检测当前线程是否被中断了,因为get方法是阻塞的。
            //如果等待的任务还没有执行完，则调用get方法的线程会被扔到Treiber栈中挂起等待，直到任务执行完毕
            if (Thread.interrupted()) {
                //检测中断后,移除任务。
                removeWaiter(q);
                //抛出了InterruptedException异常。
                throw new InterruptedException();
            }
            
            int s = state;
            //如果任务已经进入终止态（s > COMPLETING），我们就直接返回任务的状态;
            if (s > COMPLETING) {
                if (q != null)
                    q.thread = null;
                //返回任务状态
                return s;
            }
            else if (s == COMPLETING)
                //我们就让出当前线程的CPU资源继续等待
                Thread.yield();
            else if (q == null)
                //当前线程还没有进入等待队列，于是我们新建了一个WaitNode
                q = new WaitNode();
            else if (!queued)
                //queued=false，表示当前线程还没有入队，所以我们执行了:和下面的代码等价。
//            q.next = waiters; //当前节点的next指向目前的栈顶元素
//            //如果栈顶节点在这个过程中没有变，即没有发生并发入栈的情况
//            if(waiters的值还是上面q.next所使用的waiters值){
//                waiters = q; //修改栈顶的指针，指向刚刚入栈的节点
//            }
                queued = UNSAFE.compareAndSwapObject(this, waitersOffset,
                                                     q.next = waiters, q);
            else if (timed) {
                nanos = deadline - System.nanoTime();
                if (nanos <= 0L) {
                    removeWaiter(q);
                    return state;
                }
                LockSupport.parkNanos(this, nanos);
            }
            else
                //挂起线程
                LockSupport.park(this);
            //唤起线程的时机
            //2、等待的线程自身因为被中断等原因而被唤醒。
        }
    }
    
    /**将参数中的node从等待队列（即Treiber栈）中移除。如果此时线程还没
           //1、任务执行完毕了，在finishCompletion方法中会唤醒所有在Treiber栈中等待的线程有进入Treiber栈，则 q=null，那么removeWaiter(q)啥也不干
     */
    private void removeWaiter(WaitNode node) {
        if (node != null) {
            node.thread = null;
            retry:
            for (;;) {          // restart on removeWaiter race
                for (WaitNode pred = null, q = waiters, s; q != null; q = s) {
                    s = q.next;
                    if (q.thread != null)
                        pred = q;
                    else if (pred != null) {
                        pred.next = s;
                        if (pred.thread == null) // check for race
                            continue retry;
                    }
                    else if (!UNSAFE.compareAndSwapObject(this, waitersOffset,
                                                          q, s))
                        continue retry;
                }
                break;
            }
        }
    }

    /*====================================================属性偏移量====================================================*/
    private static final sun.misc.Unsafe UNSAFE;
    private static final long stateOffset;
    private static final long runnerOffset;
    private static final long waitersOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = FutureTask.class;
            //线程状态
            stateOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("state"));
            //执行FutureTask中的“Task”的线程
            runnerOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("runner"));
            //指向栈顶的指针。
            waitersOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("waiters"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
