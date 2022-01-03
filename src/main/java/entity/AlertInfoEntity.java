package entity;

import java.util.List;

public class AlertInfoEntity {
    private int rule_id;
    private int type;
    private String level;
    private float value;
    private String agent_id;
    private List<Alert_Reason> alert_reason;

    public AlertInfoEntity(int rule_id, int type, String level, float value, String agent_id, List<Alert_Reason> alert_reason) {
        this.rule_id = rule_id;
        this.type = type;
        this.level = level;
        this.value = value;
        this.agent_id = agent_id;
        this.alert_reason = alert_reason;
    }

    public int getRule_id() {
        return rule_id;
    }

    public void setRule_id(int rule_id) {
        this.rule_id = rule_id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public String getAgent_id() {
        return agent_id;
    }

    public void setAgent_id(String agent_id) {
        this.agent_id = agent_id;
    }

    public List<Alert_Reason> getAlert_reason() {
        return alert_reason;
    }

    public void setAlert_reason(List<Alert_Reason> alert_reason) {
        this.alert_reason = alert_reason;
    }
}
