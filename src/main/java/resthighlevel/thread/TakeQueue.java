package resthighlevel.thread;

import entity.AgentInfoKeyMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchStatusException;
import rules.RulesThread;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class TakeQueue extends Thread{

    private final Logger log = LogManager.getLogger(TakeQueue.class);

    @Override
    public void run() {
        int errCount = 0;

        while (true) {
            try {
                LinkedBlockingQueue<AgentInfoKeyMapping> queue = SendQueue.getQueue();
                AgentInfoKeyMapping agentInfoKeyMapping = queue.take(); // take 대기
                // rules 정보 가져오기
                List<RulesThread> list = RdbSelectThread.getList();

                if (!list.isEmpty()) {
                    for (RulesThread thread : list) {
                        // 각 쓰레드의 agent list 포함되어 있을 경우에만 해당 쓰레드 큐에 데이터 넣기
                        if (thread.getAgentIdList().contains(agentInfoKeyMapping.getKey())) {
                            thread.addQueue(agentInfoKeyMapping);
                        }
                    }
                }
                errCount = 0;
            } catch (ElasticsearchStatusException e) {
                if (errCount < 6) {
                    errCount++;
                    log.error(e.getMessage());
                    log.error("retry[{}]", errCount);
                } else break;
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }
}
