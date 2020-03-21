package com.webank.wedatasphere.linkis.engine.impala.client.protocol;

import java.util.Set;

import org.apache.hive.service.cli.thrift.TOperationState;

import com.google.common.collect.ImmutableSet;

/**
 * 任务执行状态
 * 
 * @author dingqihuang
 * @version Sep 20, 2019
 */
public class ExecStatus {
    private final static Set<Integer> ACTIVE_STATE = ImmutableSet.of(TOperationState.INITIALIZED_STATE.getValue(),
            TOperationState.RUNNING_STATE.getValue(), TOperationState.PENDING_STATE.getValue());

    private final static Set<Integer> ERROR_STATE = ImmutableSet.of(TOperationState.ERROR_STATE.getValue(),
            TOperationState.CLOSED_STATE.getValue(), TOperationState.CANCELED_STATE.getValue(),
            TOperationState.UKNOWN_STATE.getValue());

    private int code;
    private String name;
    private String errorMessage;

    /**
     * @param code
     * @param name
     * @param errorMessage
     */
    public ExecStatus(int code, String name, String errorMessage) {
        super();
        this.code = code;
        this.name = name;
        this.errorMessage = errorMessage;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isActive() {
        return ACTIVE_STATE.contains(code);
    }

    public boolean hasError() {
        return ERROR_STATE.contains(code);
    }
}
