package com.padingpading.interview.jvm.stack;

/**
 * @author libin
 * @description
 * @date 2021-07-07
 */
public class DynamicLinkTest {

    public static void main(String[] args) {

    }
    private int num = 10;

    public void methodA() {
        System.out.println("methodA");
    }

    public void methodB() {
        System.out.println("methodB");
        methodA();
        num++;
    }

}
