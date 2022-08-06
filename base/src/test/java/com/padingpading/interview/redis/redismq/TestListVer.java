package com.padingpading.interview.redis.redismq;

import com.padingpading.interview.jvm.RedisListBlockQueue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * redis实现队列
 */
@SpringBootTest
public class TestListVer {

    @Autowired
    private RedisListBlockQueue listVer;

    @Test
    void testGet(){
        List<String> result = listVer.get("listmq");
        for(String message : result){
            System.out.println(message);
        }
    }

    @Test
    void testPut(){
        listVer.put("listmq","msgtest");
    }

}
