//package com.padingpading.interview.es.controller;
//
//import com.google.common.collect.Lists;
//import com.padingpading.interview.es.service.OperateDoc;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import org.elasticsearch.action.search.SearchRequest;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.aggregations.AggregationBuilder;
//import org.elasticsearch.search.aggregations.AggregationBuilders;
//import org.elasticsearch.search.builder.SearchSourceBuilder;
//import org.elasticsearch.search.sort.SortOrder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.annotation.Resource;
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//
//@Api(value = "ES测试接口", tags = {"ES测试接口"})
//@RestController
//@RequestMapping("/es")
//@CrossOrigin(origins = "*", methods = {RequestMethod.GET,
//        RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
//public class ESController {
//    @Resource
//    private OperateIndex operateIndex;
//
//    @Resource
//    private OperateDoc operateDoc;
//    @Autowired
//    private AggsSearch aggsSearch;
//
//
//    @Resource
//    private NormalSearch normalSearch;
//
//    private  final Logger log = LoggerFactory.getLogger(ESConfig.class);
//    private final static String KIBANA_SAMPLE_DATA_FLIGHTS
//            = "kibana_sample_data_flights";
//    private final static String KIBANA_SAMPLE_DATA_LOGS
//            = "kibana_sample_data_logs";
//
//
//    @Resource
//    private RestHighLevelClient restHighLevelClient;
//
////
////    @ApiOperation(value = "es测试创建索引接口", notes = "es测试创建索引接口")
////    @RequestMapping(value = "/index/creation", method = RequestMethod.POST)
////    public ResponseBean createIndex(@RequestParam String indexName) {
////        try {
////            if (operateIndex.createIndex(indexName)) {
////                return new ResponseBean(200, "创建成功", null);
////            } else {
////                return new ResponseBean(1002, "创建失败", null);
////            }
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////        return null;
////    }
////
////    @ApiOperation(value = "es测试是否存在索引接口", notes = "es测试是否存在索引接口")
////    @RequestMapping(value = "/index/existence", method = RequestMethod.POST)
////    public ResponseBean indexExists(@RequestParam String indexName) {
////        boolean isExists = operateIndex.isIndexExists(indexName);
////        String msg = isExists? "索引存在："+indexName : "索引不存在："+indexName ;
////        return new ResponseBean(200, msg, isExists);
////    }
////
////    @ApiOperation(value = "es测试删除索引接口", notes = "es测试删除索引接口")
////    @RequestMapping(value = "/index/erasure", method = RequestMethod.POST)
////    public ResponseBean deleteIndex(@RequestParam String indexName) {
////        boolean isDelete = operateIndex.deleteIndex(indexName);
////        if (isDelete) {
////            return new ResponseBean(200, "删除成功", null);
////        } else {
////            return new ResponseBean(10002, "删除失败", null);
////        }
////    }
//
////    @ApiOperation(value = "es测试插入文档接口", notes = "es测试插入文档接口")
////    @RequestMapping(value = "/doc/insertion", method = RequestMethod.POST)
////    public ResponseBean insertDoc(@RequestBody User user,
////                                  @RequestParam String indexName,
////                                  @RequestParam  String docId) {
////        return operateDoc.insertDoc(user,indexName,docId);
////    }
////
////    @ApiOperation(value = "es测试获取文档接口", notes = "es测试插入文档接口")
////    @RequestMapping(value = "/doc/query", method = RequestMethod.GET)
////    public ResponseBean getDoc(@RequestParam String indexName,
////                               @RequestParam String docId) {
////        return operateDoc.getDoc(indexName,docId);
////    }
////
////    @ApiOperation(value = "es测试更新文档接口", notes = "es测试插入文档接口")
////    @RequestMapping(value = "/doc/update", method = RequestMethod.POST)
////    public ResponseBean updateDoc(@RequestParam String indexName,
////                                  @RequestParam String docId,
////                                  @RequestParam String fieldName,
////                                  @RequestParam String fieldValue) {
////        return operateDoc.updateDoc(indexName,docId,fieldName,fieldValue);
////    }
////
////    @ApiOperation(value = "es测试删除文档接口", notes = "es测试插入文档接口")
////    @RequestMapping(value = "/doc/erasure", method = RequestMethod.POST)
////    public ResponseBean deleteDoc(@RequestParam String indexName,
////                                  @RequestParam String docId) {
////        return operateDoc.deleteDoc(indexName,docId);
////    }
////
////    @ApiOperation(value = "_search接口基本用法", notes = "search接口基本用法")
////    @RequestMapping(value = "/search/example", method = RequestMethod.POST)
////    public ResponseBean searchExample() {
////        return normalSearch.searchExample(KIBANA_SAMPLE_DATA_FLIGHTS);
////    }
//
//    @ApiOperation(value = "基于词项的查询", notes = "基于词项的term查询")
//    @RequestMapping(value = "/search/term", method = RequestMethod.POST)
//    public ResponseBean termsBasedSearch() {
//        return normalSearch.termsBasedSearch(KIBANA_SAMPLE_DATA_FLIGHTS,
//                "dayOfWeek");
//    }
//
//    @ApiOperation(value = "基于全文的查询", notes = "基于全文的multi_match查询")
//    @RequestMapping(value = "/search/match", method = RequestMethod.POST)
//    public ResponseBean matchBasedSearch() {
//        return normalSearch.matchBasedSearch(KIBANA_SAMPLE_DATA_FLIGHTS,
//                "AT","DestCountry", "OriginCountry");
//    }
//
//    @ApiOperation(value = "基于全文的模糊查询", notes = "基于全文的模糊查询")
//    @RequestMapping(value = "/search/fuzzy", method = RequestMethod.POST)
//    public ResponseBean fuzzySearch() {
//        return normalSearch.fuzzySearch(KIBANA_SAMPLE_DATA_LOGS,
//                "message","firefix");
//    }
//
//    @ApiOperation(value = "组合查询范例", notes = "组合查询之bool查询")
//    @RequestMapping(value = "/search/combination-bool",
//            method = RequestMethod.POST)
//    public ResponseBean combinationSearch() {
//        return normalSearch.boolSearch(KIBANA_SAMPLE_DATA_LOGS);
//    }
//
//    @ApiOperation(value = "聚集查询范例", notes = "聚集查询范例")
//    @RequestMapping(value = "/search/aggsExample", method = RequestMethod.POST)
//    public ResponseBean aggsExampleSearch() {
//        return aggsSearch.aggsExampleSearch(KIBANA_SAMPLE_DATA_FLIGHTS);
//    }
//
//
//    @ApiOperation(value = "聚集查询范例", notes = "聚集查询范例")
//    @RequestMapping(value = "/search/aggsExample", method = RequestMethod.POST)
//    public ResponseBean aggsExampleSearch() throws IOException {
//        String indexNmme = "books";
//        SearchRequest searchRequest = new SearchRequest();
//        searchRequest.indices(indexNmme);
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
//
//        searchRequest.source(searchSourceBuilder);
//
//        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//
//        SearchHit[] searchHits = response.getHits().getHits();
//        List<T> rows = Lists.newArrayList();
//        for (SearchHit hit : searchHits) {
//            Map<String, Object> stringObjectMap = ESBeanConvertor.underlineTocamelMap(hit.getSourceAsMap());
//            T dto = (T) parseGson(stringObjectMap, request.getClazz());
//            if (dto != null) {
//                rows.add(dto);
//            }
//        }
//
//
//    }
//}
