package collect;

import mariadb.DataBaseCon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestHighLevelClient;
import resthighlevel.ESConnection;
import resthighlevel.thread.DataReceiveThread;
import resthighlevel.thread.RdbSelectThread;
import resthighlevel.thread.TakeQueue;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CollectorServer {

    private static final Logger log = LogManager.getLogger(CollectorServer.class);
    public static final int ES_PORT = 9200;
    public static final int PORT = 8080;
    public final static String AGENT_INFO_INDEX = "agent_info";
    public final static String JAVA_INFO_INDEX = "java_info";

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket socket = null;
        DataBaseCon dataBaseCon = new DataBaseCon();
        ESConnection connection = new ESConnection();

        try {
            serverSocket = new ServerSocket(PORT);
            dataBaseCon.connection();
            RestHighLevelClient client = connection.connection();

            // 주기적으로 rules 체크하여 rule 쓰레드 관리해주는 관리자쓰레드
            RdbSelectThread rdbSelectThread = new RdbSelectThread(dataBaseCon, client);
            rdbSelectThread.start();

            // 큐에서 데이터를 꺼내어 처리하는 쓰레드
            TakeQueue takeQueue = new TakeQueue();
            takeQueue.start();

            int errCount = 0;
            while (true) {
                try {
                    log.info("Server Socket Open");
                    socket = serverSocket.accept();
                    log.info("[{}:{} Client connection]", socket.getInetAddress().getHostAddress(), socket.getLocalPort());

                    DataReceiveThread receive = new DataReceiveThread(socket, dataBaseCon, client);
                    receive.start();
                    log.info("[{}] Start.", receive.getName());

                    errCount = 0;

                } catch (Exception e) {
                    if (errCount < 6) {
                        errCount++;
                        log.error(e.getMessage());
                        log.error("[ERROR] retry.. {}", errCount);
                    } else break;
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            dataBaseCon.close();
            connection.close();
        }
    }
}
