/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.engineplugin.jdbc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.linkis.manager.engineplugin.jdbc.conf.JDBCConfiguration;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.commons.lang.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.hadoop.fs.CommonConfigurationKeysPublic.HADOOP_SECURITY_AUTHENTICATION;
import static org.apache.hadoop.security.UserGroupInformation.AuthenticationMethod.KERBEROS;

public class ConnectionManager {

    Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    private final Map<String, DataSource> databaseToDataSources = new HashMap<String, DataSource>();

    private final Map<String, String> supportedDBs = new HashMap<String, String>();
    private final List<String> supportedDBNames = new ArrayList<String>();

    private volatile static ConnectionManager connectionManager;

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        if (connectionManager == null) {
            synchronized (ConnectionManager.class) {
                if (connectionManager == null) {
                    connectionManager = new ConnectionManager();
                }
            }
        }
        return connectionManager;
    }

    {
        String supportedDBString = JDBCConfiguration.JDBC_SUPPORT_DBS().getValue();
        String[] supportedDBs = supportedDBString.split(",");
        for (String supportedDB : supportedDBs) {
            String[] supportedDBInfo = supportedDB.split("=>");
            if (supportedDBInfo.length != 2) {
                throw new IllegalArgumentException("Illegal driver info " + supportedDB);
            }
            try {
                Class.forName(supportedDBInfo[1]);
            } catch (ClassNotFoundException e) {
                logger.info("Load " + supportedDBInfo[0] + " driver failed", e);
            }
            supportedDBNames.add(supportedDBInfo[0]);
            this.supportedDBs.put(supportedDBInfo[0], supportedDBInfo[1]);
        }
    }

    private void validateURL(String url) {
        if (StringUtils.isEmpty(url)) {
            throw new NullPointerException("jdbc.url cannot be null.");
        }
        if (!url.matches("jdbc:\\w+://\\S+:[0-9]{2,6}(/\\S*)?")) {
            throw new IllegalArgumentException("Unknown jdbc.url " + url);
        }
        for (String supportedDBName : supportedDBNames) {
            if (url.indexOf(supportedDBName) > 0) {
                return;
            }
        }
        throw new IllegalArgumentException("Illegal url or not supported url type (url: " + url + ").");
    }

    private String getRealURL(String url) {
        int index = url.indexOf("?");
        if (index < 0) {
            index = url.length();
        }
        return url.substring(0, index);
    }

    protected DataSource createDataSources(Map<String, String> properties) throws SQLException {
        String url = getJdbcUrl(properties);
        String username = properties.getOrDefault("jdbc.username", "");
        String password = StringUtils.trim(properties.getOrDefault("jdbc.password", ""));
        int index = url.indexOf(":") + 1;
        String dbType = url.substring(index, url.indexOf(":", index));
        logger.info(String.format("Try to Create a new %s JDBC DBCP with url(%s), username(%s), password(%s).", dbType, url, username, password));
        Properties props = new Properties();
        props.put("driverClassName", supportedDBs.get(dbType));
        props.put("url", url);
        props.put("username", username);
        props.put("password", password);
        props.put("maxIdle", 5);
        props.put("minIdle", 0);
        props.put("maxActive", 20);
        props.put("initialSize", 1);
        props.put("testOnBorrow", false);
        props.put("testWhileIdle", true);
        props.put("validationQuery", "show databases;");
        BasicDataSource dataSource;
        try {
            dataSource = (BasicDataSource) BasicDataSourceFactory.createDataSource(props);
        } catch (Exception e) {
            throw new SQLException(e);
        }
        return dataSource;
    }

    public Connection getConnection(Map<String, String> properties) throws SQLException {
        String url = getJdbcUrl(properties);
        String jdbcAuthType = getJdbcAuthType(properties);
        Connection connection = null;
        switch (jdbcAuthType) {
            case "SIMPLE":
                connection = getConnection(url, properties);
                break;
            case "KERBEROS":
                createKerberosSecureConfiguration(properties);
                String user = properties.get("jdbc.user");
                logger.info("The user {} has selected auth type of jdbc is kerberos.", user);
                String proxyUserProperty = properties.get("jdbc.proxy.user.property");
                if (StringUtils.isNotBlank(proxyUserProperty)) {
                    url = url.concat(";").concat(proxyUserProperty + "=" + user);
                }
                connection = getConnection(url, properties);
                break;
            case "USERNAME":
                if (StringUtils.isEmpty(properties.get("jdbc.username"))) {
                    throw new SQLException("jdbc.username is not empty.");
                }
                if (StringUtils.isEmpty(properties.get("jdbc.password"))) {
                    throw new SQLException("jdbc.password is not empty.");
                }
                connection = getConnection(url, properties);
                break;
        }
        return connection;
    }

    public void close() {
        for (DataSource dataSource : this.databaseToDataSources.values()) {
            try {
                // DataSources.destroy(dataSource);
                ((BasicDataSource) dataSource).close();
            } catch (SQLException e) {
            }
        }
    }

    private Connection getConnection(String url, Map<String, String> properties) throws SQLException {
        String key = getRealURL(url);
        DataSource dataSource = databaseToDataSources.get(key);
        if (dataSource == null) {
            synchronized (databaseToDataSources) {
                if (dataSource == null) {
                    dataSource = createDataSources(properties);
                    databaseToDataSources.put(key, dataSource);
                }
            }
        }
        return dataSource.getConnection();
    }

    private String getJdbcUrl(Map<String, String> properties) throws SQLException {
        String url = properties.get("jdbc.url");
        if (StringUtils.isEmpty(url)) {
            throw new SQLException("jdbc.url is not empty.");
        }
        url = clearUrl(url);
        validateURL(url);
        return url.trim();
    }

    private String getJdbcAuthType(Map<String, String> properties) {
        return properties.getOrDefault("jdbc.auth.type", "USERNAME").trim().toUpperCase();
    }

    private void createKerberosSecureConfiguration(Map<String, String> properties) {
        Configuration conf = new Configuration();
        conf.set(HADOOP_SECURITY_AUTHENTICATION, KERBEROS.toString());
        UserGroupInformation.setConfiguration(conf);
        try {
            if (!UserGroupInformation.isSecurityEnabled()
                    || UserGroupInformation.getCurrentUser().getAuthenticationMethod() != KERBEROS
                    || !UserGroupInformation.isLoginKeytabBased()) {
                String keytab = properties.get("jdbc.keytab.location");
                String principal = properties.get("jdbc.principal");
                UserGroupInformation.loginUserFromKeytab(principal, keytab);
                logger.info("Login successfully with keytab: {} and principal: {}", keytab, principal);
            } else {
                logger.info("The user has already logged in using keytab and principal, " +
                        "no action required");
            }
        } catch (IOException e) {
            logger.error("Failed to get either keytab location or principal name in the " +
                    "jdbc executor", e);
        }

    }

    private String clearUrl(String url) {
        if (url.startsWith("\"") && url.endsWith("\"")) {
            url = url.trim();
            return url.substring(1, url.length() - 1);
        }
        return url;
    }

    public static void main(String[] args) throws Exception {
//        Pattern pattern = Pattern.compile("^(jdbc:\\w+://\\S+:[0-9]+)\\s*");
      /*  String url = "jdbc:mysql://xxx.xxx.xxx.xxx:8504/xx?useUnicode=true&amp;characterEncoding=UTF-8&amp;createDatabaseIfNotExist=true";
        Properties properties = new Properties();
        properties.put("driverClassName", "org.apache.hive.jdbc.HiveDriver");
        properties.put("url", "jdbc:hive2://xxx.xxx.xxx.xxx:10000/");
        properties.put("username", "username");
        properties.put("password", "*****");
        properties.put("maxIdle", 20);
        properties.put("minIdle", 0);
        properties.put("initialSize", 1);
        properties.put("testOnBorrow", false);
        properties.put("testWhileIdle", true);
        properties.put("validationQuery", "select 1");
        properties.put("initialSize", 1);
        BasicDataSource dataSource = (BasicDataSource) BasicDataSourceFactory.createDataSource(properties);
        Connection conn = dataSource.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("show tables");
        while (rs.next()) {
            System.out.println(rs.getObject(1));
        }
        rs.close();
        statement.close();
        conn.close();
        dataSource.close();*/

        System.out.println("starting ......");
        Map<String, String> properties = new HashMap<>(8);
        properties.put("driverClassName", args[0]);
        properties.put("jdbc.user", args[1]);
        properties.put("jdbc.url", args[2]);
        properties.put("jdbc.username", args[3]);
        properties.put("jdbc.password", args[4]);
        properties.put("jdbc.auth.type", args[5]);
        properties.put("jdbc.principal", args[6]);
        properties.put("jdbc.keytab.location", args[7]);
        properties.put("proxy.user.property", "hive.server2.proxy.user");

        for (int i = 0; i < 2; i++) {
            ConnectionManager connectionManager = ConnectionManager.getInstance();
            Connection conn = connectionManager.getConnection(properties);
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(args[8]);
            while (rs.next()) {
                System.out.println(rs.getObject(1));
            }
            rs.close();
            statement.close();
            conn.close();
        }

        System.out.println("end .......");
    }
}
