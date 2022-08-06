package com.padingpading.interview.jvm.classloader.b_cus.unload;


import com.padingpading.interview.jvm.classloader.b_cus.load.MyClassLoader;


/**
 * 卸载类
 * -XX:+TraceClassUnloading
 */
public class ClassLoaderUnload  {
    
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        MyClassLoader loader1 = new MyClassLoader("E:\\学习\\Interviews\\jvm\\target\\classes\\com\\padingpading\\interview\\jvm\\classloader\\");
        Class clazz1 = loader1.loadClass("Demo");
         Object o1 = clazz1.newInstance();
        //强制gc
        loader1= null;
        clazz1= null;
        o1= null;
        System.gc();
        
        loader1 = new MyClassLoader("E:\\学习\\Interviews\\jvm\\target\\classes\\com\\padingpading\\interview\\jvm\\classloader\\");
        clazz1 = loader1.loadClass("Demo");
        o1 = clazz1.newInstance();
        System.out.println(loader1);
        System.out.println(clazz1);
        System.out.println(o1);
    }
}
