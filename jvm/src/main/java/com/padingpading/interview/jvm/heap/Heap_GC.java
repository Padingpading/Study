package com.padingpading.interview.jvm.heap;

import java.util.ArrayList;
import java.util.List;

/**
 * @author libin
 * @description
 * @date 2022-05-15
 */
public class Heap_GC {
    
    public static void main(String[] args) {
        int i = 0;
        try {
            List<String> list = new ArrayList<>();
            String a = "mogu blog";
            while(true) {
                list.add(a);
                a = a + a;
                i++;
            }
        }catch (Exception e) {
            e.getStackTrace();
        }
    }

}
