package insert;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestHighLevelClient;
import resthighlevel.documentcreate.DocumentCreate;
import resthighlevel.indexcreate.IndexCreate;
import resthighlevel.thread.DataReceiveThread;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AlertInsert {

    private final Logger log = LogManager.getLogger(AlertInsert.class);
    public final static String INDEX_NAME = "alert_info";
    private RestHighLevelClient client;

    public AlertInsert(RestHighLevelClient client) {
        this.client = client;
    }

    public synchronized void alertInsert(String alertToJson) {
        IndexCreate indexCreate = new IndexCreate(client);
        String typeName = getTypeName();

        // alert_info 인덱스 생성
        boolean existAlertIndex = indexCreate.existIndex(INDEX_NAME);

        if (!existAlertIndex) {
            indexCreate.createIndex(INDEX_NAME, typeName);
            log.info("[{}] {}/{} alert_agent index create !", DataReceiveThread.RESPONSE_OK, INDEX_NAME, typeName);
        }

        DocumentCreate documentCreate = new DocumentCreate();
        try {
            documentCreate.documentCreate(client, alertToJson, INDEX_NAME, typeName);
            log.info("[OK] {}/{} alert_agent document insert success", INDEX_NAME, typeName);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public String getTypeName() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(cal.getTime());
    }
}
