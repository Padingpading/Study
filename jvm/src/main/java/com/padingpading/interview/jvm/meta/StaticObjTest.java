package com.padingpading.interview.jvm.meta;


import java.util.concurrent.ThreadPoolExecutor;

/**
 * 《深入理解Java虚拟机》中的案例：
 * staticObj、instanceObj、localObj存放在哪里？
 */
public class StaticObjTest {
    static class Test {
        //对象在堆空间中，staticObj 也是在堆中
        static ObjectHolder staticObj = new ObjectHolder();
        //对象在堆空间中,instanceObj也在堆中
        ObjectHolder instanceObj = new ObjectHolder();

        void foo() {
            //对象在堆空间中，localObj在方法栈中。
            ObjectHolder localObj = new ObjectHolder();
            System.out.println("done");
        }
    }

    private static class ObjectHolder {

    }

    public static void main(String[] args) {
        Test test = new Test();
        test.foo();
    }


}
