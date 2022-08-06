package com.padingpading.interview.jvm.stack;

/**
 * @author libin
 * @description
 * @date 2021-07-06
 */
public class A_StackDeep {
    public static void main(String[] args) {
        method01();
    }

    private static int method01() {
        System.out.println("方法1的开始");
        int i = method02();
        System.out.println("方法1的结束");
        return i;
    }

    private static int method02() {
        System.out.println("方法2的开始");
        int i = method03();
        ;
        System.out.println("方法2的结束");
        return i;
    }

    private static int method03() {
        System.out.println("方法3的开始");
        int i = 30;
        System.out.println("方法3的结束");
        return i;
    }
}
