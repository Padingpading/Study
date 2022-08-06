package com.padingpading.interview.desing;


/**
 * @author libin
 * @description
 * @date 2021-08-25
 */
public class SingleObject6 {
    private  SingleObject6() {
    }

    static class Hoder{
        private final static SingleObject6 SINGLE_OBJECT_6 = new SingleObject6();
    }

    public static  SingleObject6 getInstance(){
        return Hoder.SINGLE_OBJECT_6;
    }

    public static void main(String[] args) {
        SingleObject6 instance = SingleObject6.getInstance();
        System.out.println(instance);
    }

}
