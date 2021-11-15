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
}
