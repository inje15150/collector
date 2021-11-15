package collect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import resthighlevel.thread.DataReceiveThread;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CollectorServer {

    static Logger log = LogManager.getLogger(CollectorServer.class);
    public static final int ES_PORT = 9200;
    public static final int PORT = 8080;
    public final static String AGENT_INFO_INDEX = "agent_info";
    public final static String JAVA_INFO_INDEX = "java_info";

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            while (true) {
                try {
                    log.info("Server Socket Open");
                    socket = serverSocket.accept();
                    log.info("[{}:{} Client connection]", socket.getInetAddress().getHostAddress(), socket.getLocalPort());

                    DataReceiveThread receive = new DataReceiveThread(socket);
                    receive.start();
                    log.info("[{}] Start.", receive.getName());

                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
