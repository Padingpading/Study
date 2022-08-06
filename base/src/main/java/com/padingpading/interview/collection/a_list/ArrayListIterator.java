package com.padingpading.interview.collection.a_list;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author libin
 * @description
 * @date 2022-04-24
 */
public class ArrayListIterator {

    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        list.add("e");
        list.add("f");
        list.add("g");
        list.add("h");
        list.add("i");
        list.add("g");
         Iterator<String> iterator =
                list.iterator();
         while (iterator.hasNext()) {
              String next = iterator.next();
             System.out.print(next + "\t");
        }
    }
}
