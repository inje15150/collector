package entity;

public class Rules {
    private int rules_id;
    private int resource_type;
    private int duration_time;
    private int count;

    public Rules(int rules_id, int resource_type, int duration_time, int count) {
        this.rules_id = rules_id;
        this.resource_type = resource_type;
        this.duration_time = duration_time;
        this.count = count;
    }

    public int getRules_id() {
        return rules_id;
    }

    public void setRules_id(int rules_id) {
        this.rules_id = rules_id;
    }

    public int getResource_type() {
        return resource_type;
    }

    public void setResource_type(int resource_type) {
        this.resource_type = resource_type;
    }

    public int getDuration_time() {
        return duration_time;
    }

    public void setDuration_time(int duration_time) {
        this.duration_time = duration_time;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
