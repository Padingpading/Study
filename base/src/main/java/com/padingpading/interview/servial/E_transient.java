package com.padingpading.interview.servial;

import com.padingpading.interview.context.SysConstant;
import lombok.Data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * java序列化算法潜在的问题 序列化中修改对象的值会导致同一个对象序列化前和反序列化后的对象是不一致的。
 */
public class E_transient {
    
    public static void main(String[] args) throws Exception {
        //序列化
        String path = SysConstant.SERIAL_PATH + "E_transient.txt";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
                ObjectInputStream ios = new ObjectInputStream(new FileInputStream(path))) {
            Person person = new Person("9龙", 23);
            person.setHeight(185);
            System.out.println(person);
            oos.writeObject(person);
            //修改name
            Person p1 = (Person) ios.readObject();
            System.out.println(p1);
            //Person{name='9龙', age=23', singlehood=true', height=185cm}
            //Person{name='null', age=0', singlehood=false', height=185cm}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //class Person implements Serializable{
    @Data
    static class Person implements Serializable {
        
        //省略相关属性与方法
        private transient String name;
        
        private transient int age;
        
        private int height;
        
        private transient boolean singlehood;
        
        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }
}