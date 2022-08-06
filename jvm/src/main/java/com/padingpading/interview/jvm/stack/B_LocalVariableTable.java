package com.padingpading.interview.jvm.stack;

/**
 */
public class B_LocalVariableTable {
    
    private B_LocalVariableTable b_localVariableTable;
    public static void main(String[] args) {
        B_LocalVariableTable stackTest = new B_LocalVariableTable();
        stackTest.methodA();
    }

    public void methodA() {
        int i = 20;
        int d = 10;
        methodB();
    }

    public void methodB() {
        int i = 20;
        long d = 10L;
        byte b = 30;
        B_LocalVariableTable b_localVariableTable = new B_LocalVariableTable();
        this.b_localVariableTable = b_localVariableTable;
    }

    public static void test(int num) {
        int i = num;
        B_LocalVariableTable stackTest = new B_LocalVariableTable();
        stackTest.methodA();
    }
}
