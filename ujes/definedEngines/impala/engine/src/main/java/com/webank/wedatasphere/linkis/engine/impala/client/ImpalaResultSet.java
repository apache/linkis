/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.engine.impala.client;

import java.util.List;


/**
 * Created by dingqihuang on 2019-11-01
 */
public interface ImpalaResultSet extends AutoCloseable {
	/**
	 * 获取下一行数据
	 * @return true if exist
	 */
	boolean next();
	
	public Object getObject(int columnIndex);
	public <T> T getObject(int columnIndex, Class<T> clasz);
	public String getString(int columnIndex);
	public Short getShort(int columnIndex);
	public Integer getInteger(int columnIndex);
	public Long getLong(int columnIndex);
	public Object[] getValues();
	
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
