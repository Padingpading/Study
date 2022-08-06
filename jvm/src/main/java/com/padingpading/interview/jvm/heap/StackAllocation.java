package com.padingpading.interview.jvm.heap;//package com.padingpading.interview.heap;
//
///**
// * 栈上分配测试
// * -Xmx256m -Xms256m -XX:-DoEscapeAnalysis -XX:+PrintGCDetails
// */
//public class StackAllocation {
//    public static void main(String args[]) {
//        alloc();
//    }
//    class Point {
//        private int x;
//        private int y;
//    }
//    private static void alloc() {
//        System.out.println("point.x" + point.x + ";point.y" + point.y);
//    }
//}
//
//
