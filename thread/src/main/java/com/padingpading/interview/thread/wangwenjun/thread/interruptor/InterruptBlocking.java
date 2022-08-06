package com.padingpading.interview.thread.wangwenjun.thread.interruptor;


import com.padingpading.interview.thread.wangwenjun.wangwenjun.chapter6.ThreadService;

/**	1、如果本线程是处于阻塞状态Wait()、join()、sleep(),它便不能核查共享变量，也就不能停止。
 若线程在阻塞状态时，调用了它的interrupt()方法，那么它的中断状态会被清除并且会收到一个InterruptedException异常。
 并且在抛出异常后立即将线程的中断标示位清除，即重新设置为false。抛出异常是为了线程从阻塞状态醒过来，
 并在结束线程前让程序员有足够的时间来处理中断请求。
 */
public class InterruptBlocking  extends Thread {
    
    private static final Object OBJECT = new Object();
    
    public static void main(String args[]) throws InterruptedException {
        //testSleep();
      //  testJoin();
        testWait();
    }
    public static void testSleep() throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                /*
                 * 如果线程阻塞，将不会去检查中断信号量stop变量，所 以thread.interrupt()
                 * 会使阻塞线程从阻塞的地方抛出异常，让阻塞线程从阻塞状态逃离出来，并
                 * 进行异常块进行 相应的处理
                 */
                Thread.sleep(10000);// 线程阻塞，如果线程收到中断操作信号将抛出异常
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted...");
                /*
                 * 如果线程在调用 Object.wait()方法，或者该类的 join() 、sleep()方法
                 * 过程中受阻，则其中断状态将被清除
                 */
                System.out.println(Thread.currentThread().isInterrupted());// false
            
                //中不中断由自己决定，如果需要真真中断线程，则需要重新设置中断位，如果
                //不需要，则不用调用
                Thread.currentThread().interrupt();
            }
        });
        thread.start();
        Thread.sleep(5000);
        System.out.println("5s");
        thread.interrupt();
    }
    
    public static void testJoin() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
        });
        Thread t2 = new Thread(() -> {
            try {
                /*
                 * 如果线程阻塞，将不会去检查中断信号量stop变量，所 以thread.interrupt()
                 * 会使阻塞线程从阻塞的地方抛出异常，让阻塞线程从阻塞状态逃离出来，并
                 * 进行异常块进行 相应的处理
                 */
                t1.join();// 线程阻塞，如果线程收到中断操作信号将抛出异常
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted...");
                /*
                 * 如果线程在调用 Object.wait()方法，或者该类的 join() 、sleep()方法
                 * 过程中受阻，则其中断状态将被清除
                 */
                System.out.println(Thread.currentThread().isInterrupted());// false
        
                //中不中断由自己决定，如果需要真真中断线程，则需要重新设置中断位，如果
                //不需要，则不用调用
                Thread.currentThread().interrupt();
            }
        });
        t1.start();
        t2.start();
        Thread.sleep(5000);
        System.out.println("5s后 打断t2的等待");
        t2.interrupt();
    }
    
    public static void testWait() throws InterruptedException {
        Thread t = new Thread(() -> {
            synchronized (OBJECT){
                try {
                    /*
                     * 如果线程阻塞，将不会去检查中断信号量stop变量，所 以thread.interrupt()
                     * 会使阻塞线程从阻塞的地方抛出异常，让阻塞线程从阻塞状态逃离出来，并
                     * 进行异常块进行 相应的处理
                     */
                    OBJECT.wait();// 线程阻塞，如果线程收到中断操作信号将抛出异常
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupted...");
                    /*
                     * 如果线程在调用 Object.wait()方法，或者该类的 join() 、sleep()方法
                     * 过程中受阻，则其中断状态将被清除
                     */
                    System.out.println(Thread.currentThread().isInterrupted());// false
        
                    //中不中断由自己决定，如果需要真真中断线程，则需要重新设置中断位，如果
                    //不需要，则不用调用
                    Thread.currentThread().interrupt();
                }
            }
        });
        t.start();
        Thread.sleep(5000);
        System.out.println("5s后 打断t的等待");
        t.interrupt();
    }
}
