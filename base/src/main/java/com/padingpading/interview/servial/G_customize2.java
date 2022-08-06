package com.padingpading.interview.servial;

import com.padingpading.interview.context.SysConstant;
import lombok.Data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * java序列化算法潜在的问题 序列化中修改对象的值会导致同一个对象序列化前和反序列化后的对象是不一致的。
 */
public class G_customize2 {
    
    public static void main(String[] args) throws Exception {
        //序列化
        String path = SysConstant.SERIAL_PATH + "G_customize2.txt";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
                ObjectInputStream ios = new ObjectInputStream(new FileInputStream(path))) {
            Person person = new Person("9龙", 23);
            oos.writeObject(person);
            ArrayList list = (ArrayList) ios.readObject();
            System.out.println(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //class Person implements Serializable{
    @Data
    static class Person implements Serializable {
        
        private String name;
        
        private int age;
        
        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        private Object writeReplace() throws ObjectStreamException {
            ArrayList<Object> list = new ArrayList<>(2);
            list.add(this.name);
            list.add(this.age);
            return list;
        }
    }
}