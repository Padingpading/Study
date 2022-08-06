package com.padingpading.interview.thread.wangwenjun.wangwenjun.chapter4;

/***************************************
 * @author:Alex Wang
 * @Date:2017/2/17 QQ:532500648
 * QQ交流群:286081824
 ***************************************/
public class DaemonThread {

    public static void main(String[] args) throws InterruptedException {
        Thread main = Thread.currentThread();
        Thread thread = new Thread(()->{
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.interrupt();
    }
}

/**
 * A<---------------------------------->B
 *  ->daemonThread(health check)
 * */