package com.padingpading.interview.jvm.stack;

/**
 * @author libin
 * @description
 * @date 2021-07-08
 */


class Animal {

    public void eat() {
        System.out.println("动物进食");
    }
}

interface Huntable{

    void hunt();
}

class Dog extends  Animal implements  Huntable{

    @Override
    public void hunt() {
        System.out.println("狗吃骨头");
    }

    @Override
    public void eat() {
        System.out.println("捕食耗子，多管闲事");
    }
}

class Cat extends  Animal implements  Huntable{

    public Cat() {
        super();
    }

    public Cat(String name) {
        this();
    }



    @Override
    public void hunt() {
        System.out.println("猫吃鱼");
    }

    @Override
    public void eat() {
        System.out.println("捕食耗子，天经地义");
    }

    public static void main(String[] args) {

    }
}


public class AnimalTest {
    public void showAnimal(Animal animal){
        //晚期绑定,编译之后无法确定
        animal.eat();
    }

    public void showHunt(Huntable huntable){
        //晚期绑定,编译之后无法确定
        huntable.hunt();
    }

    public static void main(String[] args) {

    }
}