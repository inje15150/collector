package insert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ElasticsearchSearch {

    private final HttpGet get;

    public ElasticsearchSearch() {
        get = new HttpGet("http://192.168.60.80:9200/_cat/nodes?format=json&filter_path=ip,name");
        get.setHeader("Accept", "application/json");
        get.setHeader("Content-Type", "application/json");
        get.setHeader("keep-alive", "false");
    }

    public Map<String, String> search() {
        Map<String, String> resultMap = new ConcurrentHashMap<>();
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse response = httpClient.execute(get);

            String result = EntityUtils.toString(response.getEntity());

            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, String>> readValue = objectMapper.readValue(result, new TypeReference<List<Map<String, String>>>(){});

            for (Map<String, String> stringStringMap : readValue) {
                String ip = stringStringMap.get("ip");
                String name = stringStringMap.get("name");

                resultMap.put(ip, name);
            }
            return resultMap;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultMap;
    }
}
