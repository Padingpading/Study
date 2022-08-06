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

import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**满足 执行的任务和返回的结果 合二为一
 * Runnable:执行的任务
 * Future:返回的结果
 */
public interface RunnableFuture<V> extends Runnable, Future<V> {
    
    void run();
}
