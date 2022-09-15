package com.padingpading.interview.es.controller;//package com.padingpading.interview.es.controller;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.Cardinality;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

@Api(value = "ES测试接口", tags = {"ES测试接口"})
@RestController
@RequestMapping("/es")
public class ESController11 {
    
    
    @Resource
    private RestHighLevelClient restHighLevelClient;
    
    
    @ApiOperation(value = "es测试创建索引接口", notes = "es测试创建索引接口")
    @RequestMapping(value = "/index/creation", method = RequestMethod.POST)
    public void createIndex(@RequestParam String indexName) throws IOException {
//        String indexNmme = "books";
//        Integer pageNum = 1;
//        Integer pageSize = 3;
//        SearchRequest searchRequest = new SearchRequest();、//        searchRequest.indices(indexNmme);
//        //search
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        searchSourceBuilder.query(QueryBuilders.matchQuery("author","史蒂芬霍金"));
//        searchSourceBuilder.sort("time", SortOrder.DESC);
//        //aggregation
//        AggregationBuilder aggregation =
//                AggregationBuilders
//                        .terms("agg").field("name")
//                        .subAggregation(
//                                AggregationBuilders.topHits("top").sort("name",SortOrder.DESC).size(1)
//                        );
//        searchSourceBuilder.aggregation(aggregation);
//        searchSourceBuilder.from((pageNum - 1) * pageSize);
//        searchSourceBuilder.size(pageSize);
    
        String indexNmme = "books";
    
        SearchRequest searchRequest1 = new SearchRequest();
        searchRequest1.indices(indexNmme);
        
        SearchSourceBuilder searchSourceBuilder1 = new SearchSourceBuilder();
        AggregationBuilder aggregation1 = AggregationBuilders.cardinality("sdfsdf").field("name.keyword");
        searchSourceBuilder1.aggregation(aggregation1);
        searchRequest1.source(searchSourceBuilder1);
        SearchResponse response = restHighLevelClient.search(searchRequest1, RequestOptions.DEFAULT);
    
        Aggregations aggregations = response.getAggregations();
        Cardinality cardinality = aggregations.get("sdfsdf");
        System.out.println(cardinality.getValue());
//
//44444444444444444//        searchRequest.source(searchSourceBuilder);4111//        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//
//        SearchHit[] searchHits = response.getHits().getHits();
    }
}
