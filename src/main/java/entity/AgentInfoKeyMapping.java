package entity;

public class AgentInfoKeyMapping {
    private String key;
    private AgentInfoEntity agentInfoEntity;

    public AgentInfoKeyMapping(String key, AgentInfoEntity agentInfoEntity) {
        this.key = key;
        this.agentInfoEntity = agentInfoEntity;
    }

    public String getKey() {
        return key;
    }

    public AgentInfoEntity getAgentInfoEntity() {
        return agentInfoEntity;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setAgentInfoEntity(AgentInfoEntity agentInfoEntity) {
        this.agentInfoEntity = agentInfoEntity;
    }
}
