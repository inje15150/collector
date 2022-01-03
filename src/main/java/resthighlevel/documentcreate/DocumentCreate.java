package resthighlevel.documentcreate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;

public class DocumentCreate {

    private final Logger log = LogManager.getLogger(DocumentCreate.class);

    public void documentCreate(RestHighLevelClient client, String toJson, String indexName, String typeName) throws IOException {

        IndexRequest request = new IndexRequest(indexName, typeName); // request 객체 생성

        request.source(toJson, XContentType.JSON);

        IndexResponse index = client.index(request, RequestOptions.DEFAULT);// request document create

//        log.info( "[{}/{}] : insert document successfully !", indexName, typeName);
    }
}
