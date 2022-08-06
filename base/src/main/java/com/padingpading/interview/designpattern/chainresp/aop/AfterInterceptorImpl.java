package com.padingpading.interview.designpattern.chainresp.aop;


import org.springframework.stereotype.Service;

@Service
public class AfterInterceptorImpl implements IMyInterceptor {
    
    /**
     * 后置通知的方法
     * @param targetService
     */
    @Override
    public void adviceMethod(ITargetService targetService) {
        //调用执行目标方法的业务逻辑：
        targetService.targetMethod();
        System.out.println("执行后置通知...");
    }
}