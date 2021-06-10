package com.webank.wedatasphere.linkis.manager.common.entity.metrics;


public class NodeOverLoadInfo {

    private Long maxMemory;

    private Long usedMemory;

    private Float systemCPUUsed;

    private Long systemLeftMemory;

    public Long getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(Long maxMemory) {
        this.maxMemory = maxMemory;
    }

    public Long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(Long usedMemory) {
        this.usedMemory = usedMemory;
    }

    public Float getSystemCPUUsed() {
        return systemCPUUsed;
    }

    public void setSystemCPUUsed(Float systemCPUUsed) {
        this.systemCPUUsed = systemCPUUsed;
    }

    public Long getSystemLeftMemory() {
        return systemLeftMemory;
    }

    public void setSystemLeftMemory(Long systemLeftMemory) {
        this.systemLeftMemory = systemLeftMemory;
    }
}
