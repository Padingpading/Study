package com.padingpading.interview.jvm.classloader.a_javaclassloader;


import java.net.URL;
import java.security.Provider;

/**
 * 引导类加载器
 */
public class BootstrapLoader {
    public static void main(String[] args) {
        System.out.println("*********启动类加载器************");
        //获取加载路径属性
//        D:\Java\jdk1.8.0_101\jre\lib\resources.jar;
//        D:\Java\jdk1.8.0_101\jre\lib\rt.jar;
//        D:\Java\jdk1.8.0_101\jre\lib\sunrsasign.jar;
//        D:\Java\jdk1.8.0_101\jre\lib\jsse.jar;
//        D:\Java\jdk1.8.0_101\jre\lib\jce.jar;
//        D:\Java\jdk1.8.0_101\jre\lib\charsets.jar;
//        D:\Java\jdk1.8.0_101\jre\lib\jfr.jar;
//        D:\Java\jdk1.8.0_101\jre\classes  可以将.class文件放到该目录下,让启动列加载器加载
        System.out.println(System.getProperty("sun.boot.class.path"));
        // 获取BootstrapClassLoader 能够加载的API的路径
        URL[] urls = sun.misc.Launcher.getBootstrapClassPath().getURLs();
        for (URL url : urls) {
            System.out.println(url.toExternalForm());
        }
//        // 从上面路径中，随意选择一个类，来看看他的类加载器是什么：得到的是null，说明是  根加载器
        ClassLoader classLoader = Provider.class.getClassLoader();
        System.out.println(classLoader);
        //*********启动类加载器************
        //file:/D:/Java/jdk1.8.0_101/jre/lib/resources.jar
        //file:/D:/Java/jdk1.8.0_101/jre/lib/rt.jar
        //file:/D:/Java/jdk1.8.0_101/jre/lib/sunrsasign.jar
        //file:/D:/Java/jdk1.8.0_101/jre/lib/jsse.jar
        //file:/D:/Java/jdk1.8.0_101/jre/lib/jce.jar
        //file:/D:/Java/jdk1.8.0_101/jre/lib/charsets.jar
        //file:/D:/Java/jdk1.8.0_101/jre/lib/jfr.jar
        //file:/D:/Java/jdk1.8.0_101/jre/classes
        //null
        //应用加载器的变更
        System.out.println(System.getProperty("java.system.class.loader"));
    }
}
