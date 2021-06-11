package com.webank.wedatasphere.linkis.protocol.engine;


public class EngineInfo {

    private Long id;
    private EngineState engineState;

    public EngineInfo() {}

    public EngineInfo(Long id, EngineState state) {
        this.id = id;
        this.engineState = state;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EngineState getEngineState() {
        return engineState;
    }

    public void setEngineState(EngineState engineState) {
        this.engineState = engineState;
    }
}
