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

package com.padingpading.interview.thread.wangwenjun.threadpool.sourcecode;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**提供了处理任务的方法。
 *	1、提交任务，提交多个任务，
 * 	2、提交任务，通过futrue去管理结果，。
 * 	3、提交有返回值的任务，和没有返回值的任务，
 */
public interface ExecutorService extends Executor {

    /**平滑的关闭ExecutorService，
     * 当此方法被调用时，
     * ExecutorService停止接收新的任务并且等待已经提交的任务（包含提交正在执行和提交未执行）执行完成。
     * 当所有提交任务执行完毕，线程池即被关闭。
     */
    void shutdown();

    /**
     */
    List<Runnable> shutdownNow();

    /**
     */
    boolean isShutdown();

    /**
     */
    boolean isTerminated();

    /**
     */
    boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException;
    
    /*=======================================================submit====================================================*/

    /**有返回值得任务提交方法。
     * Future.get()获取结果。
     */
    <T> Future<T> submit(Callable<T> task);
    
    /**没有返回值的任务提交
     * Future.get() 返回为空。
     */
    Future<?> submit(Runnable task);

    /**返回给定值的任务提交
     * 返回指定结果result
     */
    <T> Future<T> submit(Runnable task, T result);



    /**
     */
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
        throws InterruptedException;

    /**
     */
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                  long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     */
    <T> T invokeAny(Collection<? extends Callable<T>> tasks)
        throws InterruptedException, ExecutionException;

    /**
     */
    <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                    long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}
