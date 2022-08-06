package com.padingpading.interview.designpattern.chainresp.aop;


public interface IMyInterceptor{
    void adviceMethod(ITargetService targetService);
}