package insert;

import collect.CollectorServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import resthighlevel.documentcreate.DocumentCreate;
import resthighlevel.indexcreate.IndexCreate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ElasticsearchInsert {

    private String agentInfoEntity;
    private String javaInfoEntity;
    static final Logger log = LogManager.getLogger(ElasticsearchInsert.class);

    public ElasticsearchInsert(String agentInfoEntity, String javaInfoEntity) {
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
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String typeName = simpleDateFormat.format(date);

            /** RestHighLevelClient */
            // 인덱스 생성
            IndexCreate indexCreate = new IndexCreate();

            indexCreate.connection();

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
            documentCreate.documentCreate(indexCreate.getHighLevelClient(), getJavaInfoEntity(), CollectorServer.JAVA_INFO_INDEX, typeName);

            indexCreate.close();

        } catch (IOException e) {
            log.error("insert Document Failed !!");
            log.error(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
