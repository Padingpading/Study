package com.padingpading.interview.designpattern.chainresp.aop;


import org.springframework.stereotype.Service;

@Service
public class BeforeInterceptorImpl implements IMyInterceptor {
    
    /**
     * 前置通知的方法
     * @param targetService
     */
    @Override
    public void adviceMethod(ITargetService targetService) {
        System.out.println("执行前置通知...");
        //调用执行目标方法的业务逻辑：
        targetService.targetMethod();
    }
}