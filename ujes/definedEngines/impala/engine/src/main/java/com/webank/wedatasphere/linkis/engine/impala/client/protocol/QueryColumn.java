package com.webank.wedatasphere.linkis.engine.impala.client.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 查询的列名
 * @author dingqihuang
 * @version Sep 20, 2019
 */
public class QueryColumn {
	private String label;
	private int index;

	/**
	 * @param label
	 * @param index
	 */
	public QueryColumn(String label, int index) {
		super();
		this.label = label;
		this.index = index;
	}

	public String getLabel() {
		return label;
	}

	public int getIndex() {
		return index;
	}
}
