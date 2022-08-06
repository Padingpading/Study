package com.padingpading.interview.advance.spi;

/**
 * 具体实现
 */
public class Cat implements IShout {
    @Override
    public void shout() {
        System.out.println("miao miao");
    }
}
