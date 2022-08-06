package com.padingpading.interview.jvm.classloader.a_javaclassloader;



//        ThreadClassloader threadClassloader = new ThreadClassloader();
//        ClassLoader loader =  Thread.currentThread().getContextClassLoader();;
//        try {
//            //设置
//            Thread.currentThread().setContextClassLoader(targetLoader);
//            //执行方法
//            method()
//        } finally {
//            //还原
//            Thread.currentThread().setContextClassLoader(threadClassloader);
//        }
/**
 * 线程类加载器
 */
public class ThreadClassloader implements  Runnable{
    
    private Thread thread;
    
    public ThreadClassloader() {
        thread=new Thread(this);
        thread.start();
    }
    
    @Override
    public void run() {
        ClassLoader classloader = this.thread.getContextClassLoader();
        this.thread.setContextClassLoader(classloader);
        //系统类加载器
        System.out.println("class:"+classloader.getClass());
        //拓展类加载器
        System.out.println("parent:"+classloader.getParent().getClass());
    }
    
    public static void main(String[] args) {

    }
}
