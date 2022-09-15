package com.padingpading.interview.redis.basetypes;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Map;

@SpringBootTest
public class TestRedisBloomFilter {

    private static final int DAY_SEC = 60 * 60 * 24;
    
    @Resource
    private RestHighLevelClient restHighLevelClient;
    
    @Test
    public void testInsert() throws Exception {
        String indexNmme = "books";
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indexNmme);
        //search
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("author","史蒂芬霍金"));
        searchSourceBuilder.sort("time", SortOrder.DESC);
        //aggregation
        AggregationBuilder aggregation =
                AggregationBuilders
                        .terms("agg").field("name")
                        .subAggregation(
                                AggregationBuilders.topHits("top").sort("name",SortOrder.DESC).size(1)
                        );
        searchSourceBuilder.aggregation(aggregation);
    
        searchRequest.source(searchSourceBuilder);
    
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    
        SearchHit[] searchHits = response.getHits().getHits();
        for (SearchHit hit : searchHits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            System.out.println(sourceAsMap);
        }
    }

}
