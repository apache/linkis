/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.engineplugin.hbase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.linkis.manager.engineplugin.hbase.constant.HBaseEngineConnConstant;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class HBaseConnectionManagerTest {
    @Disabled
    @Test
    @DisplayName("testGetConnection")
    public void testGetConnection() throws IOException {
        Map<String, String> properties = new HashMap<>(8);
        properties.put(HBaseEngineConnConstant.ZOOKEEPER_QUORUM, "localhost");
        properties.put(HBaseEngineConnConstant.ZOOKEEPER_CLIENT_PORT, "2181");
        properties.put(HBaseEngineConnConstant.HBASE_SECURITY_AUTH, "simple");
        // properties.put(HBaseEngineConnConstant.HBASE_SECURITY_AUTH, "kerberos");
        properties.put(HBaseEngineConnConstant.KERBEROS_PRINCIPAL, "hdfs@HADOOP.COM");
        properties.put(HBaseEngineConnConstant.KERBEROS_KEYTAB_FILE, "/etc/hbase/conf/hdfs.keytab");
        // 设置kerberos代理用户
        properties.put(HBaseEngineConnConstant.KERBEROS_PROXY_USER, "leojie");
        properties.put(HBaseEngineConnConstant.HBASE_REGION_SERVER_KERBEROS_PRINCIPAL, "hbase/_HOST@HADOOP.COM");
        properties.put(HBaseEngineConnConstant.HBASE_MASTER_KERBEROS_PRINCIPAL, "hbase/_HOST@HADOOP.COM");
        Connection connection = HBaseConnectionManager.getInstance().getConnection(properties);
        try (Admin admin = connection.getAdmin()) {
            TableName[] tableNames = admin.listTableNames();
            System.out.println(tableNames);
        }
    }

}
