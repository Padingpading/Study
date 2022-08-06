package com.padingpading.interview.jvm.classloader.a_javaclassloader;



/**
 * 系统类加载器
 */
public class AppLoader {
    public static void main(String[] args) {
        System.out.println("*********系统类加载器************");
        //{项目}\target\classes
        System.out.println(System.getProperty("java.class.path"));
    }
}


