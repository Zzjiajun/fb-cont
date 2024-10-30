//package cn.itcast.hotel;
//
//import cn.itcast.hotel.pojo.Hotel;
//import cn.itcast.hotel.pojo.HotelDoc;
//import cn.itcast.hotel.service.IHotelService;
//import com.alibaba.fastjson.JSON;
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import org.apache.http.HttpHost;
//import org.apache.lucene.search.TotalHits;
//import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
//import org.elasticsearch.action.bulk.BulkRequest;
//import org.elasticsearch.action.delete.DeleteRequest;
//import org.elasticsearch.action.delete.DeleteResponse;
//import org.elasticsearch.action.get.GetRequest;
//import org.elasticsearch.action.get.GetResponse;
//import org.elasticsearch.action.index.IndexRequest;
//import org.elasticsearch.action.index.IndexResponse;
//import org.elasticsearch.action.search.SearchRequest;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.action.update.UpdateRequest;
//import org.elasticsearch.action.update.UpdateResponse;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.client.RestClient;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.client.indices.CreateIndexRequest;
//import org.elasticsearch.client.indices.DeleteAliasRequest;
//import org.elasticsearch.client.indices.GetIndexRequest;
//import org.elasticsearch.common.unit.DistanceUnit;
//import org.elasticsearch.common.xcontent.XContentType;
//import org.elasticsearch.index.get.GetResult;
//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.QueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.SearchHits;
//import org.elasticsearch.search.builder.SearchSourceBuilder;
//import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
//import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
//import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
//import org.elasticsearch.search.sort.SortOrder;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.util.CollectionUtils;
//
//import java.io.IOException;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@SpringBootTest
//public class HotelIndexTest {
//
//    @Autowired
//    private IHotelService iHotelService;
//
//
//    public static final String MAPPING_TEMPLATE = "{\n" +
//            "  \"mappings\": {\n" +
//            "    \"properties\": {\n" +
//            "      \"id\":{\n" +
//            "        \"type\": \"keyword\"\n" +
//            "      },\n" +
//            "      \"name\":{\n" +
//            "        \"type\": \"text\",\n" +
//            "        \"analyzer\": \"ik_max_word\", \n" +
//            "        \"copy_to\": \"all\"\n" +
//            "      },\n" +
//            "      \"address\":{\n" +
//            "        \"type\": \"keyword\",\n" +
//            "        \"index\": false\n" +
//            "      },\n" +
//            "      \"price\":{\n" +
//            "        \"type\": \"integer\"\n" +
//            "      },\n" +
//            "      \"score\":{\n" +
//            "        \"type\": \"integer\"\n" +
//            "      },\n" +
//            "      \"brand\":{\n" +
//            "        \"type\": \"keyword\",\n" +
//            "        \"copy_to\": \"all\"\n" +
//            "      },\n" +
//            "      \"city\":{\n" +
//            "        \"type\": \"keyword\"\n" +
//            "      },\n" +
//            "      \"starName\":{\n" +
//            "        \"type\": \"keyword\"\n" +
//            "      },\n" +
//            "      \"business\":{\n" +
//            "        \"type\": \"keyword\",\n" +
//            "        \"copy_to\": \"all\"\n" +
//            "      },\n" +
//            "      \"location\":{\n" +
//            "        \"type\": \"geo_point\"\n" +
//            "      },\n" +
//            "      \"pic\":{\n" +
//            "        \"type\": \"keyword\",\n" +
//            "        \"index\": false\n" +
//            "      },\n" +
//            "      \"all\":{\n" +
//            "        \"type\": \"text\",\n" +
//            "        \"analyzer\": \"ik_max_word\"\n" +
//            "      }\n" +
//            "    }\n" +
//            "  }\n" +
//            "}";
//
//
//    //es 初始化
//    private RestHighLevelClient client;
//
//    //添加变量值 测试开始之前
//    @BeforeEach
//    void setUp() {
//        this.client = new RestHighLevelClient(RestClient.builder(
//                HttpHost.create("http://192.168.22.132:9200")
//        ));
//    }
//
//    //测试完后销毁
//    @AfterEach
//    void tearDown() throws IOException {
//        this.client.close();
//    }
//
//    //创建索引库
//    @Test
//    void createHotelIndex() throws IOException {
//        //1创建 Request对象  代表的 put/hotel
//        CreateIndexRequest request = new CreateIndexRequest("hotel");
//        //2准备请求参数 ：DSL 语句 具体每个字段的 mappings
//        request.source(MAPPING_TEMPLATE, XContentType.JSON);
//        //3发送请求 发送到es当中去
//        client.indices().create(request, RequestOptions.DEFAULT);
//
//    }
//
//    //删除索引库
//    @Test
//    void deleteHotelIndex() throws IOException {
//        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
//        client.indices().delete(request, RequestOptions.DEFAULT);
//    }
//
//    //判断是否存在
//    @Test
//    void existsHotelIndex() throws IOException {
//        GetIndexRequest request = new GetIndexRequest("hotel");
//        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
//        if (exists) {
//            System.out.println("存在");
//        } else {
//            System.out.println("不存在");
//        }
//        System.err.println(exists ? "存在" : "不存在");
//    }
//
//    //新增文档
//    @Test
//    void testAddDocument() throws IOException {
//        //查询数据
//        Hotel hotel = iHotelService.getById(60214L);
//        //转换为文档数据
//        HotelDoc hotelDoc = new HotelDoc(hotel);
//        //准备req对象
//        IndexRequest req = new IndexRequest("hotel").id(String.valueOf(hotelDoc.getId()));
//        //准备json文档
//        req.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
//        //发送请求
//        client.index(req, RequestOptions.DEFAULT);
//    }
//
//    //查询文档
//    @Test
//    void testGetDocument() throws IOException {
//        GetRequest request = new GetRequest("hotel").id("60214");
//        GetResponse response = client.get(request, RequestOptions.DEFAULT);
//        String sourceAsString = response.getSourceAsString();
//        HotelDoc hotelDoc = JSON.parseObject(sourceAsString, HotelDoc.class);
////        //先将map转为String字符串
////        String s = JSON.toJSONString(map);
////        //再将String转为Bank类
////        Bank bank = JSON.parseObject(s, Bank.class);
//        System.out.println(hotelDoc);
//    }
//
//    //更新文档
//    @Test
//    void testUpdateDocument() throws IOException {
//        UpdateRequest request = new UpdateRequest("hotel", "60214");
//        //准备数据
//        Map<String, Object> map = new HashMap<>();
//        map.put("price", "951");
//        map.put("starName", "一砖");
//        request.doc(map);
//        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
//    }
//
//    //删除文档
//    @Test
//    void testDeleteDocument() throws IOException {
//        DeleteRequest deleteRequest = new DeleteRequest("hotel").id("60214");
//        DeleteResponse response = client.delete(deleteRequest, RequestOptions.DEFAULT);
//        String s = response.toString();
//        System.out.println(s);
//    }
//
//
//    //需求：批量查询酒店数据，然后批量导入索引库中
//    //利用mybatis-plus查询酒店数据
//    //将查询到的酒店数据（Hotel）转换为文档类型数据（HotelDoc）
//    //利用JavaRestClient中的Bulk批处理，实现批量新增文档，示例代码如下
//    @Test
//    void testBulkRequest() throws IOException {
//        BulkRequest request = new BulkRequest();
//        QueryWrapper<Hotel> wrapper = new QueryWrapper<>();
//        wrapper.ne("id", 60214);
//        List<Hotel> list = iHotelService.list();
//        list.forEach(s -> {
//            HotelDoc hotelDoc = new HotelDoc(s);
//            //类似与添加一条文档的操作一样
//            request.add(new IndexRequest("hotel")
//                    .id(hotelDoc.getId().toString())
//                    .source(JSON.toJSONString(hotelDoc), XContentType.JSON));
//        });
//        client.bulk(request,RequestOptions.DEFAULT);
//    }
//
//
//    //不通过id来查询文档 对比match_all
//    @Test
//    void testMatchAll() throws IOException {
//        //1.准备Request
//        SearchRequest searchRequest = new SearchRequest("hotel");
//        //2.准备Dsl
//        searchRequest.source().query(QueryBuilders.matchAllQuery());
//        //3.发送请求
//        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
//        SearchHits hits = search.getHits();
//        //查询总条数
//        TotalHits totalHits = hits.getTotalHits();
//        //查询的结果数组
//        ArrayList<HotelDoc> hotelDocs = new ArrayList<>();
//        SearchHit[] hits1 = hits.getHits();
////        for (SearchHit hit:hits1){
////            String s = hit.getSourceAsString();
////            HotelDoc hotelDoc = JSON.parseObject(s, HotelDoc.class);
////            hotelDocs.add(hotelDoc);
////        }
//        hotelDocs= (ArrayList<HotelDoc>) Arrays.stream(hits1)
//                .map(hit -> JSON.parseObject(hit.getSourceAsString(), HotelDoc.class)).collect(Collectors.toList());
//        System.out.println(hotelDocs);
//    }
//
//    // 对比  "query": {
//    //    "match": {
//    //      "all": "外滩"
//    //    }
//    //  }  单字段
//    @Test
//    void testMatch() throws IOException {
//        SearchRequest searchRequest = new SearchRequest("hotel");
//        //单字段
//        searchRequest.source().query(QueryBuilders.matchQuery("all","如家"));
//        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
//    }
//
//    //"multi_match": {
//    //      "query": "上海",
//    //      "fields": ["brand","name"]
//    //    } 多字段
//    @Test
//    void testMultiMatch() throws IOException {
//        SearchRequest searchRequest = new SearchRequest("hotel");
//        //多字段
//        searchRequest.source().query(QueryBuilders.multiMatchQuery("如家","brand","name","business"));
//        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
//    }
//
//    //精确查询     "term": {
//    //      "city": {
//    //        "value": "深圳"
//    //      }
//    @Test
//    void testTerm() throws IOException {
//        SearchRequest searchRequest = new SearchRequest("hotel");
//        searchRequest.source().query(QueryBuilders.termQuery("city", "深圳"));
//        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
//    }
//    //范围查询 range 查询  对数字进行范围查询  gte 大于  lte 小于
//    //  "range": {
//    //      "price": {
//    //        "gte": 100,
//    //        "lte": 500
//    //      }
//    //    }
//    @Test
//    void testRange() throws IOException {
//        SearchRequest searchRequest = new SearchRequest("hotel");
//        searchRequest.source().query(QueryBuilders.rangeQuery("price").gte(100).lte(500));
//        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
//    }
//    /**
//     *  #复合查询 Boolean Query 布尔查询是一个或多个查询子句的组合。子查询的组合方式有
//     * #must：必须匹配每个子查询，类似“与”
//     * #should：选择性匹配子查询，类似“或”
//     * #must_not：必须不匹配，不参与算分，类似“非”
//     * #filter：必须匹配，不参与算分
//     * "bool": {
//     *       "must": [
//     *         {"match": {
//     *           "name": "如家"
//     *         }}
//     *       ],
//     *       "must_not": [
//     *         {"range": {
//     *           "price": {
//     *             "gte": 400
//     *           }
//     *         }}
//     *       ],
//     *       "filter": [
//     *         {"geo_distance":{"distance": "10km","location": "31.21,121.5"}}
//     *       ]
//     *     }
//     */
//    @Test
//    void testBoolean() throws IOException {
//        SearchRequest searchRequest = new SearchRequest("hotel");
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        //添加must条件
//        boolQueryBuilder.must(QueryBuilders.matchQuery("name","如家"));
//        //添加must_not条件
//        boolQueryBuilder.mustNot(QueryBuilders.rangeQuery("price").gte(400));
//        //添加filter条件
//        boolQueryBuilder.filter(QueryBuilders.geoDistanceQuery("location").distance("10km").point(31.21,121.5));
//        searchRequest.source().query(boolQueryBuilder);
//        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
//        SearchHits hits = search.getHits();
//        long total=hits.getTotalHits().value;
//    }
//
//    /**
//     * 排序
//     *  "query": {
//     *     "match_all": {}
//     *   },
//     *   "sort": [
//     *     {
//     *       "score": "desc"
//     *     },
//     *     {
//     *       "score": "asc"
//     *     }
//     *   ]
//     */
//    @Test
//    void testSort() throws IOException {
//        SearchRequest searchRequest = new SearchRequest("hotel");
//        searchRequest.source().query(QueryBuilders.matchAllQuery()).from(0).size(5).sort("price", SortOrder.ASC);
////        //分页
////        searchRequest.source().from(0).size(5);
////        //排序
////        searchRequest.source().sort("price", SortOrder.ASC);
//    }
//    /**
//     * 位置排序
//     *  "query": {
//     *     "match_all": {}
//     *   },
//     *   "sort": [
//     *     {
//     *       "_geo_distance": {
//     *         "location": {
//     *           "lat": 22.659218,
//     *           "lon": 114.025762
//     *         },
//     *         "order": "asc",
//     *         "unit": "km"
//     *       }
//     *     }
//     *   ]
//     */
//    @Test
//    void testSort1() throws IOException {
//        SearchRequest searchRequest = new SearchRequest("hotel");
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//        // 添加匹配所有文档的查询条件
//        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
//
//        // 添加地理距离排序条件
//        GeoDistanceSortBuilder geoDistanceSortBuilder = new GeoDistanceSortBuilder("location", 22.659218, 114.025762);
//        geoDistanceSortBuilder.order(SortOrder.ASC);
//        geoDistanceSortBuilder.unit();
//        searchSourceBuilder.sort(geoDistanceSortBuilder);
//        // 设置查询请求的源
//        searchRequest.source(searchSourceBuilder);
//        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
//        SearchHits hits = search.getHits();
//        long total=hits.getTotalHits().value;
//        SearchHit[] hits1 = hits.getHits();
//    }
//    /**
//     * 高亮
//     *   "highlight": {
//     *     "fields": {
//     *       "name": {
//     *         "require_field_match": "false",
//     *         "pre_tags": "<em>",
//     *         "post_tags": "</em>"
//     *       }
//     *     }
//     *   },
//     */
//    @Test
//    void testHighlight() throws IOException {
//        SearchRequest searchRequest = new SearchRequest("hotel");
//        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//        boolQuery.must(QueryBuilders.matchQuery("all","如家"));
//        boolQuery.mustNot(QueryBuilders.rangeQuery("price").gte(400));
//        boolQuery.filter(QueryBuilders.geoDistanceQuery("location").distance("10km").point(31.21,121.5));
//        //高亮
//        searchRequest.source().query(boolQuery).highlighter(new HighlightBuilder().field("name")
//        //是否需要匹配查询字段
//        .requireFieldMatch(false));
//        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
//        handleResponse(search);
//        //高亮的结果解析
//
//    }
//    private void handleResponse(SearchResponse response){
//        //解析响应
//        SearchHits hits = response.getHits();
//        //获取总条数
//        long total=hits.getTotalHits().value;
//        System.out.println("共搜索到"+total+"条数据");
//        //文档数组
//        SearchHit[] hitsHits = hits.getHits();
//        //遍历
//        for (SearchHit hit :hitsHits){
//            //获取文档source
//            String sourceAsString = hit.getSourceAsString();
//            HotelDoc hotelDoc = JSON.parseObject(sourceAsString, HotelDoc.class);
//            //获取高亮结果
//            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
//            if (!CollectionUtils.isEmpty(highlightFields)){
//                //根据字段获取高亮结果
//                HighlightField name = highlightFields.get("name");
//                if (name!=null){
//                    //获取结果
//                    String s = name.getFragments()[0].toString();
//                    hotelDoc.setName(s);
//                }
//            }
//            System.out.println("hotelDoc"+hotelDoc);
//        }
//    }
//}
