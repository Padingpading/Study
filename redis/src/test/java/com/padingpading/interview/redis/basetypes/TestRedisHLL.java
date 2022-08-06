package com.padingpading.interview.redis.basetypes;

import com.padingpading.interview.redis.func.hyperLogLog.RedisHLL;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestRedisHLL {

    @Autowired
    private RedisHLL redisHLL;

    @Test
    void testCount(){
        redisHLL.count();
    }

}
