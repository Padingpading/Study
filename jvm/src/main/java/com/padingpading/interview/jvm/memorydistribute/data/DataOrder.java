package com.padingpading.interview.jvm.memorydistribute.data;

import com.padingpading.interview.jvm.memorydistribute.Demo;
import org.openjdk.jol.info.ClassLayout;

/**字段排序
 */
public class DataOrder {
    
    public static void main(String[] args) {
        //查看对象的内存布局
        Demo demo = new Demo();
        User user = new User();
        user.setDemo(demo);
        System.out.println(ClassLayout.parseInstance(user).toPrintable());
    }
}

class User {
    int id,age,weight;
    byte sex;
    long phone;
    char local;
    private Demo demo;
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public int getWeight() {
        return weight;
    }
    
    public void setWeight(int weight) {
        this.weight = weight;
    }
    
    public byte getSex() {
        return sex;
    }
    
    public void setSex(byte sex) {
        this.sex = sex;
    }
    
    public long getPhone() {
        return phone;
    }
    
    public void setPhone(long phone) {
        this.phone = phone;
    }
    
    public char getLocal() {
        return local;
    }
    
    public void setLocal(char local) {
        this.local = local;
    }
    
    public Demo getDemo() {
        return demo;
    }
    
    public void setDemo(Demo demo) {
        this.demo = demo;
    }
}