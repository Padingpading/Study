package com.padingpading.interview.thread.wangwenjun.mult;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;

/**
 * @author libin
 * @description callable方式实现
 * @date 2021-07-12
 */
public class MyThreadCallable {

    public static void main(String[] args) {
        MyThread call = new MyThread();
        FutureTask<String> task = new FutureTask<>(call);
        Thread t = new Thread(task);
        t.start();
        try {
            //获取当前线程的值
            System.out.println(task.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}

//实现Callable接口
class MyThread implements Callable<String> {
    @Override
    public String call() {
        String s = "使用Callable来返回值";
        return s;
    }
}


