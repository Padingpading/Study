package com.padingpading.interview.jvm.classloader.c_namespace;

import com.padingpading.interview.jvm.classloader.b_cus.load.MyClassLoader;


/**
 * 命名空间
 */
public class NameSpace {
    
    public static void main(String[] args) throws ClassNotFoundException {
        test();
        test1();
    }
    
    /**
     * 相同类加载器加载相同类
     */
    public static void test1() throws ClassNotFoundException {
        MyClassLoader loader1 = new MyClassLoader(
                "E:\\学习\\Interviews\\jvm\\target\\classes\\com\\padingpading\\interview\\jvm\\classloader\\");
        Class clazz1 = loader1.loadClass("Demo");
        Class clazz2 = loader1.loadClass("Demo");
        System.out.println(clazz1.hashCode());
        System.out.println(clazz2.hashCode());
    }
    /**
     * 不同类加载加载相同类
     */
    public static void test() throws ClassNotFoundException {
        MyClassLoader loader1 = new MyClassLoader(
                "E:\\学习\\Interviews\\jvm\\target\\classes\\com\\padingpading\\interview\\jvm\\classloader\\");
        Class clazz1 = loader1.loadClass("Demo");
        
        MyClassLoader loader2 = new MyClassLoader(
                "E:\\学习\\Interviews\\jvm\\target\\classes\\com\\padingpading\\interview\\jvm\\classloader\\");
        Class clazz2 = loader2.loadClass("Demo");
        System.out.println(clazz1.hashCode());
        System.out.println(clazz2.hashCode());
    }
}
