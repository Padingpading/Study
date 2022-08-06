package com.padingpading.interview.thread.wangwenjun.thread.group;

import java.util.Arrays;

/**线程组
 *
 */
public class ThreadGroupApi {
    public static void main(String[] args) {
        //1、创建ThreaGroup,没有指定父线程，默认是main
        ThreadGroup tg1 = new ThreadGroup("tg1");
        Thread t1 = new Thread(tg1,"t1"){
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        };
        t1.start();
        
        ThreadGroup tg2 = new ThreadGroup(tg1,"tg2");
        Thread thread = new Thread(tg2,"tg2"){
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                
            }
        };
        thread.start();
        //1、activeCount()  返回此线程组及其子组中活动线程数的估计  --> 2
        System.out.println(tg1.activeCount());
        //2、activeGroupCount()  查看t1线程组的线程组  --> 1
        System.out.println(tg1.activeGroupCount());
        //3、checkAccess()当前运行的线程是否，有权限修改当前线程组，
        tg2.checkAccess();
        //4、destroy()，销毁当前线程组和子线程组，如果线程组不为空，获取已经被destro，则会抛出一场
        // tg1.destroy();
        System.out.println("=========================");
        //5、enumerate(Thread[] list) 将此线程组及其子组中的每个活动线程复制到指定的数组中
        Thread[] ts1 = new Thread[tg1.activeCount()];
        tg1.enumerate(ts1);
        Arrays.asList(ts1).forEach(System.out::println);
        System.out.println("=========================");
        //6、enumerate(Thread[] list, boolean recurse)，复制到该线程组中每个活动子组的指定数组引用,递归拿全部的线程
        Thread[] ts2 = new Thread[tg1.activeCount()];
        tg1.enumerate(ts2,false);
        //没有打印tg2的线程
        Arrays.asList(ts2).forEach(a-> System.out.println("ts2:"+a));
        
        //7、interrupt() 中断此线程组中的所有线程,
        //tg1.interrupt();
        //8、setDaemon(boolean daemon),线程组的中的线程全部结束后，当前的线程组就会被destory
        tg1.setDaemon(true);
    }
}
