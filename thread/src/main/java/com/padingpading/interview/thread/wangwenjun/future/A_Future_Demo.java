package com.padingpading.interview.thread.wangwenjun.future;


import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author libin
 * @description
 * @date 2022-05-09
 */
public class A_Future_Demo {
    
    public static void main(String[] args) {
        Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                return  new Random().nextInt(500);
            }
        };
        FutureTask<Integer> futureTask = new FutureTask(callable);
        
        new Thread(futureTask).start();
        System.out.println("thread start");
        try {
            int o = futureTask.get();
            System.out.println(o);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

}
