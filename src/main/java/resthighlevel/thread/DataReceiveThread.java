package resthighlevel.thread;

import com.google.gson.Gson;
import entity.AgentInfoEntity;
import entity.AgentInfoKeyMapping;
import entity.JavaInfoEntity;
import insert.ElasticsearchInsert;
import insert.ElasticsearchSearch;
import mariadb.DataBaseCon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class DataReceiveThread extends Thread {

    private final Logger log = LogManager.getLogger(DataReceiveThread.class);
    private Socket socket;
    private String key;
    private final static String keyRule = "%%#!";
    public final static String RESPONSE_OK = "ok";
    private boolean stop;
    private Gson gson = new Gson();
    private final static int CPU_TYPE = 1;
    private final static int MEM_TYPE = 2;
    private final DataBaseCon dataBaseCon;
    private final SendQueue sendQueue = new SendQueue();
    private RestHighLevelClient client;


    public DataReceiveThread(DataBaseCon dataBaseCon) {
        this.dataBaseCon = dataBaseCon;
    }

    public DataReceiveThread(Socket socket, DataBaseCon dataBaseCon, RestHighLevelClient client) {
        this.socket = socket;
        this.stop = false;
        this.dataBaseCon = dataBaseCon;
        this.client = client;
    }

    public String getKey() {
        return key;
    }

    @Override
    public void run() {
        BufferedReader reader = null;
        PrintWriter writer = null;
        // _cat/nodes api 사용하여 ip 별 노드네임 정보 얻어오기
        ElasticsearchSearch search = new ElasticsearchSearch();
        Map<String, String> nodeName = search.search();

        while (!stop) {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());

                String data = reader.readLine();

                if (key == null) {
                    stop = keyAuthentication(reader, writer, data); // 키 값이 null 일 때 들어온 키 값 인증
                    if (stop) {
                        log.error("[{}] Not exact key value", "ERROR");
                        reader.close();
                        writer.close();
                        break;
                    }
                    log.info("[{}] key authentication success !", RESPONSE_OK);
                }
                if (isEqualKey(data)) {
                    response_OK(writer); // ok 전송

                    String agentInfo = reader.readLine(); // 데이터 수신 대기
                    AgentInfoEntity agentInfoEntity = dataReceive(agentInfo, AgentInfoEntity.class);// agentInfo class 전달 받아 담기

                    String agentInfoEntityToJson = gson.toJson(agentInfoEntity); // 담은 agentInfoEntity insert 를 위한 json 변환
                    response_OK(writer); // ok 전송

                    String javaInfo = reader.readLine();
                    JavaInfoEntity javaInfoEntity = dataReceive(javaInfo, JavaInfoEntity.class); // javaInfo class 전달 받아 담기
                    String javaInfoEntityToJson = gson.toJson(javaInfoEntity); // 담은 javaInfoEntity insert 를 위한 json 변환

                    response_OK(writer); // ok 전송

                    // DB 키 값들 select 후 list 담기
                    if (!dataBaseCon.select().contains(key)) { // 현재 key 값이 포함되어 있지 않으면 insert
                        dataBaseCon.insert(agentInfoEntity.getIp(), getKey(), agentInfoEntity.getMac_address(), agentInfoEntity.getGateway(), agentInfoEntity.getOsInfo(), nodeName.get(agentInfoEntity.getIp()));
                    } else {
                        dataBaseCon.update(agentInfoEntity.getIp(), getKey(), agentInfoEntity.getMac_address(), agentInfoEntity.getGateway(), agentInfoEntity.getOsInfo(), nodeName.get(agentInfoEntity.getIp()));
                    }

                    // elasticSearch insert
                   elasticSearchInsert(agentInfoEntityToJson, javaInfoEntityToJson);// agentInfo, javaInfo insert

                    // queue 에 담기
                    sendQueue.queueAdd(new AgentInfoKeyMapping(key, agentInfoEntity));

                } else {
                    log.error("Incorrect key between server and client.");
                    writer.println("incorrect key");
                    writer.flush();

                    reader.close();
                    writer.close();
                    stop = true;
                    break;
                }
            } catch (IOException e) {
                log.error(e.getMessage()); // 쓰레드 종료
                stop = true;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        log.error(ex.getMessage());
                    }
                }
                if (writer != null) {
                    writer.close();
                }
                break;
            }
        }
    }

    public boolean isEqualKey(String data) {
        try {
            return key.equals(data);
        } catch (NullPointerException e) {
            log.error("key binding error");
            return false;
        }
    }

    // 서버 측에 키 값이 존재하지 않을 때
    public boolean keyAuthentication(BufferedReader reader, PrintWriter writer, String data) throws IOException {
        if (data.split(keyRule).length == 3) { // 규칙에 맞는 키 값이면 false 반환
            key = data;
            log.info("key save success.");
            return false;
        } else { // 전달 온 키 값이 규칙과 일치하지 않으면 true 반환
            log.error("key authentication failed..");
            writer.println("key authentication failed");
            writer.flush();

            reader.close();
            writer.close();
            return true;
        }
    }

    // 서버 측에 키 값이 존재
    public <T> T dataReceive(String entity, Class<T> clazz) {

        T t = gson.fromJson(entity, clazz);
        log.info("[{}] {} data receive successfully.", RESPONSE_OK, clazz.getSimpleName());

        return t;
    }

    public void response_OK(PrintWriter writer) {
        writer.println(RESPONSE_OK);
        writer.flush();
    }

    public void elasticSearchInsert(String agentInfo, String javaInfo) {
        ElasticsearchInsert insert = new ElasticsearchInsert(client, agentInfo, javaInfo);
        insert.insert();
    }
}
