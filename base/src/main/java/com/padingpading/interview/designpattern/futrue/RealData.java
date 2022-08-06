package com.padingpading.interview.designpattern.futrue;

/**
 * @author libin
 * @description
 * @date 2021-07-21
 */
public class RealData implements Data {

    public final String result;

    public RealData(String result) {
        System.out.println("RealData start " + System.currentTimeMillis());
        //模拟构造时间长
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 10; i++) {
            sb.append(result);
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("RealData end " + System.currentTimeMillis());
        this.result = sb.toString();
    }

    @Override
    public String getResult() {
        return result;
    }
}