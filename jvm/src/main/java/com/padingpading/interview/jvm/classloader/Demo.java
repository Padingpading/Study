package com.padingpading.interview.jvm.classloader;

public class Demo {
    
    Demo demo;
    
    public void setDemo(Object o) {
        this.demo = (Demo)o;
    }
    
    
    public static void main(String[] args) {
        final ClassLoader classLoader =
                Demo.class.getClassLoader();
        System.out.println(classLoader);
        final Class<? extends ClassLoader> aClass =
                classLoader.getClass();
        final ClassLoader classLoader1 =
                aClass.getClassLoader();
        System.out.println(classLoader1);
    }

}
