package com.padingpading.interview.jvm.bytecode.clazz;


public class ByteCode {
    private int a = 1;
    
    public int getA() {
        setA(1);;
        return a;
    }
    private double b = 234234;
    
    public int setA(int a) {
        this.a = a;
        return 3;
    }
}
