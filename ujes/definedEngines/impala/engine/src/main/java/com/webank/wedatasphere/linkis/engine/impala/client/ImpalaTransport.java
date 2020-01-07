/**
 * @Title ImpalaTransport.java
 * @description TODO
 * @time Nov 5, 2019 3:42:15 PM
 * @author dingqihuang
 * @version 1.0
**/
package com.webank.wedatasphere.linkis.engine.impala.client;

import java.io.Closeable;

import javax.net.ssl.TrustManager;

/**
 * @author dingqihuang
 * @version Nov 5, 2019
 */
public abstract class ImpalaTransport<T> implements Closeable {

    /**
     * 按构造属性创建并打开连接
     * 
     * @return 传输对象
     * @throws Exception 建立连接失败
     */
    public abstract T getTransport() throws Exception;

    protected String host;
    protected int port;
    protected String username;
    protected String password;
    protected boolean useTicket;
    protected String ticketBin;
    protected boolean useSsl;
    protected String sslType;
    protected TrustManager[] trustManagers;
    protected int connectionTimeout;

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
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
