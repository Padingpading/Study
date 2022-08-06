package com.padingpading.interview.advance.spi;

import java.util.ServiceLoader;

/**
 * @author libin
 * @description
 * @date 2022-05-02
 */
public class SpITest {
    public static void main(String[] args) {
        ServiceLoader<IShout> shouts = ServiceLoader.load(IShout.class);
        for (IShout s : shouts) {
            s.shout();
        }
    }
}
