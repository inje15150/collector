package resthighlevel;

import collect.CollectorServer;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

public class ESConnection {

    private RestHighLevelClient client;

    public RestHighLevelClient connection() {
        client = new RestHighLevelClient(RestClient.builder(new HttpHost("192.168.60.80", CollectorServer.ES_PORT, "http")));
        return client;
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
