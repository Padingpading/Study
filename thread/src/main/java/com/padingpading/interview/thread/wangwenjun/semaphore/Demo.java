package com.padingpading.interview.thread.wangwenjun.semaphore;

import java.util.concurrent.Semaphore;

/**官方demo
 */
public class Demo {
    
    public static void main(String[] args) throws InterruptedException {
        Pool pool = new Pool();
        pool.putItem("s");
        System.out.println( pool.getItem());
    }

    
}

class Pool {
    private static final int MAX_AVAILABLE = 100;
    // 初始化一个信号量，设置为公平锁模式，总资源数为100个
    private final Semaphore available = new Semaphore(MAX_AVAILABLE, true);
    
    public Object getItem() throws InterruptedException {
        // 获取一个信号量
        available.acquire();
        return getNextAvailableItem();
    }
    
    public void putItem(Object x) {
        if (markAsUnused(x))
            available.release();
    }
    
    protected Object[] items = new Object[MAX_AVAILABLE];
    protected boolean[] used = new boolean[MAX_AVAILABLE];
    
    protected synchronized Object getNextAvailableItem() {
        for (int i = 0; i < MAX_AVAILABLE; ++i) {
            if (!used[i]) {
                used[i] = true;
                return items[i];
            }
        }
        return null; // not reached
    }
    
    protected synchronized boolean markAsUnused(Object item) {
        for (int i = 0; i < MAX_AVAILABLE; ++i) {
            if (item == items[i]) {
                if (used[i]) {
                    used[i] = false;
                    return true;
                } else
                    return false;
            }
        }
        return false;
    }
    
}