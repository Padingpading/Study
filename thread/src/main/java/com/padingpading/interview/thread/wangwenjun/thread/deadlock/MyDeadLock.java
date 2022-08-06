package com.padingpading.interview.thread.wangwenjun.thread.deadlock;

/**死锁
 */
public class MyDeadLock {
    public static void main(String[] args) {
        Object o1 = new Object();
        Object o2 = new Object();
        Thread t1 = new Thread(() -> {
            synchronized (o1) {
                try {
                    Thread.sleep(1000);
                    synchronized (o2) {
                        System.out.println("hahahahah");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        Thread t2 = new Thread(() -> {
            synchronized (o2) {
                try {
                    Thread.sleep(1000);
                    synchronized (o1) {
                        System.out.println("hahahahah");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t1.start();
        t2.start();
    }
}
