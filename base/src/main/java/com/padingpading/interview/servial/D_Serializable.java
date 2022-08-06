package com.padingpading.interview.servial;

import com.padingpading.interview.context.SysConstant;
import lombok.Data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * java序列化算法潜在的问题
 * 序列化中修改对象的值会导致同一个对象序列化前和反序列化后的对象是不一致的。
 */
public class D_Serializable {
    
    public static void main(String[] args) throws Exception {
        //序列化
        String path = SysConstant.SERIAL_PATH + "C_Serializable.txt";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("person.txt"));
                ObjectInputStream ios = new ObjectInputStream(new FileInputStream("person.txt"))) {
            //第一次序列化person
            Person person = new Person("9龙", 23);
            oos.writeObject(person);
            System.out.println(person);
            //修改name
            person.setName("海贼王");
            System.out.println(person);
            //第二次序列化person
            oos.writeObject(person);
            //依次反序列化出p1、p2
            Person p1 = (Person) ios.readObject();
            Person p2 = (Person) ios.readObject();
            System.out.println(p1 == p2);
            System.out.println(p1.getName().equals(p2.getName()));
            //输出结果
            //Person{name='9龙', age=23}
            //Person{name='海贼王', age=23}
            //true
            //true
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //class Person implements Serializable{
    @Data
    static class Person implements Serializable {
        
        //省略相关属性与方法
        private String name;
        
        private int age;
        
        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }
    
    @Data
    static class Teacher implements Serializable {
        
        private String name;
        
        private Person person;
        
        public Teacher(String name, Person person) {
            this.name = name;
            this.person = person;
        }
    }
}