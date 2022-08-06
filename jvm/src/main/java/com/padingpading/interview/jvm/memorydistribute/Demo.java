package com.padingpading.interview.jvm.memorydistribute;

import org.openjdk.jol.vm.VM;

/**测试
 */
public class Demo {
    
    private String name;
    private int age;
    public static void main(String[] args) {
        System.out.println(VM.current().details());
    }
    
}
