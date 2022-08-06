package com.padingpading.interview.jvm.classloader.d_all;

/**
 * @author libin
 * @description
 * @date 2022-05-01
 */
public class Sample {
    
    public Sample() {
        System.out.println("Sample is loaded by " +this.getClass().getClassLoader());
        new Cat();
    }
}
