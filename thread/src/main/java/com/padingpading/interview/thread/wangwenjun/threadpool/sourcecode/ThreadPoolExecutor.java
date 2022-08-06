//
//package com.padingpading.interview.thread.wangwenjun.threadpool.sourcecode;
//
//import java.security.AccessControlContext;
//import java.security.AccessController;
//import java.security.PrivilegedAction;
//import java.util.ArrayList;
//import java.util.ConcurrentModificationException;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.concurrent.AbstractExecutorService;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//import java.util.concurrent.FutureTask;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.RejectedExecutionException;
//import java.util.concurrent.RejectedExecutionHandler;
//import java.util.concurrent.SynchronousQueue;
//import java.util.concurrent.ThreadFactory;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.locks.AbstractQueuedSynchronizer;
//import java.util.concurrent.locks.Condition;
//import java.util.concurrent.locks.ReentrantLock;
//
///**
// * Executor:任务提交
// * ExecutorService:提供了处理任务的方法
// * AbstractExecutorService:任务处理的方法
// * 线程池的状态:
// * 1、线程池本身的状态 ctl的高3位。
// * 2、线程池运行线程的数量。 ctl的低29位。
// */
//public class ThreadPoolExecutor extends AbstractExecutorService {
//    /**线程池的状态&线程池线程的个数。
//     */
//    private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
//    //29位,线程池中管理线程的数量。
//    private static final int COUNT_BITS = Integer.SIZE - 3;
//    //ctl&CAPACITY=线程数量。
//    private static final int CAPACITY   = (1 << COUNT_BITS) - 1;
//
//    /*
//    * RUNNING:线程池可以接受到新的任务提交,并且还可以正常处理阻塞队列中的任务。
//    * SHUTDOWN:不在接受新的任务提交,不过线程池可以继续处理阻塞队列中的任务。
//    * STOP:不在接受新的任务,同事还会丢弃阻塞队列中既有的任务,还会中断正在处理的任务。
//    * TIDYING:所有的任务都执行完毕以后,当前线程中的活动线程的数量降为0,会调用terminated方法。
//    * TERMINATED:线程的终止状态,当terminated方法执行完毕以后,线程池将会处于该状态之下。
//    *
//    * RUNNING->SHUTDOWN:当调用线程的shutdown方法,或者当finalized方法被隐士的调用后（方法内部会调用shutdown方法）
//    * RUNNING,SHUTDOWN->STOP:当调用线程池的shutdownnow()
//    * SHUTDOWN->TIDYING:当线程池与阻塞队列均变为空时。
//    * STOP->TIDYING:线程池变为空的时候。
//    * TIDYING->TERMINATED:在terminated()执行完毕之后。
//    * */
//    //线程池的状态
//    private static final int RUNNING    = -1 << COUNT_BITS;
//    private static final int SHUTDOWN   =  0 << COUNT_BITS;
//    private static final int STOP       =  1 << COUNT_BITS;
//    private static final int TIDYING    =  2 << COUNT_BITS;
//    private static final int TERMINATED =  3 << COUNT_BITS;
//    //当前线程的状态
//    private static int runStateOf(int c)     { return c & ~CAPACITY; }
//    //线程的数量
//    private static int workerCountOf(int c)  { return c & CAPACITY; }
//    //
//    private static int ctlOf(int rs, int wc) { return rs | wc; }
//
//    private static boolean runStateLessThan(int c, int s) {
//        return c < s;
//    }
//    private static boolean runStateAtLeast(int c, int s) {
//        return c >= s;
//    }
//    private static boolean isRunning(int c) {
//        return c < SHUTDOWN;
//    }
//
//    /**
//     * Attempts to CAS-increment the workerCount field of ctl.
//     */
//    private boolean compareAndIncrementWorkerCount(int expect) {
//        return ctl.compareAndSet(expect, expect + 1);
//    }
//
//    /**
//     * Attempts to CAS-decrement the workerCount field of ctl.
//     */
//    private boolean compareAndDecrementWorkerCount(int expect) {
//        return ctl.compareAndSet(expect, expect - 1);
//    }
//
//    /**
//     * Decrements the workerCount field of ctl. This is called only on
//     * abrupt termination of a thread (see processWorkerExit). Other
//     * decrements are performed within getTask.
//     */
//    private void decrementWorkerCount() {
//        do {} while (! compareAndDecrementWorkerCount(ctl.get()));
//    }
//
//    /**
//     * The queue used for holding tasks and handing off to worker
//     * threads.  We do not require that workQueue.poll() returning
//     * null necessarily means that workQueue.isEmpty(), so rely
//     * solely on isEmpty to see if the queue is empty (which we must
//     * do for example when deciding whether to transition from
//     * SHUTDOWN to TIDYING).  This accommodates special-purpose
//     * queues such as DelayQueues for which poll() is allowed to
//     * return null even if it may later return non-null when delays
//     * expire.
//     */
//    private final BlockingQueue<Runnable> workQueue;
//
//    /**
//     * Lock held on access to workers set and related bookkeeping.
//     * While we could use a concurrent set of some sort, it turns out
//     * to be generally preferable to use a lock. Among the reasons is
//     * that this serializes interruptIdleWorkers, which avoids
//     * unnecessary interrupt storms, especially during shutdown.
//     * Otherwise exiting threads would concurrently interrupt those
//     * that have not yet interrupted. It also simplifies some of the
//     * associated statistics bookkeeping of largestPoolSize etc. We
//     * also hold mainLock on shutdown and shutdownNow, for the sake of
//     * ensuring workers set is stable while separately checking
//     * permission to interrupt and actually interrupting.
//     */
//    private final ReentrantLock mainLock = new ReentrantLock();
//
//    /**
//     * Set containing all worker threads in pool. Accessed only when
//     * holding mainLock.
//     */
//    private final HashSet<Worker> workers = new HashSet<Worker>();
//
//    /**
//     * Wait condition to support awaitTermination
//     */
//    private final Condition termination = mainLock.newCondition();
//
//    /**
//     * Tracks largest attained pool size. Accessed only under
//     * mainLock.
//     */
//    private int largestPoolSize;
//
//    /**线程池完成任务的个数。
//     * 只有在线程termination的时候会终止的时候,会把线程完成任务的个数+过来。
//     */
//    private long completedTaskCount;
//
//    /*
//     * All user control parameters are declared as volatiles so that
//     * ongoing actions are based on freshest values, but without need
//     * for locking, since no internal invariants depend on them
//     * changing synchronously with respect to other actions.
//     */
//
//    /**
//     * Factory for new threads. All threads are created using this
//     * factory (via method addWorker).  All callers must be prepared
//     * for addWorker to fail, which may reflect a system or user's
//     * policy limiting the number of threads.  Even though it is not
//     * treated as an error, failure to create threads may result in
//     * new tasks being rejected or existing ones remaining stuck in
//     * the queue.
//     *
//     * We go further and preserve pool invariants even in the face of
//     * errors such as OutOfMemoryError, that might be thrown while
//     * trying to create threads.  Such errors are rather common due to
//     * the need to allocate a native stack in Thread.start, and users
//     * will want to perform clean pool shutdown to clean up.  There
//     * will likely be enough memory available for the cleanup code to
//     * complete without encountering yet another OutOfMemoryError.
//     */
//    private volatile ThreadFactory threadFactory;
//
//    /**
//     * Handler called when saturated or shutdown in execute.
//     */
//    private volatile RejectedExecutionHandler handler;
//
//    /**
//     * Timeout in nanoseconds for idle threads waiting for work.
//     * Threads use this timeout when there are more than corePoolSize
//     * present or if allowCoreThreadTimeOut. Otherwise they wait
//     * forever for new work.
//     */
//    private volatile long keepAliveTime;
//
//    /**
//     * If false (default), core threads stay alive even when idle.
//     * If true, core threads use keepAliveTime to time out waiting
//     * for work.
//     */
//    private volatile boolean allowCoreThreadTimeOut;
//
//    /**
//     * Core pool size is the minimum number of workers to keep alive
//     * (and not allow to time out etc) unless allowCoreThreadTimeOut
//     * is set, in which case the minimum is zero.
//     */
//    private volatile int corePoolSize;
//
//    /**
//     * Maximum pool size. Note that the actual maximum is internally
//     * bounded by CAPACITY.
//     */
//    private volatile int maximumPoolSize;
//
//    /**
//     * The default rejected execution handler
//     */
//    private static final RejectedExecutionHandler defaultHandler =
//        new AbortPolicy();
//
//    /**
//     * Permission required for callers of shutdown and shutdownNow.
//     * We additionally require (see checkShutdownAccess) that callers
//     * have permission to actually interrupt threads in the worker set
//     * (as governed by Thread.interrupt, which relies on
//     * ThreadGroup.checkAccess, which in turn relies on
//     * SecurityManager.checkAccess). Shutdowns are attempted only if
//     * these checks pass.
//     *
//     * All actual invocations of Thread.interrupt (see
//     * interruptIdleWorkers and interruptWorkers) ignore
//     * SecurityExceptions, meaning that the attempted interrupts
//     * silently fail. In the case of shutdown, they should not fail
//     * unless the SecurityManager has inconsistent policies, sometimes
//     * allowing access to a thread and sometimes not. In such cases,
//     * failure to actually interrupt threads may disable or delay full
//     * termination. Other uses of interruptIdleWorkers are advisory,
//     * and failure to actually interrupt will merely delay response to
//     * configuration changes so is not handled exceptionally.
//     */
//    private static final RuntimePermission shutdownPerm =
//        new RuntimePermission("modifyThread");
//
//    /* The context to be used when executing the finalizer, or null. */
//    private final AccessControlContext acc;
//
//    /**Worker
//     * Runnable:任务最终执行的方法
//     */
//    private final class Worker
//        extends AbstractQueuedSynchronizer
//        implements Runnable
//    {
//        /**
//         * This class will never be serialized, but we provide a
//         * serialVersionUID to suppress a javac warning.
//         */
//        private static final long serialVersionUID = 6138294804551838833L;
//
//        /** Thread this worker is running in.  Null if factory fails. */
//        final Thread thread;
//        /** Initial task to run.  Possibly null. */
//        Runnable firstTask;
//        /** Per-thread task counter */
//        volatile long completedTasks;
//
//        /**创建Worker
//         */
//        Worker(Runnable firstTask) {
//            //设置aqs state=-1 让worker只有在运行的时候再能被重中断。
//            setState(-1);
//            //设置任务
//            this.firstTask = firstTask;
//            //获取线程工厂自定义thread,并添加任务.
//            this.thread = getThreadFactory().newThread(this);
//        }
//
//        /**构造方法将worker添加进去了。
//         * 最终执行的方法。
//         */
//        public void run() {
//            runWorker(this);
//        }
//
//        // Lock methods
//        //
//        // The value 0 represents the unlocked state.
//        // The value 1 represents the locked state.
//
//        protected boolean isHeldExclusively() {
//            return getState() != 0;
//        }
//
//        protected boolean tryAcquire(int unused) {
//            if (compareAndSetState(0, 1)) {
//                setExclusiveOwnerThread(Thread.currentThread());
//                return true;
//            }
//            return false;
//        }
//
//        protected boolean tryRelease(int unused) {
//            setExclusiveOwnerThread(null);
//            setState(0);
//            return true;
//        }
//
//        public void lock()        { acquire(1); }
//        public boolean tryLock()  { return tryAcquire(1); }
//        public void unlock()      { release(1); }
//        public boolean isLocked() { return isHeldExclusively(); }
//
//        void interruptIfStarted() {
//            Thread t;
//            if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
//                try {
//                    t.interrupt();
//                } catch (SecurityException ignore) {
//                }
//            }
//        }
//    }
//
//    /*
//     * Methods for setting control state
//     */
//
//    /**
//     * Transitions runState to given target, or leaves it alone if
//     * already at least the given target.
//     *
//     * @param targetState the desired state, either SHUTDOWN or STOP
//     *        (but not TIDYING or TERMINATED -- use tryTerminate for that)
//     */
//    private void advanceRunState(int targetState) {
//        for (;;) {
//            int c = ctl.get();
//            //cas操作。
//            if (runStateAtLeast(c, targetState) ||
//                ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c))))
//                break;
//        }
//    }
//
//    /**尝试将线程池改为Termminaterd状态
//     * 1、shutdown():线程池和阻塞队列都为空。
//     * 2、shop()线程池空了,阻塞队列中的任务会返回。
//     */
//    final void tryTerminate() {
//        for (;;) {
//            //获取状态
//            int c = ctl.get();
//            //判断状态
//            if (isRunning(c) ||
//                runStateAtLeast(c, TIDYING) ||
//                (runStateOf(c) == SHUTDOWN && ! workQueue.isEmpty()))
//                //线程池还存活,或者正在关闭任务。
//                return;
//            //工作线程不等于0,当前关闭的线程不是最后一个线程。
//            if (workerCountOf(c) != 0) { // Eligible to terminate
//                interruptIdleWorkers(ONLY_ONE);
//                return;
//            }
//
//            final ReentrantLock mainLock = this.mainLock;
//            mainLock.lock();
//            try {
//                //线程池数量和任务队列数量都为0，设置状态。
//                if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
//                    try {
//                        //调用已经关闭方法。
//                        terminated();
//                    } finally {
//                        //设置线程池状态为TERMINATED
//                        ctl.set(ctlOf(TERMINATED, 0));
//                        //唤醒condition的阻塞的线程
//                        termination.signalAll();
//                    }
//                    return;
//                }
//            } finally {
//                mainLock.unlock();
//            }
//            // else retry on failed CAS
//        }
//    }
//
//    /*
//     * Methods for controlling interrupts to worker threads.
//     */
//
//    /**
//     * If there is a security manager, makes sure caller has
//     * permission to shut down threads in general (see shutdownPerm).
//     * If this passes, additionally makes sure the caller is allowed
//     * to interrupt each worker thread. This might not be true even if
//     * first check passed, if the SecurityManager treats some threads
//     * specially.
//     */
//    private void checkShutdownAccess() {
//        SecurityManager security = System.getSecurityManager();
//        if (security != null) {
//            security.checkPermission(shutdownPerm);
//            final ReentrantLock mainLock = this.mainLock;
//            mainLock.lock();
//            try {
//                for (Worker w : workers)
//                    security.checkAccess(w.thread);
//            } finally {
//                mainLock.unlock();
//            }
//        }
//    }
//
//    /**中断所有线程
//     */
//    private void interruptWorkers() {
//        final ReentrantLock mainLock = this.mainLock;
//        mainLock.lock();
//        try {
//            //中断所有任務。
//            for (Worker w : workers)
//                //没有获取锁,直接中断。
//                w.interruptIfStarted();
//        } finally {
//            mainLock.unlock();
//        }
//    }
//
//    /**中断闲置的线程
//     *
//     */
//    private void interruptIdleWorkers(boolean onlyOne) {
//        final ReentrantLock mainLock = this.mainLock;
//        mainLock.lock();
//        try {
//            for (Worker w : workers) {
//                Thread t = w.thread;
//                //t.isInterrupted():判断线程是否已经中断。
//                //w.tryLock()):获取锁,获取到锁,线程空闲,线程在执行任务是需要上锁的。
//                if (!t.isInterrupted() && w.tryLock()) {
//                    try {
//                        //线程中断。
//                        t.interrupt();
//                    } catch (SecurityException ignore) {
//                    } finally {
//                        //释放锁。
//                        w.unlock();
//                    }
//                }
//                if (onlyOne)
//                    break;
//            }
//        } finally {
//            //释放锁。
//            mainLock.unlock();
//        }
//    }
//
//    /**
//     * Common form of interruptIdleWorkers, to avoid having to
//     * remember what the boolean argument means.
//     */
//    private void interruptIdleWorkers() {
//        interruptIdleWorkers(false);
//    }
//
//    private static final boolean ONLY_ONE = true;
//
//    /*
//     * Misc utilities, most of which are also exported to
//     * ScheduledThreadPoolExecutor
//     */
//
//    /**
//     * Invokes the rejected execution handler for the given command.
//     * Package-protected for use by ScheduledThreadPoolExecutor.
//     */
//    final void reject(Runnable command) {
//        handler.rejectedExecution(command, this);
//    }
//
//    /**
//     * Performs any further cleanup following run state transition on
//     * invocation of shutdown.  A no-op here, but used by
//     * ScheduledThreadPoolExecutor to cancel delayed tasks.
//     */
//    void onShutdown() {
//    }
//
//    /**
//     * State check needed by ScheduledThreadPoolExecutor to
//     * enable running tasks during shutdown.
//     *
//     * @param shutdownOK true if should return true if SHUTDOWN
//     */
//    final boolean isRunningOrShutdown(boolean shutdownOK) {
//        int rs = runStateOf(ctl.get());
//        return rs == RUNNING || (rs == SHUTDOWN && shutdownOK);
//    }
//
//    /**阻塞队列中的任务赋值到新的容器里。
//     *
//     */
//    private List<Runnable> drainQueue() {
//        BlockingQueue<Runnable> q = workQueue;
//        ArrayList<Runnable> taskList = new ArrayList<Runnable>();
//        q.drainTo(taskList);
//        if (!q.isEmpty()) {
//            for (Runnable r : q.toArray(new Runnable[0])) {
//                if (q.remove(r))
//                    taskList.add(r);
//            }
//        }
//        return taskList;
//    }
//
//    /*
//     * Methods for creating, running and cleaning up after workers
//     */
//
//    /**
//     *firstTask:新创建线程执行的任务。
//     * core
//     *   true:使用核心线程判断
//     *   false:使用最大线程判断
//     */
//    private boolean addWorker(Runnable firstTask, boolean core) {
//        retry:
//        for (;;) {
//            int c = ctl.get();
//            //线程池状态
//            int rs = runStateOf(c);
//            //不创建线程。
//            //1、运行状态为 STOP,TIDYING,TERMINATED
//            //2、运行状态为SHUTDOWN、执行任务firstTask为null,阻塞队列为空
//            if (rs >= SHUTDOWN &&
//                ! (rs == SHUTDOWN &&
//                   firstTask == null &&
//                   ! workQueue.isEmpty()))
//                //直接返回,不创建线程。
//                return false;
//
//            for (;;) {
//                //获取线程数量
//                int wc = workerCountOf(c);
//                //
//                //1、线程数大于最大数量wc >= CAPACITY
//                //2、core决定是否根据那个做比较。
//                //   corePoolSize:大于核心
//                //   maximumPoolSize:大于最大线程数
//                if (wc >= CAPACITY ||
//                    wc >= (core ? corePoolSize : maximumPoolSize))
//                    //直接返回,不创建线程。
//                    return false;
//                //CAS添加线程池的线程的数量 +1
//                if (compareAndIncrementWorkerCount(c))
//                    break retry;
//                c = ctl.get();  // Re-read ctl
//                if (runStateOf(c) != rs)
//                    //如果状态已经被修改,跳到外层循环再来一次retry
//                    continue retry;
//                //进行内部循环
//            }
//        }
//
//        //工作线程是否启动
//        boolean workerStarted = false;
//        //
//        boolean workerAdded = false;
//        Worker w = null;
//        try {
//            //创建新的worker
//            w = new Worker(firstTask);
//            //拿到thread
//            final Thread t = w.thread;
//            if (t != null) {
//                //线程池的排它锁
//                final ReentrantLock mainLock = this.mainLock;
//                mainLock.lock();
//                try {
//                    // Recheck while holding lock.
//                    // Back out on ThreadFactory failure or if
//                    // shut down before lock acquired.
//                    //获取线程池的状态
//                    int rs = runStateOf(ctl.get());
//                    //线程池正在运行,
//                    // 线程池关闭&&firstTask！=null，线程池关闭了,队列中的任务还会执行。
//                    if (rs < SHUTDOWN ||
//                        (rs == SHUTDOWN && firstTask == null)) {
//                        //线程已经被关闭。
//                        if (t.isAlive()) // precheck that t is startable
//                            throw new IllegalThreadStateException();
//                        //添加到worker集合
//                        workers.add(w);
//                        int s = workers.size();
//                        if (s > largestPoolSize)
//                            largestPoolSize = s;
//                        //添加到worker的集合中
//                        workerAdded = true;
//                    }
//                } finally {
//                    //释放锁
//                    mainLock.unlock();
//                }
//                if (workerAdded) {
//                    //开启线程
//                    t.start();
//                    //工作线程启动
//                    workerStarted = true;
//                }
//            }
//        } finally {
//            //woker启动失败,回滚。
//            if (! workerStarted)
//                addWorkerFailed(w);
//        }
//        return workerStarted;
//    }
//
//    /**
//     * Rolls back the worker thread creation.
//     * - removes worker from workers, if present
//     * - decrements worker count
//     * - rechecks for termination, in case the existence of this
//     *   worker was holding up termination
//     */
//    private void addWorkerFailed(Worker w) {
//        final ReentrantLock mainLock = this.mainLock;
//        mainLock.lock();
//        try {
//            if (w != null)
//                //移除集合
//                workers.remove(w);
//            //线程数-1
//            decrementWorkerCount();
//            tryTerminate();
//        } finally {
//            mainLock.unlock();
//        }
//    }
//
//    /**工作线程的销毁
//     * completedAbruptly
//     *    true:正常执行完成
//     *    false:被打断。
//     */
//    private void processWorkerExit(Worker w, boolean completedAbruptly) {
//        if (completedAbruptly) // If abrupt, then workerCount wasn't adjusted
//            decrementWorkerCount();
//        //线程池的排它锁。
//        final ReentrantLock mainLock = this.mainLock;
//        mainLock.lock();
//        try {
//            //将执行任务的个数,添加到线程池完成任务的个数。
//            completedTaskCount += w.completedTasks;、
//            //移除worker.
//            workers.remove(w);
//        } finally {
//            mainLock.unlock();
//        }
//        //尝试终止。
//        tryTerminate();
//
//        int c = ctl.get();
//        if (runStateLessThan(c, STOP)) {
//            if (!completedAbruptly) {
//                int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
//                if (min == 0 && ! workQueue.isEmpty())
//                    min = 1;
//                if (workerCountOf(c) >= min)
//                    return; // replacement not needed
//            }
//            addWorker(null, false);
//        }
//    }
//
//    /**
//     * Performs blocking or timed wait for a task, depending on
//     * current configuration settings, or returns null if this worker
//     * must exit because of any of:
//     * 1. There are more than maximumPoolSize workers (due to
//     *    a call to setMaximumPoolSize).
//     * 2. The pool is stopped.
//     * 3. The pool is shutdown and the queue is empty.
//     * 4. This worker timed out waiting for a task, and timed-out
//     *    workers are subject to termination (that is,
//     *    {@code allowCoreThreadTimeOut || workerCount > corePoolSize})
//     *    both before and after the timed wait, and if the queue is
//     *    non-empty, this worker is not the last thread in the pool.
//     *
//     * @return task, or null if the worker must exit, in which case
//     *         workerCount is decremented
//     */
//    private Runnable getTask() {
//        boolean timedOut = false; // Did the last poll() time out?
//
//        for (;;) {
//            int c = ctl.get();
//            int rs = runStateOf(c);
//
//            // Check if queue empty only if necessary.
//            if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
//                decrementWorkerCount();
//                return null;
//            }
//
//            int wc = workerCountOf(c);
//
//            // Are workers subject to culling?
//            boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;
//
//            if ((wc > maximumPoolSize || (timed && timedOut))
//                && (wc > 1 || workQueue.isEmpty())) {
//                if (compareAndDecrementWorkerCount(c))
//                    return null;
//                continue;
//            }
//
//            try {
//                Runnable r = timed ?
//                    workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
//                    workQueue.take();
//                if (r != null)
//                    return r;
//                timedOut = true;
//            } catch (InterruptedException retry) {
//                timedOut = false;
//            }
//        }
//    }
//
//    /**
//     */
//    final void runWorker(Worker w) {
//        Thread wt = Thread.currentThread();
//        //首个执行的任务,线程可能是新被创建的,所以会携带任务。
//        Runnable task = w.firstTask;
//        w.firstTask = null;
//        //创建worker对象的时候,state=-1,启动前不让中断/
//        //这里设置state=0,允许中断。
//        w.unlock(); // allow interrupts
//        boolean completedAbruptly = true;
//        try {
//            //无限循环,从阻塞队列中获取任务。
//            //1、获取的task是传入的task
//            //2、阻塞队列中的task。
//            while (task != null || (task = getTask()) != null) {
//                //上锁
//                w.lock();
//                //线程池停止,确保线程被中断。
//                //线程池没有停止,线程不会被中断。
//                //下面判断线程池是不是stop状态。
//                if ((runStateAtLeast(ctl.get(), STOP) ||
//                     (Thread.interrupted() &&
//                      runStateAtLeast(ctl.get(), STOP))) &&
//                    !wt.isInterrupted())
//                    wt.interrupt();
//                try {
//                    //执行之前钩子方法
//                    beforeExecute(wt, task);
//                    Throwable thrown = null;
//                    try {
//                        task.run();
//                    } catch (RuntimeException x) {
//                        thrown = x; throw x;
//                    } catch (Error x) {
//                        thrown = x; throw x;
//                    } catch (Throwable x) {
//                        thrown = x; throw new Error(x);
//                    } finally {
//                        //执行之后钩子方法
//                        afterExecute(task, thrown);
//                    }
//                } finally {
//                    task = null;
//                    //设置任务完成的个数。
//                    w.completedTasks++;
//                    //解锁
//                    w.unlock();
//                }
//            }
//            completedAbruptly = false;
//        } finally {
//            //正常和异常下 工作线程的退出,
//            processWorkerExit(w, completedAbruptly);
//        }
//    }
//
//    // Public constructors and methods
//    /*=================================================构造方法========================================================*/
//    /**
//     */
//    public ThreadPoolExecutor(int corePoolSize,
//                              int maximumPoolSize,
//                              long keepAliveTime,
//                              TimeUnit unit,
//                              BlockingQueue<Runnable> workQueue) {
//        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
//             Executors.defaultThreadFactory(), defaultHandler);
//    }
//
//    /**
//     */
//    public ThreadPoolExecutor(int corePoolSize,
//                              int maximumPoolSize,
//                              long keepAliveTime,
//                              TimeUnit unit,
//                              BlockingQueue<Runnable> workQueue,
//                              ThreadFactory threadFactory) {
//        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
//             threadFactory, defaultHandler);
//    }
//
//    /**
//     */
//    public ThreadPoolExecutor(int corePoolSize,
//                              int maximumPoolSize,
//                              long keepAliveTime,
//                              TimeUnit unit,
//                              BlockingQueue<Runnable> workQueue,
//                              RejectedExecutionHandler handler) {
//        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
//             Executors.defaultThreadFactory(), handler);
//    }
//
//    /**
//     * corePoolSize:核心线程数
//     * maximumPoolSize:最大可存活的线程
//     * keepAliveTime:等待任务最大的时间,超过该时间,线程会被回收。
//     * unit:时间单位
//     * BlockingQueue:阻塞队列,执行的任务队列。
//     * threadFactory:线程的创建工厂,用户自定义线程。
//     * RejectedExecutionHandler:拒绝策略。
//     */
//    public ThreadPoolExecutor(int corePoolSize,
//                              int maximumPoolSize,
//                              long keepAliveTime,
//                              TimeUnit unit,
//                              BlockingQueue<Runnable> workQueue,
//                              ThreadFactory threadFactory,
//                              RejectedExecutionHandler handler) {
//        if (corePoolSize < 0 ||
//            maximumPoolSize <= 0 ||
//            maximumPoolSize < corePoolSize ||
//            keepAliveTime < 0)
//            throw new IllegalArgumentException();
//        if (workQueue == null || threadFactory == null || handler == null)
//            throw new NullPointerException();
//        this.acc = System.getSecurityManager() == null ?
//                null :
//                AccessController.getContext();
//        this.corePoolSize = corePoolSize;
//        this.maximumPoolSize = maximumPoolSize;
//        this.workQueue = workQueue;
//        this.keepAliveTime = unit.toNanos(keepAliveTime);
//        this.threadFactory = threadFactory;
//        this.handler = handler;
//    }
//
//    /**执行入口
//     */
//    public void execute(Runnable command) {
//        if (command == null)
//            throw new NullPointerException();
//        //线程的状态和线程池维护线程的数量
//        int c = ctl.get();
//        //线程池线程数量<核心线程数
//        if (workerCountOf(c) < corePoolSize) {
//            //并发添加一个worker线程,并执行command
//            if (addWorker(command, true))
//                //成功返回
//                return;
//            //重新获取。
//            c = ctl.get();
//        }
//        //检查线程池的状态 并且 添加到阻塞队列成功。
//        if (isRunning(c) && workQueue.offer(command)) {
//            //状态检查。
//            int recheck = ctl.get();
//            if (!isRunning(recheck) && remove(command))
//                //线程池关了,并且移除任务。
//                reject(command);
//            //添加进队列任务,正好线程已经没有了。
//            else if (workerCountOf(recheck) == 0)
//                addWorker(null, false);
//        }
//        //阻塞队列超过最大值,开辟新的线程。
//        else if (!addWorker(command, false))
//            //执行拒绝策略
//            reject(command);
//    }
//
//    /**RUNNING->SHUTDOWN 线程池不在接受任务,但还会执行阻塞队列中的任务。
//     * 1、新的任务不会再接受。
//     * 2、之前提交的任务,不会执行。
//     */
//    public void shutdown() {
//        //获取排它锁
//        final ReentrantLock mainLock = this.mainLock;
//        mainLock.lock();
//        try {
//            //权限检查。
//            checkShutdownAccess();
//            //提交将线程池状态设置为SHUTDOWN
//            advanceRunState(SHUTDOWN);
//            //中断空闲的任务,正在执行的任务不做中断。
//            interruptIdleWorkers();
//            onShutdown();
//        } finally {
//            mainLock.unlock();
//        }
//        //
//        tryTerminate();
//    }
//
//    /**关闭线程池,停止接受任务,并返回等待处理的任务。
//     *
//     */
//    public List<Runnable> shutdownNow() {
//        List<Runnable> tasks;
//        final ReentrantLock mainLock = this.mainLock;
//        mainLock.lock();
//        try {
//            checkShutdownAccess();
//            //线程池状态:STOP
//            advanceRunState(STOP);
//            //中断所有线程
//            interruptWorkers();
//            //阻塞队列中获取所有任务。
//            tasks = drainQueue();
//        } finally {
//            mainLock.unlock();
//        }
//        tryTerminate();
//        return tasks;
//    }
//
//    public boolean isShutdown() {
//        return ! isRunning(ctl.get());
//    }
//
//    /**
//     * Returns true if this executor is in the process of terminating
//     * after {@link #shutdown} or {@link #shutdownNow} but has not
//     * completely terminated.  This method may be useful for
//     * debugging. A return of {@code true} reported a sufficient
//     * period after shutdown may indicate that submitted tasks have
//     * ignored or suppressed interruption, causing this executor not
//     * to properly terminate.
//     *
//     * @return {@code true} if terminating but not yet terminated
//     */
//    public boolean isTerminating() {
//        int c = ctl.get();
//        return ! isRunning(c) && runStateLessThan(c, TERMINATED);
//    }
//
//    public boolean isTerminated() {
//        return runStateAtLeast(ctl.get(), TERMINATED);
//    }
//
//    /**阻塞式判断线程池是否已经关闭
//     *
//     */
//    public boolean awaitTermination(long timeout, TimeUnit unit)
//        throws InterruptedException {
//        long nanos = unit.toNanos(timeout);
//        final ReentrantLock mainLock = this.mainLock;
//        mainLock.lock();
//        try {
//            for (;;) {
//                if (runStateAtLeast(ctl.get(), TERMINATED))
//                    return true;
//                if (nanos <= 0)
//                    return false;
//                nanos = termination.awaitNanos(nanos);
//            }
//        } finally {
//            mainLock.unlock();
//        }
//    }
//
//    /**
//     * Invokes {@code shutdown} when this executor is no longer
//     * referenced and it has no threads.
//     */
//    protected void finalize() {
//        SecurityManager sm = System.getSecurityManager();
//        if (sm == null || acc == null) {
//            shutdown();
//        } else {
//            PrivilegedAction<Void> pa = () -> { shutdown(); return null; };
//            AccessController.doPrivileged(pa, acc);
//        }
//    }
//
//    /**
//     * Sets the thread factory used to create new threads.
//     *
//     * @param threadFactory the new thread factory
//     * @throws NullPointerException if threadFactory is null
//     * @see #getThreadFactory
//     */
//    public void setThreadFactory(ThreadFactory threadFactory) {
//        if (threadFactory == null)
//            throw new NullPointerException();
//        this.threadFactory = threadFactory;
//    }
//
//
//
//    /**
//     * Sets a new handler for unexecutable tasks.
//     *
//     * @param handler the new handler
//     * @throws NullPointerException if handler is null
//     * @see #getRejectedExecutionHandler
//     */
//    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
//        if (handler == null)
//            throw new NullPointerException();
//        this.handler = handler;
//    }
//
//    /**
//     * Returns the current handler for unexecutable tasks.
//     *
//     * @return the current handler
//     * @see #setRejectedExecutionHandler(RejectedExecutionHandler)
//     */
//    public RejectedExecutionHandler getRejectedExecutionHandler() {
//        return handler;
//    }
//
//    /**
//     * Sets the core number of threads.  This overrides any value set
//     * in the constructor.  If the new value is smaller than the
//     * current value, excess existing threads will be terminated when
//     * they next become idle.  If larger, new threads will, if needed,
//     * be started to execute any queued tasks.
//     *
//     * @param corePoolSize the new core size
//     * @throws IllegalArgumentException if {@code corePoolSize < 0}
//     * @see #getCorePoolSize
//     */
//    public void setCorePoolSize(int corePoolSize) {
//        if (corePoolSize < 0)
//            throw new IllegalArgumentException();
//        int delta = corePoolSize - this.corePoolSize;
//        this.corePoolSize = corePoolSize;
//        if (workerCountOf(ctl.get()) > corePoolSize)
//            interruptIdleWorkers();
//        else if (delta > 0) {
//            // We don't really know how many new threads are "needed".
//            // As a heuristic, prestart enough new workers (up to new
//            // core size) to handle the current number of tasks in
//            // queue, but stop if queue becomes empty while doing so.
//            int k = Math.min(delta, workQueue.size());
//            while (k-- > 0 && addWorker(null, true)) {
//                if (workQueue.isEmpty())
//                    break;
//            }
//        }
//    }
//
//    /**
//     * Returns the core number of threads.
//     *
//     * @return the core number of threads
//     * @see #setCorePoolSize
//     */
//    public int getCorePoolSize() {
//        return corePoolSize;
//    }
//
//    /**
//     * Starts a core thread, causing it to idly wait for work. This
//     * overrides the default policy of starting core threads only when
//     * new tasks are executed. This method will return {@code false}
//     * if all core threads have already been started.
//     *
//     * @return {@code true} if a thread was started
//     */
//    public boolean prestartCoreThread() {
//        return workerCountOf(ctl.get()) < corePoolSize &&
//            addWorker(null, true);
//    }
//
//    /**
//     * Same as prestartCoreThread except arranges that at least one
//     * thread is started even if corePoolSize is 0.
//     */
//    void ensurePrestart() {
//        int wc = workerCountOf(ctl.get());
//        if (wc < corePoolSize)
//            addWorker(null, true);
//        else if (wc == 0)
//            addWorker(null, false);
//    }
//
//    /**
//     * Starts all core threads, causing them to idly wait for work. This
//     * overrides the default policy of starting core threads only when
//     * new tasks are executed.
//     *
//     * @return the number of threads started
//     */
//    public int prestartAllCoreThreads() {
//        int n = 0;
//        while (addWorker(null, true))
//            ++n;
//        return n;
//    }
//
//    /**
//     * Returns true if this pool allows core threads to time out and
//     * terminate if no tasks arrive within the keepAlive time, being
//     * replaced if needed when new tasks arrive. When true, the same
//     * keep-alive policy applying to non-core threads applies also to
//     * core threads. When false (the default), core threads are never
//     * terminated due to lack of incoming tasks.
//     *
//     * @return {@code true} if core threads are allowed to time out,
//     *         else {@code false}
//     *
//     * @since 1.6
//     */
//    public boolean allowsCoreThreadTimeOut() {
//        return allowCoreThreadTimeOut;
//    }
//
//    /**
//     * Sets the policy governing whether core threads may time out and
//     * terminate if no tasks arrive within the keep-alive time, being
//     * replaced if needed when new tasks arrive. When false, core
//     * threads are never terminated due to lack of incoming
//     * tasks. When true, the same keep-alive policy applying to
//     * non-core threads applies also to core threads. To avoid
//     * continual thread replacement, the keep-alive time must be
//     * greater than zero when setting {@code true}. This method
//     * should in general be called before the pool is actively used.
//     *
//     * @param value {@code true} if should time out, else {@code false}
//     * @throws IllegalArgumentException if value is {@code true}
//     *         and the current keep-alive time is not greater than zero
//     *
//     * @since 1.6
//     */
//    public void allowCoreThreadTimeOut(boolean value) {
//        if (value && keepAliveTime <= 0)
//            throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
//        if (value != allowCoreThreadTimeOut) {
//            allowCoreThreadTimeOut = value;
//            if (value)
//                interruptIdleWorkers();
//        }
//    }
//
//    /**
//     * Sets the maximum allowed number of threads. This overrides any
//     * value set in the constructor. If the new value is smaller than
//     * the current value, excess existing threads will be
//     * terminated when they next become idle.
//     *
//     * @param maximumPoolSize the new maximum
//     * @throws IllegalArgumentException if the new maximum is
//     *         less than or equal to zero, or
//     *         less than the {@linkplain #getCorePoolSize core pool size}
//     * @see #getMaximumPoolSize
//     */
//    public void setMaximumPoolSize(int maximumPoolSize) {
//        if (maximumPoolSize <= 0 || maximumPoolSize < corePoolSize)
//            throw new IllegalArgumentException();
//        this.maximumPoolSize = maximumPoolSize;
//        if (workerCountOf(ctl.get()) > maximumPoolSize)
//            interruptIdleWorkers();
//    }
//
//    /**
//     * Returns the maximum allowed number of threads.
//     *
//     * @return the maximum allowed number of threads
//     * @see #setMaximumPoolSize
//     */
//    public int getMaximumPoolSize() {
//        return maximumPoolSize;
//    }
//
//    /**
//     * Sets the time limit for which threads may remain idle before
//     * being terminated.  If there are more than the core number of
//     * threads currently in the pool, after waiting this amount of
//     * time without processing a task, excess threads will be
//     * terminated.  This overrides any value set in the constructor.
//     *
//     * @param time the time to wait.  A time value of zero will cause
//     *        excess threads to terminate immediately after executing tasks.
//     * @param unit the time unit of the {@code time} argument
//     * @throws IllegalArgumentException if {@code time} less than zero or
//     *         if {@code time} is zero and {@code allowsCoreThreadTimeOut}
//     * @see #getKeepAliveTime(TimeUnit)
//     */
//    public void setKeepAliveTime(long time, TimeUnit unit) {
//        if (time < 0)
//            throw new IllegalArgumentException();
//        if (time == 0 && allowsCoreThreadTimeOut())
//            throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
//        long keepAliveTime = unit.toNanos(time);
//        long delta = keepAliveTime - this.keepAliveTime;
//        this.keepAliveTime = keepAliveTime;
//        if (delta < 0)
//            interruptIdleWorkers();
//    }
//
//    /**
//     * Returns the thread keep-alive time, which is the amount of time
//     * that threads in excess of the core pool size may remain
//     * idle before being terminated.
//     *
//     * @param unit the desired time unit of the result
//     * @return the time limit
//     * @see #setKeepAliveTime(long, TimeUnit)
//     */
//    public long getKeepAliveTime(TimeUnit unit) {
//        return unit.convert(keepAliveTime, TimeUnit.NANOSECONDS);
//    }
//
//    /* User-level queue utilities */
//
//    /**
//     * Returns the task queue used by this executor. Access to the
//     * task queue is intended primarily for debugging and monitoring.
//     * This queue may be in active use.  Retrieving the task queue
//     * does not prevent queued tasks from executing.
//     *
//     * @return the task queue
//     */
//    public BlockingQueue<Runnable> getQueue() {
//        return workQueue;
//    }
//
//    /**
//     * Removes this task from the executor's internal queue if it is
//     * present, thus causing it not to be run if it has not already
//     * started.
//     *
//     * <p>This method may be useful as one part of a cancellation
//     * scheme.  It may fail to remove tasks that have been converted
//     * into other forms before being placed on the internal queue. For
//     * example, a task entered using {@code submit} might be
//     * converted into a form that maintains {@code Future} status.
//     * However, in such cases, method {@link #purge} may be used to
//     * remove those Futures that have been cancelled.
//     *
//     * @param task the task to remove
//     * @return {@code true} if the task was removed
//     */
//    public boolean remove(Runnable task) {
//        boolean removed = workQueue.remove(task);
//        tryTerminate(); // In case SHUTDOWN and now empty
//        return removed;
//    }
//
//    /**
//     * Tries to remove from the work queue all {@link Future}
//     * tasks that have been cancelled. This method can be useful as a
//     * storage reclamation operation, that has no other impact on
//     * functionality. Cancelled tasks are never executed, but may
//     * accumulate in work queues until worker threads can actively
//     * remove them. Invoking this method instead tries to remove them now.
//     * However, this method may fail to remove tasks in
//     * the presence of interference by other threads.
//     */
//    public void purge() {
//        final BlockingQueue<Runnable> q = workQueue;
//        try {
//            Iterator<Runnable> it = q.iterator();
//            while (it.hasNext()) {
//                Runnable r = it.next();
//                if (r instanceof Future<?> && ((Future<?>)r).isCancelled())
//                    it.remove();
//            }
//        } catch (ConcurrentModificationException fallThrough) {
//            // Take slow path if we encounter interference during traversal.
//            // Make copy for traversal and call remove for cancelled entries.
//            // The slow path is more likely to be O(N*N).
//            for (Object r : q.toArray())
//                if (r instanceof Future<?> && ((Future<?>)r).isCancelled())
//                    q.remove(r);
//        }
//
//        tryTerminate(); // In case SHUTDOWN and now empty
//    }
//
//    /* Statistics */
//
//    /**
//     * Returns the current number of threads in the pool.
//     *
//     * @return the number of threads
//     */
//    public int getPoolSize() {
//        final ReentrantLock mainLock = this.mainLock;
//        mainLock.lock();
//        try {
//            // Remove rare and surprising possibility of
//            // isTerminated() && getPoolSize() > 0
//            return runStateAtLeast(ctl.get(), TIDYING) ? 0
//                : workers.size();
//        } finally {
//            mainLock.unlock();
//        }
//    }
//
//    /**
//     * Returns the approximate number of threads that are actively
//     * executing tasks.
//     *
//     * @return the number of threads
//     */
//    public int getActiveCount() {
//        final ReentrantLock mainLock = this.mainLock;
//        mainLock.lock();
//        try {
//            int n = 0;
//            for (Worker w : workers)
//                if (w.isLocked())
//                    ++n;
//            return n;
//        } finally {
//            mainLock.unlock();
//        }
//    }
//
//    /**
//     * Returns the largest number of threads that have ever
//     * simultaneously been in the pool.
//     *
//     * @return the number of threads
//     */
//    public int getLargestPoolSize() {
//        final ReentrantLock mainLock = this.mainLock;
//        mainLock.lock();
//        try {
//            return largestPoolSize;
//        } finally {
//            mainLock.unlock();
//        }
//    }
//
//    /**
//     * Returns the approximate total number of tasks that have ever been
//     * scheduled for execution. Because the states of tasks and
//     * threads may change dynamically during computation, the returned
//     * value is only an approximation.
//     *
//     * @return the number of tasks
//     */
//    public long getTaskCount() {
//        final ReentrantLock mainLock = this.mainLock;
//        mainLock.lock();
//        try {
//            long n = completedTaskCount;
//            for (Worker w : workers) {
//                n += w.completedTasks;
//                if (w.isLocked())
//                    ++n;
//            }
//            return n + workQueue.size();
//        } finally {
//            mainLock.unlock();
//        }
//    }
//
//    /**
//     * Returns the approximate total number of tasks that have
//     * completed execution. Because the states of tasks and threads
//     * may change dynamically during computation, the returned value
//     * is only an approximation, but one that does not ever decrease
//     * across successive calls.
//     *
//     * @return the number of tasks
//     */
//    public long getCompletedTaskCount() {
//        final ReentrantLock mainLock = this.mainLock;
//        mainLock.lock();
//        try {
//            long n = completedTaskCount;
//            for (Worker w : workers)
//                n += w.completedTasks;
//            return n;
//        } finally {
//            mainLock.unlock();
//        }
//    }
//
//    /**
//     * Returns a string identifying this pool, as well as its state,
//     * including indications of run state and estimated worker and
//     * task counts.
//     *
//     * @return a string identifying this pool, as well as its state
//     */
//    public String toString() {
//        long ncompleted;
//        int nworkers, nactive;
//        final ReentrantLock mainLock = this.mainLock;
//        mainLock.lock();
//        try {
//            ncompleted = completedTaskCount;
//            nactive = 0;
//            nworkers = workers.size();
//            for (Worker w : workers) {
//                ncompleted += w.completedTasks;
//                if (w.isLocked())
//                    ++nactive;
//            }
//        } finally {
//            mainLock.unlock();
//        }
//        int c = ctl.get();
//        String rs = (runStateLessThan(c, SHUTDOWN) ? "Running" :
//                     (runStateAtLeast(c, TERMINATED) ? "Terminated" :
//                      "Shutting down"));
//        return super.toString() +
//            "[" + rs +
//            ", pool size = " + nworkers +
//            ", active threads = " + nactive +
//            ", queued tasks = " + workQueue.size() +
//            ", completed tasks = " + ncompleted +
//            "]";
//    }
//
//
//    /**获取线程工厂
//     */
//    public ThreadFactory getThreadFactory() {
//        return threadFactory;
//    }
//
//    /* Extension hooks */
//
//    /**留给子类的回调方法。
//     */
//    protected void beforeExecute(Thread t, Runnable r) { }
//
//    /**留给子类的回调方法
//     */
//    protected void afterExecute(Runnable r, Throwable t) { }
//
//    /**留给子类的回调方法
//     */
//    protected void terminated() { }
//
//
//
//
//    /* ===================================================拒绝策略===================================================== */
//
//    /**调用主线程执行任务。
//     *
//     */
//    public static class CallerRunsPolicy implements RejectedExecutionHandler {
//        public CallerRunsPolicy() { }
//
//        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
//            if (!e.isShutdown()) {
//                r.run();
//            }
//        }
//    }
//
//    /**直接丢弃
//     */
//    public static class AbortPolicy implements RejectedExecutionHandler {
//
//        public AbortPolicy() { }
//
//        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
//            throw new RejectedExecutionException("Task " + r.toString() +
//                                                 " rejected from " +
//                                                 e.toString());
//        }
//    }
//
//    /**直接丢弃任务,不做处理
//     */
//    public static class DiscardPolicy implements RejectedExecutionHandler {
//
//        public DiscardPolicy() { }
//
//
//        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
//        }
//    }
//
//
//    /**
//     * 丢弃队列最前面的任务，然后重新尝试执行任务（重复此过程
//     */
//    public static class DiscardOldestPolicy implements RejectedExecutionHandler {
//
//        public DiscardOldestPolicy() { }
//        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
//            if (!e.isShutdown()) {
//                e.getQueue().poll();
//                e.execute(r);
//            }
//        }
//    }
//}
