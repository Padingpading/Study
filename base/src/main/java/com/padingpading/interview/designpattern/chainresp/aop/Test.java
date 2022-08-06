package com.padingpading.interview.designpattern.chainresp.aop;

import java.util.ArrayList;
import java.util.List;

/**
 * @author libin
 * @description
 * @date 2022-06-05
 */
public class Test {
    
    public static void main(String[] args) {
        IMyInterceptor before =   new BeforeInterceptorImpl();
        IMyInterceptor after =   new AfterInterceptorImpl();
        List list = new ArrayList<>();
        list.add(before);
        list.add(after);
        TargetServiceImpl targetService = new TargetServiceImpl(list);
        targetService.targetMethod();
    }
    
}
