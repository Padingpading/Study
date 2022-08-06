package com.padingpading.interview.thread.wangwenjun.threadlocal;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author libin
 * @description
 * @date 2022-05-07
 */
public class ThreadLocalHash {
    
    private static final int HASH_INCREMENT = 0x61c88647;
   
    public static void main(String[] args) {
        int hashcode=  0;
    
        for (int i = 0; i < 16; i++) {
            hashcode = i + HASH_INCREMENT;
            int bucket = hashcode & 15;
            System.out.println(bucket);
        }
    }
}

