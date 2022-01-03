package entity;

import java.util.List;
import java.util.Map;

public class RulesMapping {
    private Rules rules;
    private List<String> agentList;
    private Map<String, Float> valueAndLevelComment;

    public RulesMapping(Rules rules, List<String> agentList, Map<String, Float> valueAndLevelComment) {
        this.rules = rules;
        this.agentList = agentList;
        this.valueAndLevelComment = valueAndLevelComment;
    }

    public Rules getRules() {
        return rules;
    }

    public void setRules(Rules rules) {
        this.rules = rules;
    }

    public List<String> getAgentList() {
        return agentList;
    }

    public void setAgentList(List<String> agentList) {
        this.agentList = agentList;
    }

    public Map<String, Float> getValueAndLevelComment() {
        return valueAndLevelComment;
    }

    public void setValueAndLevelComment(Map<String, Float> valueAndLevelComment) {
        this.valueAndLevelComment = valueAndLevelComment;
    }
}
