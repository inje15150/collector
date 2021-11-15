package entity;

public class ProcessInfo {

    private String name;
    private String pid;
    private String usedMem;

    public ProcessInfo(String name, String pid, String usedMem) {
        this.name = name;
        this.pid = pid;
        this.usedMem = usedMem;
    }
}
