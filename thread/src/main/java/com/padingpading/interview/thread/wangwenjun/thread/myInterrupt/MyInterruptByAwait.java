package com.padingpading.interview.thread.wangwenjun.thread.myInterrupt;

/**
 * @author libin
 * @description interrupt打断阻塞的线程
 * @date 2021-07-14
 */
public class MyInterruptByAwait implements Runnable {

    private final Object object = new Object();

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new MyInterruptByAwait());
        thread.start();
        Thread.sleep(500);
        thread.interrupt();
    }

    @Override
    public void run() {
        while (true) {
            synchronized (object){
                try {
                    object.wait(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("执行中");
        }
    }
}