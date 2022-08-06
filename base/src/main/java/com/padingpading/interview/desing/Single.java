package com.padingpading.interview.desing;


/**
 * @author libin
 * @description
 * @date 2021-08-25
 */
public class Single {
    private static volatile Single single;

    private Single() {
    }

    public Single getInstance() {
        if (single == null) {
            synchronized (Single.class) {
                if (single == null) {
                    single = new Single();
                }
            }

        }
        return single;
    }
}
