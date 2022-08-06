package com.padingpading.interview.thread.wangwenjun.thread.group;

import java.util.Arrays;

/**
 * 线程组
 */
public class Group {
    public static void main(String[] args) {
        Thread t = new Thread() {
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        System.out.println(threadGroup.activeCount());
        Thread[] threads = new Thread[threadGroup.activeCount()];
        threadGroup.enumerate(threads);
        Arrays.asList(threads).forEach(System.out::println);
    }
}
