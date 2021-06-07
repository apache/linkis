package com.webank.wedatasphere.linkis.manager.common.protocol.engine;



public class EngineStopResponse {

    public EngineStopResponse() {}

    public EngineStopResponse(boolean stopStatus, String msg) {
        this.stopStatus = stopStatus;
        this.msg = msg;
    }

    private boolean stopStatus;
    private String msg;

    public boolean getStopStatus() {
        return stopStatus;
    }

    public void setStopStatus(boolean status) {
        this.stopStatus = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
