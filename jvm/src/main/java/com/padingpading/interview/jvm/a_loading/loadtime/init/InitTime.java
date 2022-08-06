package com.padingpading.interview.jvm.a_loading.loadtime.init;



import java.util.UUID;

/**
 * final字符串常量的加载
 */
public class InitTime {
    public static void main(String[] args) {
        //initTime 会加载Init的(字符串、int、float)常量加载到InitTime的字符串常量池
       // System.out.println(InitConstant.INIT);
        
       //  System.out.println(InitObject.INIT);
        System.out.println(InitObjectNull.NullTest);
    }
}

class InitConstant {
     public static final String INIT ="12312";
     static{
         System.out.println("InitConstant");
     }
}
class InitObject {
     //编译器无法确定值
     public static final String INIT = UUID.randomUUID().toString();
     static{
         System.out.println("InitObject");
     }
}
class InitObjectNull {
     //编译器无法确定值
     public static final String NullTest = "";
     static{
         System.out.println("InitObject");
     }
}
