package com.padingpading.interview.jvm.stack;

/**
 * @author libin
 * @description
 * @date 2022-05-06
 */
class Father {
    
    public Father() {
        System.out.println("Father默认构造器");
    }
    
    public static void showStatic(String s) {
        System.out.println("Father show static" + s);
    }
    
    public final void showFinal() {
        System.out.println("Father show final");
    }
    
    public void showCommon() {
        System.out.println("Father show common");
    }
    
}

public class Son extends Father {
    
    public Son() {
        super();
    }
    
    public Son(int age) {
        this();
    }
    
    public static void main(String[] args) {
        Son son = new Son();
        son.show();
    }
    
    //不是重写的父类方法，因为静态方法不能被重写
    public static void showStatic(String s) {
        System.out.println("Son show static" + s);
    }
    
    private void showPrivate(String s) {
        System.out.println("Son show private" + s);
    }
    
    public void show() {
        //invoke static
        showStatic(" 大头儿子");
        //invoke static
        Father.showStatic(" 大头儿子");
        //invoke special
        showPrivate(" hello!");
        //invoke special
        super.showCommon();
        //invoke virtual 因为此方法声明有final 不能被子类重写，所以也认为该方法是非虚方法
        showFinal();
        
        //虚方法如下
        //invoke virtual
        showCommon();//没有显式加super，被认为是虚方法，因为子类可能重写showCommon
        info();
        
        MethodInterface in = null;
        //invoke interface  不确定接口实现类是哪一个 需要重写
        in.methodA();
        
    }
    
    public void info() {
    
    }
    
}

interface MethodInterface {
    
    void methodA();
}

    

