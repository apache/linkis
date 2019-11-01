package com.webank.wedatasphere.linkis.engine.impala.client;

import java.util.List;


/**
 * Impala查询结果集
 * @author dingqihuang
 * @version Sep 20, 2019
 */
public interface ImpalaResultSet extends AutoCloseable {
	/**
	 * 获取下一行数据
	 * @return true if exist
	 */
	boolean next();
	public Object[] getValues();
	public Object getObject(int columnIndex);
	public <T> T getObject(int columnIndex, Class<T> clasz);
	public String getString(int columnIndex);
	public Short getShort(int columnIndex);
	public Integer getInteger(int columnIndex);
	public Long getLong(int columnIndex);
	
	public int getColumnSize();
	
	/*
	 * 获取字段类型
	 */
	public Class<?> getType(int columnIndex);
	
	
	/**
	 * 获取字段信息
	 * @return
	 */
	public List<String> getColumns();
}
