package com.padingpading.interview.jvm.a_loading.loadtime.init.single;

/**
 * @author libin
 * @description
 * @date 2022-04-30
 */
public class SingleTest {
    
    public static void main(String[] args) {
        //1、类不存在,加载Singleton
        Singleton singleton = Singleton.getInstance();
        //1
        System.out.println(Singleton.counter1);
        //0
        System.out.println(Singleton.counter2);
    }
    
}

class Singleton {
    
    //2、链接-阶段赋初值=0
    //4、初始化阶段 赋值
    public static int counter1;
    
    //3、链接-阶段赋初值 null
    //4、初始化阶段,创建对象,发现已加载,已初始化.
    private static Singleton singleton = new Singleton();
    
    private Singleton() {
        //5、赋值
        //赋值=1
        counter1++;
        //赋值=1
        counter2++;
    }
    
    //2、链接-阶段赋初值 0
    //6、初始化counter=0
    public static int counter2 = 0;
    
    public static Singleton getInstance() {
        return singleton;
    }
}
