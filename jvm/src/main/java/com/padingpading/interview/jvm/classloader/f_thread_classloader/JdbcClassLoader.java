package com.padingpading.interview.jvm.classloader.f_thread_classloader;

import java.sql.Driver;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author libin
 * @description
 * @date 2022-05-01
 */
public class JdbcClassLoader {
    
    public static void main(String[] args) {
        //设置为拓展类加载器,驱动不会被加载到。
       //  Thread.currentThread().setContextClassLoader(JdbcClassLoader.class.getClassLoader().getParent());
//        drive:class com.mysql.jdbc.Driver, laodersun.misc.Launcher$AppClassLoader@18b4aac2
//        drive:class com.mysql.fabric.jdbc.FabricMySQLDriver, laodersun.misc.Launcher$AppClassLoader@18b4aac2
        //当前线程上线文加载器为 应用类加载器
        ServiceLoader<Driver> load = ServiceLoader.load(Driver.class);
        Iterator<Driver> iterator = load.iterator();
        while (iterator.hasNext()){
            Driver driver = iterator.next();
            System.out.println("drive:"+driver.getClass()+", laoder" +driver.getClass().getClassLoader());
        }
        //应用类加载器
        System.out.println("当前线程的上线文列加载器" + Thread.currentThread().getContextClassLoader());
        //null 启动类加载器
        System.out.println("ServiceLoader上线文列加载器" + ServiceLoader.class.getClassLoader());
    }

}
