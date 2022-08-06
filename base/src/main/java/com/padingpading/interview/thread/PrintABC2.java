//package com.padingpading.interview.thread;
//
//import java.util.concurrent.locks.Condition;
//import java.util.concurrent.locks.ReentrantLock;
//
///**
// * @author libin
// * @description
// * @date 2021-08-24
// */
//public class PrintABC2 {
//    private  ReentrantLock lock = new ReentrantLock();//通过JDK5中的锁来保证线程的访问的互斥
//    private  volatile static int state = 0;
//    private final static Object o = new Object();
//
//    public static void main(String[] args) {
//        ReentrantLock lock = new ReentrantLock();//通过JDK5中的锁来保证线程的访问的互斥
//        Condition condition1 = lock.newCondition();
//        Condition condition2 = lock.newCondition();
//        Condition condition3 = lock.newCondition();
//
//        new Thread(()->{
//            while (true){
//                lock.lock();
//                synchronized (o){
//                    if (state % 3 == 0) {
//                        System.out.print("a");
//                        state++;
//                        o.notifyAll();
//                    } else {
//                        try {
//                            o.wait();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//                lock.unlock();
//            }
//
//        }).start();
//        new Thread(()->{
//            while (true){
//                synchronized (o){
//                    if (state % 3 == 1) {
//                        System.out.print("b");
//                        state++;
//                        o.notifyAll();
//                    } else {
//                        try {
//                            o.wait();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//
//        }).start();
//        new Thread(()->{
//            while (true){
//                synchronized (o){
//                    if (state % 3 == 2) {
//                        System.out.print("c");
//                        state++;
//                        o.notifyAll();
//                    } else {
//                        try {
//                            o.wait();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//
//        }).start();
//
//    }
//
//    static class ThreadA extends Thread {
//        private  Object object;
//        @Override
//        public void run() {
//            while (true){
//                lock.lock();
//                if (state % 3 == 0) {
//                    System.out.print("A");
//                    state++;
//                }
//                lock.unlock();
//            }
//        }
//    }
//
//    static class ThreadB extends Thread {
//        private  Object object;
//
//        @Override
//        public void run() {
//            while (true){
//                lock.lock();
//                if (state % 3 == 1) {
//                    System.out.print("B");
//                    state++;
//                }
//                lock.unlock();
//            }
//        }
//    }
//
//    static class ThreadC extends Thread {
//        private  Object object;
//
//        @Override
//        public void run() {
//            while (true){
//                lock.lock();
//                if (state % 3 == 2) {
//                    System.out.print("C");
//                    state++;
//                }
//                lock.unlock();
//            }
//        }
//    }
//
//
//}