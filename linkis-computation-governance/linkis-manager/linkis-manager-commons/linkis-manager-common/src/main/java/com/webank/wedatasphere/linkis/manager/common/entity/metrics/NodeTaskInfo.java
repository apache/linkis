package com.webank.wedatasphere.linkis.manager.common.entity.metrics;

/**
 * @author peacewong
 * @date 2020/6/7 20:56
 */
public class NodeTaskInfo {

    private int runningTasks;

    private int pendingTasks;

    private int succeedTasks;

    private int failedTasks;

    public int getTasks() {
        return runningTasks + pendingTasks;
    }

    public int getRunningTasks() {
        return runningTasks;
    }

    public void setRunningTasks(int runningTasks) {
        this.runningTasks = runningTasks;
    }

    public int getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(int pendingTasks) {
        this.pendingTasks = pendingTasks;
    }

    public int getSucceedTasks() {
        return succeedTasks;
    }

    public void setSucceedTasks(int succeedTasks) {
        this.succeedTasks = succeedTasks;
    }

    public int getFailedTasks() {
        return failedTasks;
    }

    public void setFailedTasks(int failedTasks) {
        this.failedTasks = failedTasks;
    }
}
