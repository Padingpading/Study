package com.padingpading.interview.jvm.a_loading.loadtime.init.test;



/**
 * LoadClass不会导致初始化,反射会初始化。
 */
public class B_init {
    public static void main(String[] args) throws ClassNotFoundException {
        //系统类加载器
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        //不会导致初始化
        Class<?> aClass = loader.loadClass("com.padingpading.interview.jvm.a_loading.loadtime.init.test.BAnimal");
        System.out.println(aClass);
        System.out.println("=======================");
        //会导致初始化
        aClass =
                Class.forName("com.padingpading.interview.jvm.a_loading.loadtime.init.test.BAnimal");
        System.out.println(aClass);
    }
}

class BAnimal{
    static  int banimal = 0;
    static {
        System.out.println("banimal");
    }
    public static void  eat(){
        System.out.println("eat");
    }
}

