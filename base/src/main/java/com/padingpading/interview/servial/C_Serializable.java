package com.padingpading.interview.servial;

import com.padingpading.interview.context.SysConstant;
import lombok.Data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author libin
 * @description
 * @date 2022-04-26
 */
public class C_Serializable {
    
    public static void main(String[] args) throws Exception {
        //序列化
        String path = SysConstant.SERIAL_PATH + "C_Serializable.txt";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            Person person = new Person("路飞", 20);
            Teacher t1 = new Teacher("雷利", person);
            Teacher t2 = new Teacher("红发香克斯", person);
            //依次将4个对象写入输入流
            oos.writeObject(t1);
            oos.writeObject(t2);
            oos.writeObject(person);
            oos.writeObject(t2);
        }
        //反序列化
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            Teacher t1 = (Teacher) ois.readObject();
            Teacher t2 = (Teacher) ois.readObject();
            Person p = (Person) ois.readObject();
            Teacher t3 = (Teacher) ois.readObject();
            //按照序列化的顺序反序列化出对象。
            //false
            System.out.println(t1 == t2);
            //true
            System.out.println(t1.getPerson() == p);
            //true
            System.out.println(t2.getPerson() == p);
            //ture
            System.out.println(t2 == t3);
            //true
            System.out.println(t1.getPerson() == t2.getPerson());
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