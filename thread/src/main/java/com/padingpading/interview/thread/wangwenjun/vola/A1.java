package com.padingpading.interview.thread.wangwenjun.vola;

import org.openjdk.jol.info.ClassLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author libin
 * @description
 * @date 2022-05-08
 */
public class A1 {
    public static void main(String[] args) throws Exception {
        //延时产生可偏向对象
        Thread.sleep(5000);
        
        //创造100个偏向线程t1的偏向锁
        List<A> listA = new ArrayList<>();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                A a = new A();
                synchronized (a) {
                    listA.add(a);
                }
            }
            try {
                //为了防止JVM线程复用，在创建完对象后，保持线程t1状态为存活
                Thread.sleep(100000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t1.start();
        
        //睡眠3s钟保证线程t1创建对象完成
        Thread.sleep(3000);
        System.out.println("打印t1线程，list中第20个对象的对象头：");
        System.out.println((ClassLayout.parseInstance(listA.get(19)).toPrintable()));
        
        //创建线程t2竞争线程t1中已经退出同步块的锁
        Thread t2 = new Thread(() -> {
            //这里面只循环了30次！！！
            for (int i = 0; i < 30; i++) {
                A a = listA.get(i);
                synchronized (a) {
                    //分别打印第19次和第20次偏向锁重偏向结果
                    if (i <= 19) {
                        System.out.println("第" + (i + 1) + "次偏向结果");
                        System.out.println((ClassLayout.parseInstance(a).toPrintable()));
                    }
                }
            }
            try {
                Thread.sleep(10000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t2.start();
        
        Thread.sleep(3000);
        System.out.println("打印list中第11个对象的对象头：");
        System.out.println((ClassLayout.parseInstance(listA.get(10)).toPrintable()));
        System.out.println("打印list中第26个对象的对象头：");
        System.out.println((ClassLayout.parseInstance(listA.get(25)).toPrintable()));
        System.out.println("打印list中第41个对象的对象头：");
        System.out.println((ClassLayout.parseInstance(listA.get(40)).toPrintable()));
    }
}

