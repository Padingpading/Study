package com.padingpading.interview.thread.wangwenjun.thread.stopthread;

/**
 * @author libin
 * @description 通过 interrupt强制关闭。
 * 业务场景:执行一个任务,设置一个时间，如果超过这个时间还没有完成，关闭任务。
 * 实现原理:开启两个线程
 * 主线程:如果到了，还没有执行完，线程
 * 守护线程:执行任务。主线程关闭，守护线程也会关闭。
 *
 * @date 2021-07-15
 */
public class ThreadCloseInterrupt {

    private Thread executeThread;

    private boolean finished = false;

    public void execute(Runnable task) {
        executeThread = new Thread() {
            @Override
            public void run() {
                Thread runner = new Thread(task);
                runner.setDaemon(true);
                runner.start();
                try {
                    runner.join();
                    finished = true;
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
        };

        executeThread.start();
    }
    public void shutdown(long mills) {
        long currentTime = System.currentTimeMillis();
        while (!finished) {
            if ((System.currentTimeMillis() - currentTime) >= mills) {
                System.out.println("任务超时，需要结束他!");
                executeThread.interrupt();
                break;
            }

            try {
                executeThread.sleep(1);
            } catch (InterruptedException e) {
                System.out.println("执行线程被打断!");
                break;
            }
        }
        finished = false;
    }
}

