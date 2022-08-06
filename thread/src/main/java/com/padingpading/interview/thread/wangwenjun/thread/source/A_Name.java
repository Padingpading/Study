package com.padingpading.interview.thread.wangwenjun.thread.source;

/**
 * 线程名称
 */
public class A_Name {
    
    public static void main(String[] args) {
        //主线程名称
        Thread mainThread = Thread.currentThread();
        System.out.println(mainThread.getName());
        
        //非主线程名称
        new Thread(()->{
            Thread thread  = Thread.currentThread();
            System.out.println(thread.getName());
        }).start();
    }
    
}
