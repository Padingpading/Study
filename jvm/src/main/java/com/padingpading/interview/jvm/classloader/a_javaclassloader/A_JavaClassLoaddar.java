package com.padingpading.interview.jvm.classloader.a_javaclassloader;


/**
 * java的类加载器
 */
public class A_JavaClassLoaddar {
    
    public static void main(String[] args) {
        final Thread thread1 = new Thread();
        ClassLoader thread = Thread.currentThread().getContextClassLoader();
        System.out.println(thread);

        //系统类加载器
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        System.out.println(systemClassLoader);//sun.misc.Launcher$AppClassLoader@18b4aac2
        
        //拓展类加载器
        ClassLoader extClassLoader = systemClassLoader.getParent();//
        System.out.println(extClassLoader);//sun.misc.Launcher$ExtClassLoader@4554617c
        
        //试图bootstrap classloader，获取不到引导类加载器
        ClassLoader bootstrapClassLoader = extClassLoader.getParent();
        System.out.println(bootstrapClassLoader);//null
        
        //获取用户自定义类的加载器:默认系统类加载器加载
        ClassLoader classLoader = A_JavaClassLoaddar.class.getClassLoader();
        System.out.println(classLoader);//sun.misc.Launcher$AppClassLoader@18b4aac2
        
        //String类使用引导类加载器加载,java的核心类库通过引导类加载器加载。
        ClassLoader classLoader1 = String.class.getClassLoader();
        System.out.println(classLoader1);//null
    }
}
