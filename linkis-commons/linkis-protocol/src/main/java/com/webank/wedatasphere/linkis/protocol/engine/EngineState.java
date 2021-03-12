package com.webank.wedatasphere.linkis.protocol.engine;


public enum EngineState {

    /**
     * 引擎的各种状态
     */
    Starting, Idle, Busy, ShuttingDown, Error, Dead, Success;

    public int id() {
        return this.ordinal();
    }

    public static boolean isCompleted(EngineState engineState) {
        switch (engineState) {
            case Error:
            case Dead:
            case Success:
                return true;
            default:
                return false;
        }
    }

    public static boolean isAvailable(EngineState engineState) {
        switch (engineState) {
            case Idle:
            case Busy:
                return true;
            default:
                return false;
        }
    }
}
