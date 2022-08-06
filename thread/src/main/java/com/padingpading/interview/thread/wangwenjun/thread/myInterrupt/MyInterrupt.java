package com.padingpading.interview.thread.wangwenjun.thread.myInterrupt;

/**
 * @author libin
 * @description interrupt
 * @date 2021-07-14
 */
public class MyInterrupt implements Runnable {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new MyInterrupt());
        thread.start();
        Thread.sleep(500);
        thread.interrupt();
    }

    @Override
    public void run() {
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("I am interrupted...");
            } else {
                System.out.println("I am running...");
            }
        }
    }
}
