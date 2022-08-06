package com.padingpading.interview.collection.a_list;

import com.padingpading.interview.context.SysConstant;
import com.padingpading.interview.servial.H_customize3;
import org.apache.catalina.LifecycleState;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author libin
 * @description
 * @date 2022-04-24
 */
public class ArrayListSerial {
    
   public static void main(String[] args) throws Exception {
        //序列化
        String path = SysConstant.LIST_PATH + "ArrayListSerial.txt";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path,true));
                ObjectInputStream ios = new ObjectInputStream(new FileInputStream(path))) {
            ArrayList<String> lists = new ArrayList<>();
            lists.add("1");
            lists.add("2");
            lists.add("3");
            lists.add("4");
            oos.writeObject(lists);
            ArrayList<String> strings = (ArrayList<String>) ios.readObject();
            System.out.println(strings);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
