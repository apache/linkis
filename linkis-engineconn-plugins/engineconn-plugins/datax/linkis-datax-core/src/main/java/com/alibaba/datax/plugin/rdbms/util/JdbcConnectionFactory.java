package com.alibaba.datax.plugin.rdbms.util;

import java.sql.Connection;

/**
 * Date: 15/3/16 下午3:12
 */
public class JdbcConnectionFactory implements ConnectionFactory {

    private DataBaseType dataBaseType;

    private String jdbcUrl;

    private String userName;

    private String password;

    private String proxyHost;

    private int proxyPort;

    public JdbcConnectionFactory(DataBaseType dataBaseType, String jdbcUrl, String userName, String password,
                                 String proxyHost, int proxyPort) {
        this.dataBaseType = dataBaseType;
        this.jdbcUrl = jdbcUrl;
        this.userName = userName;
        this.password = password;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    @Override
    public Connection getConnecttion() {
        return DBUtil.getConnection(dataBaseType, jdbcUrl, userName, password, proxyHost, proxyPort);
    }

    @Override
    public Connection getConnecttionWithoutRetry() {
        return DBUtil.getConnectionWithoutRetry(dataBaseType, jdbcUrl, userName, password, proxyHost, proxyPort);
    }

    @Override
    public String getConnectionInfo() {
        return "jdbcUrl:" + jdbcUrl;
    }
}
