package entity;

public class Alert_Reason_Memory implements Alert_Reason{
    private float memory;
    private String event_time;

    public Alert_Reason_Memory(float memory, String event_time) {
        this.memory = memory;
        this.event_time = event_time;
    }

    public float getMemory() {
        return memory;
    }

    public void setMemory(float memory) {
        this.memory = memory;
    }

    public String getEvent_time() {
        return event_time;
    }

    public void setEvent_time(String event_time) {
        this.event_time = event_time;
    }
}
