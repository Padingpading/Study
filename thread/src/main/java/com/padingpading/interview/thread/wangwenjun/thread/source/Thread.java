///*
// * Copyright (c) 1994, 2016, Oracle and/or its affiliates. All rights reserved.
// * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// */
//
//package com.padingpading.interview.thread.wangwenjun.thread.source;
//
//import sun.nio.ch.Interruptible;
//import sun.reflect.CallerSensitive;
//import sun.reflect.Reflection;
//import sun.security.util.SecurityConstants;
//
//import java.lang.ref.Reference;
//import java.lang.ref.ReferenceQueue;
//import java.lang.ref.WeakReference;
//import java.security.AccessControlContext;
//import java.security.AccessController;
//import java.security.PrivilegedAction;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//import java.util.concurrent.locks.LockSupport;
//
///**Thread
// * 实现接口Runnable,重写run方法。
// */
//public class Thread implements Runnable {
//
//    //定义registerNatives()本地方法注册系统资源
//    private static native void registerNatives();
//
//    static {
//        //在静态代码块中调用注册本地系统资源的方法
//        registerNatives();
//    }
//
//    private volatile String name;
//    private int            priority;
//    private Thread         threadQ;
//    private long           eetop;
//
//    /* Whether or not to single_step this thread. */
//    private boolean     single_step;
//
//    /* Whether or not the thread is a daemon thread. */
//    private boolean     daemon = false;
//
//    /* JVM state */
//    private boolean     stillborn = false;
//
//    /* What will be run. */
//    private Runnable target;
//
//    /* The group of this thread */
//    private ThreadGroup group;
//
//    /**上下文Classloader
//     * 便于 上层类加载器加载的核心类 能够获取到该classloader,进行队列的加载。
//     * 虽然能够能够通过Class.getClassLoader获取到类加载,但是对下层类是不可见的。
//     */
//    private ClassLoader contextClassLoader;
//
//    /* The inherited AccessControlContext of this thread */
//    private AccessControlContext inheritedAccessControlContext;
//
//    /* For autonumbering anonymous threads. */
//    private static int threadInitNumber;
//    private static synchronized int nextThreadNum() {
//        return threadInitNumber++;
//    }
//
//    /**
//     * 当前线程的Threadlocal
//     */
//    ThreadLocal.ThreadLocalMap threadLocals = null;
//
//
//    /**
//     * 父线程的 threadlocal
//     * 能够让子线程使用父线程的 threadlocal
//     */
//    ThreadLocal.ThreadLocalMap inheritableThreadLocals = null;
//
//    /*当前线程jvm栈的深度
//     */
//    private long stackSize;
//
//    /*
//     * JVM-private state that persists after native thread termination.
//     */
//    private long nativeParkEventPointer;
//
//    /*
//     * Thread ID
//     */
//    private long tid;
//
//    /**线程的序号(静态变量)
//     * 在不指定线程名称下,线程的名称为 Thread+threadSeqNumber
//     */
//    private static long threadSeqNumber;
//
//    /* Java thread status for tools,
//     * initialized to indicate thread 'not yet started'
//     */
//
//    private volatile int threadStatus = 0;
//
//
//    /**threadSeqNumber++ 同步方法
//     */
//    private static synchronized long nextThreadID() {
//        return ++threadSeqNumber;
//    }
//
//    /**
//     * The argument supplied to the current call to
//     * java.util.concurrent.locks.LockSupport.park.
//     * Set by (private) java.util.concurrent.locks.LockSupport.setBlocker
//     * Accessed using java.util.concurrent.locks.LockSupport.getBlocker
//     */
//    volatile Object parkBlocker;
//
//    /* The object in which this thread is blocked in an interruptible I/O
//     * operation, if any.  The blocker's interrupt method should be invoked
//     * after setting this thread's interrupt status.
//     */
//    private volatile Interruptible blocker;
//    private final Object blockerLock = new Object();
//
//    /* Set the blocker field; invoked via sun.misc.SharedSecrets from java.nio code
//     */
//    void blockedOn(Interruptible b) {
//        synchronized (blockerLock) {
//            blocker = b;
//        }
//    }
//
//    /**
//     */
//    public final static int MIN_PRIORITY = 1;
//
//   /**
//     */
//    public final static int NORM_PRIORITY = 5;
//
//    /**
//     */
//    public final static int MAX_PRIORITY = 10;
//
//    /**
//     */
//    public static native Thread currentThread();
//
//
//
//    /*==================================================构造函数=====================================================*/
//    /**
//     * 空构造
//     */
//    public Thread() {
//        init(null, null, "Thread-" + nextThreadNum(), 0);
//    }
//
//    /**
//     * 添加执行任务
//     */
//    public Thread(Runnable target) {
//        init(null, target, "Thread-" + nextThreadNum(), 0);
//    }
//
//    /**
//     * Creates a new Thread that inherits the given AccessControlContext.
//     * This is not a public constructor.
//     */
//    Thread(Runnable target, AccessControlContext acc) {
//        init(null, target, "Thread-" + nextThreadNum(), 0, acc, false);
//    }
//
//    /**初始化线程
//     * 线程组
//     * 执行任务
//     */
//    public Thread(ThreadGroup group, Runnable target) {
//        init(group, target, "Thread-" + nextThreadNum(), 0);
//    }
//
//    /**初始化线程名字
//     */
//    public Thread(String name) {
//        init(null, null, name, 0);
//    }
//
//    /**指定线程组,线程的名称。
//     */
//    public Thread(ThreadGroup group, String name) {
//        init(group, null, name, 0);
//    }
//
//    /**初始化线程
//     * target:执行任务。
//     * name:线程名称
//     */
//    public Thread(Runnable target, String name) {
//        init(null, target, name, 0);
//    }
//
//    /**初始化线程
//     * group:线程组
//     * target:执行任务
//     * name:线程名称
//     */
//    public Thread(ThreadGroup group, Runnable target, String name) {
//        init(group, target, name, 0);
//    }
//
//    /**分配一个新的 Thread对象，以便它具有 target作为其运行对象，
//     * 将指定的 name正如其名，以及属于该线程组由称作 group ，并具有指定的 堆栈大小。
//     */
//    public Thread(ThreadGroup group, Runnable target, String name,
//            long stackSize) {
//        init(group, target, name, stackSize);
//    }
//
//
//    /**
//     */
//    private void init(ThreadGroup g, Runnable target, String name,
//                      long stackSize) {
//        init(g, target, name, stackSize, null, true);
//    }
//
//    /**
//     * @param g
//     * @param target 任务
//     * @param name 线程名字
//     * @param stackSize 栈深度
//     * @param acc
//     * @param inheritThreadLocals 是否继承父线程的 ThreadLocals
//     */
//    private void init(ThreadGroup g, Runnable target, String name,
//                      long stackSize, AccessControlContext acc,
//                      boolean inheritThreadLocals) {
//        //线程的名称
//        if (name == null) {
//            throw new NullPointerException("name cannot be null");
//        }
//        this.name = name;
//
//        //父线程
//        Thread parent = currentThread();
//        //获取系统安全管理器
//        SecurityManager security = System.getSecurityManager();
//        //线程组为空
//        if (g == null) {
//
//            //获取的系统安全管理器不为空
//            if (security != null) {
//                //从系统安全管理器中获取一个线程分组
//                g = security.getThreadGroup();
//            }
//            //线程分组为空，则从父线程获取
//            if (g == null) {
//                g = parent.getThreadGroup();
//            }
//        }
//
//        //检查线程组的访问权限
//        g.checkAccess();
//
//        //检查权限
//        if (security != null) {
//            if (isCCLOverridden(getClass())) {
//                security.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
//            }
//        }
//
//        g.addUnstarted();
//
//        //当前线程继承父线程的相关属性
//        //线程组
//        this.group = g;
//        //线程组是否是守护线程
//        this.daemon = parent.isDaemon();
//        //父线程的优先级
//        this.priority = parent.getPriority();
//        //线程的Classloader,获取父线程ClassLoader,父线程为main线程
//        //main线程的类是应用列加载器加载,所以获取到父线程的加载器为应用类加载器。
//        if (security == null || isCCLOverridden(parent.getClass()))
//            this.contextClassLoader = parent.getContextClassLoader();
//        else
//            this.contextClassLoader = parent.contextClassLoader;
//
//        this.inheritedAccessControlContext =
//                acc != null ? acc : AccessController.getContext();
//        //Runable执行任务对象
//        this.target = target;
//        //设置线程优先级
//        setPriority(priority);
//
//        //父线程的threadlocal不为空。
//        if (inheritThreadLocals && parent.inheritableThreadLocals != null)
//            //获取父线程的Treadlocals
//            this.inheritableThreadLocals =
//                ThreadLocal.createInheritedMap(parent.inheritableThreadLocals);
//
//        //jvm栈的深度。
//        this.stackSize = stackSize;
//
//        //线程id
//        tid = nextThreadID();
//    }
//
//
//    /**强制释放cpu执行权,不会释放锁
//     */
//    public static native void yield();
//
//    /**sleep
//     */
//    public static native void sleep(long millis) throws InterruptedException;
//
//    /**sleep
//     */
//    public static void sleep(long millis, int nanos)
//            throws InterruptedException {
//        if (millis < 0) {
//            throw new IllegalArgumentException("timeout value is negative");
//        }
//
//        if (nanos < 0 || nanos > 999999) {
//            throw new IllegalArgumentException(
//                    "nanosecond timeout value out of range");
//        }
//
//        if (nanos >= 500000 || (nanos != 0 && millis == 0)) {
//            millis++;
//        }
//
//        sleep(millis);
//    }
//
//    /**
//     * Throws CloneNotSupportedException as a Thread can not be meaningfully
//     * cloned. Construct a new Thread instead.
//     *
//     * @throws  CloneNotSupportedException
//     *          always
//     */
//    @Override
//    protected Object clone() throws CloneNotSupportedException {
//        throw new CloneNotSupportedException();
//    }
//
//    /**线程开启方法 新生状态->可运行状态
//     *
//     */
//    public synchronized void start() {
//        //线程是初始化状态，则直接抛出异常
//        //死亡状态可否
//        if (threadStatus != 0)
//            throw new IllegalThreadStateException();
//        //添加当前启动的线程到线程组
//        group.add(this);
//
//        //标记线程是否已经启动
//        boolean started = false;
//        try {
//            //调用本地方法启动线程
//            start0();
//            started = true;
//        } finally {
//            //线程未启动成功
//            try {
//                if (!started) {
//                    //将线程在线程组里标记为启动失败
//                    group.threadStartFailed(this);
//                }
//            } catch (Throwable ignore) {
//            }
//        }
//    }
//
//    private native void start0();
//
//    /**
//     *
//     */
//    @Override
//    public void run() {
//        if (target != null) {
//            target.run();
//        }
//    }
//
//    /**
//     * This method is called by the system to give a Thread
//     * a chance to clean up before it actually exits.
//     */
//    private void exit() {
//        if (group != null) {
//            group.threadTerminated(this);
//            group = null;
//        }
//        /* Aggressively null out all reference fields: see bug 4006245 */
//        target = null;
//        /* Speed the release of some of these resources */
//        threadLocals = null;
//        inheritableThreadLocals = null;
//        inheritedAccessControlContext = null;
//        blocker = null;
//        uncaughtExceptionHandler = null;
//    }
//
//    /**抛出ThreadDeath对象，终止线程
//     */
//    @Deprecated
//    public final void stop() {
//        SecurityManager security = System.getSecurityManager();
//        if (security != null) {
//            checkAccess();
//            if (this != Thread.currentThread()) {
//                security.checkPermission(SecurityConstants.STOP_THREAD_PERMISSION);
//            }
//        }
//        if (threadStatus != 0) {
//            resume();
//        }
//        stop0(new ThreadDeath());
//    }
//
//    /**
//     * Throws {@code UnsupportedOperationException}.
//     *
//     * @param obj ignored
//     *
//     * @deprecated This method was originally designed to force a thread to stop
//     *        and throw a given {@code Throwable} as an exception. It was
//     *        inherently unsafe (see {@link #stop()} for details), and furthermore
//     *        could be used to generate exceptions that the target thread was
//     *        not prepared to handle.
//     *        For more information, see
//     *        <a href="{@docRoot}/../technotes/guides/concurrency/threadPrimitiveDeprecation.html">Why
//     *        are Thread.stop, Thread.suspend and Thread.resume Deprecated?</a>.
//     */
//    @Deprecated
//    public final synchronized void stop(Throwable obj) {
//        throw new UnsupportedOperationException();
//    }
//
//
//    /**interrupt():设法是中断线程，将会设置该线程的中断状态位，即设置为true，中断的结果线程是死亡、还是等待新的任务或是继续运行至下一步，
//     * 就取决于这个程序本身。线程会不时地检测这个中断标示位，以判断线程是否应该被中断（中断标示值是否为true）。
//     * 它并不像stop方法那样会中断一个正在运行的线程。
//     */
//    public void interrupt() {
//        if (this != Thread.currentThread())
//            checkAccess();
//
//        synchronized (blockerLock) {
//            Interruptible b = blocker;
//            if (b != null) {
//                interrupt0();           // Just to set the interrupt flag
//                b.interrupt(this);
//                return;
//            }
//        }
//        //调用本地方法中断线程
//        interrupt0();
//    }
//
//    /**interrupted()
//     */
//    public static boolean interrupted() {
//        return currentThread().isInterrupted(true);
//    }
//
//    /**检测当前的中断标记（类似属性的get方法）
//     */
//    public boolean isInterrupted() {
//        return isInterrupted(false);
//    }
//
//    /**
//     */
//    private native boolean isInterrupted(boolean ClearInterrupted);
//
//    /**
//     */
//    @Deprecated
//    public void destroy() {
//        throw new NoSuchMethodError();
//    }
//
//    /**判断线程是否存活,已经开始并且还为结束。
//     */
//    public final native boolean isAlive();
//
//    /**
//     */
//    @Deprecated
//    public final void suspend() {
//        checkAccess();
//        suspend0();
//    }
//
//    /**
//     */
//    @Deprecated
//    public final void resume() {
//        checkAccess();
//        resume0();
//    }
//
//    /**设置线程优先级,最大10,最小1
//     *
//     */
//    public final void setPriority(int newPriority) {
//        ThreadGroup g;
//        checkAccess();
//        if (newPriority > MAX_PRIORITY || newPriority < MIN_PRIORITY) {
//            throw new IllegalArgumentException();
//        }
//        if((g = getThreadGroup()) != null) {
//            if (newPriority > g.getMaxPriority()) {
//                //子线程的优先级不能超过
//                newPriority = g.getMaxPriority();
//            }
//            //底层设置线程native方法
//            setPriority0(priority = newPriority);
//        }
//    }
//
//    /**获取优先级
//     */
//    public final int getPriority() {
//        return priority;
//    }
//
//    /**设置线程的名称
//     */
//    public final synchronized void setName(String name) {
//        checkAccess();
//        if (name == null) {
//            throw new NullPointerException("name cannot be null");
//        }
//
//        this.name = name;
//        if (threadStatus != 0) {
//            setNativeName(name);
//        }
//    }
//
//    /**获取线程名称
//     */
//    public final String getName() {
//        return name;
//    }
//
//    /**获取线程组
//     */
//    public final ThreadGroup getThreadGroup() {
//        return group;
//    }
//
//    /**线程组的线程活跃数
//     */
//    public static int activeCount() {
//        return currentThread().getThreadGroup().activeCount();
//    }
//
//    /**
//     */
//    public static int enumerate(Thread tarray[]) {
//        return currentThread().getThreadGroup().enumerate(tarray);
//    }
//
//    /**
//     */
//    @Deprecated
//    public native int countStackFrames();
//    /*==================================================Join=====================================================*/
//
//    /**join()方法会一直等待线程超时或者终止，代码如下所示。
//     * join
//     */
//    public final void join() throws InterruptedException {
//        //阻塞,知道当前线程执行完成。
//        join(0);
//    }
//
//    /**join同步方法
//     */
//    public final synchronized void join(long millis)
//    throws InterruptedException {
//        //获取当前时间
//        long base = System.currentTimeMillis();
//        long now = 0;
//        //校验
//        if (millis < 0) {
//            throw new IllegalArgumentException("timeout value is negative");
//        }
//
//        if (millis == 0) {
//            //判断线程是否存活
//            while (isAlive()) {
//                //进入当前线程的等待队列中
//                wait(0);
//            }
//        } else {
//            //判断线程是否存活
//            while (isAlive()) {
//                //最多等待时间
//                long delay = millis - now;
//                if (delay <= 0) {
//                    break;
//                }
//                //进入当前thread的等待队列中
//                wait(delay);
//                now = System.currentTimeMillis() - base;
//            }
//        }
//    }
//
//    /**等待阻塞 纳秒级别,实际还是为毫秒。
//     */
//    public final synchronized void join(long millis, int nanos)
//    throws InterruptedException {
//        if (millis < 0) {
//            throw new IllegalArgumentException("timeout value is negative");
//        }
//        if (nanos < 0 || nanos > 999999) {
//            throw new IllegalArgumentException(
//                                "nanosecond timeout value out of range");
//        }
//        if (nanos >= 500000 || (nanos != 0 && millis == 0)) {
//            millis++;
//        }
//        join(millis);
//    }
//
//
//
//    /**
//     */
//    public static void dumpStack() {
//        new Exception("Stack trace").printStackTrace();
//    }
//
//    /**设置为守护线程
//     */
//    public final void setDaemon(boolean on) {
//        checkAccess();
//        if (isAlive()) {
//            throw new IllegalThreadStateException();
//        }
//        daemon = on;
//    }
//
//    /**是否为守护线程
//     */
//    public final boolean isDaemon() {
//        return daemon;
//    }
//
//    /**
//     */
//    public final void checkAccess() {
//        SecurityManager security = System.getSecurityManager();
//        if (security != null) {
//            security.checkAccess(this);
//        }
//    }
//
//    /**
//     */
//    public String toString() {
//        ThreadGroup group = getThreadGroup();
//        if (group != null) {
//            return "Thread[" + getName() + "," + getPriority() + "," +
//                           group.getName() + "]";
//        } else {
//            return "Thread[" + getName() + "," + getPriority() + "," +
//                            "" + "]";
//        }
//    }
//
//    /**返回上下雯的Classloader
//     */
//    @CallerSensitive
//    public ClassLoader getContextClassLoader() {
//        if (contextClassLoader == null)
//            return null;
//        SecurityManager sm = System.getSecurityManager();
//        if (sm != null) {
//            ClassLoader.checkClassLoaderPermission(contextClassLoader,
//                                                   Reflection.getCallerClass());
//        }
//        return contextClassLoader;
//    }
//
//    /** 设置线程的classloader
//     *
//     */
//    public void setContextClassLoader(ClassLoader cl) {
//        SecurityManager sm = System.getSecurityManager();
//        if (sm != null) {
//            sm.checkPermission(new RuntimePermission("setContextClassLoader"));
//        }
//        contextClassLoader = cl;
//    }
//
//    /**
//     */
//    public static native boolean holdsLock(Object obj);
//
//    private static final StackTraceElement[] EMPTY_STACK_TRACE
//        = new StackTraceElement[0];
//
//    /**
//     */
//    public StackTraceElement[] getStackTrace() {
//        if (this != Thread.currentThread()) {
//            // check for getStackTrace permission
//            SecurityManager security = System.getSecurityManager();
//            if (security != null) {
//                security.checkPermission(
//                    SecurityConstants.GET_STACK_TRACE_PERMISSION);
//            }
//            // optimization so we do not call into the vm for threads that
//            // have not yet started or have terminated
//            if (!isAlive()) {
//                return EMPTY_STACK_TRACE;
//            }
//            StackTraceElement[][] stackTraceArray = dumpThreads(new Thread[] {this});
//            StackTraceElement[] stackTrace = stackTraceArray[0];
//            // a thread that was alive during the previous isAlive call may have
//            // since terminated, therefore not having a stacktrace.
//            if (stackTrace == null) {
//                stackTrace = EMPTY_STACK_TRACE;
//            }
//            return stackTrace;
//        } else {
//            // Don't need JVM help for current thread
//            return (new Exception()).getStackTrace();
//        }
//    }
//
//    /**
//     * Returns a map of stack traces for all live threads.
//     * The map keys are threads and each map value is an array of
//     * <tt>StackTraceElement</tt> that represents the stack dump
//     * of the corresponding <tt>Thread</tt>.
//     * The returned stack traces are in the format specified for
//     * the {@link #getStackTrace getStackTrace} method.
//     *
//     * <p>The threads may be executing while this method is called.
//     * The stack trace of each thread only represents a snapshot and
//     * each stack trace may be obtained at different time.  A zero-length
//     * array will be returned in the map value if the virtual machine has
//     * no stack trace information about a thread.
//     *
//     * <p>If there is a security manager, then the security manager's
//     * <tt>checkPermission</tt> method is called with a
//     * <tt>RuntimePermission("getStackTrace")</tt> permission as well as
//     * <tt>RuntimePermission("modifyThreadGroup")</tt> permission
//     * to see if it is ok to get the stack trace of all threads.
//     *
//     * @return a <tt>Map</tt> from <tt>Thread</tt> to an array of
//     * <tt>StackTraceElement</tt> that represents the stack trace of
//     * the corresponding thread.
//     *
//     * @throws SecurityException
//     *        if a security manager exists and its
//     *        <tt>checkPermission</tt> method doesn't allow
//     *        getting the stack trace of thread.
//     * @see #getStackTrace
//     * @see SecurityManager#checkPermission
//     * @see RuntimePermission
//     * @see Throwable#getStackTrace
//     *
//     * @since 1.5
//     */
//    public static Map<Thread, StackTraceElement[]> getAllStackTraces() {
//        // check for getStackTrace permission
//        SecurityManager security = System.getSecurityManager();
//        if (security != null) {
//            security.checkPermission(
//                SecurityConstants.GET_STACK_TRACE_PERMISSION);
//            security.checkPermission(
//                SecurityConstants.MODIFY_THREADGROUP_PERMISSION);
//        }
//
//        // Get a snapshot of the list of all threads
//        Thread[] threads = getThreads();
//        StackTraceElement[][] traces = dumpThreads(threads);
//        Map<Thread, StackTraceElement[]> m = new HashMap<>(threads.length);
//        for (int i = 0; i < threads.length; i++) {
//            StackTraceElement[] stackTrace = traces[i];
//            if (stackTrace != null) {
//                m.put(threads[i], stackTrace);
//            }
//            // else terminated so we don't put it in the map
//        }
//        return m;
//    }
//
//
//    private static final RuntimePermission SUBCLASS_IMPLEMENTATION_PERMISSION =
//                    new RuntimePermission("enableContextClassLoaderOverride");
//
//    /** cache of subclass security audit results */
//    /* Replace with ConcurrentReferenceHashMap when/if it appears in a future
//     * release */
//    private static class Caches {
//        /** cache of subclass security audit results */
//        static final ConcurrentMap<WeakClassKey,Boolean> subclassAudits =
//            new ConcurrentHashMap<>();
//
//        /** queue for WeakReferences to audited subclasses */
//        static final ReferenceQueue<Class<?>> subclassAuditsQueue =
//            new ReferenceQueue<>();
//    }
//
//    /**
//     * Verifies that this (possibly subclass) instance can be constructed
//     * without violating security constraints: the subclass must not override
//     * security-sensitive non-final methods, or else the
//     * "enableContextClassLoaderOverride" RuntimePermission is checked.
//     */
//    private static boolean isCCLOverridden(Class<?> cl) {
//        if (cl == Thread.class)
//            return false;
//
//        processQueue(Caches.subclassAuditsQueue, Caches.subclassAudits);
//        WeakClassKey key = new WeakClassKey(cl, Caches.subclassAuditsQueue);
//        Boolean result = Caches.subclassAudits.get(key);
//        if (result == null) {
//            result = Boolean.valueOf(auditSubclass(cl));
//            Caches.subclassAudits.putIfAbsent(key, result);
//        }
//
//        return result.booleanValue();
//    }
//
//    /**
//     * Performs reflective checks on given subclass to verify that it doesn't
//     * override security-sensitive non-final methods.  Returns true if the
//     * subclass overrides any of the methods, false otherwise.
//     */
//    private static boolean auditSubclass(final Class<?> subcl) {
//        Boolean result = AccessController.doPrivileged(
//            new PrivilegedAction<Boolean>() {
//                public Boolean run() {
//                    for (Class<?> cl = subcl;
//                         cl != Thread.class;
//                         cl = cl.getSuperclass())
//                    {
//                        try {
//                            cl.getDeclaredMethod("getContextClassLoader", new Class<?>[0]);
//                            return Boolean.TRUE;
//                        } catch (NoSuchMethodException ex) {
//                        }
//                        try {
//                            Class<?>[] params = {ClassLoader.class};
//                            cl.getDeclaredMethod("setContextClassLoader", params);
//                            return Boolean.TRUE;
//                        } catch (NoSuchMethodException ex) {
//                        }
//                    }
//                    return Boolean.FALSE;
//                }
//            }
//        );
//        return result.booleanValue();
//    }
//
//    private native static StackTraceElement[][] dumpThreads(Thread[] threads);
//    private native static Thread[] getThreads();
//
//    /**
//     * Returns the identifier of this Thread.  The thread ID is a positive
//     * <tt>long</tt> number generated when this thread was created.
//     * The thread ID is unique and remains unchanged during its lifetime.
//     * When a thread is terminated, this thread ID may be reused.
//     *
//     * @return this thread's ID.
//     * @since 1.5
//     */
//    public long getId() {
//        return tid;
//    }
//
//    /**
//     * A thread state.  A thread can be in one of the following states:
//     * <ul>
//     * <li>{@link #NEW}<br>
//     *     A thread that has not yet started is in this state.
//     *     </li>
//     * <li>{@link #RUNNABLE}<br>
//     *     A thread executing in the Java virtual machine is in this state.
//     *     </li>
//     * <li>{@link #BLOCKED}<br>
//     *     A thread that is blocked waiting for a monitor lock
//     *     is in this state.
//     *     </li>
//     * <li>{@link #WAITING}<br>
//     *     A thread that is waiting indefinitely for another thread to
//     *     perform a particular action is in this state.
//     *     </li>
//     * <li>{@link #TIMED_WAITING}<br>
//     *     A thread that is waiting for another thread to perform an action
//     *     for up to a specified waiting time is in this state.
//     *     </li>
//     * <li>{@link #TERMINATED}<br>
//     *     A thread that has exited is in this state.
//     *     </li>
//     * </ul>
//     *
//     * <p>
//     * A thread can be in only one state at a given point in time.
//     * These states are virtual machine states which do not reflect
//     * any operating system thread states.
//     *
//     * @since   1.5
//     * @see #getState
//     */
//    public enum State {
//        //初始化状态
//        NEW,
//        //可运行状态，此时的可运行包括运行中的状态和就绪状态
//        RUNNABLE,
//        //线程阻塞状态
//        BLOCKED,
//        //等待状态
//        WAITING,
//        //超时等待状态
//        TIMED_WAITING,
//        //线程终止状态
//        TERMINATED;
//    }
//
//    /**
//     * Returns the state of this thread.
//     * This method is designed for use in monitoring of the system state,
//     * not for synchronization control.
//     *
//     * @return this thread's state.
//     * @since 1.5
//     */
//    public State getState() {
//        // get current thread state
//        return sun.misc.VM.toThreadState(threadStatus);
//    }
//
//    /*================================================异常逃逸begin=======================================================*/
//    // Added in JSR-166
//
//    /**线程异常对象捕捉器,允许获取到对应线程中的异常信息,主线程然后去做处理
//     */
//    @FunctionalInterface
//    public interface UncaughtExceptionHandler {
//        /**
//         */
//        void uncaughtException(Thread t, Throwable e);
//    }
//
//    // null unless explicitly set
//    private volatile UncaughtExceptionHandler uncaughtExceptionHandler;
//
//    // null unless explicitly set
//    private static volatile UncaughtExceptionHandler defaultUncaughtExceptionHandler;
//
//    public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
//        SecurityManager sm = System.getSecurityManager();
//        if (sm != null) {
//            sm.checkPermission(
//                new RuntimePermission("setDefaultUncaughtExceptionHandler")
//                    );
//        }
//
//         defaultUncaughtExceptionHandler = eh;
//     }
//
//    public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler(){
//        return defaultUncaughtExceptionHandler;
//    }
//
//    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
//        return uncaughtExceptionHandler != null ?
//            uncaughtExceptionHandler : group;
//    }
//
//    public void setUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
//        checkAccess();
//        uncaughtExceptionHandler = eh;
//    }
//
//    private void dispatchUncaughtException(Throwable e) {
//        getUncaughtExceptionHandler().uncaughtException(this, e);
//    }
//    /*================================================异常逃逸end=======================================================*/
//
//
//
//
//    /**
//     * Removes from the specified map any keys that have been enqueued
//     * on the specified reference queue.
//     */
//    static void processQueue(ReferenceQueue<Class<?>> queue,
//                             ConcurrentMap<? extends
//                             WeakReference<Class<?>>, ?> map)
//    {
//        Reference<? extends Class<?>> ref;
//        while((ref = queue.poll()) != null) {
//            map.remove(ref);
//        }
//    }
//
//    /**
//     *  Weak key for Class objects.
//     **/
//    static class WeakClassKey extends WeakReference<Class<?>> {
//        /**
//         * saved value of the referent's identity hash code, to maintain
//         * a consistent hash code after the referent has been cleared
//         */
//        private final int hash;
//
//        /**
//         * Create a new WeakClassKey to the given object, registered
//         * with a queue.
//         */
//        WeakClassKey(Class<?> cl, ReferenceQueue<Class<?>> refQueue) {
//            super(cl, refQueue);
//            hash = System.identityHashCode(cl);
//        }
//
//        /**
//         * Returns the identity hash code of the original referent.
//         */
//        @Override
//        public int hashCode() {
//            return hash;
//        }
//
//        /**
//         * Returns true if the given object is this identical
//         * WeakClassKey instance, or, if this object's referent has not
//         * been cleared, if the given object is another WeakClassKey
//         * instance with the identical non-null referent as this one.
//         */
//        @Override
//        public boolean equals(Object obj) {
//            if (obj == this)
//                return true;
//
//            if (obj instanceof WeakClassKey) {
//                Object referent = get();
//                return (referent != null) &&
//                       (referent == ((WeakClassKey) obj).get());
//            } else {
//                return false;
//            }
//        }
//    }
//
//
//    // The following three initially uninitialized fields are exclusively
//    // managed by class java.util.concurrent.ThreadLocalRandom. These
//    // fields are used to build the high-performance PRNGs in the
//    // concurrent code, and we can not risk accidental false sharing.
//    // Hence, the fields are isolated with @Contended.
//
//    /** The current seed for a ThreadLocalRandom */
//    @sun.misc.Contended("tlr")
//    long threadLocalRandomSeed;
//
//    /** Probe hash value; nonzero if threadLocalRandomSeed initialized */
//    @sun.misc.Contended("tlr")
//    int threadLocalRandomProbe;
//
//    /** Secondary seed isolated from public ThreadLocalRandom sequence */
//    @sun.misc.Contended("tlr")
//    int threadLocalRandomSecondarySeed;
//
//    /* Some private helper methods */
//    private native void setPriority0(int newPriority);
//    private native void stop0(Object o);
//    private native void suspend0();
//    private native void resume0();
//    private native void interrupt0();
//    private native void setNativeName(String name);
//}
