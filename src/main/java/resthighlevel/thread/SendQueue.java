package resthighlevel.thread;

import entity.AgentInfoKeyMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.LinkedBlockingQueue;

public class SendQueue {

    private final Logger log = LogManager.getLogger(SendQueue.class);
    private static LinkedBlockingQueue<AgentInfoKeyMapping> queue = new LinkedBlockingQueue<>();

    public void queueAdd(AgentInfoKeyMapping agentInfoEntity) {
        try {
            queue.put(agentInfoEntity);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    public static LinkedBlockingQueue<AgentInfoKeyMapping> getQueue() {
        return queue;
    }
}
