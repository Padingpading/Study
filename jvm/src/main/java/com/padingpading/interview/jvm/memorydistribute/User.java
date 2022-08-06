package com.padingpading.interview.jvm.memorydistribute;

import org.openjdk.jol.info.ClassLayout;

/**
 * @author libin
 * @description
 * @date 2022-05-06
 */
public class User {
    public static void main(String[] args) {
        User user=new User();
        synchronized (user){
            System.out.println(ClassLayout.parseInstance(user).toPrintable());
        }
    }
}
