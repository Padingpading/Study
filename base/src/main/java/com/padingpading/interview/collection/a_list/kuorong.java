package com.padingpading.interview.collection.a_list;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * @author libin
 * @description
 * @date 2022-04-24
 */
public class kuorong {

//    第一次 扩容 取最小容量1    最后容量1
//    第二次 扩容 取最小容量2    最后容量2
//    第三次 扩容   2+1     最后容量3
//    第四次 扩容   3+1     最后容量4
//    第五次 扩容   4+2     最后容量6
//    第六次 无扩容  最后容量 6
//    第七次 扩容    6+3  最后容量9
//    第八次 无扩容   最后容量9
//    第九次 无扩容   最后容量9
//    第十次 扩容  最后容量 9+4 = 13
    public static void main(String[] args) {
        List<String> list = new ArrayList<>(0);
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
    }
}
