package com.webank.wedatasphere.linkis.engine.impala.client.protocol;

import java.util.Set;

import org.apache.hive.service.cli.thrift.TOperationState;

import com.google.common.collect.ImmutableSet;


/**
 * 任务执行状态
 * @author dingqihuang
 * @version Sep 20, 2019
 */
public class ExecStatus {
	
	public ExecStatus(int code, String name, String errorMessage) {
		super();
		this.code = code;
		this.name = name;
		this.errorMessage = errorMessage;
	}

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
	
	public boolean isActive() {
		return ACTIVE_STATE.contains(code);
	}
	
	public boolean hasError() {
		return ERROR_STATE.contains(code);
	}

	public static Set<Integer> getActiveState() {
		return ACTIVE_STATE;
	}

	public static Set<Integer> getErrorState() {
		return ERROR_STATE;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setErrorMessage(String errorMessage) {
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
	
	
}
