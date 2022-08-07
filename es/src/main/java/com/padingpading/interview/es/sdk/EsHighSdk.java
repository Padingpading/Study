package com.padingpading.interview.es.sdk;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EsHighSdk {
    
    private static  RestHighLevelClient restHighLevelClient;
    
    //索引名称
    private static String indexName = "high_sdk_test";
    private static String docId = "1";
    static {
        RestClientBuilder restClientBuilder =
                RestClient.builder(
                        new HttpHost("59.110.155.28",9200,"http")
                );
        restHighLevelClient =
                new RestHighLevelClient(restClientBuilder);
    }

    public static void main(String[] args) throws IOException {
        //创建索引
        //createIndexRequest();
        /*索引(保存)文档*/
       // extracted(restHighLevelClient, indexName, docId);
        //
        find();
    
    }
    
    private static void find() throws IOException {
        /*查询文档*/
        GetRequest getRequest = new GetRequest(indexName, docId);
//        {
//            "_index" : "high_sdk_test",
//                "_type" : "_doc",
//                "_id" : "1",
//                "_version" : 1,
//                "_seq_no" : 0,
//                "_primary_term" : 1,
//                "found" : true,
//                "_source" : {
//                    "user" : "Mark",
//                    "postData" : "2022-08-06T07:13:24.648Z",
//                    "message" : "Go ELK"
//        }
//        }
        GetResponse getResponse =
                restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        System.out.println(" get indexname = "+getResponse.getIndex());
        if(getResponse.isExists()){
            //获取的文档：{"user":"Mark","postData":"2022-08-06T07:13:24.648Z","message":"Go ELK"}z
            System.out.println("获取的文档："+getResponse.getSourceAsString());
        }else{
            System.out.println("文档不存在");
        }
    }
    
    private static void createIndexRequest() throws IOException {
        /*客户端的创建*/
        /*创建索引*/
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
        //setting设置
        createIndexRequest.settings(Settings.builder()
        .put("index.number_of_shards",3)
        .put("index.codec","best_compression"));
        
        //构建json
        //        {
        //            "properties":{
        //            "message":{
        //                "type":"text"
        //            }
        //        }
        Map<String,Object> message = new HashMap<>();
        message.put("type","text");
        Map<String,Object> properties = new HashMap<>();
        properties.put("message",message);
        Map<String,Object> mapping = new HashMap<>();
        mapping.put("properties",properties);
        //设置mapping
        createIndexRequest.mapping(mapping);
        String s = JSON.toJSONString(mapping);
        System.out.println(s);
    
        //
        //        createIndexRequest.mapping("{" +
        //                "\"msg\":\"Java Low Level REST Client\""+
        //                "}", XContentType.JSON);
        //
        //        XContentBuilder.builder()
    
        CreateIndexResponse createIndexResponse
                = restHighLevelClient.indices().create(createIndexRequest,
                RequestOptions.DEFAULT);
        System.out.println(createIndexResponse.index());
        System.out.println(createIndexResponse.isAcknowledged());
    }
    
    /**
     * 创建文档
     */
    private static void extracted(RestHighLevelClient restHighLevelClient, String indexName, String docId)
            throws IOException {
        IndexRequest indexRequest = new IndexRequest(indexName);
        indexRequest.id(docId);
        
        //构建请求体
        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder();
        xContentBuilder.startObject();
        {
            xContentBuilder.field("user","Mark");
            xContentBuilder.timeField("postData",new Date());
            xContentBuilder.field("message","Go ELK");
        }
        xContentBuilder.endObject();
        //
        indexRequest.source(xContentBuilder);
        IndexResponse index =
                restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(index.getIndex());
        System.out.println(index.getId());
    }
    
}
