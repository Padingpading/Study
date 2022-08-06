package com.padingpading.interview.jvm.a_loading.loadtime;


import com.fasterxml.jackson.databind.node.POJONode;

/**
 * 类的静态变量主动使用,加载
 */
public class LoadOrder {
    
    public static void main(String[] args) {
        //加載順序
        //1、加载People。
        //2、加载父类Animal
        //3、打印animal out
        //4、打印People out
        People people = new People();
        //5、答应people 已经加载完了
        System.out.println("people 已经加载完了");
        //6、加载dog
        //7、打印dog out
        Dog dog = new Dog();
        //打印汪
        dog.say();
    }
}

class Animal {
    static String animalStr = "animal";
    
    static {
        System.out.println("animal out");
    }
}

class People extends Animal {
     static String peopleStr = "People";
    
    static {
        System.out.println("People out ");
    }
}

class Dog {
     static String animalStr = "dog";
    
    static {
        System.out.println("dog out");
    }
    
    public void say(){
        System.out.println("汪");
    }
}