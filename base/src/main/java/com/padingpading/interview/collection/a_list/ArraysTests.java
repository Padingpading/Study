package com.padingpading.interview.collection.a_list;


import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author libin
 * @description
 * @date 2022-04-24
 */
public class ArraysTests{

    public static void main(String[] args) {
         //当项目中代码里如果有固定的list,建议使用。确保无论任何情况下都不会对它进行修改
        //但是可以对单个缩影下的值进行修改
        final List<Object> objects = Arrays.asList();
        //抛出异常
        objects.add("sdf");
    
        //使用场景:判断逻辑需要返回空的list,不占用堆内存空间。
        //没有重写增、删、改 方法。返回
        List<String> lists = Collections.emptyList();
    }
}
