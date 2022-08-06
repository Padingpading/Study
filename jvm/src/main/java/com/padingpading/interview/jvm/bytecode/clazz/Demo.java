package com.padingpading.interview.jvm.bytecode.clazz;

/**
 * 字节码文件
 */
public class Demo {
    private int num = 1;
    
    public int add() {
        num = num + 2;
        return num;
    }
}

