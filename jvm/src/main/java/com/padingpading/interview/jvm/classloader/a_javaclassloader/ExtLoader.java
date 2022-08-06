package com.padingpading.interview.jvm.classloader.a_javaclassloader;


import sun.security.ec.CurveDB;

/**
 * 拓展类加载器
 */
public class ExtLoader {
    public static void main(String[] args) {
        System.out.println("*********拓展类类加载器************");
        // {jdk}\jre\lib\ext;
        // C:\WINDOWS\Sun\Java\lib\ext
        System.out.println(System.getProperty("java.ext.dirs"));
        //获取拓展类加载器
     //   String property = System.getProperty("java.ext.dirs");
//        for (String path : property.split(";")) {
//            System.out.println(path);
//        }
//        ClassLoader classLoader = CurveDB.class.getClassLoader();
//        System.out.println(classLoader);
        //    *********拓展类加载器************
        //D:\Java\jdk1.8.0_101\jre\lib\ext
        //C:\Windows\Sun\Java\lib\ext
        //sun.misc.Launcher$ExtClassLoader@7adf9f5f
    }
}


