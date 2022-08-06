package com.padingpading.interview.thread.wangwenjun.lock.condition;

import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/**
 * 有界容器
 */
public class BoundContainer {
    
    private String[] ele = new String[10];
    
    private Lock lock = new ReentrantLock();
    
    private Condition notEmpty = lock.newCondition();
    
    private Condition notFull = lock.newCondition();
    
    //已有的元素
    private int elementCount;
    
    private int putIndex;
    
    private int takeIndex;
    
    public void put(String element) throws InterruptedException {
        lock.lock();
        try {
            while (elementCount == ele.length) {
                notFull.await();
            }
            //有位子了
            ele[putIndex] = element;
            //已经到最后一个元素了
            if (++putIndex == ele.length) {
                //重置位置
                putIndex = 0;
            }
            elementCount++;
            System.out.println("put method:" + Arrays.toString(ele));
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }
    
    public String take() throws InterruptedException {
        lock.lock();
        try {
            while (elementCount == 0) {
                notEmpty.await();
            }
            String element = ele[takeIndex];
            ele[takeIndex] = null;
            if (++takeIndex == ele.length) {
                takeIndex = 0;
            }
            --elementCount;
            System.out.println("take method:" + Arrays.toString(ele));
            notFull.signal();
            return element;
        } finally {
            lock.unlock();
        }
    }
    
    public static void main(String[] args) {
        BoundContainer boundContainer = new BoundContainer();
        IntStream.range(0,10).forEach(i->new Thread(()->{
            try {
                boundContainer.put("hello");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start());
    
        IntStream.range(0,10).forEach(i->new Thread(()->{
            try {
                String take = boundContainer.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start());
        
    }
    
}
