package com.padingpading.interview.designpattern.chainresp.aop;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TargetServiceImpl implements ITargetService {
    
    private List<IMyInterceptor> myInterceptorList; //拦截器集合，即拦截器链
    
    //实例化目标类对象的时候，注入属性满足依赖；
    public TargetServiceImpl(List<IMyInterceptor> myInterceptorList) {
        this.myInterceptorList = myInterceptorList;
    }
    
    private int currentInterceptorIndex = -1; //该指针驱动责任链条向前执行
    
    /**
     * 目标方法，切入点
     */
    @Override
    public void targetMethod() {
        if (currentInterceptorIndex == this.myInterceptorList.size() - 1) {
            System.out.println("具体业务方法，正在执行具体业务逻辑...");
            return;
        }
        //获取拦截器链中当前的拦截器
        IMyInterceptor myInterceptor = this.myInterceptorList.get(++this.currentInterceptorIndex);
        //执行当前拦截器的Advice通知：
        myInterceptor.adviceMethod(this);
    }
    
    public TargetServiceImpl() { }
    
}