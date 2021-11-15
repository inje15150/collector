package resthighlevel.thread;

import com.google.gson.Gson;
import entity.AgentInfoEntity;
import entity.JavaInfoEntity;
import insert.ElasticsearchInsert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataReceiveThread extends Thread {

    static Logger log = LogManager.getLogger(DataReceiveThread.class);
    private Socket socket;
    private String key;
    private final static String keyRule = "%%#!";
    private final static String RESPONSE_OK = "ok";
    private boolean stop;
    private Map<String, Socket> socketList = new ConcurrentHashMap<>();
    private Gson gson = new Gson();

    public DataReceiveThread(Socket socket) {
        this.socket = socket;
        this.stop = false;
    }
    @Override
    public void run() {
        BufferedReader reader = null;
        PrintWriter writer = null;

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
                }
                if (isEqualKey(data)) {
                    log.info("[{}] key authentication success !", RESPONSE_OK);
                    response_OK(writer); // ok 전송

                    String agentInfo = reader.readLine(); // 데이터 수신 대기
                    AgentInfoEntity agentInfoEntity = dataReceive(agentInfo, AgentInfoEntity.class);// agentInfo class 전달 받아 담기

                    String agentInfoEntityToJson = gson.toJson(agentInfoEntity); // 담은 agentInfoEntity insert 를 위한 json 변환
                    response_OK(writer); // ok 전송

                    String javaInfo = reader.readLine();
                    JavaInfoEntity javaInfoEntity = dataReceive(javaInfo, JavaInfoEntity.class); // javaInfo class 전달 받아 담기
                    String javaInfoEntityToJson = gson.toJson(javaInfoEntity); // 담은 javaInfoEntity insert 를 위한 json 변환

                    response_OK(writer); // ok 전송

                    elasticSearchInsert(agentInfoEntityToJson, javaInfoEntityToJson); // agentInfo, javaInfo insert

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
                try {
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                writer.close();
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
        ElasticsearchInsert insert = new ElasticsearchInsert(agentInfo, javaInfo);
        insert.insert();
    }
}
