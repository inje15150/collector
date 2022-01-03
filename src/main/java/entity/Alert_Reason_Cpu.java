package entity;

public class Alert_Reason_Cpu implements Alert_Reason{
    private float cpu;
    private String event_time;

    public Alert_Reason_Cpu(float cpu, String event_time) {
        this.cpu = cpu;
        this.event_time = event_time;
    }

    public float getCpu() {
        return cpu;
    }

    public void setCpu(float cpu) {
        this.cpu = cpu;
    }

    public String getEvent_time() {
        return event_time;
    }

    public void setEvent_time(String event_time) {
        this.event_time = event_time;
    }
}
