package com.padingpading.interview.thread.wangwenjun.thread.join;

/**
 * @author libin
 * @description
 * @date 2022-05-05
 */
public class JoinTets {
    
    public static void main(String[] args) throws InterruptedException {
    
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                    System.out.println("先执行");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        thread.join(5000);
        System.out.println("执行完毕");
    }
}
