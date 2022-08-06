package com.padingpading.interview.thread.wangwenjun.lock.condition;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*连个线程从容器中获取元素。
 *1、为空的时候,
 *  put线程可以执行。
 *  get线程阻塞。
 *2、满的时候
 *  put线程阻塞。
 *  get线程可以执行。
 */
class BoundedBuffer {
    final Lock lock = new ReentrantLock();
    final Condition notFull  = lock.newCondition();
    final Condition notEmpty = lock.newCondition();
    
    final Object[] items = new Object[100];
    int putptr, takeptr, count;
    
    public void put(Object x) throws InterruptedException {
        lock.lock();
        try {
            while (count == items.length)
                notFull.await();
            items[putptr] = x;
            if (++putptr == items.length) putptr = 0;
            ++count;
            //通知take元素
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }
    
    public Object take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0)
                notEmpty.await();
            Object x = items[takeptr];
            if (++takeptr == items.length) takeptr = 0;
            --count;
            notFull.signal();
            return x;
        } finally {
            lock.unlock();
        }
    }
}
