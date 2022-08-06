
package com.padingpading.interview.thread.wangwenjun.threadpool.sourcecode;


/**将任务的提交与任务的执行分离
 *任务的提交者和任务的执行者分离。
 * Executor.execute(()->{});执行任务。
 * 使用者不需要下列方式去执行
 * new Thread(()->{}).start()
 *
 *模式:生产者,线程池,消费者。
 *生产者:只需要提供任务,runable。
 *线程池;提供任务的分配。
 *消费者:Worker对象。
 */
public interface Executor {

    /**执行
     */
    void execute(Runnable command);
}
