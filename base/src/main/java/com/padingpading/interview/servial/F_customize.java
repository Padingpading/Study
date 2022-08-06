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

/**
 * java序列化算法潜在的问题 序列化中修改对象的值会导致同一个对象序列化前和反序列化后的对象是不一致的。
 */
public class F_customize {
    
    public static void main(String[] args) throws Exception {
        //序列化
        String path = SysConstant.SERIAL_PATH + "E_transient.txt";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
                ObjectInputStream ios = new ObjectInputStream(new FileInputStream(path))) {
            Person person = new Person("9龙", 23);
            System.out.println(person);
            oos.writeObject(person);
            //修改name
            Person p1 = (Person) ios.readObject();
            System.out.println(p1);
            //F_customize.Person(name=9龙, age=23)
            //F_customize.Person(name=9龙, age=23)
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
    
        private void writeObject(ObjectOutputStream out) throws IOException {
            //将名字反转写入二进制流
            out.writeObject(new StringBuffer(this.name).reverse());
            out.writeInt(age);
        }
        
        private void readObject(ObjectInputStream ins) throws IOException, ClassNotFoundException {
            //将读出的字符串反转恢复回来
            this.name = ((StringBuffer) ins.readObject()).toString();
            this.age = ins.readInt();
        }
    }
}