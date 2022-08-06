package com.padingpading.interview.string;

/**
 * @author libin
 * @description
 * @date 2022-05-15
 */
public class B_Str_StrBui {
    
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        method1(200000);
        long end = System.currentTimeMillis();
        System.out.println(end-start);
    
        long start1 = System.currentTimeMillis();
        method1(200000);
        long end1 = System.currentTimeMillis();
        System.out.println(end1-start1);
    }
    
    public static void method1(int highLevel) {
        String src = "";
        for (int i = 0; i < highLevel; i++) {
            src += "a"; // 每次循环都会创建一个StringBuilder对象和String对象
        }
    }
    public static void method2(int highLevel) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < highLevel; i++) {
            sb.append("a");
        }
    }
}
