package com.padingpading.interview.thread.wangwenjun.wangwenjun.chapter1;//package com.wangwenjun.concurrency.chapter1;
//
///***************************************
// * @author:Alex Wang
// * @Date:2017/2/14 QQ:532500648
// * QQ交流群:286081824
// ***************************************/
//public class TemplateMethod {
//
//    public void run() {
//        try {
//        ...
//            /*
//             * 不管循环里是否调用过线程阻塞的方法如sleep、join、wait，这里还是需要加上
//             * !Thread.currentThread().isInterrupted()条件，虽然抛出异常后退出了循环，显
//             * 得用阻塞的情况下是多余的，但如果调用了阻塞方法但没有阻塞时，这样会更安全、更及时。
//             */
//            while (!Thread.currentThread().isInterrupted()&& more work to do) {
//                do more work
//            }
//        } catch (InterruptedException e) {
//            //线程在wait或sleep期间被中断了
//        } finally {
//            //线程结束前做一些清理工作
//        }
//    }
//    protected void wrapPrint(String message) {
//
//    }
//
//    public static void main(String[] args) {
//        TemplateMethod t1 = new TemplateMethod(){
//            @Override
//            protected void wrapPrint(String message) {
//                System.out.println("*"+message+"*");
//            }
//        };
//        t1.print("Hello Thread");
//
//        TemplateMethod t2 = new TemplateMethod(){
//            @Override
//            protected void wrapPrint(String message) {
//                System.out.println("+"+message+"+");
//            }
//        };
//
//        t2.print("Hello Thread");
//
//    }
//}
