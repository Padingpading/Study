package com.padingpading.interview.thread.wangwenjun.thread.group;

import java.util.Arrays;

/**线程组
 *
 */
public class ThreadGroupCreate {
    public static void main(String[] args) {
        //1、创建ThreaGroup,没有指定父线程，默认是main
        ThreadGroup Tg1 = new ThreadGroup("Tg1");
        Thread t1 = new Thread(Tg1,"t1"){
            @Override
            public void run() {
                try {
                    //1、获取当前的线程组名字
                    System.out.println("t1:"+getThreadGroup().getName());//Tg1
                    //2、获取当前线程组的父线程组java.lang.ThreadGroup[name=main,maxpri=10]
                    System.out.println("t1:"+getThreadGroup().getParent());
                    //3、获取当前线程组的父线程组，活跃的线程组合线程数量
                    System.out.println("t1:"+getThreadGroup().getParent().activeCount());
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t1.start();
        
        //2、创建THread，指定父线程
        ThreadGroup threadGroup2 = new ThreadGroup(Tg1,"Tg2");
        //2.1获取当前线程组的名字 tg2
        System.out.println("tg2:"+threadGroup2.getName());
        //2.2获取当前线程组父线程的名字  tg1
        System.out.println("tg2:"+threadGroup2.getParent().getName());
        
        //3、访问同级的线程组信息，Tg1，都是main线程租的子线程组
        ThreadGroup tg3 = new ThreadGroup("tg3");
        Thread thread = new Thread(tg3,"tg3"){
            @Override
            public void run() {
                //3.1访问统计线程组的名字 :Tg1
                System.out.println("tg3:"+Tg1.getName());
                //3.1访问统计线程组的名字 :获取tg1线程组的线程
                Thread[] threads = new Thread[Tg1.activeCount()];
                //tg1有一个活跃的线程 t1
                System.out.println(Tg1.activeCount());
                Tg1.enumerate(threads);
                Arrays.asList(threads).forEach(a-> System.out.println("tg3:"+a));
            }
        };
        thread.start();
    }
}
