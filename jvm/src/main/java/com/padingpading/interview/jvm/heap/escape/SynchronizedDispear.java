package com.padingpading.interview.jvm.heap.escape;

/**
 * @author libin
 * @description
 * @date 2022-05-15
 */
public class SynchronizedDispear {
    public static void main(String[] args) {
    }
    public  void f() {
        Object hellis = new Object();
        synchronized(hellis) {
            System.out.println(hellis);
        }
    }
}
