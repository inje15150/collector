package entity;

import java.util.List;

public class AgentInfoEntity {

    private String hostname;
    private String osInfo;
    private String interface_name;
    private String ip;
    private String gateway;
    private String mac_address;
    private float cpu;
    private float memory;
    private float disk;
    private String event_time;
    private List<ProcessInfo> processes;

    public AgentInfoEntity() {

    }
    public AgentInfoEntity(String hostname, String osInfo, String interface_name, String ip, String gateway, String mac_address, float cpu, float memory, float disk, String event_time, List<ProcessInfo> processes) {
        this.hostname = hostname;
        this.osInfo = osInfo;
        this.interface_name = interface_name;
        this.ip = ip;
        this.gateway = gateway;
        this.mac_address = mac_address;
        this.cpu = cpu;
        this.memory = memory;
        this.disk = disk;
        this.event_time = event_time;
        this.processes = processes;
    }

    public String getEvent_time() {
        return event_time;
    }

    public String getOsInfo() {
        return osInfo;
    }

    public String getIp() {
        return ip;
    }

    public String getGateway() {
        return gateway;
    }

    public String getMac_address() {
        return mac_address;
    }

    public float getCpu() {
        return cpu;
    }

    public float getMemory() {
        return memory;
    }

    public List<ProcessInfo> getProcesses() {
        return processes;
    }


    @Override
    public String toString() {
        return "AgentInfoEntity{" +
                "hostname='" + hostname + '\'' +
                ", osInfo='" + osInfo + '\'' +
                ", interface_name='" + interface_name + '\'' +
                ", ip='" + ip + '\'' +
                ", gateway='" + gateway + '\'' +
                ", mac_address='" + mac_address + '\'' +
                ", cpu=" + cpu +
                ", memory=" + memory +
                ", disk=" + disk +
                ", event_time='" + event_time + '\'' +
                '}';
    }
}
