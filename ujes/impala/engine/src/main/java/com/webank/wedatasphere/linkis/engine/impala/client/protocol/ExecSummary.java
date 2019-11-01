package com.webank.wedatasphere.linkis.engine.impala.client.protocol;


/**
 * 任务信息
 * @author dingqihuang
 * @version Sep 20, 2019
 */
public class ExecSummary {
	private ExecStatus status;
	private ExecProgress progress;
	private int nodeNum;
	public ExecStatus getStatus() {
		return status;
	}
	public void setStatus(ExecStatus status) {
		this.status = status;
	}
	public ExecProgress getProgress() {
		return progress;
	}
	public void setProgress(ExecProgress progress) {
		this.progress = progress;
	}
	public int getNodeNum() {
		return nodeNum;
	}
	public ExecSummary(ExecStatus status, ExecProgress progress, int nodeNum) {
		super();
		this.status = status;
		this.progress = progress;
		this.nodeNum = nodeNum;
	}
	public void setNodeNum(int nodeNum) {
		this.nodeNum = nodeNum;
	}
	
}
