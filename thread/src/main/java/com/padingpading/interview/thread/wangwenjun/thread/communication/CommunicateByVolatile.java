package com.padingpading.interview.thread.wangwenjun.thread.communication;

/**
 * @author libin
 * @description 生产者、消费者 依次执行。
 * @date 2021-07-19
 */
public class CommunicateByVolatile {

    private volatile boolean flag = true;

    public void producer() {
        while (true) {
            if (flag) {
                System.out.println("P");
                flag = false;
            }
        }
    }

    public void consume() {
        while (true) {
            if (!flag) {
                System.out.println("C");
                flag = true;
            }
        }
    }

    public static void main(String[] args) {
        CommunicateByVolatile communicate = new CommunicateByVolatile();
        new Thread() {
            @Override
            public void run() {
                communicate.consume();
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                communicate.producer();
            }
        }.start();
    }
}
