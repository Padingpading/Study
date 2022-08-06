package com.padingpading.interview.jvm.a_loading.loadtime.init.test;


/**
 * 如果通过子类访问父类的静态变量和静态方式,实际上是对父类的主动使用，而不是子类。
 */
public class A_init {
    
    public static void main(String[] args) {
//        Animal
//        0
//        ==========
//        eat
        System.out.println(ADog.animal);
        System.out.println("==========");
        ADog.eat();
    }
}

class AAnimal{
    static  int animal = 0;
    static {
        System.out.println("Animal");
    }
    
    public static void  eat(){
        System.out.println("eat");
    }
}
class ADog extends  AAnimal{
    static  int dog = 0;
    static {
        System.out.println("ADog");
    }
}