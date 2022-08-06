package com.padingpading.interview.servial;

import com.padingpading.interview.context.SysConstant;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author libin
 * @description
 * @date 2022-04-26
 */


public class A_Serializable {
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String path = SysConstant.SERIAL_PATH + "oos.txt";
        //序列化
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path, true));
        User userSerial = new User("test", 23);
        oos.writeObject(userSerial);
        //反序列化Deserialization
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
        User userDeserial = (User) ois.readObject();
        System.out.println(userDeserial);
    }
}

 class User implements Serializable {
    
    private String name;
    
    private final int age;
    
    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    @Override
    public String toString() {
        return "User{" + "name='" + name + '\'' + ", age=" + age + '}';
    }
}

