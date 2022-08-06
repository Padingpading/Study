///*
// * Copyright (c) 1998, 2012, Oracle and/or its affiliates. All rights reserved.
// * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// *
// */
//
//package com.padingpading.interview.thread.wangwenjun.threadlocal;
//
///**可继承的InheritableThreadLocal
// * @since   1.2
// */
//public class InheritableThreadLocal<T> extends java.lang.ThreadLocal<T> {
//    /**复制的时候回调用该方法
//     * 直接返回值,不做任何处理。
//     */
//    protected T childValue(T parentValue) {
//        return parentValue;
//    }
//
//    /**
//     */
//    ThreadLocalMap getMap(Thread t) {
//       return t.inheritableThreadLocals;
//    }
//
//    /**
//     */
//    void createMap(Thread t, T firstValue) {
//        t.inheritableThreadLocals = new ThreadLocalMap(this, firstValue);
//    }
//}
