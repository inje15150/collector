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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getUsedMem() {
        return usedMem;
    }

    public void setUsedMem(String usedMem) {
        this.usedMem = usedMem;
    }
}
