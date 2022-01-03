package entity;

public class ValueByAgentInfo {
    private AgentInfoEntity agentInfoEntity;
    private float rules_value;

    public ValueByAgentInfo(AgentInfoEntity agentInfoEntity, float rules_value) {
        this.agentInfoEntity = agentInfoEntity;
        this.rules_value = rules_value;
    }

    public AgentInfoEntity getAgentInfoEntity() {
        return agentInfoEntity;
    }

    public void setAgentInfoEntity(AgentInfoEntity agentInfoEntity) {
        this.agentInfoEntity = agentInfoEntity;
    }

    public float getRules_value() {
        return rules_value;
    }

    public void setRules_value(float rules_value) {
        this.rules_value = rules_value;
    }
}
