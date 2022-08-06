package com.padingpading.interview.jvm.stack;

/**
 * @author libin
 * @description
 * @date 2021-07-08
 */
public class StackException {
    public static void main(String[] args) {

    }

    public void method1() {

    }

    public void method2() {
        try {
            method1();
        } catch (Exception e) {
            e.getMessage();
        }
    }
}
