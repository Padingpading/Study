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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**异步计算的结果
 * 1、判断异步任务是否完成。
 * 2、等待异步任务的结果。
 * 3、获取异步任务的结果。
 */
public interface Future<V> {

    /**取消任务
     * cancel失败
     * 1、任务已经执行完成了
     * 2、任务已经被取消过了
     * 3、任务因为某种原因不能被取消
     * 其它情况下，cancel操作将返回true。
     */
    boolean cancel(boolean mayInterruptIfRunning);

    /**任务是否被取消。
     */
    boolean isCancelled();

    /**如果一个任务已经结束, 则返回true。注意, 这里的任务结束包含了以下三种情况:
     * 任务正常执行完毕
     * 任务抛出了异常
     * 任务已经被取消
     */
    boolean isDone();

    /**返回future异步执行任务的结果
     * 如果任务还没有完成,get方法进入阻塞状态。
     */
    V get() throws InterruptedException, ExecutionException;

    /**可超时返回future异步执行任务的结果
     * 超时抛出TimeoutException。
     */
    V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}
