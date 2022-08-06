package com.padingpading.interview.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@PropertySource("classpath:application.properties")
public class JedisPoolConf {
    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private int port;

    @Value("${redis.timeout}")
    private int timeout;
    
    @Value("${redis.password}")
    private String password;

    @Value("${redis.maxIdle}")
    private int maxIdle;

    @Value("${redis.maxWaitMillis}")
    private int maxWaitMillis;

    @Value("${redis.blockWhenExhausted}")
    private Boolean blockWhenExhausted;

    @Value("${redis.JmxEnabled}")
    private Boolean JmxEnabled;

    @Bean
    public JedisPool jedisPoolFactory() {
        JedisPoolConfig jedisPoolConf = new JedisPoolConfig();
        jedisPoolConf.setMaxIdle(maxIdle);
        jedisPoolConf.setMaxWaitMillis(maxWaitMillis);
        // 连接耗尽时是否阻塞, false报异常,true阻塞直到超时, 默认true
        jedisPoolConf.setBlockWhenExhausted(blockWhenExhausted);
        // 是否启用pool的jmx管理功能, 默认true
        jedisPoolConf.setJmxEnabled(JmxEnabled);
        JedisPool jedisPool = new JedisPool(jedisPoolConf, host, port, timeout,password);
        return jedisPool;
    }
}
