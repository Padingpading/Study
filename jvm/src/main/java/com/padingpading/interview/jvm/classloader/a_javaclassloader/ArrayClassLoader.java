package com.padingpading.interview.jvm.classloader.a_javaclassloader;

/**
 * @author libin
 * @description
 * @date 2022-04-30
 */
public class ArrayClassLoader {
    
    public static void main(String[] args) {
        String[] str = new String[2];
        //null
        System.out.println(str.getClass().getClassLoader());
        //null
        int[] ints = new int[2];
        System.out.println(ints.getClass().getClassLoader());

    }

}
