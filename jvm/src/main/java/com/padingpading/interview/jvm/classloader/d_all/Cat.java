package com.padingpading.interview.jvm.classloader.d_all;

/**
 * @author libin
 * @description
 * @date 2022-05-01
 */
public class Cat {
    
    public Cat() {
        System.out.println("cat is loaded by " +this.getClass().getClassLoader());
    }
}
