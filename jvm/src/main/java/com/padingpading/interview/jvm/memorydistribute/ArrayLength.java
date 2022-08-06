package com.padingpading.interview.jvm.memorydistribute;

import org.openjdk.jol.info.ClassLayout;

/**对象头的数组长度
 */
public class ArrayLength {
    public static void main(String[] args) {
        User[] user=new User[2];
        //查看对象的内存布局
        System.out.println(ClassLayout.parseInstance(user).toPrintable());
    }
}
