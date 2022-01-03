package insert;

import collect.CollectorServer;
import com.google.gson.Gson;
import entity.AgentInfoEntity;
import entity.ProcessInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestHighLevelClient;
import resthighlevel.documentcreate.DocumentCreate;
import resthighlevel.indexcreate.IndexCreate;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ElasticsearchInsert {

    private final Logger log = LogManager.getLogger(ElasticsearchInsert.class);
    private String agentInfoEntity;
    private String javaInfoEntity;
    private RestHighLevelClient client;

    public ElasticsearchInsert(RestHighLevelClient client, String agentInfoEntity, String javaInfoEntity) {
        this.client = client;
        this.agentInfoEntity = agentInfoEntity;
        this.javaInfoEntity = javaInfoEntity;
    }

    public String getAgentInfoEntity() {
        return agentInfoEntity;
    }

    public String getJavaInfoEntity() {
        return javaInfoEntity;
    }

    public synchronized void insert() {
        try {
            String typeName = getTypeName();

            /** RestHighLevelClient */
            // 인덱스 생성
            IndexCreate indexCreate = new IndexCreate(client);

//            indexCreate.connection();

            boolean agentInfoAck = indexCreate.existIndex(CollectorServer.AGENT_INFO_INDEX); // agent_info index exist
            boolean javaInfoAck = indexCreate.existIndex(CollectorServer.JAVA_INFO_INDEX); // java_info index exist

            // 인덱스 존재하지 않을 시 인덱스 생성
            if (!agentInfoAck) {
                indexCreate.createIndex(CollectorServer.AGENT_INFO_INDEX, typeName);
            }
            if (!javaInfoAck) {
                indexCreate.createIndex(CollectorServer.JAVA_INFO_INDEX, typeName);
            }

            // 도큐먼트 생성
            DocumentCreate documentCreate = new DocumentCreate();

            documentCreate.documentCreate(indexCreate.getHighLevelClient(), getAgentInfoEntity(), CollectorServer.AGENT_INFO_INDEX, typeName);

            for (int i = 1; i < 100; i++) {
                AgentInfoEntity agentInfo = addAgentInfo(i);
                Gson gson = new Gson();
                String agentInfoToJson = gson.toJson(agentInfo);
                documentCreate.documentCreate(indexCreate.getHighLevelClient(), agentInfoToJson, CollectorServer.AGENT_INFO_INDEX, typeName);
            }
            documentCreate.documentCreate(indexCreate.getHighLevelClient(), getJavaInfoEntity(), CollectorServer.JAVA_INFO_INDEX, typeName);

        } catch (IOException e) {
            log.error("insert Document Failed !!");
            log.error(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public static String getTypeName() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(date);
    }

    public AgentInfoEntity addAgentInfo(int count) {
        List<ProcessInfo> list = new ArrayList<>();
        list.add(new ProcessInfo("process" + count, "pid" + count, "30"));

        return new AgentInfoEntity(
                "hostName" + count,
                "Linux CentOS" + count,
                "eth" + count,
                "111.111.111." + count,
                "111.111.111.1",
                "aa:aa:aa:aa:" + count,
                5,
                70,
                30,
                nowDate(),
                list
        );
    }

    public String nowDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return df.format(cal.getTime());
    }
}

