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
package com.webank.wedatasphere.linkis.entrance.conf.executer;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public  class  ConnectionManager {

    Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    private final Map<String, DataSource> databaseToDataSources = new HashMap<String, DataSource>();

    private final Map<String, String> supportedDBs = new HashMap<String, String>();
    private final List<String> supportedDBNames = new ArrayList<String>();

    private volatile static ConnectionManager connectionManager;
    private ConnectionManager(){
    }
    public static ConnectionManager getInstance(){
        if (connectionManager== null) {
            synchronized (ConnectionManager.class) {
                if (connectionManager== null) {
                    connectionManager= new ConnectionManager();
                }
            }
        }
        return connectionManager;
    }

    {
        String supportedDBString ="mysql=>com.mysql.jdbc.Driver,postgresql=>org.postgresql.Driver," +
                "oracle=>oracle.jdbc.driver.OracleDriver,hive2=>org.apache.hive.jdbc.HiveDriver";
        String[] supportedDBs =  supportedDBString.split(",");
        for (String supportedDB : supportedDBs) {
            String[] supportedDBInfo = supportedDB.split("=>");
            if(supportedDBInfo.length != 2) {
                throw new IllegalArgumentException("Illegal driver info " + supportedDB);
            }
            try {
                Class.forName(supportedDBInfo[1]);
            } catch (ClassNotFoundException e) {
                logger.info("Load " + supportedDBInfo[0] + " driver failed",e);
            }
            supportedDBNames.add(supportedDBInfo[0]);
            this.supportedDBs.put(supportedDBInfo[0], supportedDBInfo[1]);
        }
    }

    private void validateURL(String url) {
        if(StringUtils.isEmpty(url)) {
            throw new NullPointerException("jdbc.url cannot be null.");
        }
        if(!url.matches("jdbc:\\w+://\\S+:[0-9]{2,6}(/\\S*)?")) {
            throw new IllegalArgumentException("Unknown jdbc.url " + url);
        }
        for (String supportedDBName: supportedDBNames) {
            if(url.indexOf(supportedDBName) > 0) {
                return;
            }
        }
        throw new IllegalArgumentException("Illegal url or not supported url type (url: " + url + ").");
    }

    private final Pattern pattern = Pattern.compile("^(jdbc:\\w+://\\S+:[0-9]+)\\s*");
    private String getRealURL(String url) {
        Matcher matcher = pattern.matcher(url.trim());
        matcher.find();
        return matcher.group(1);
    }


    protected DataSource createDataSources(HashMap<String, String> properties) throws SQLException {
        String url = properties.get("jdbc.url");
        String username = properties.get("jdbc.username").trim();
        String password = StringUtils.trim(properties.get("jdbc.password"));
        validateURL(url);
        int index = url.indexOf(":") + 1;
        String dbType = url.substring(index, url.indexOf(":", index));
        System.out.println(String.format("Try to Create a new %s JDBC DBCP with url(%s), username(%s), password(%s).", dbType, url, username, password));
        Properties props = new Properties();
        props.put("driverClassName", supportedDBs.get(dbType));
        props.put("url", url.trim());
        props.put("username", username);
        props.put("password", password);
        props.put("maxIdle", 5);
        props.put("minIdle", 0);
        props.put("maxActive", 20);
        props.put("initialSize", 1);
        props.put("testOnBorrow", false);
        props.put("testWhileIdle", true);
        props.put("validationQuery", "select 1");
        props.put("initialSize", 1);
        BasicDataSource dataSource;
        try {
            dataSource = (BasicDataSource) BasicDataSourceFactory.createDataSource(props);
        } catch (Exception e) {
            throw new SQLException(e);
        }
//        ComboPooledDataSource dataSource = new ComboPooledDataSource();
//        dataSource.setUser(username);
//        dataSource.setPassword(password);
//        dataSource.setJdbcUrl(url.trim());
//        try {
//            dataSource.setDriverClass(supportedDBs.get(dbType));
//        } catch (PropertyVetoException e) {
//            throw new SQLException(e);
//        }
//        dataSource.setInitialPoolSize(1);
//        dataSource.setMinPoolSize(0);
//        dataSource.setMaxPoolSize(20);
//        dataSource.setMaxStatements(40);
//        dataSource.setMaxIdleTime(60);
        return dataSource;
    }

    public Connection getConnection(HashMap<String, String> properties) throws SQLException {
        String url = properties.get("jdbc.url");
        if(StringUtils.isEmpty(properties.get("jdbc.username"))) {
            throw new NullPointerException("jdbc.username cannot be null.");
        }
        url = getRealURL(url);
        //这里通过URL+username识别
        String key = url + "/" + properties.get("jdbc.username").trim();
        DataSource dataSource = databaseToDataSources.get(key);
        if(dataSource == null) {
            synchronized (databaseToDataSources) {
                if(dataSource == null) {
                    dataSource = createDataSources(properties);
                    databaseToDataSources.put(key, dataSource);
                }
            }
        }
        return dataSource.getConnection();
    }

    public void close() {
        for (DataSource dataSource: this.databaseToDataSources.values()) {
            try {
//                DataSources.destroy(dataSource);
                ((BasicDataSource)dataSource).close();
            } catch (SQLException e) {}
        }
    }

    public static void main(String[] args) throws Exception {
//        Pattern pattern = Pattern.compile("^(jdbc:\\w+://\\S+:[0-9]+)\\s*");
        String url = "jdbc:mysql://xxx.xxx.xxx.xxx:8504/xx?useUnicode=true&amp;characterEncoding=UTF-8&amp;createDatabaseIfNotExist=true";
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
        while(rs.next()) {
            System.out.println(rs.getObject(1));
        }
        rs.close();
        statement.close();
        conn.close();
        dataSource.close();
    }
}
