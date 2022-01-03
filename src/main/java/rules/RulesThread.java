package rules;

import com.google.gson.Gson;
import entity.*;
import insert.AlertInsert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestHighLevelClient;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class RulesThread extends Thread {

    private final Logger log = LogManager.getLogger(RulesThread.class);
    public final static int CPU_TYPE = 1;
    public final static int MEM_TYPE = 2;
    private RestHighLevelClient client = null;
    private RulesMapping rulesMapping;
    private LinkedBlockingQueue<AgentInfoKeyMapping> queue;
    public RulesThread(RestHighLevelClient client, RulesMapping rulesMapping) {
        this.client = client;
        this.rulesMapping = rulesMapping;
        this.queue = new LinkedBlockingQueue<>();
    }

    public Rules getRule() {
        return rulesMapping.getRules();
    }

    public List<String> getAgentIdList() {
        return rulesMapping.getAgentList();
    }

    @Override
    public void run() {
        log.info(Thread.currentThread().getName() + " start");
        Map<String, List<ValueByAgentInfo>> keyByAgentList = new HashMap<>();
        while (true) {
            try {
                AgentInfoKeyMapping agentInfoKeyMapping = queue.take();

                // 모든 agentInfo 정보를 전달 받아 해당 쓰레드 rule 이 CPU 타입일 때는 agent cpu 체크
                if (rulesMapping.getRules().getResource_type() == CPU_TYPE) {
                    typeByAdd(keyByAgentList, rulesMapping.getValueAndLevelComment(), agentInfoKeyMapping, agentInfoKeyMapping.getAgentInfoEntity().getCpu());
                }
                // 모든 agentInfo 정보를 전달 받아 해당 쓰레드 rule 이 MEMORY 타입일 때는 agent memory 체크
                else if (rulesMapping.getRules().getResource_type() == MEM_TYPE) {
                    typeByAdd(keyByAgentList, rulesMapping.getValueAndLevelComment(), agentInfoKeyMapping, agentInfoKeyMapping.getAgentInfoEntity().getMemory());
                }
                // TODO: 2021-12-20 resource_type 추가 시 로직 추가

                // key 별 value list size 체크
                for (String key : keyByAgentList.keySet()) {
                    List<ValueByAgentInfo> valueByAgentInfos = keyByAgentList.get(key);
                    // list size 가 rule count 와 같을 때
                    if (valueByAgentInfos.size() == rulesMapping.getRules().getCount()) {
                        float min = 100;
                        for (ValueByAgentInfo valueByAgentInfo : valueByAgentInfos) {
                            if (valueByAgentInfo.getRules_value() < min) {
                                min = valueByAgentInfo.getRules_value();
                            }
                        }
                        // 가장 처음 list agent 정보
                        AgentInfoEntity firstAgentInfo = valueByAgentInfos.get(0).getAgentInfoEntity();
                        // 가장 마지막 list agent 정보
                        AgentInfoEntity lastAgentInfo = valueByAgentInfos.get(valueByAgentInfos.size() - 1).getAgentInfoEntity();

                        Long diffTime = timeDifference(firstAgentInfo.getEvent_time(), lastAgentInfo.getEvent_time());

                        if (diffTime != null) {
                            if (diffTime < rulesMapping.getRules().getDuration_time()) {
                                alertElasticsearchInsert(rulesMapping.getRules(), new AlertInsert(client), agentInfoKeyMapping.getAgentInfoEntity(), min, valueByAgentInfos, key);
                            }
                        }
                        // 가장 처음 원소 제거
                        keyByAgentList.get(key).remove(0);
                    }
                }
            } catch (InterruptedException e) {
                log.error("Thread stop..");
                break;
            }
        }
    }

    // Map<Key, 해당 value list>
    public void typeByAdd(Map<String, List<ValueByAgentInfo>> keyByAgentList, Map<String, Float> valueByComment, AgentInfoKeyMapping agentInfoKeyMapping, float resource_value) {

        // key 값 존재하지 않을 때 최초 담을 List 생성
        List<ValueByAgentInfo> eventTimeByValue = new ArrayList<>();
        if (keyByAgentList.isEmpty() || !keyByAgentList.containsKey(agentInfoKeyMapping.getKey())) {
            // N개의 value 중 해당되는 최종 value
            float finalValue = finalValue(valueByComment, resource_value);

            //finalValue 0이 아니면 key 별 list add
            if (finalValue != 0) {
                eventTimeByValue.add(new ValueByAgentInfo(agentInfoKeyMapping.getAgentInfoEntity(), finalValue));
                keyByAgentList.put(agentInfoKeyMapping.getKey(), eventTimeByValue);
            }
        } else {
            // 해당 key 의 value list 추출
            List<ValueByAgentInfo> valueByAgentInfoList = keyByAgentList.get(agentInfoKeyMapping.getKey());

            float finalValue = finalValue(valueByComment, resource_value);

            if (finalValue != 0) {
                valueByAgentInfoList.add(new ValueByAgentInfo(agentInfoKeyMapping.getAgentInfoEntity(), finalValue));
                keyByAgentList.put(agentInfoKeyMapping.getKey(), valueByAgentInfoList);
            }
        }
    }

    // Map <"심각", 90>, <"경계", 80> ...
    public float finalValue(Map<String, Float> commentByValue, float agent_resource) {
        float rule_value = 0;
        // 조건에 해당되는 rule_value 중 큰 값
        for (String comment : commentByValue.keySet()) {
            if (commentByValue.get(comment) < agent_resource) {
                if (rule_value < commentByValue.get(comment)) {
                    rule_value = commentByValue.get(comment);
                }
            }
        }
        return rule_value;
    }

    public Long timeDifference(String firstTime, String lasTime) {
        try {
            DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date firstDate = df.parse(firstTime);
            Date lastDate = df.parse(lasTime);

            return (lastDate.getTime() - firstDate.getTime()) / 1000;

        } catch (ParseException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public void addQueue(AgentInfoKeyMapping agentInfoKeyMapping) {
        try {
            queue.put(agentInfoKeyMapping);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void alertElasticsearchInsert(Rules rules, AlertInsert alertInsert, AgentInfoEntity agentInfoEntity, float min, List<ValueByAgentInfo> valueByAgentInfos, String key) {
        Map<String, Float> valueAndLevelComment = rulesMapping.getValueAndLevelComment();
        List<Alert_Reason> alert_reason = new ArrayList<>();

        for (ValueByAgentInfo valueByAgentInfo : valueByAgentInfos) {
            if (rules.getResource_type() == CPU_TYPE) {
                Alert_Reason_Cpu alert_reason_cpu = new Alert_Reason_Cpu(valueByAgentInfo.getAgentInfoEntity().getCpu(), valueByAgentInfo.getAgentInfoEntity().getEvent_time());
                alert_reason.add(alert_reason_cpu);
            } else if (rules.getResource_type() == MEM_TYPE) {
                Alert_Reason_Memory alert_reason_memory = new Alert_Reason_Memory(valueByAgentInfo.getAgentInfoEntity().getMemory(), valueByAgentInfo.getAgentInfoEntity().getEvent_time());
                alert_reason.add(alert_reason_memory);
            }
        }

        for (String comment : valueAndLevelComment.keySet()) {
            if (valueAndLevelComment.get(comment) == min) {
                AlertInfoEntity alertInfoEntity = new AlertInfoEntity(rules.getRules_id(), rules.getResource_type(), comment, valueAndLevelComment.get(comment), key, alert_reason);
                Gson gson = new Gson();
                String toJson = gson.toJson(alertInfoEntity);
                alertInsert.alertInsert(toJson);
                getAlertConsole(rulesMapping.getRules().getResource_type(), rulesMapping.getRules(), agentInfoEntity, valueAndLevelComment.get(comment), comment);
            }
        }
    }

    public void getAlertConsole(int type, Rules rules, AgentInfoEntity agentInfoEntity, float value, String comment) {
        System.out.println("===============================");
        System.out.println("Thread_name: " + currentThread().getName());
        System.out.println("ip: " + agentInfoEntity.getIp());
        System.out.println("type: " + type);
        System.out.println("count: " + rules.getCount());
        System.out.println("duration_time: " + rules.getDuration_time());
        System.out.println("value: " + value);
        System.out.println("resource_type: " + rules.getResource_type());
        System.out.println("level: " + comment);
        System.out.println("event_time: " + agentInfoEntity.getEvent_time());
        System.out.println("===============================");
    }
}

