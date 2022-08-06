package com.padingpading.interview.collection.b_linkedhashmap;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**遍历
 */
public class A_Iterator{
    public static void main(String[] args) {
        //Map<String, Integer> map = new HashMap<>();
        Map<String, Integer> map = new LinkedHashMap<>();
        // 使用三个参数的构造法方法来指定 accessOrder 参数的值
        //Map<String, Integer> map = new LinkedHashMap<>(10,0.75f,true);
    
    
        map.put("老大", 1);
        map.put("老二", 2);
        map.put("老三", 3);
        map.put("老四", 4);
    
        Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
        Iterator iter1 = entrySet.iterator();
    
    
        while (iter1.hasNext()) {
            Map.Entry entry = (Map.Entry) iter1.next();
            System.out.print("key:  " + entry.getKey() + "   ");
            System.out.println("value:  " + entry.getValue());
        }
    
        System.out.println("老三的值为：" + map.get("老三"));
        System.out.println("老大的值为：" + map.put("老大",1000));
    
        Iterator iter2 = entrySet.iterator();
        while (iter2.hasNext()) {
            // 遍历时，需先获取entry，再分别获取key、value
            Map.Entry entry = (Map.Entry) iter2.next();
            System.out.print("key:  " + entry.getKey() + "   ");
            System.out.println("value:  " + entry.getValue());
        }
    }

}
