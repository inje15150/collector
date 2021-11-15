package resthighlevel.search;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import resthighlevel.indexcreate.IndexCreate;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Search {

    static final Logger log = LogManager.getLogger(Search.class);
    SearchRequest searchRequest = null;
    IndexCreate create = new IndexCreate();
    RestHighLevelClient client = null;

    public Search() {
        create.connection();
        client = create.getHighLevelClient();
    }

    // 인덱스 별 전체 조회
    public void matchAllQuery(String indexName) {
        try {
            searchRequest = new SearchRequest((indexName));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            searchRequest.source(searchSourceBuilder);

            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            log.info("result : {}", response);

        } catch (IOException e) {
            log.info("search fail !! {}", e.getMessage());
        }
    }

    // 인덱스 일별 데이터 조회
    public void matchAllQuery(String indexName, String typeName) {
        try {
            searchRequest = new SearchRequest(indexName);
            searchRequest.types(typeName);

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            searchRequest.source(searchSourceBuilder);

            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            log.info("result : {}", response);

        } catch (IOException e) {
            log.error("search fail !! {}", e.getMessage());
        }
    }

    // 인덱스 실시간 분당 데이터 조회
    public void dataPerMin(String indexName, String typeName) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String lt = df.format(cal.getTime()) + ":00";

            cal.add(Calendar.MINUTE, -1);

            String gte = df.format(cal.getTime()) + ":00";

            searchRequest = new SearchRequest(indexName);
            searchRequest.types(typeName);

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders
                    .rangeQuery("event_time")
                    .gte(gte)
                    .lt(lt));
            searchRequest.source(searchSourceBuilder);

            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            log.info("result : {}", response);

        } catch (Exception e) {
            log.error("search fail !! {}", e.getMessage());
        }
    }

    // 인덱스 시간별 데이터 조회
    public void dataPerDay(String indexName, String typeName) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH");
            String gte = df.format(cal.getTime()) + ":00:00";

            cal.add(Calendar.HOUR, 1);
            String lt = df.format(cal.getTime()) + ":00:00";

            searchRequest = new SearchRequest(indexName);
            searchRequest.types(typeName);

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(0);
            searchSourceBuilder.size(60);
            searchSourceBuilder.query(QueryBuilders
                    .rangeQuery("event_time")
                    .gte(gte)
                    .lt(lt));
            searchRequest.source(searchSourceBuilder);

            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            log.info("result : {}", response);

        } catch (IOException e) {
            log.info("search fail !! {}", e.getMessage());
        }
    }
}
