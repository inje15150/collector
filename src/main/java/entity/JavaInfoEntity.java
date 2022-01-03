package entity;

public class JavaInfoEntity {

    private float totalMem;
    private float usedMem;
    private float freeMem;
    private String event_time;

    public JavaInfoEntity(float totalMem, float usedMem, float freeMem, String event_time) {
        this.totalMem = totalMem;
        this.usedMem = usedMem;
        this.freeMem = freeMem;
        this.event_time = event_time;
    }

    public float getTotalMem() {
        return totalMem;
    }

    public void setTotalMem(float totalMem) {
        this.totalMem = totalMem;
    }

    public float getUsedMem() {
        return usedMem;
    }

    public void setUsedMem(float usedMem) {
        this.usedMem = usedMem;
    }

    public float getFreeMem() {
        return freeMem;
    }

    public void setFreeMem(float freeMem) {
        this.freeMem = freeMem;
    }

    public String getEvent_time() {
        return event_time;
    }

    public void setEvent_time(String event_time) {
        this.event_time = event_time;
    }
}
