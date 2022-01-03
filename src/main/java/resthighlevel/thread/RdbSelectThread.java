package resthighlevel.thread;

import entity.Rules;
import entity.RulesMapping;
import mariadb.DataBaseCon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestHighLevelClient;
import rules.RulesThread;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RdbSelectThread extends Thread{

    private final Logger log = LogManager.getLogger(RdbSelectThread.class);
    private DataBaseCon dataBaseCon;
    private static List<RulesMapping> currentRulesMapping = new ArrayList<>();
    private RestHighLevelClient client;
    private static List<RulesThread> list = new ArrayList<>();

    public RdbSelectThread(DataBaseCon dataBaseCon, RestHighLevelClient client) {
        this.dataBaseCon = dataBaseCon;
        this.client = client;
    }

    public static List<RulesThread> getList() {
        return list;
    }

    @Override
    public void run() {
        int errCount = 0;
        // 쓰레드 관리 list
        while (true) {
            try {
                List<Rules> rules = dataBaseCon.getRules();
                List<RulesMapping> newRulesMapping = new ArrayList<>();

                for (Rules rule : rules) {
                    List<String> agentList = dataBaseCon.mappingAgent(rule.getRules_id());
                    Map<String, Float> valueAndLevelComment = dataBaseCon.mappingRiskLevel(rule.getRules_id());
                    newRulesMapping.add(new RulesMapping(rule, agentList, valueAndLevelComment));
                }

                // 최초 실행 시 rules Thread 실행
                // TODO: 2021-12-17 기능 나누기
                if (currentRulesMapping.isEmpty()) {
                    firstThreadStart(newRulesMapping, list);
                } else { // rules 가 존재하면 rules 변경 여부 체크
                    for (RulesMapping mapping : currentRulesMapping) {
                        for (RulesMapping newMapping : newRulesMapping) {
                            if (mapping.getRules().getRules_id() == newMapping.getRules().getRules_id()) {
                                // 기존 rules 와 신규 rules 변경 유무 확인(count, duration_time)
                                boolean isRulesValueDifferent = isRulesDifferent(mapping.getRules(), newMapping.getRules());
                                // mapping agent 변경 유무 확인
                                boolean isMappingAgentDifferent = isMappingAgentDifferent(mapping.getAgentList(), newMapping.getAgentList());
                                // risk_level 별 value 값 변경 유무 확인
                                boolean isRiskByValueDifferent = isRiskByValueDifferent(mapping.getValueAndLevelComment(), newMapping.getValueAndLevelComment());

                                if (isRulesValueDifferent || isMappingAgentDifferent || isRiskByValueDifferent) {
                                    log.info("The rules setting value has been changed.");

                                    threadStop(mapping.getRules());
                                    list.removeIf(rulesThread -> rulesThread.getRule().getRules_id() == mapping.getRules().getRules_id());
                                    log.info("Remove a thread from the list.");

                                    list = threadStart(newMapping, list);
                                }
                            }
                        }
                    }
                    // rules 자체가 add or remove 여부 체크 후
                    list = rulesAddOrRemoveCheck(newRulesMapping, list);
                }
                currentRulesMapping = newRulesMapping;

                errCount = 0;
                sleep(1000 * 30);
            } catch (Exception e) {
                if (errCount < 6) {
                    errCount++;
                    log.error(e.getMessage());
                    log.error("Attempting to reconnect to database. retry[{}]", errCount);
                } else break;
            }
        }
    }

    public boolean isRiskByValueDifferent(Map<String, Float> originRiskByComment, Map<String, Float> newRiskByComment) {
        // 기존 rule comment 사이즈 동일 할 경우
        if (newRiskByComment.size() == originRiskByComment.size()) {
            for (String newComment : newRiskByComment.keySet()) {
                if (originRiskByComment.containsKey(newComment)) {
                    if (isValueByCommentDifferent(newRiskByComment.get(newComment), originRiskByComment.get(newComment))) {
                        return true;
                    }
                } else return true;
            }
            return false;
        } else return true; // rule comment 사이즈 동일하지 않으면 true
    }

    public boolean isValueByCommentDifferent(float newValue, float originValue) {
        return newValue != originValue;
    }

    public List<RulesThread> rulesAddOrRemoveCheck(List<RulesMapping> newRulesMapping, List<RulesThread> list) {
        boolean isExistRuleId = false;
        for (RulesMapping newMapping : newRulesMapping) {
            for (RulesMapping mapping : currentRulesMapping) {
                if (newMapping.getRules().getRules_id() == mapping.getRules().getRules_id()) {
                    isExistRuleId = true;
                    break;
                } else isExistRuleId = false;
            }
            if (!isExistRuleId) {
                list = threadStart(newMapping, list);
            }
        }
        for (RulesMapping mapping : currentRulesMapping) {
            for (RulesMapping selectMapping : newRulesMapping) {
                if (selectMapping.getRules().getRules_id() == mapping.getRules().getRules_id()) {
                    isExistRuleId = true;
                    break;
                } else isExistRuleId = false;
            }
            if (!isExistRuleId) {
                threadStop(mapping.getRules());
                list.removeIf(rulesThread -> rulesThread.getRule().getRules_id() == mapping.getRules().getRules_id());
            }
        }
        return list;
    }

    // rule agent mapping 정보 변경 체크
    public boolean isMappingAgentDifferent(List<String> originAgentList, List<String> newAgentList) {
        if (originAgentList.size() == newAgentList.size()) {
            for (String agent : newAgentList) {
                if (!originAgentList.contains(agent)) {
                    return true;
                }
            }
            return false;
        } else return true;
    }

    public List<RulesThread> threadStart(RulesMapping rulesMapping, List<RulesThread> list) {
        RulesThread rulesThread = new RulesThread(client, rulesMapping);
        rulesThread.setName("Rules-" + rulesMapping.getRules().getRules_id());
        list.add(rulesThread);
        rulesThread.start();

        return list;
    }

    // 최초 실행 시 rule thread 전부 start
    public void firstThreadStart(List<RulesMapping> selectRulesMapping, List<RulesThread> list) {
        for (RulesMapping mapping : selectRulesMapping) {
            RulesThread rulesThread = new RulesThread(client, mapping);
            rulesThread.setName("Rules-" + mapping.getRules().getRules_id());
            // Thread 관리 list 추가
            list.add(rulesThread);
            rulesThread.start();
        }
    }

    // rule count, duration_time, resource_type 변경 체크
    public boolean isRulesDifferent(Rules beforeRules, Rules afterRules) {
        return (beforeRules.getCount() != afterRules.getCount() || beforeRules.getDuration_time() != afterRules.getDuration_time() || beforeRules.getResource_type() != afterRules.getResource_type());
    }

    // 쓰레드 종료
    public void threadStop(Rules rules) {
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        for (Thread thread : threads) {
            if (thread.getName().equals("Rules-" + rules.getRules_id())) {
                thread.interrupt();
            }
        }
    }
}
