package com.padingpading.interview.redis.redismq;

import com.padingpading.interview.jvm.RedisfZSetDelayQueue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestZSetVer {

    @Autowired
    private RedisfZSetDelayQueue zSetVer;

    @Test
    void testConsumerDelayMessage(){
        zSetVer.consumerDelayMessage();
    }

    @Test
    void testProducer(){
        zSetVer.producer();
    }

}
