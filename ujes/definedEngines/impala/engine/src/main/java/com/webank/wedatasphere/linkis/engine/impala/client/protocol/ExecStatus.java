package com.webank.wedatasphere.linkis.engine.impala.client.protocol;

import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import org.apache.hive.service.cli.thrift.TOperationState;

import java.util.Set;

/**
 * 任务执行状态
 *
 * @author dingqihuang
 * @version Sep 20, 2019
 */
public class ExecStatus {
    private final static Set<Integer> ACTIVE_STATE = ImmutableSet.of(
            TOperationState.INITIALIZED_STATE.getValue(),
            TOperationState.RUNNING_STATE.getValue(),
            TOperationState.PENDING_STATE.getValue()
    );

    private final static Set<Integer> ERROR_STATE = ImmutableSet.of(
            TOperationState.ERROR_STATE.getValue(),
            TOperationState.CLOSED_STATE.getValue(),
            TOperationState.CANCELED_STATE.getValue(),
            TOperationState.UKNOWN_STATE.getValue()
    );

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

    public String getName() {
        return name;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isActive() {
        return ACTIVE_STATE.contains(code);
    }

    public boolean hasError() {
        return ERROR_STATE.contains(code);
    }
}
