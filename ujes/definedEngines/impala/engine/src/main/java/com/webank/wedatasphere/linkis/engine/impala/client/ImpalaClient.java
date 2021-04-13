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


import com.webank.wedatasphere.linkis.engine.impala.client.exception.SubmitException;
import com.webank.wedatasphere.linkis.engine.impala.client.exception.TransportException;
import com.webank.wedatasphere.linkis.engine.impala.client.protocol.ExecProgress;
import com.webank.wedatasphere.linkis.engine.impala.client.protocol.ExecStatus;
import com.webank.wedatasphere.linkis.engine.impala.client.protocol.ExecSummary;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * Impala客户端
 */
public interface ImpalaClient extends AutoCloseable {
	
	/**
	 * 异步执行查询
	 * @param sql 
	 * @param resultListener 查询结果的回调函数
	 * @return queryId 查询的唯一标识符
	 * @throws TransportException 传输错误
	 * @throws SubmitException 执行错误
	 */
	String executeAsync(String sql, ResultListener resultListener) throws TransportException, SubmitException;

	/**
	 * 异步执行查询，可阻塞
	 * @param sql
	 * @param resultListener
	 * @return
	 * @throws TransportException
	 * @throws SubmitException
	 */
	Future<Long> executeWithFuture(String sql, ResultListener resultListener) throws TransportException, SubmitException;
	
	/**
	 * 同步执行查询
	 * @param sql 
	 * @param resultListener 查询结果的回调函数
	 * @throws TransportException 传输错误
	 * @throws SubmitException 执行错误
	 */
	void execute(String sql, ResultListener resultListener) throws TransportException, SubmitException;
	
	/**
	 * 取消查询
	 * @param queryId 查询ID
	 * @throws TransportException 传输错误
	 */
	void cancel(String queryId) throws TransportException;
	
	/**
	 * 查询执行状况
	 * @param queryId 查询ID
	 * @return
	 * @throws TransportException 传输错误
	 */
	ExecSummary getExecSummary(String queryId) throws TransportException;
	
	/**
	 * 查询执行进度
	 * @param queryId 查询ID
	 * @return
	 * @throws TransportException 传输错误
	 */
	ExecProgress getExecProgress(String queryId) throws TransportException;
	
	/**
	 * 查询执行状态
	 * @param queryId 查询ID
	 * @return
	 * @throws TransportException 传输错误
	 */
	ExecStatus getExecStatus(String queryId) throws TransportException;
	
	/**
	 * 设置查询队列
	 * @param poolName 队列名称
	 * @throws TransportException 传输错误
	 */
	void setRequestPool(String poolName) throws TransportException;
	
	/**
	 * 设置查询参数，详情见impala官网
	 * @param key
	 * @param value
	 * @throws TransportException 传输错误
	 */
	void setQueryOption(String key, String value) throws TransportException;
	
	/**
	 * 获取查询参数，返回不可变集合
	 * @return
	 * @throws TransportException 传输错误
	 */
	Map<String, String> getQueryOptions() throws TransportException;
	
	/**
	 * 重置查询参数
	 * @param key
	 * @throws TransportException
	 */
	void unsetQueryOption(String key) throws TransportException;
	
	/**
	 * 获取正在运行的查询数目
	 * @return 数目
	 */
	int getExecutionCount();
	
	/**
	 * 设置结果集缓冲区大小
	 * @param batchSize
	 */
	void setBatchSize(int batchSize);
}
