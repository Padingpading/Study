package com.padingpading.interview.thread.wangwenjun.thread.communication;

/**
 * @author libin
 * @description 生产者、消费者 依次执行。
 * @date 2021-07-19
 */
public class CommunicateByWait {

    private int i = 0;

    private final Object LOCK = new Object();

    private volatile  boolean flags = false;

   public void producer() {
       synchronized (LOCK){
           while (flags){
               try {
                   LOCK.wait();
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
           i++;
           System.out.println("p->"+i);
           LOCK.notifyAll();
           flags = true;
       }
   }

   public void consume(){
       synchronized (LOCK){
           while (!flags){
               try {
                   LOCK.wait();
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
           System.out.println("C->"+i);
           LOCK.notifyAll();
           flags = false;
       }
   }

    public static void main(String[] args) {
        CommunicateByWait communicate = new CommunicateByWait();
        for (int i = 0; i < 100; i++) {
            new Thread(){
                @Override
                public void run() {
                    communicate.consume();
                }
            }.start();
        }
        for (int i = 0; i < 100; i++) {
            new Thread(){
                @Override
                public void run() {
                    communicate.producer();
                }
            }.start();
        }
    }
}
