package com.padingpading.interview.es.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ESConfig {

    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private int port;


    @Bean
    public RestClientBuilder restClientBuilder() {
        HttpHost httpHost = new HttpHost(host, port, "http");
        RestClientBuilder restClientBuilder = RestClient.builder(httpHost);
        return restClientBuilder;
    }

    @Bean
    public RestHighLevelClient highLevelClient(@Autowired RestClientBuilder restClientBuilder) {
        return new RestHighLevelClient(restClientBuilder);
    }

}
