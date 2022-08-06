package com.padingpading.interview.jvm.classloader.d_all;

import com.padingpading.interview.jvm.classloader.b_cus.load.MyClassLoader;

/**
 * @author libin
 * @description
 * @date 2022-05-01
 */
public class Test {
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
       MyClassLoader loader1 = new MyClassLoader("classloader1");
       Class<?> class1 = loader1.loadClass("com.padingpading.interview.jvm.classloader.d_all.Sample");
        System.out.println("Class1 "+ class1.hashCode());
        Object o = class1.newInstance();
    }
}
