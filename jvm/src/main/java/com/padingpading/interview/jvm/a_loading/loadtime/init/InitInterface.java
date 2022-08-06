package com.padingpading.interview.jvm.a_loading.loadtime.init;



import java.util.Random;
import java.util.UUID;

/**
 * final字符串常量的加载
 */
public class InitInterface {
    public static void main(String[] args) {
        //会加载父InitParentInterface
        //InitSonInterface
        System.out.println(InitSonInterface.INIT_SON);
    }
}

interface InitParentInterface {
     public static final String INIT_PARENT ="12312";
}

interface InitSonInterface extends InitParentInterface {
     public static final  String INIT_SON = "123123";
}

