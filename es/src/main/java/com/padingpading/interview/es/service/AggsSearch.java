package com.padingpading.interview.es.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.padingpading.interview.es.vo.ResponseBean;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.metrics.ParsedAvg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Service
public class AggsSearch {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Resource
    private SendSearchRequest sendSearchRequest;

    /**
     * POST /kibana_sample_data_flights/_search?filter_path=aggregations
     * {
     * 	"query": {
     * 		"term": {"OriginCountry": "CN"}
     *        },
     * 	"aggs":
     *    {
     * 		"date_price_histogram": {
     * 			"date_histogram": {
     * 				"field": "timestamp",
     * 				"interval": "month"
     *            },
     * 			"aggs": {
     * 				"avg_price": {"avg": {"field": "FlightDelayMin"}}
     *            }
     *        }
     *    }
     * }
     *
     * {
     *   "aggregations" : {
     *     "date_price_histogram" : {
     *       "buckets" : [
     *         {
     *           "key_as_string" : "2022-07-01T00:00:00.000Z",
     *           "key" : 1656633600000,
     *           "doc_count" : 256,
     *           "avg_price" : {
     *             "value" : 54.7265625
     *           }
     *         },
     *         {
     *           "key_as_string" : "2022-08-01T00:00:00.000Z",
     *           "key" : 1659312000000,
     *           "doc_count" : 1657,
     *           "avg_price" : {
     *             "value" : 51.03802051901026
     *           }
     *         },
     *         {
     *           "key_as_string" : "2022-09-01T00:00:00.000Z",
     *           "key" : 1661990400000,
     *           "doc_count" : 225,
     *           "avg_price" : {
     *             "value" : 58.2
     *           }
     *         }
     *       ]
     *     }
     *   }
     * }··
     * @param indexName
     * @return
     */
    public ResponseBean aggsExampleSearch(String indexName) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indexName);

        /*query部分*/
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(
                QueryBuilders.termQuery("OriginCountry","CN"));


        /*聚集部分*/
        DateHistogramAggregationBuilder date_price_histogram
                = AggregationBuilders.dateHistogram("date_price_histogram");
        //根据时间间隔
        date_price_histogram.field("timestamp")
                //1个月
                .fixedInterval(DateHistogramInterval.days(30));
        date_price_histogram.subAggregation(
                AggregationBuilders.avg("avg_price").field("FlightDelayMin")
        );
        //聚集
        searchSourceBuilder.aggregation(date_price_histogram);
        searchRequest.source(searchSourceBuilder);

        JSONArray jsonArray = new JSONArray();
        try {
            SearchResponse searchResponse
                    = sendSearchRequest.sendAndProcessHits(
                            searchRequest, RequestOptions.DEFAULT,jsonArray);
            
            
            Aggregations aggregations = searchResponse.getAggregations();
            for(Aggregation aggregation:aggregations){
                String aggString = JSON.toJSONString(aggregation);
                jsonArray.add(JSON.parseObject(aggString));
                List<? extends Histogram.Bucket> buckets
                        = ((Histogram) aggregation).getBuckets();
                for(Histogram.Bucket bucket:buckets){
                    System.out.println("--------------------------------------");
                    System.out.println(bucket.getKeyAsString());
                    System.out.println(bucket.getDocCount());
                    ParsedAvg parsedAvg
                            = (ParsedAvg)bucket.getAggregations().getAsMap().get("avg_price");
                    System.out.println(parsedAvg.getValueAsString());
                }
            }

            return new ResponseBean(200,"查询文档成功",jsonArray);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseBean(200,"查询文档失败",null);
        }

    }
}
