package com.padingpading.interview.jvm.a_loading.loadtime.init;




/**
 * 加载数组
 */
public class InitArray {
    public static void main(String[] args) {
    ArrayTest[] arrayTests = new ArrayTest[10];
    }
}

class ArrayTest{
    static{
        System.out.println("array test");
    }
}
