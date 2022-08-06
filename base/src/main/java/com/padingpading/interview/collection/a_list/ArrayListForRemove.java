package com.padingpading.interview.collection.a_list;

import javax.swing.plaf.IconUIResource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author libin
 * @description
 * @date 2022-04-24
 */
public class ArrayListForRemove {

    public static void main(String[] args) {
        testForEach();
    
    
    }
    public static  void testForEach(){
        //forEach
//        List<String> userNames = new ArrayList<String>() {{
//            add("Hollis");
//            add("hollis");
//            add("HollisChuang");
//            add("H");
//        }};
//
//        for (String userName : userNames) {
//            if (userName.equals("Hollis")) {
//                userNames.remove(userName);
//            }
//
//        }
//        System.out.println(userNames);
        // 使用ImmutableList初始化一个List
        //foreach->转换为迭代器
//        List<String> userNames = new ArrayList<String>() {{
//            add("Hollis");
//            add("hollis");
//            add("HollisChuang");
//            add("H");
//        }};
//
//        Iterator iterator = userNames.iterator();
//        do
//        {
//            if(!iterator.hasNext())
//                break;
//            String userName = (String)iterator.next();
//            if(userName.equals("Hollis")){
                //只能移除一个
//                userNames.remove(userName);
//                System.out.println("移除了");
//            }
//
//        } while(true);
//        System.out.println(userNames);
        //正确remove
        List<String> userNames = new ArrayList<String>() {{
            add("Hollis");
            add("hollis");
            add("HollisChuang");
            add("H");
        }};

        Iterator iterator = userNames.iterator();

        while (iterator.hasNext()) {
            if (iterator.next().equals("Hollis")) {
                //使用迭代器进行remove
                iterator.remove();
            }
        }
        System.out.println(userNames);
    

    }
    
    public static  void testFori(){
        // 使用双括弧语法（double-brace syntax）建立并初始化一个List
        List<String> userNames = new ArrayList<String>() {{
            add("Hollis");
            add("hollis");
            add("HollisChuang");
            add("H");
        }};
    
        for (int i = 0; i < userNames.size(); i++) {
            if (userNames.get(i).equals("Hollis")) {
                userNames.remove(i);
            }
        }
        System.out.println(userNames);
    }
}
