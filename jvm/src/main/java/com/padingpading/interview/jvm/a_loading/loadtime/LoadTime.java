package com.padingpading.interview.jvm.a_loading.loadtime;



/**
 * 类的静态变量主动使用,
 */
public class LoadTime {
    
    public static void main(String[] args) {
        //虽然使用的Son，但是并没有加载子类子类加载了，但是没有被初始化。
        //parent out
        //parent
      //  System.out.println(Son.parentStr);
        
        //使son,先加载父类,后加载子类
        //parent out
        //son out
        //son
        System.out.println(Son.sonStr);
    }
}

class Parent {
     static String parentStr = "parent";
    
    static {
        System.out.println("parent out");
    }
}

class Son extends Parent {
     public static final String sonStr ="12312";
    
//    static {
//        System.out.println("son out ");
//    }
}