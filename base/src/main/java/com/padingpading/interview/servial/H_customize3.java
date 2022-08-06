package com.padingpading.interview.servial;

import com.padingpading.interview.context.SysConstant;
import lombok.Data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * readResolve：反序列化时替换反序列化出的对象，反序列化出来的对象被立即丢弃。此方法在readeObject后调用。
 */
public class H_customize3 {
    
    public static void main(String[] args) throws Exception {
        //序列化
        String path = SysConstant.SERIAL_PATH + "G_customize2.txt";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
                ObjectInputStream ios = new ObjectInputStream(new FileInputStream(path))) {
            Person person = new Person("9龙", 23);
            oos.writeObject(person);
            HashMap map = (HashMap) ios.readObject();
            System.out.println(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //class Person implements Serializable{
    @Data
    static class Person implements Serializable {
        
        private static final long serialVersionUID = 1111013L;
        
        private String name;
        
        private int age;
        
        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        private Object readResolve() throws ObjectStreamException {
            HashMap<String, Integer> map = new HashMap<>();
            map.put(name, age);
            return map;
        }
    }
}