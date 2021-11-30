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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class TestConnectionManager {
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
        // export LINKIS_JDBC_KERBEROS_REFRESH_INTERVAL=10000

        Map<String, String> properties = new HashMap<>(8);
        properties.put("driverClassName", args[0]);
        properties.put("jdbc.url", args[1]);
        properties.put("jdbc.username", args[2]);
        properties.put("jdbc.password", args[3]);
        properties.put("jdbc.auth.type", args[4]);
        properties.put("jdbc.principal", args[5]);
        properties.put("jdbc.keytab.location", args[6]);
        properties.put("jdbc.proxy.user", args[7]);
        properties.put("jdbc.proxy.user.property", "hive.server2.proxy.user");
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        connectionManager.startRefreshKerberosLoginStatusThread();
        for (int i = 0; i < 200000; i++) {
            Connection conn = connectionManager.getConnection(properties);
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(args[8]);
            while (rs.next()) {
                System.out.println(rs.getObject(1));
            }
            rs.close();
            statement.close();
            conn.close();
            Thread.sleep(100000);
        }


        System.out.println("end .......");
    }
}
