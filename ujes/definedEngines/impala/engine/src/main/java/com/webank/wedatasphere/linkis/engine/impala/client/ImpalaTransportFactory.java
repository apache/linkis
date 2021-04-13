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

import org.apache.thrift.transport.TTransport;

import javax.net.ssl.TrustManager;

/**
 * Created by dingqihuang on 2019-11-01
 */
public abstract class ImpalaTransportFactory<T extends TTransport> {
	
	/**
	 * 按构造属性创建并打开连接
	 * @return 传输对象
	 * @throws Exception 建立连接失败
	 */
	public abstract T getTransport() throws Exception;

	protected String[] hosts;
	protected int[] ports;
	protected String username;
	protected String password;
	protected boolean useTicket;
	protected String ticketBin;
	protected boolean useSsl;
	protected String sslType;
	protected TrustManager[] trustManagers;
	protected int connectionTimeout;
	
	public void setHosts(String[] hosts) {
		this.hosts = hosts;
	}
	public void setPorts(int[] ports) {
		this.ports = ports;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setUseTicket(boolean useTicket) {
		this.useTicket = useTicket;
	}
	public void setTicketBin(String ticketBin) {
		this.ticketBin = ticketBin;
	}
	public void setUseSsl(boolean useSsl) {
		this.useSsl = useSsl;
	}
	public void setSslType(String sslType) {
		this.sslType = sslType;
	}
	public void setTrustManagers(TrustManager[] trustManagers) {
		this.trustManagers = trustManagers;
	}
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}
}
