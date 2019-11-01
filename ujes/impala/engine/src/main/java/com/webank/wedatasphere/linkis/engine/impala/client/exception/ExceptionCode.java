package com.webank.wedatasphere.linkis.engine.impala.client.exception;

/**
 * 错误代码
 * 
 * @author dingqihuang
 * @version Sep 20, 2019
 */
public enum ExceptionCode {
	ClosedError("Session is closed."),
	ExecutionError("Server report an error."), 
	CommunicateError("Could not communicate with target host."), 
	StillRunningError("Target is still running."), 
	InvalidHandleError("Current handle is invalid."),
	ParallelLimitError("Reach the parallel limit.");

	private String message;

	ExceptionCode(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
