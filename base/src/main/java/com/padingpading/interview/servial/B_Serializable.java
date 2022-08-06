package com.padingpading.interview.servial;

import com.padingpading.interview.context.SysConstant;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author libin
 * @description
 * @date 2022-04-26
 */
public class B_Serializable {
    
    public static void main(String[] args) throws Exception {
        String path = SysConstant.SERIAL_PATH + "teacher.txt";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            Person person = new Person("路飞", 20);
            Teacher teacher = new Teacher("雷利", person);
            oos.writeObject(teacher);
        }
    }
}

//class Person implements Serializable{
class Person {
    //省略相关属性与方法
    private String name;
    private int age;
    
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

 class Teacher implements Serializable {
    
    private String name;
    
    private Person person;
    
    public Teacher(String name, Person person) {
        this.name = name;
        this.person = person;
    }

}