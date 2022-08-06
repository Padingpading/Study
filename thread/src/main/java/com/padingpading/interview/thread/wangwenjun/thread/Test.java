package com.padingpading.interview.thread.wangwenjun.thread;

/**
 * @author libin
 * @description
 * @date 2021-07-12
 */
public class Test {

    public static void main(String[] args) {
        new Thread(()->{
            String name = Thread.currentThread().getName();
            System.out.println(name);
        }).start();
    }
}
