package com.padingpading.interview.jvm.memorydistribute;

import org.openjdk.jol.info.ClassLayout;

/**
 * @author libin
 * @description
 * @date 2022-05-06
 */
public class MarkHashCode {
    public static void main(String[] args) {
        User user=new User();
        //打印内存布局
        System.out.println(ClassLayout.parseInstance(user).toPrintable());
        //计算hashCode
        System.out.println(user.hashCode());
        //再次打印内存布局
        System.out.println(ClassLayout.parseInstance(user).toPrintable());
    }
}
