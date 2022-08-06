package com.padingpading.interview.jvm.heap;//package com.padingpading.interview.heap;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author libin
//public class HeapDe
// * @description
// * @date 2021-07-08
// */mo {
//
//    public static void main(String[] args) {
//        byte[] a = new byte[2000];
//        //返回Java虚拟机中的堆内存总量
//        long initialMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;
//        //返回Java虚拟机试图使用的最大堆内存量
//        long maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024;
//
//        System.out.println("-Xms : " + initialMemory + "M");
//        System.out.println("-Xmx : " + maxMemory + "M");
//        try {
//            Thread.sleep(100000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
