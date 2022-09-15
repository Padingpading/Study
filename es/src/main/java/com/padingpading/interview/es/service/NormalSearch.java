//package com.padingpading.interview.es.service;
//
//import com.padingpading.interview.es.vo.ResponseBean;
//import org.elasticsearch.action.search.SearchRequest;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.common.unit.Fuzziness;
//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.FuzzyQueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.index.query.TermQueryBuilder;
//import org.elasticsearch.search.builder.SearchSourceBuilder;
//import org.elasticsearch.search.sort.FieldSortBuilder;
//import org.elasticsearch.search.sort.SortOrder;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//
//@Service
//public class NormalSearch {
//
//    @Resource
//    private RestHighLevelClient restHighLevelClient;
//
//    @Resource
//    private SendSearchRequest sendSearchRequest;
//    /**
//     * get kibana_sample_data_flights/_search
//     * {
//     * 	"from":100,
//     * 	"size":20,
//     * 	"query":{
//     * 		"match_all":{}
//     *        },
//     * 	"_source":["Origin*","*Weather"],
//     * 	"sort":[{"DistanceKilometers":"asc"},{"FlightNum":"desc"}]
//     * }
//     * @param indexName
//     * @return
//     */
//    public ResponseBean searchExample(String indexName) {
//        SearchRequest searchRequest = new SearchRequest();
//        searchRequest.indices(indexName);
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        searchSourceBuilder.from(0);
//        searchSourceBuilder.size(5);
//        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
//        String[] includeFields = new String[]{"Origin*","*Weather"};
//        searchSourceBuilder.fetchSource(includeFields,null);
//        searchSourceBuilder.sort(new FieldSortBuilder("DistanceKilometers")
//                .order(SortOrder.ASC)
//
//        );
//        searchSourceBuilder.sort(new FieldSortBuilder("FlightNum")
//                .order(SortOrder.DESC)
//
//        );
//        searchRequest.source(searchSourceBuilder);
//        return sendSearchRequest.send(searchRequest, RequestOptions.DEFAULT);
//    }
//
//    /**
//     * get kibana_sample_data_flights/_search
//     * {
//     * 	"query":{
//     * 		"term":{
//     * 			"dayOfWeek":3
//     *                }* 	}
//     * }
//     * @param indexName
//     * @param fieldName
//     * @return
//     */
//    public ResponseBean termsBasedSearch(String indexName,
//                                         String fieldName) {
//        SearchRequest searchRequest = new SearchRequest();
//        //索引名称
//        searchRequest.indices(indexName);
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        //字段名称,和索引值
//        TermQueryBuilder termQueryBuilder =
//                QueryBuilders.termQuery(fieldName,3);
//        searchSourceBuilder.query(termQueryBuilder);
//        searchRequest.source(searchSourceBuilder);
//        return sendSearchRequest.send(searchRequest, RequestOptions.DEFAULT);
//    }
//
//    /**全文检索,fields包含AT
//     * POST /kibana_sample_data_flights/_search
//     * {
//     * 	"query": {
//     * 		"multi_match": {
//     * 			"query":"AT",
//     * 			"fields":["DestCountry", "OriginCountry"]
//     *                }* 	}
//     * }
//     */
//    public ResponseBean matchBasedSearch(String indexName,
//                                         Object text, String... fieldsName) {
//        SearchRequest searchRequest = new SearchRequest();
//        searchRequest.indices(indexName);
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(text,fieldsName));
//
//        searchRequest.source(searchSourceBuilder);
//        return sendSearchRequest.send(searchRequest, RequestOptions.DEFAULT);
//    }
//
//    /**模糊查询
//     * get kibana_sample_data_logs/_search
//     * {
//     *     "query": {
//     *         "fuzzy": {
//     *             "message": {
//     *                 "value": "firefix",
//     *                 "fuzziness": "1" 编辑距离。
//     *             }
//     *         }
//     *     }
//     * }
//     * @return
//     */
//    public ResponseBean fuzzySearch(String indexName,
//                                    String fieldsName, String text) {
//        SearchRequest searchRequest = new SearchRequest();
//        searchRequest.indices(indexName);
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//        FuzzyQueryBuilder fuzzyQueryBuilder = QueryBuilders.fuzzyQuery(fieldsName, text);
//        //编辑距离。
//        fuzzyQueryBuilder.fuzziness(Fuzziness.ONE);
//
//        searchSourceBuilder.query(fuzzyQueryBuilder);
//        searchRequest.source(searchSourceBuilder);
//        return sendSearchRequest.send(searchRequest, RequestOptions.DEFAULT);
//    }
//
//    /**布尔查询
//     * POST /kibana_sample_data_logs/_search
//     * {
//     * 	"query": {
//     * 		"bool": {
//     * 			"must":[
//     *                                {"match": { "message": "firefox"} }
//     * 			],
//     * 			"should":[
//     *                {"term": { "geo. src": "CN"}},
//     *                {"term": { "geo. dest": "CN"}}
//     * 			]
//     * 		}
//     * 	}
//     * }
//     * @param indexName
//     * @return
//     */
//    public ResponseBean boolSearch(String indexName) {
//        SearchRequest searchRequest = new SearchRequest();
//        searchRequest.indices(indexName);
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        boolQueryBuilder
//                .must(QueryBuilders.matchQuery("message","firefox"))
//                .should(QueryBuilders.termQuery("geo.src","CN"))
//                .should(QueryBuilders.termQuery("geo.dest","CN"));
//
//        searchSourceBuilder.query(boolQueryBuilder);
//        searchRequest.source(searchSourceBuilder);
//        return sendSearchRequest.send(searchRequest, RequestOptions.DEFAULT);
//    }
//}
