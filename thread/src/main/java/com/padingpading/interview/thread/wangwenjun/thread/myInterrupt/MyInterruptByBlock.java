package com.padingpading.interview.thread.wangwenjun.thread.myInterrupt;

/**
 * @author libin
 * @description interrupt打断阻塞的线程
 * @date 2021-07-14
 */
public class MyInterruptByBlock implements Runnable {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new MyInterruptByBlock());
        thread.start();
        Thread.sleep(500);
        thread.interrupt();
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("try:" + Thread.currentThread().isInterrupted());
                Thread.sleep(100000);
            } catch (InterruptedException e) {
                //会接收到异常,但是标志位不会变为true。
                e.printStackTrace();
                System.out.println("catch:" + Thread.currentThread().isInterrupted());
            }
        }
    }
}