package com.padingpading.interview.thread.wangwenjun.thread.stopthread;

/**
 * @author libin
 * @description 通过标志位关闭
 * @date 2021-07-15
 */
public class ThreadCloseFlag {

    private static class Worker extends Thread {

        private volatile boolean start = true;

        @Override
        public void run() {
            while (start) {
                //
            }
            System.out.println("结束");
        }

        public void shutdown() {
            this.start = false;
        }
    }

    public static void main(String[] args) {
        Worker worker = new Worker();
        worker.start();
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        worker.shutdown();
    }
}
