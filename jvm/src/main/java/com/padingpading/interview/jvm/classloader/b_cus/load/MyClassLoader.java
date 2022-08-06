package com.padingpading.interview.jvm.classloader.b_cus.load;


import java.io.*;


/**
 * 自定义类加载器,指定特定位置。
 */
public class MyClassLoader extends ClassLoader {
    
    private String byteCodePath;
    
    /**
     * 默认的系统类加载器为父类加载器
     */
    public MyClassLoader(String byteCodePath) {
        this.byteCodePath = byteCodePath;
    }
    
    /**
     * 传入父类加载器
     */
    public MyClassLoader(ClassLoader paren, String byteCodePath) {
        super(paren);
        this.byteCodePath = byteCodePath;
    }
    
    /**
     * loadcalss()方法内部会调用该方法。
     */
    @Override
    protected Class<?> findClass(String className) {
        System.out.println("类加载器 加载");
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;
        try {
            
            //获取字节码文件的完整路径
            String fileName = byteCodePath + className + ".class";
            //获取一个输入流
            bis = new BufferedInputStream(new FileInputStream(fileName));
            //获取一个输出流
            baos = new ByteArrayOutputStream();
            //具体读入数据并写出的过程
            int len;
            byte[] data = new byte[1024];
            while ((len = bis.read(data)) != -1) {
                baos.write(data, 0, len);
            }
            //获取内存中的完整的字节数组的数据
            byte[] byteCodes = baos.toByteArray();
            //调用defineClass()，将字节数组的数据转换为Class的实例。
            Class clazz = defineClass(null, byteCodes, 0, byteCodes.length);
            return clazz;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public static void main(String[] args) {
        MyClassLoader loader = new MyClassLoader(
                "E:\\学习\\Interviews\\jvm\\target\\classes\\com\\padingpading\\interview\\jvm\\classloader\\");
        try {
            //因为当前类在编译路径下,所以由系统类加载器加载。
            // Class clazz = loader.loadClass("com.padingpading.interview.jvm.classloader.Demo");
            //系统类加载器找不到,由当前类加载器加载。
            Class clazz = loader.loadClass("Demo");
            System.out.println("加载此类的类的加载器为：" + clazz.getClassLoader().getClass()
                    .getName());//com.dsh.jvmp2.chapter04.java2.MyClassLoader
            System.out.println("加载当前Demo1类的类的加载器的父类加载器为：" + clazz.getClassLoader().getParent().getClass()
                    .getName());//sun.misc.Launcher$AppClassLoader
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
