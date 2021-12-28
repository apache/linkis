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
 
package org.apache.linkis.computation.client;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.computation.client.once.simple.SubmittableSimpleOnceJob;
import org.apache.linkis.computation.client.operator.impl.EngineConnLogOperator;
import org.apache.linkis.computation.client.operator.impl.EngineConnLogs;
import org.apache.linkis.computation.client.operator.impl.EngineConnMetricsOperator;
import org.apache.linkis.computation.client.utils.LabelKeyUtils;

public class FlinkOnceJobTest {
    public static void main(String[] args) {
        // TODO First, set the right gateway url.
        LinkisJobClient.config().setDefaultServerUrl("http://127.0.0.1:9002");
        // TODO Second, modify the sql, so Flink engineConn can run it successfully.
        String sql = "CREATE TABLE mysql_binlog (\n"
                + " id INT NOT NULL,\n"
                + " name STRING,\n"
                + " age INT\n"
                + ") WITH (\n"
                + " 'connector' = 'mysql-cdc',\n"
                + " 'hostname' = 'ip',\n"
                + " 'port' = 'port',\n"
                + " 'username' = '${username}',\n"
                + " 'password' = '${password}',\n"
                + " 'database-name' = '${database}',\n"
                + " 'table-name' = '${tablename}',\n"
                + " 'debezium.snapshot.locking.mode' = 'none'\n"
                + ");\n"
                + "CREATE TABLE sink_table (\n"
                + " id INT NOT NULL,\n"
                + " name STRING,\n"
                + " age INT,\n"
                + " primary key(id) not enforced\n"
                + ") WITH (\n"
                + "  'connector' = 'jdbc',\n"
                + "  'url' = 'jdbc:mysql://${ip}:port/${database}',\n"
                + " 'table-name' = '${tablename}',\n"
                + "  'driver' = 'com.mysql.jdbc.Driver',\n"
                + "  'username' = '${username}',\n"
                + "  'password' = '${password}'\n"
                + ");\n"
                + "INSERT INTO sink_table SELECT id, name, age FROM mysql_binlog";
        // TODO Thirdly, please modify the user_creator label and executeUser
        SubmittableSimpleOnceJob onceJob = LinkisJobClient.once().simple().builder().setCreateService("Flink-Test")
                .setMaxSubmitTime(300000)
                .addLabel(LabelKeyUtils.ENGINE_TYPE_LABEL_KEY(), "flink-1.12.2")
                .addLabel(LabelKeyUtils.USER_CREATOR_LABEL_KEY(), "hadoop-Streamis")
                .addLabel(LabelKeyUtils.ENGINE_CONN_MODE_LABEL_KEY(), "once")
                .addStartupParam(Configuration.IS_TEST_MODE().key(), true)
                .addExecuteUser("hadoop").addJobContent("runType", "sql").addJobContent("code", sql).addSource("jobName", "OnceJobTest")
                .build();
        onceJob.submit();
        System.out.println(onceJob.getId());
        EngineConnLogOperator logOperator = (EngineConnLogOperator) onceJob.getOperator(EngineConnLogOperator.OPERATOR_NAME());
        EngineConnMetricsOperator metricsOperator = (EngineConnMetricsOperator) onceJob.getOperator(EngineConnMetricsOperator.OPERATOR_NAME());
        logOperator.setFromLine(1);
        logOperator.setPageSize(100);
        EngineConnLogs logs = (EngineConnLogs) logOperator.apply();
        System.out.println("logPath: " + logs.logPath() + ", logs: " + logs.logs());
        System.out.println("Metrics: " + metricsOperator.apply());
        onceJob.waitForCompleted();
        System.exit(0);
    }
}
