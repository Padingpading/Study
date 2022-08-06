package com.padingpading.interview.jvm.stringtable;


import java.util.TreeMap;

public class InternTets {
        public static void main(String[] args) {
            new Runnable(){
                @Override
                public void run() {

                }
            };
            TreeMap treeMap = new TreeMap();
            treeMap.put(null,"23");
            treeMap.put(1,"23");
            treeMap.put(1,"123");
            System.out.println(treeMap.get(1));
            System.out.println(treeMap.get(null));
        }
}
