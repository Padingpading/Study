package com.padingpading.interview.designpattern.futrue;

/**
 * @author libin
 * @description
 * @date 2021-07-21
 */
public class FutureData implements Data{

    public RealData realData = null;
    public boolean isReady = false;

    public synchronized void setRealData(RealData realData){
        if (isReady){
            return;
        }
        this.realData = realData;
        isReady = true;
        this.notifyAll();    //realData已经被注入 通知getResult启动
    }

    @Override
    public synchronized String getResult() {
        while (!isReady){
            try {
                System.out.println("wait start " + System.currentTimeMillis());
                this.wait();     //等待realData被注入
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("wait end " + System.currentTimeMillis());
        return realData.result;
    }
        }