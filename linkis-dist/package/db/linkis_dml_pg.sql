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



-- 变量：
SET VAR.SPARK_LABEL="spark-3.2.1";
SET VAR.HIVE_LABEL="hive-3.1.3";
SET VAR.PYTHON_LABEL="python-python2";
SET VAR.PIPELINE_LABEL="pipeline-1";
SET VAR.JDBC_LABEL="jdbc-4";
SET VAR.PRESTO_LABEL="presto-0.234";
SET VAR.TRINO_LABEL="trino-371";
SET VAR.IO_FILE_LABEL="io_file-1.0";
SET VAR.OPENLOOKENG_LABEL="openlookeng-1.5.0";
SET VAR.ELASTICSEARCH_LABEL="elasticsearch-7.6.2";

delete from linkis_ps_configuration_config_key;
alter sequence linkis_ps_configuration_config_key_id_seq restart with 1;
-- Global Settings
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName") VALUES ('wds.linkis.rm.yarnqueue', 'yarn队列名', 'yarn队列名', 'default', 'None', NULL, '0', '0', '1', '队列资源');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName") VALUES ('wds.linkis.rm.yarnqueue.instance.max', '取值范围：1-128，单位：个', '队列实例最大个数', '128', 'Regex', '^(?:[1-9]\d?|[1234]\d{2}|128)$', '0', '0', '1', '队列资源');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName") VALUES ('wds.linkis.rm.yarnqueue.cores.max', '取值范围：1-500，单位：个', '队列CPU使用上限', '500', 'Regex', '^(?:[1-9]\d?|[1234]\d{2}|500)$', '0', '0', '1', '队列资源');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName") VALUES ('wds.linkis.rm.yarnqueue.memory.max', '取值范围：1-1000，单位：G', '队列内存使用上限', '1000G', 'Regex', '^([1-9]\d{0,2}|1000)(G|g)$', '0', '0', '1', '队列资源');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName") VALUES ('wds.linkis.rm.client.memory.max', '取值范围：1-100，单位：G', '全局各个引擎内存使用上限', '100G', 'Regex', '^([1-9]\d{0,1}|100)(G|g)$', '0', '0', '1', '队列资源');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName") VALUES ('wds.linkis.rm.client.core.max', '取值范围：1-128，单位：个', '全局各个引擎核心个数上限', '128', 'Regex', '^(?:[1-9]\d?|[1][0-2][0-8])$', '0', '0', '1', '队列资源');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName") VALUES ('wds.linkis.rm.instance', '范围：1-20，单位：个', '全局各个引擎最大并发数', '20', 'NumInterval', '[1,20]', '0', '0', '1', '队列资源');
-- spark
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.rm.instance', '范围：1-20，单位：个', 'spark引擎最大并发数', '20', 'NumInterval', '[1,20]', '0', '0', '1', '队列资源', 'spark');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('spark.executor.instances', '取值范围：1-40，单位：个', 'spark执行器实例最大并发数', '1', 'NumInterval', '[1,40]', '0', '0', '2', 'spark资源设置', 'spark');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('spark.executor.cores', '取值范围：1-8，单位：个', 'spark执行器核心个数',  '1', 'NumInterval', '[1,8]', '0', '0', '1','spark资源设置', 'spark');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('spark.executor.memory', '取值范围：1-15，单位：G', 'spark执行器内存大小', '1g', 'Regex', '^([1-9]|1[0-5])(G|g)$', '0', '0', '3', 'spark资源设置', 'spark');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('spark.driver.cores', '取值范围：只能取1，单位：个', 'spark驱动器核心个数', '1', 'NumInterval', '[1,1]', '0', '1', '1', 'spark资源设置','spark');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('spark.driver.memory', '取值范围：1-15，单位：G', 'spark驱动器内存大小','1g', 'Regex', '^([1-9]|1[0-5])(G|g)$', '0', '0', '1', 'spark资源设置', 'spark');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.engineconn.max.free.time', '取值范围：3m,15m,30m,1h,2h', '引擎空闲退出时间','1h', 'OFT', '["1h","2h","30m","15m","3m"]', '0', '0', '1', 'spark引擎设置', 'spark');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('spark.tispark.pd.addresses', NULL, NULL, 'pd0:2379', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('spark.tispark.tidb.addr', NULL, NULL, 'tidb', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('spark.tispark.tidb.password', NULL, NULL, NULL, 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('spark.tispark.tidb.port', NULL, NULL, '4000', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('spark.tispark.tidb.user', NULL, NULL, 'root', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('spark.python.version', '取值范围：python2,python3', 'python版本','python3', 'OFT', '["python3","python2"]', '0', '0', '1', 'spark引擎设置', 'spark');
-- hive
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.rm.instance', '范围：1-20，单位：个', 'hive引擎最大并发数', '20', 'NumInterval', '[1,20]', '0', '0', '1', '队列资源', 'hive');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.engineconn.java.driver.memory', '取值范围：1-10，单位：G', 'hive引擎初始化内存大小','1g', 'Regex', '^([1-9]|10)(G|g)$', '0', '0', '1', 'hive引擎设置', 'hive');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('hive.client.java.opts', 'hive客户端进程参数', 'hive引擎启动时jvm参数','', 'None', NULL, '1', '1', '1', 'hive引擎设置', 'hive');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('mapred.reduce.tasks', '范围：-1-10000，单位：个', 'reduce数', '-1', 'NumInterval', '[-1,10000]', '0', '1', '1', 'hive资源设置', 'hive');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.engineconn.max.free.time', '取值范围：3m,15m,30m,1h,2h', '引擎空闲退出时间','1h', 'OFT', '["1h","2h","30m","15m","3m"]', '0', '0', '1', 'hive引擎设置', 'hive');

-- python
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.rm.client.memory.max', '取值范围：1-100，单位：G', 'python驱动器内存使用上限', '20G', 'Regex', '^([1-9]\d{0,1}|100)(G|g)$', '0', '0', '1', '队列资源', 'python');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.rm.client.core.max', '取值范围：1-128，单位：个', 'python驱动器核心个数上限', '10', 'Regex', '^(?:[1-9]\d?|[1234]\d{2}|128)$', '0', '0', '1', '队列资源', 'python');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.rm.instance', '范围：1-20，单位：个', 'python引擎最大并发数', '10', 'NumInterval', '[1,20]', '0', '0', '1', '队列资源', 'python');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.engineconn.java.driver.memory', '取值范围：1-2，单位：G', 'python引擎初始化内存大小', '1g', 'Regex', '^([1-2])(G|g)$', '0', '0', '1', 'python引擎设置', 'python');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('python.version', '取值范围：python2,python3', 'python版本','python2', 'OFT', '["python3","python2"]', '0', '0', '1', 'python引擎设置', 'python');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.engineconn.max.free.time', '取值范围：3m,15m,30m,1h,2h', '引擎空闲退出时间','1h', 'OFT', '["1h","2h","30m","15m","3m"]', '0', '0', '1', 'python引擎设置', 'python');

-- pipeline
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('pipeline.output.mold', '取值范围：csv或excel', '结果集导出类型','csv', 'OFT', '["csv","excel"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('pipeline.field.split', '取值范围：，或\t', 'csv分隔符',',', 'OFT', '[",","\\t"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('pipeline.output.charset', '取值范围：utf-8或gbk', '结果集导出字符集','gbk', 'OFT', '["utf-8","gbk"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('pipeline.output.isoverwrite', '取值范围：true或false', '是否覆写','true', 'OFT', '["true","false"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.rm.instance', '范围：1-3，单位：个', 'pipeline引擎最大并发数','3', 'NumInterval', '[1,3]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.engineconn.java.driver.memory', '取值范围：1-10，单位：G', 'pipeline引擎初始化内存大小','2g', 'Regex', '^([1-9]|10)(G|g)$', '0', '0', '1', 'pipeline资源设置', 'pipeline');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('pipeline.output.shuffle.null.type', '取值范围：NULL或者BLANK', '空值替换','NULL', 'OFT', '["NULL","BLANK"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
-- jdbc
insert into "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.jdbc.connect.url', '例如:jdbc:mysql://127.0.0.1:10000', 'jdbc连接地址', 'jdbc:mysql://127.0.0.1:10000', 'Regex', '^\s*jdbc:\w+://([^:]+)(:\d+)(/[^\?]+)?(\?\S*)?$', '0', '0', '1', '数据源配置', 'jdbc');
insert into "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.jdbc.driver', '例如:com.mysql.jdbc.Driver', 'jdbc连接驱动', '', 'None', '', '0', '0', '1', '用户配置', 'jdbc');
insert into "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.jdbc.version', '取值范围：jdbc3,jdbc4', 'jdbc版本','jdbc4', 'OFT', '["jdbc3","jdbc4"]', '0', '0', '1', '用户配置', 'jdbc');
insert into "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.jdbc.username', 'username', '数据库连接用户名', '', 'None', '', '0', '0', '1', '用户配置', 'jdbc');
insert into "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.jdbc.password', 'password', '数据库连接密码', '', 'None', '', '0', '0', '1', '用户配置', 'jdbc');
insert into "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.jdbc.connect.max', '范围：1-20，单位：个', 'jdbc引擎最大连接数', '10', 'NumInterval', '[1,20]', '0', '0', '1', '数据源配置', 'jdbc');

-- io_file
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.rm.instance', '范围：1-20，单位：个', 'io_file引擎最大并发数', '10', 'NumInterval', '[1,20]', '0', '0', '1', 'io_file引擎资源上限', 'io_file');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.rm.client.memory.max', '取值范围：1-50，单位：G', 'io_file引擎最大内存', '20G', 'Regex', '^([1-9]\d{0,1}|100)(G|g)$', '0', '0', '1', 'io_file引擎资源上限', 'io_file');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName", "engine_conn_type") VALUES ('wds.linkis.rm.client.core.max', '取值范围：1-100，单位：个', 'io_file引擎最大核心数', '40', 'Regex', '^(?:[1-9]\d?|[1234]\d{2}|128)$', '0', '0', '1', 'io_file引擎资源上限', 'io_file');

-- openlookeng
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.openlookeng.url', '例如:http://127.0.0.1:8080', '连接地址', 'http://127.0.0.1:8080', 'Regex', '^\s*http://([^:]+)(:\d+)(/[^\?]+)?(\?\S*)?$', 'openlookeng', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.openlookeng.catalog', 'catalog', 'catalog', 'system', 'None', '', 'openlookeng', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.openlookeng.source', 'source', 'source', 'global', 'None', '', 'openlookeng', '0', '0', 1, '数据源配置');

-- elasticsearch
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.es.cluster', '例如:http://127.0.0.1:9200', '连接地址', 'http://127.0.0.1:9200', 'None', '', 'elasticsearch', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.es.datasource', '连接别名', '连接别名', 'hadoop', 'None', '', 'elasticsearch', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.es.username', 'username', 'ES集群用户名', '无', 'None', '', 'elasticsearch', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.es.password', 'password', 'ES集群密码', '无', 'None', '','elasticsearch', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.es.auth.cache', '客户端是否缓存认证', '客户端是否缓存认证', 'false', 'None', '', 'elasticsearch', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.es.sniffer.enable', '客户端是否开启 sniffer', '客户端是否开启 sniffer', 'false', 'None', '', 'elasticsearch', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.es.http.method', '调用方式', 'HTTP请求方式', 'GET', 'None', '', 'elasticsearch', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.es.http.endpoint', '/_search', 'JSON 脚本调用的 Endpoint', '/_search', 'None', '', 'elasticsearch', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.es.sql.endpoint', '/_sql', 'SQL 脚本调用的 Endpoint', '/_sql', 'None', '', 'elasticsearch', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.es.sql.format', 'SQL 脚本调用的模板，%s 替换成 SQL 作为请求体请求Es 集群', '请求体', '{"query":"%s"}', 'None', '', 'elasticsearch', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.es.headers.*', '客户端 Headers 配置', '客户端 Headers 配置', '无', 'None', '', 'elasticsearch', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.engineconn.concurrent.limit', '引擎最大并发', '引擎最大并发', '100', 'None', '', 'elasticsearch', '0', '0', 1, '数据源配置');

-- presto
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('wds.linkis.presto.url', 'Presto 集群连接', 'presto连接地址', 'http://127.0.0.1:8080', 'None', NULL, 'presto', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('wds.linkis.presto.catalog', '查询的 Catalog ', 'presto连接的catalog', 'hive', 'None', NULL, 'presto', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('wds.linkis.presto.schema', '查询的 Schema ', '数据库连接schema', '', 'None', NULL, 'presto', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('wds.linkis.presto.source', '查询使用的 source ', '数据库连接source', '', 'None', NULL, 'presto', '0', '0', 1, '数据源配置');

-- trino
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.trino.default.limit', '查询的结果集返回条数限制', '结果集条数限制', '5000', 'None', '', 'trino', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.trino.http.connectTimeout', '连接Trino服务器的超时时间', '连接超时时间（秒）', '60', 'None', '', 'trino', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.trino.http.readTimeout', '等待Trino服务器返回数据的超时时间', '传输超时时间（秒）', '60', 'None', '', 'trino', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.trino.resultSet.cache.max', 'Trino结果集缓冲区大小', '结果集缓冲区', '512k', 'None', '', 'trino', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.trino.url', 'Trino服务器URL', 'Trino服务器URL', 'http://127.0.0.1:9401', 'None', '', 'trino', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.trino.user', '用于连接Trino查询服务的用户名', '用户名', 'null', 'None', '', 'trino', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.trino.password', '用于连接Trino查询服务的密码', '密码', 'null', 'None', '', 'trino', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.trino.passwordCmd', '用于连接Trino查询服务的密码回调命令', '密码回调命令', 'null', 'None', '', 'trino', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.trino.catalog', '连接Trino查询时使用的catalog', 'Catalog', 'system', 'None', '', 'trino', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.trino.schema', '连接Trino查询服务的默认schema', 'Schema', '', 'None', '', 'trino', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.trino.ssl.insecured', '是否忽略服务器的SSL证书', '验证SSL证书', 'false', 'None', '', 'trino', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.engineconn.concurrent.limit', '引擎最大并发', '引擎最大并发', '100', 'None', '', 'trino', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.trino.ssl.keystore', 'Trino服务器SSL keystore路径', 'keystore路径', 'null', 'None', '', 'trino', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.trino.ssl.keystore.type', 'Trino服务器SSL keystore类型', 'keystore类型', 'null', 'None', '', 'trino', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.trino.ssl.keystore.password', 'Trino服务器SSL keystore密码', 'keystore密码', 'null', 'None', '', 'trino', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.trino.ssl.truststore', 'Trino服务器SSL truststore路径', 'truststore路径', 'null', 'None', '', 'trino', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.trino.ssl.truststore.type', 'Trino服务器SSL truststore类型', 'truststore类型', 'null', 'None', '', 'trino', '0', '0', 1, '数据源配置');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "engine_conn_type", "is_hidden", "is_advanced", "level", "treeName") VALUES ('linkis.trino.ssl.truststore.password', 'Trino服务器SSL truststore密码', 'truststore密码', 'null', 'None', '', 'trino', '0', '0', 1, '数据源配置');


delete from linkis_cg_manager_label;
alter sequence linkis_cg_manager_label_id_seq restart with 1;
-- Configuration first level directory
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType','*-全局设置,*-*', 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType','*-IDE,*-*', 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType','*-Visualis,*-*', 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType','*-nodeexecution,*-*', 'OPTIONAL', 2, now(), now());


-- Engine level default configuration
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType','*-*,*-*', 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType',(select  '*-*,' || current_setting('VAR.SPARK_LABEL')), 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType',(select  '*-*,' || current_setting('VAR.HIVE_LABEL')), 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType',(select  '*-*,' || current_setting('VAR.PYTHON_LABEL')), 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType',(select  '*-*,' || current_setting('VAR.PIPELINE_LABEL')), 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType',(select  '*-*,' || current_setting('VAR.JDBC_LABEL')), 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType',(select  '*-*,' || current_setting('VAR.OPENLOOKENG_LABEL')), 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType', (select  '*-*,' || current_setting('VAR.ELASTICSEARCH_LABEL')), 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType', (select  '*-*,' || current_setting('VAR.PRESTO_LABEL')), 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType', (select  '*-*,' || current_setting('VAR.TRINO_LABEL')), 'OPTIONAL', 2, now(), now());

delete from linkis_ps_configuration_key_engine_relation;
alter sequence linkis_ps_configuration_key_engine_relation_id_seq restart with 1;
-- Custom correlation engine (e.g. spark) and configKey value
-- Global Settings
insert into "linkis_ps_configuration_key_engine_relation" ("config_key_id", "engine_type_label_id")
(select config.id as "config_key_id", label.id AS "engine_type_label_id" FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type is null and label.label_value = '*-*,*-*');

-- spark(Here choose to associate all spark type Key values with spark)
insert into "linkis_ps_configuration_key_engine_relation" ("config_key_id", "engine_type_label_id")
(select config.id as "config_key_id", label.id AS "engine_type_label_id" FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'spark' and label.label_value = (select  '*-*,' || current_setting('VAR.SPARK_LABEL')));

-- hive
insert into "linkis_ps_configuration_key_engine_relation" ("config_key_id", "engine_type_label_id")
(select config.id as "config_key_id", label.id AS "engine_type_label_id" FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'hive' and label_value = (select  '*-*,' || current_setting('VAR.HIVE_LABEL')));

-- python-python2
insert into "linkis_ps_configuration_key_engine_relation" ("config_key_id", "engine_type_label_id")
(select config.id as "config_key_id", label.id AS "engine_type_label_id" FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'python' and label_value = (select  '*-*,' || current_setting('VAR.PYTHON_LABEL')));

-- pipeline-*
insert into "linkis_ps_configuration_key_engine_relation" ("config_key_id", "engine_type_label_id")
(select config.id as "config_key_id", label.id AS "engine_type_label_id" FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'pipeline' and label_value = (select  '*-*,' || current_setting('VAR.PIPELINE_LABEL')));

-- jdbc-4
insert into "linkis_ps_configuration_key_engine_relation" ("config_key_id", "engine_type_label_id")
(select config.id as "config_key_id", label.id AS "engine_type_label_id" FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'jdbc' and label_value = (select  '*-*,' || current_setting('VAR.JDBC_LABEL')));

-- io_file-1.0
INSERT INTO "linkis_ps_configuration_key_engine_relation" ("config_key_id", "engine_type_label_id")
(SELECT config.id AS "config_key_id", label.id AS "engine_type_label_id" FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'io_file' and label_value = (select  '*-*,' || current_setting('VAR.IO_FILE_LABEL')));

-- openlookeng-*
insert into "linkis_ps_configuration_key_engine_relation" ("config_key_id", "engine_type_label_id")
(select config.id as "config_key_id", label.id AS "engine_type_label_id" FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'openlookeng' and label_value = (select  '*-*,' || current_setting('VAR.OPENLOOKENG_LABEL')));

-- elasticsearch-7.6.2
insert into "linkis_ps_configuration_key_engine_relation" ("config_key_id", "engine_type_label_id")
(select config.id as "config_key_id", label.id AS "engine_type_label_id" FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'elasticsearch' and label_value = (select  '*-*,' || current_setting('VAR.ELASTICSEARCH_LABEL')));

-- presto-0.234
insert into "linkis_ps_configuration_key_engine_relation" ("config_key_id", "engine_type_label_id")
(select config.id as "config_key_id", label.id AS "engine_type_label_id" FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'presto' and label_value = (select  '*-*,' || current_setting('VAR.PRESTO_LABEL')));


-- trino-371
insert into "linkis_ps_configuration_key_engine_relation" ("config_key_id", "engine_type_label_id")
(select config.id as config_key_id, label.id AS engine_type_label_id FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'trino' and label_value = (select  '*-*,' || current_setting('VAR.TRINO_LABEL')));

-- If you need to customize the parameters of the new engine, the following configuration does not need to write SQL initialization
-- Just write the SQL above, and then add applications and engines to the management console to automatically initialize the configuration


-- Configuration secondary directory (creator level default configuration)
-- IDE
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType',(select  '*-IDE,' || current_setting('VAR.SPARK_LABEL')), 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType',(select  '*-IDE,' || current_setting('VAR.HIVE_LABEL')), 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType',(select  '*-IDE,' || current_setting('VAR.PYTHON_LABEL')), 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType',(select  '*-IDE,' || current_setting('VAR.PIPELINE_LABEL')), 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType',(select  '*-IDE,' || current_setting('VAR.JDBC_LABEL')), 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType',(select  '*-IDE,' || current_setting('VAR.OPENLOOKENG_LABEL')), 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType', (select  '*-IDE,' || current_setting('VAR.ELASTICSEARCH_LABEL')), 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType', (select  '*-IDE,' || current_setting('VAR.PRESTO_LABEL')), 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType', (select  '*-IDE,' || current_setting('VAR.TRINO_LABEL')), 'OPTIONAL', 2, now(), now());

-- Visualis
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType',(select  '*-Visualis,' || current_setting('VAR.SPARK_LABEL')), 'OPTIONAL', 2, now(), now());
-- nodeexecution
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType',(select  '*-nodeexecution,' || current_setting('VAR.SPARK_LABEL')), 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType',(select  '*-nodeexecution,' || current_setting('VAR.HIVE_LABEL')), 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType',(select  '*-nodeexecution,' || current_setting('VAR.PYTHON_LABEL')), 'OPTIONAL', 2, now(), now());


delete from linkis_ps_configuration_category;
alter sequence linkis_ps_configuration_category_id_seq restart with 1;
-- Associate first-level and second-level directories
insert into linkis_ps_configuration_category ("label_id", "level") VALUES ((select id from linkis_cg_manager_label where "label_value" = '*-全局设置,*-*'), 1);
insert into linkis_ps_configuration_category ("label_id", "level") VALUES ((select id from linkis_cg_manager_label where "label_value" = '*-IDE,*-*'), 1);
insert into linkis_ps_configuration_category ("label_id", "level") VALUES ((select id from linkis_cg_manager_label where "label_value" = '*-Visualis,*-*'), 1);
insert into linkis_ps_configuration_category ("label_id", "level") VALUES ((select id from linkis_cg_manager_label where "label_value" = '*-nodeexecution,*-*'), 1);
insert into linkis_ps_configuration_category ("label_id", "level") VALUES ((select id from linkis_cg_manager_label where "label_value" = (select  '*-IDE,' || current_setting('VAR.SPARK_LABEL'))), 2);
insert into linkis_ps_configuration_category ("label_id", "level") VALUES ((select id from linkis_cg_manager_label where "label_value" = (select  '*-IDE,' || current_setting('VAR.HIVE_LABEL'))), 2);
insert into linkis_ps_configuration_category ("label_id", "level") VALUES ((select id from linkis_cg_manager_label where "label_value" = (select  '*-IDE,' || current_setting('VAR.PYTHON_LABEL'))), 2);
insert into linkis_ps_configuration_category ("label_id", "level") VALUES ((select id from linkis_cg_manager_label where "label_value" = (select  '*-IDE,' || current_setting('VAR.PIPELINE_LABEL'))), 2);
insert into linkis_ps_configuration_category ("label_id", "level") VALUES ((select id from linkis_cg_manager_label where "label_value" = (select  '*-IDE,' || current_setting('VAR.JDBC_LABEL'))), 2);
insert into linkis_ps_configuration_category ("label_id", "level") VALUES ((select id from linkis_cg_manager_label where "label_value" = (select  '*-IDE,' || current_setting('VAR.OPENLOOKENG_LABEL'))), 2);
insert into linkis_ps_configuration_category ("label_id", "level") VALUES ((select id from linkis_cg_manager_label where "label_value" = (select  '*-Visualis,' || current_setting('VAR.SPARK_LABEL'))), 2);
insert into linkis_ps_configuration_category ("label_id", "level") VALUES ((select id from linkis_cg_manager_label where "label_value" = (select  '*-nodeexecution,' || current_setting('VAR.SPARK_LABEL'))), 2);
insert into linkis_ps_configuration_category ("label_id", "level") VALUES ((select id from linkis_cg_manager_label where "label_value" = (select  '*-nodeexecution,' || current_setting('VAR.HIVE_LABEL'))), 2);
insert into linkis_ps_configuration_category ("label_id", "level") VALUES ((select id from linkis_cg_manager_label where "label_value" = (select  '*-nodeexecution,' || current_setting('VAR.PYTHON_LABEL'))), 2);


delete from linkis_ps_configuration_config_value;
alter sequence linkis_ps_configuration_config_value_id_seq restart with 1;
-- Associate label and default configuration
insert into "linkis_ps_configuration_config_value" ("config_key_id", "config_value", "config_label_id")
(select "relation"."config_key_id" AS "config_key_id", '' AS "config_value", "relation"."engine_type_label_id" AS "config_label_id" FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = '*-*,*-*');

-- spark default configuration
insert into "linkis_ps_configuration_config_value" ("config_key_id", "config_value", "config_label_id")
(select "relation"."config_key_id" AS "config_key_id", '' AS "config_value", "relation"."engine_type_label_id" AS "config_label_id" FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = (select  '*-*,' || current_setting('VAR.SPARK_LABEL')));

-- hive default configuration
insert into "linkis_ps_configuration_config_value" ("config_key_id", "config_value", "config_label_id")
(select "relation"."config_key_id" AS "config_key_id", '' AS "config_value", "relation"."engine_type_label_id" AS "config_label_id" FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = (select  '*-*,' || current_setting('VAR.HIVE_LABEL')));

-- python default configuration
insert into "linkis_ps_configuration_config_value" ("config_key_id", "config_value", "config_label_id")
(select "relation"."config_key_id" AS "config_key_id", '' AS "config_value", "relation"."engine_type_label_id" AS "config_label_id" FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = (select  '*-*,' || current_setting('VAR.PYTHON_LABEL')));

-- pipeline default configuration
insert into "linkis_ps_configuration_config_value" ("config_key_id", "config_value", "config_label_id")
(select "relation"."config_key_id" AS "config_key_id", '' AS "config_value", "relation"."engine_type_label_id" AS "config_label_id" FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = (select  '*-*,' || current_setting('VAR.PIPELINE_LABEL')));

-- jdbc default configuration
insert into "linkis_ps_configuration_config_value" ("config_key_id", "config_value", "config_label_id")
(select "relation"."config_key_id" AS "config_key_id", '' AS "config_value", "relation"."engine_type_label_id" AS "config_label_id" FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = (select  '*-*,' || current_setting('VAR.JDBC_LABEL')));

-- openlookeng default configuration
insert into "linkis_ps_configuration_config_value" ("config_key_id", "config_value", "config_label_id")
(select "relation"."config_key_id" AS "config_key_id", '' AS "config_value", "relation"."engine_type_label_id" AS "config_label_id" FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = (select  '*-*,' || current_setting('VAR.OPENLOOKENG_LABEL')));

-- elasticsearch default configuration
insert into "linkis_ps_configuration_config_value" ("config_key_id", "config_value", "config_label_id")
(select "relation"."config_key_id" AS "config_key_id", '' AS "config_value", "relation"."engine_type_label_id" AS "config_label_id" FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = (select  '*-*,' || current_setting('VAR.ELASTICSEARCH_LABEL')));

-- presto default configuration
insert into "linkis_ps_configuration_config_value" ("config_key_id", "config_value", "config_label_id")
(select "relation"."config_key_id" AS "config_key_id", '' AS "config_value", "relation"."engine_type_label_id" AS "config_label_id" FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = (select  '*-*,' || current_setting('VAR.PRESTO_LABEL')));

-- trino default configuration
insert into "linkis_ps_configuration_config_value" ("config_key_id", "config_value", "config_label_id")
(select relation.config_key_id AS config_key_id, '' AS config_value, relation.engine_type_label_id AS config_label_id FROM "linkis_ps_configuration_key_engine_relation" relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = (select  '*-*,' || current_setting('VAR.TRINO_LABEL')));


delete from linkis_cg_rm_external_resource_provider;
alter sequence linkis_cg_rm_external_resource_provider_id_seq restart with 1;
insert  into "linkis_cg_rm_external_resource_provider"("resource_type","name","labels","config") values
('Yarn','default',NULL,'{"rmWebAddress":"@YARN_RESTFUL_URL","hadoopVersion":"@HADOOP_VERSION","authorEnable":@YARN_AUTH_ENABLE,"user":"@YARN_AUTH_USER","pwd":"@YARN_AUTH_PWD","kerberosEnable":@YARN_KERBEROS_ENABLE,"principalName":"@YARN_PRINCIPAL_NAME","keytabPath":"@YARN_KEYTAB_PATH","krb5Path":"@YARN_KRB5_PATH"}');


delete from linkis_ps_error_code;
alter sequence linkis_ps_error_code_id_seq restart with 1;
-- errorcode
-- 01 linkis server
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('01001','您的任务没有路由到后台ECM，请联系管理员','The em of labels',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('01002','Linkis服务负载过高，请联系管理员扩容','Unexpected end of file from server',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('01003','Linkis服务负载过高，请联系管理员扩容','failed to ask linkis Manager Can be retried SocketTimeoutException',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('01004','引擎在启动时被Kill，请联系管理员',' [0-9]+ Killed',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('01005','请求Yarn获取队列信息重试2次仍失败，请联系管理员','Failed to request external resourceClassCastException',0);


-- 11 linkis resource 12 user resource 13 user task resouce
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('01101','ECM资源不足，请联系管理员扩容','ECM resources are insufficient',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('01102','ECM 内存资源不足，请联系管理员扩容','ECM memory resources are insufficient',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('01103','ECM CPU资源不足，请联系管理员扩容','ECM CPU resources are insufficient',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('01004','ECM 实例资源不足，请联系管理员扩容','ECM Insufficient number of instances',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('01005','机器内存不足，请联系管理员扩容','Cannot allocate memory',0);

INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('12001','队列CPU资源不足，可以调整Spark执行器个数','Queue CPU resources are insufficient',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('12002','队列内存资源不足，可以调整Spark执行器个数','Insufficient queue memory',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('12003','队列实例数超过限制','Insufficient number of queue instances',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('12004','全局驱动器内存使用上限，可以设置更低的驱动内存','Drive memory resources are insufficient',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('12005','超出全局驱动器CPU个数上限，可以清理空闲引擎','Drive core resources are insufficient',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('12006','超出引擎最大并发数上限，可以清理空闲引擎','Insufficient number of instances',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('12008','获取Yarn队列信息异常,可能是您设置的yarn队列不存在','获取Yarn队列信息异常',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('12009','会话创建失败，%s队列不存在，请检查队列设置是否正确','queue (\S+) does not exist in YARN',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('12010','集群队列内存资源不足，可以联系组内人员释放资源','Insufficient cluster queue memory',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('12011','集群队列CPU资源不足，可以联系组内人员释放资源','Insufficient cluster queue cpu',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('12012','集群队列实例数超过限制','Insufficient cluster queue instance',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('12013','资源不足导致启动引擎超时，您可以进行任务重试','wait for DefaultEngineConn',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('12014','请求引擎超时，可能是因为队列资源不足导致，请重试','wait for engineConn initial timeout',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('12015','您设置的执行器内存已经超过了集群的限定值%s，请减少到限定值以下','is above the max threshold (\S+.+\))',0);


INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('13001','Java进程内存溢出，建议优化脚本内容','OutOfMemoryError',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('13002','使用资源过大，请调优sql或者加大资源','Container killed by YARN for exceeding memory limits',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('13003','使用资源过大，请调优sql或者加大资源','read record exception',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('13004','引擎意外退出，可能是使用资源过大导致','failed because the engine quitted unexpectedly',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('13005','Spark app应用退出，可能是复杂任务导致','Spark application has already stopped',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('13006','Spark context退出，可能是复杂任务导致','Spark application sc has already stopped',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('13007','Pyspark子进程意外退出，可能是复杂任务导致','Pyspark process  has stopped',0);
-- 21 cluster Authority  22 db Authority
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('21001','会话创建失败，用户%s不能提交应用到队列：%s，请联系提供队列给您的人员','User (\S+) cannot submit applications to queue (\S+)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('21002','创建Python解释器失败，请联系管理员','initialize python executor failed',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('21003','创建单机Python解释器失败，请联系管理员','PythonSession process cannot be initialized',0);

INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('22001','%s无权限访问，请申请开通数据表权限，请联系您的数据管理人员','Permission denied:\s*user=[a-zA-Z0-9_]+,\s*access=[A-Z]+\s*,\s*inode="([a-zA-Z0-9/_\.]+)"',0);
-- INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('22002','您可能没有相关权限','Permission denied',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('22003','所查库表无权限','Authorization failed:No privilege',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('22004','用户%s在机器不存在，请确认是否申请了相关权限','user (\S+) does not exist',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('22005','用户在机器不存在，请确认是否申请了相关权限','engineConnExec.sh: Permission denied',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('22006','用户在机器不存在，请确认是否申请了相关权限','at com.sun.security.auth.UnixPrincipal',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('22007','用户在机器不存在，请确认是否申请了相关权限','LoginException: java.lang.NullPointerException: invalid null input: name',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('22008','用户在机器不存在，请确认是否申请了相关权限','User not known to the underlying authentication module',0);

-- 30 Space exceeded 31 user operation
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('30001','库超过限制','is exceeded',0);

INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('31001','用户主动kill任务','is killed by user',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('31002','您提交的EngineTypeLabel没有对应的引擎版本','EngineConnPluginNotFoundException',0);

-- 41 not exist 44 sql  43 python 44 shell 45 scala 46 importExport
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('41001','数据库%s不存在，请检查引用的数据库是否有误','Database ''([a-zA-Z_0-9]+)'' not found',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('41001','数据库%s不存在，请检查引用的数据库是否有误','Database does not exist: ([a-zA-Z_0-9]+)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('41002','表%s不存在，请检查引用的表是否有误','Table or view not found: (["\.a-zA-Z_0-9]+)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('41002','表%s不存在，请检查引用的表是否有误','Table not found ''([a-zA-Z_0-9]+)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('41002','表%s不存在，请检查引用的表是否有误','Table ([a-zA-Z_0-9]+) not found',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('41003','字段%s不存在，请检查引用的字段是否有误','cannot resolve ''"(.+)"'' given input columns',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('41003','字段%s不存在，请检查引用的字段是否有误',' Invalid table alias or column reference ''(.+)'':',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('41003','字段%s不存在，请检查引用的字段是否有误','Column ''(.+)'' cannot be resolved',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('41004','分区字段%s不存在，请检查引用的表%s是否为分区表或分区字段有误','([a-zA-Z_0-9]+) is not a valid partition column in table (["\.a-zA-Z_0-9]+)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('41004','分区字段%s不存在，请检查引用的表是否为分区表或分区字段有误','Partition spec \{(\S+)\} contains non-partition columns',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('41004','分区字段%s不存在，请检查引用的表是否为分区表或分区字段有误','table is not partitioned but partition spec exists:\{(.+)\}',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('41004','表对应的路径不存在，请联系您的数据管理人员','Path does not exist: viewfs',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('41005','文件%s不存在','Caused by:\s*java.io.FileNotFoundException',0);

-- 42 sql
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42001','括号不匹配，请检查代码中括号是否前后匹配','extraneous input ''\)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42002','非聚合函数%s必须写在group by中，请检查代码的group by语法','expression ''(\S+)'' is neither present in the group by',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42002','非聚合函数%s必须写在group by中，请检查代码的group by语法','grouping expressions sequence is empty,\s?and ''(\S+)'' is not an aggregate function',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42002','非聚合函数%s必须写在group by中，请检查代码的group by语法','Expression not in GROUP BY key ''(\S+)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42003','未知函数%s，请检查代码中引用的函数是否有误','Undefined function: ''(\S+)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42003','未知函数%s，请检查代码中引用的函数是否有误','Invalid function ''(\S+)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42004','字段%s存在名字冲突，请检查子查询内是否有同名字段','Reference ''(\S+)'' is ambiguous',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42004','字段%s存在名字冲突，请检查子查询内是否有同名字段','Ambiguous column Reference ''(\S+)'' in subquery',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42005','字段%s必须指定表或者子查询别名，请检查该字段来源','Column ''(\S+)'' Found in more than One Tables/Subqueries',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42006','表%s在数据库%s中已经存在，请删除相应表后重试','Table or view ''(\S+)'' already exists in database ''(\S+)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42006','表%s在数据库中已经存在，请删除相应表后重试','Table (\S+) already exists',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42006','表%s在数据库中已经存在，请删除相应表后重试','Table already exists',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42006','表%s在数据库中已经存在，请删除相应表后重试','AnalysisException: (S+) already exists',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42007','插入目标表字段数量不匹配,请检查代码！','requires that the data to be inserted have the same number of columns as the target table',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42008','数据类型不匹配，请检查代码！','due to data type mismatch: differing types in',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42009','字段%s引用有误，请检查字段是否存在！','Invalid column reference (S+)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42010','字段%s提取数据失败','Can''t extract value from (S+): need',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42011','括号或者关键字不匹配，请检查代码！','mismatched input ''(\S+)'' expecting',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42012','group by 位置2不在select列表中，请检查代码！','GROUP BY position (S+) is not in select list',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42013','字段提取数据失败请检查字段类型','Can''t extract value from (S+): need struct type but got string',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42014','插入数据未指定目标表字段%s，请检查代码！','Cannot insert into target table because column number/types are different ''(S+)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42015','表别名%s错误，请检查代码！','Invalid table alias ''(\S+)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42016','UDF函数未指定参数，请检查代码！','UDFArgumentException Argument expected',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42017','聚合函数%s不能写在group by 中，请检查代码！','aggregate functions are not allowed in GROUP BY',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42018','您的代码有语法错误，请您修改代码之后执行','SemanticException Error in parsing',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42019','表不存在，请检查引用的表是否有误','table not found',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42020','函数使用错误，请检查您使用的函数方式','No matching method',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42021','您的sql代码可能有语法错误，请检查sql代码','FAILED: ParseException',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42022','您的sql代码可能有语法错误，请检查sql代码','org.apache.spark.sql.catalyst.parser.ParseException',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42022','您的sql代码可能有语法错误，请检查sql代码','org.apache.hadoop.hive.ql.parse.ParseException',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42023','聚合函数不能嵌套','aggregate function in the argument of another aggregate function',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42024','聚合函数不能嵌套','aggregate function parameters overlap with the aggregation',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42025','union 的左右查询字段不一致','Union can only be performed on tables',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42025','hql报错，union 的左右查询字段不一致','both sides of union should match',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42025','union左表和右表类型不一致','on first table and type',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42026','您的建表sql不能推断出列信息','Unable to infer the schema',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42027','动态分区的严格模式需要指定列，您可用通过设置set hive.exec.dynamic.partition.mode=nostrict','requires at least one static partition',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42028','函数输入参数有误','Invalid number of arguments for function',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42029','sql语法报错，select * 与group by无法一起使用','not allowed in select list when GROUP BY  ordinal',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42030','where/having子句之外不支持引用外部查询的表达式','the outer query are not supported outside of WHERE',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42031','sql语法报错，group by 后面不能跟一个表','show up in the GROUP BY list',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42032','hql报错，窗口函数中的字段重复','check for circular dependencies',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42033','sql中出现了相同的字段','Found duplicate column',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42034','sql语法不支持','not supported in current context',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42035','hql语法报错，嵌套子查询语法问题','Unsupported SubQuery Expression',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42036','hql报错，子查询中in 用法有误','in definition of SubQuery',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43037','表字段类型修改导致的转型失败，请联系修改人员','cannot be cast to',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43038','select 的表可能有误','Invalid call to toAttribute on unresolved object',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43039','语法问题，请检查脚本','Distinct window functions are not supported',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43040','查询一定要指定数据源和库信息','Schema must be specified when session schema is not set',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43041','用户UDF函数 %s 加载失败，请检查后再执行','Invalid function (\S+)',0);
--  43 python
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43001','代码中存在NoneType空类型变量，请检查代码','''NoneType'' object',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43002','数组越界','IndexError:List index out of range',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43003','您的代码有语法错误，请您修改代码之后执行','SyntaxError',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43004','python代码变量%s未定义','name ''(S+)'' is not defined',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43005','python udf %s 未定义','Undefined function:s+''(S+)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43006','python执行不能将%s和%s两种类型进行连接','cannot concatenate ''(S+)'' and ''(S+)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43007','pyspark执行失败，可能是语法错误或stage失败','Py4JJavaError: An error occurred',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43009','python代码缩进有误','unexpected indent',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43010','python代码反斜杠后面必须换行','unexpected character after line',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43011','导出Excel表超过最大限制1048575','Invalid row number',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43012','python save as table未指定格式，默认用parquet保存，hive查询报错','parquet.io.ParquetDecodingException',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43013','索引使用错误','IndexError',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43014','sql语法有问题','raise ParseException',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43015','当前节点需要的CS表解析失败，请检查当前CSID对应的CS表是否存在','Cannot parse cs table for node',0);

-- 46 importExport
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('46001','找不到导入文件地址：%s','java.io.FileNotFoundException: (\S+) \(No such file or directory\)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('46002','导出为excel时临时文件目录权限异常','java.io.IOException: Permission denied(.+)at org.apache.poi.xssf.streaming.SXSSFWorkbook.createAndRegisterSXSSFSheet',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('46003','导出文件时无法创建目录：%s','java.io.IOException: Mkdirs failed to create (\S+) (.+)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('46004','导入模块错误，系统没有%s模块，请联系运维人员安装','ImportError: No module named (S+)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('46005','导出语句错误，请检查路径或命名','Illegal out script',0);

-- 91 wtss
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('91001','找不到变量值，请确认您是否设置相关变量','not find variable substitution for',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('91002','不存在的代理用户，请检查你是否申请过平台层（bdp或者bdap）用户','failed to change current working directory ownership',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('91003','请检查提交用户在WTSS内是否有该代理用户的权限，代理用户中是否存在特殊字符，是否用错了代理用户，OS层面是否有该用户，系统设置里面是否设置了该用户为代理用户','没有权限执行当前任务',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('91004','平台层不存在您的执行用户，请在ITSM申请平台层（bdp或者bdap）用户','使用chown命令修改',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('91005','未配置代理用户，请在ITSM走WTSS用户变更单，为你的用户授权改代理用户','请联系系统管理员为您的用户添加该代理用户',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('91006','您的用户初始化有问题，请联系管理员','java: No such file or directory',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('91007','JobServer中不存在您的脚本文件，请将你的脚本文件放入对应的JobServer路径中', 'Could not open input file for reading%does not exist',0);

delete from linkis_mg_gateway_auth_token;
alter sequence linkis_mg_gateway_auth_token_id_seq restart with 1;
-- ----------------------------
-- Default Tokens
-- ----------------------------
INSERT INTO "linkis_mg_gateway_auth_token"("token_name","legal_users","legal_hosts","business_owner","create_time","update_time","elapse_day","update_by") VALUES ('QML-AUTH','*','*','BDP',now(),now(),-1,'LINKIS');
INSERT INTO "linkis_mg_gateway_auth_token"("token_name","legal_users","legal_hosts","business_owner","create_time","update_time","elapse_day","update_by") VALUES ('BML-AUTH','*','*','BDP',now(),now(),-1,'LINKIS');
INSERT INTO "linkis_mg_gateway_auth_token"("token_name","legal_users","legal_hosts","business_owner","create_time","update_time","elapse_day","update_by") VALUES ('WS-AUTH','*','*','BDP',now(),now(),-1,'LINKIS');
INSERT INTO "linkis_mg_gateway_auth_token"("token_name","legal_users","legal_hosts","business_owner","create_time","update_time","elapse_day","update_by") VALUES ('DSS-AUTH','*','*','BDP',now(),now(),-1,'LINKIS');
INSERT INTO "linkis_mg_gateway_auth_token"("token_name","legal_users","legal_hosts","business_owner","create_time","update_time","elapse_day","update_by") VALUES ('QUALITIS-AUTH','*','*','BDP',now(),now(),-1,'LINKIS');
INSERT INTO "linkis_mg_gateway_auth_token"("token_name","legal_users","legal_hosts","business_owner","create_time","update_time","elapse_day","update_by") VALUES ('VALIDATOR-AUTH','*','*','BDP',now(),now(),-1,'LINKIS');
INSERT INTO "linkis_mg_gateway_auth_token"("token_name","legal_users","legal_hosts","business_owner","create_time","update_time","elapse_day","update_by") VALUES ('LINKISCLI-AUTH','*','*','BDP',now(),now(),-1,'LINKIS');
INSERT INTO "linkis_mg_gateway_auth_token"("token_name","legal_users","legal_hosts","business_owner","create_time","update_time","elapse_day","update_by") VALUES ('DSM-AUTH','*','*','BDP',now(),now(),-1,'LINKIS');
INSERT INTO "linkis_mg_gateway_auth_token"("token_name","legal_users","legal_hosts","business_owner","create_time","update_time","elapse_day","update_by") VALUES ('LINKIS_CLI_TEST','*','*','BDP',now(),now(),-1,'LINKIS');

delete from linkis_ps_dm_datasource_type;
alter sequence linkis_ps_dm_datasource_type_id_seq restart with 1;
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers", "description_en", "option_en", "classifier_en") VALUES ('kafka', 'kafka', 'kafka', '消息队列', '', 2, 'Kafka', 'Kafka', 'Message Queue');
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers", "description_en", "option_en", "classifier_en") VALUES ('hive', 'hive数据库', 'hive', '大数据存储', '', 3, 'Hive Database', 'Hive', 'Big Data storage');
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers", "description_en", "option_en", "classifier_en") VALUES ('elasticsearch', 'elasticsearch数据源', 'es无结构化存储', '分布式全文索引', '', 3, 'Elasticsearch Datasource', 'Es No Structured Storage', 'Distributed Full-Text Indexing');
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers", "description_en", "option_en", "classifier_en") VALUES ('mongodb', 'mongodb', 'NoSQL文档存储', 'NoSQL', null, 3, 'mongodb', 'NoSQL Document Storage', 'NOSQL');

-- jdbc
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers", "description_en", "option_en", "classifier_en") VALUES ('mysql', 'mysql数据库', 'mysql数据库', '关系型数据库', '', 3, 'Mysql Database', 'Mysql Database', 'Relational Database');
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers", "description_en", "option_en", "classifier_en") VALUES ('oracle', 'oracle数据库', 'oracle', '关系型数据库', '', 3, 'Oracle Database', 'Oracle Relational Database', 'Relational Database');
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers", "description_en", "option_en", "classifier_en") VALUES ('dm', '达梦数据库', 'dm', '关系型数据库', '', 3, 'Dameng Database', 'Dm', 'Relational Database');
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers", "description_en", "option_en", "classifier_en") VALUES ('kingbase', '人大金仓数据库', 'kingbase', '关系型数据库', '', 3, 'Renmin Jincang Database', 'Kingbase', 'Relational Database');
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers", "description_en", "option_en", "classifier_en") VALUES ('postgresql', 'postgresql数据库', 'postgresql', '关系型数据库', '', 3, 'Postgresql Database', 'Postgresql', 'Relational Database');
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers", "description_en", "option_en", "classifier_en") VALUES ('sqlserver', 'sqlserver数据库', 'sqlserver', '关系型数据库', '', 3, 'Sqlserver Database', 'Sqlserver', 'Relational Database');
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers", "description_en", "option_en", "classifier_en") VALUES ('db2', 'db2数据库', 'db2', '关系型数据库', '', 3, 'Db2 Database', 'Db2', 'Relational Database');
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers", "description_en", "option_en", "classifier_en") VALUES ('greenplum', 'greenplum数据库', 'greenplum', '关系型数据库', '', 3, 'Greenplum Database', 'Greenplum', 'Relational Database');
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers", "description_en", "option_en", "classifier_en") VALUES ('doris', 'doris数据库', 'doris', 'olap', '', 4, 'Doris Database', 'Doris', 'Olap');
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers", "description_en", "option_en", "classifier_en") VALUES ('clickhouse', 'clickhouse数据库', 'clickhouse', 'olap', '', 4, 'Clickhouse Database', 'Clickhouse', 'Olap');
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers", "description_en", "option_en", "classifier_en") VALUES ('tidb', 'tidb数据库', 'tidb', '关系型数据库', '', 3, 'TiDB Database', 'TiDB', 'Relational Database');
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers", "description_en", "option_en", "classifier_en") VALUES ('starrocks', 'starrocks数据库', 'starrocks', 'olap', '', 4, 'StarRocks Database', 'StarRocks', 'Olap');
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers", "description_en", "option_en", "classifier_en") VALUES ('gaussdb', 'gaussdb数据库', 'gaussdb', '关系型数据库', '', 3, 'GaussDB Database', 'GaussDB', 'Relational Database');
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers", "description_en", "option_en", "classifier_en") VALUES ('oceanbase', 'oceanbase数据库', 'oceanbase', 'olap', '', 4, 'oceanbase Database', 'oceanbase', 'Olap');


delete from linkis_ps_dm_datasource_type_key;
alter sequence linkis_ps_dm_datasource_type_key_id_seq restart with 1;
INSERT INTO "linkis_ps_dm_datasource_type_key" ("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time") VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'mongodb'), 'username', '用户名', 'Username', NULL, 'TEXT', NULL, '1', '用户名', 'Username', '^[0-9A-Za-z_-]+$', NULL, '', NULL, now(), now());
INSERT INTO "linkis_ps_dm_datasource_type_key" ("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time") VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'mongodb'), 'password', '密码', 'Password', NULL, 'PASSWORD', NULL, '1', '密码', 'Password', '', NULL, '', NULL,  now(), now());
INSERT INTO "linkis_ps_dm_datasource_type_key" ("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time") VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'mongodb'), 'database', '默认库', 'Database', NULL, 'TEXT', NULL, '1', '默认库', 'Database', '^[0-9A-Za-z_-]+$', NULL, '', NULL,  now(), now());
INSERT INTO "linkis_ps_dm_datasource_type_key" ("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time") VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'mongodb'), 'host', 'Host', 'Host', NULL, 'TEXT', NULL, '1', 'mongodb Host', 'Host', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO "linkis_ps_dm_datasource_type_key" ("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time") VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'mongodb'), 'port', '端口', 'Port', NULL, 'TEXT', NULL, '1', '端口', 'Port', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO "linkis_ps_dm_datasource_type_key" ("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time") VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'mongodb'), 'params', '连接参数', 'Params', NULL, 'TEXT', NULL, '0', '输入JSON格式: {"param":"value"}', 'Input JSON Format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now());



INSERT INTO "linkis_ps_dm_datasource_type_key"
    ("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time")
VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'hive'), 'envId', '集群环境(Cluster env)', 'Cluster env', NULL, 'SELECT', NULL, '1', '集群环境(Cluster env)', 'Cluster env', NULL, NULL, NULL, (select '/data-source-manager/env-list/all/type/' || (select id from "linkis_ps_dm_datasource_type" where "name" = 'hive')), now(), now());

INSERT INTO "linkis_ps_dm_datasource_type_key"
    ("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time")
VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'kafka'), 'envId', '集群环境(Cluster env)', 'Cluster env', NULL, 'SELECT', NULL, '1', '集群环境(Cluster env)', 'Cluster env', NULL, NULL, NULL, (select '/data-source-manager/env-list/all/type/' || (select id from "linkis_ps_dm_datasource_type" where "name" = 'kafka')), now(), now());


INSERT INTO "linkis_ps_dm_datasource_type_key"
    ("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time")
VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'elasticsearch'), 'username', '用户名(Username)' , 'Username', NULL, 'TEXT', NULL, '1', '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, '', NULL, now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'elasticsearch'), 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, '1', '密码(Password)', 'Password', '', NULL, '', NULL, now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'elasticsearch'), 'elasticUrls', 'ES连接URL(Elastic Url)', 'Elastic Url', NULL, 'TEXT', NULL, '1', 'ES连接URL(Elastic Url)', 'Elastic Url', '', NULL, '', NULL, now(), now());

-- https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-jdbc-url-format.html
INSERT INTO "linkis_ps_dm_datasource_type_key"
    ("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time")
VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'mysql'), 'address', '地址', 'Address', NULL, 'TEXT', NULL, '0', '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'mysql'), 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, '0', '主机名(Host)', 'Host', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'mysql'), 'port', '端口号(Port)','Port', NULL, 'TEXT', NULL, '0', '端口号(Port)','Port', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'mysql'), 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'com.mysql.jdbc.Driver', 'TEXT', NULL, '0', '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'mysql'), 'params', '连接参数(Connection params)', 'Connection params', NULL, 'TEXT', NULL, '0', '输入JSON格式(Input JSON format): {"param":"value"}', 'Input JSON format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'mysql'), 'username', '用户名(Username)', 'Username', NULL, 'TEXT', NULL, '0', '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'mysql'), 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, '0', '密码(Password)', 'Password', '', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'mysql'), 'databaseName', '数据库名(Database name)', 'Database name', NULL, 'TEXT', NULL, '0', '数据库名(Database name)', 'Database name', NULL, NULL, NULL, NULL,  now(), now());

-- https://docs.oracle.com/en/database/oracle/oracle-database/21/jajdb/oracle/jdbc/OracleDriver.html
INSERT INTO "linkis_ps_dm_datasource_type_key"
    ("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time")
VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'oracle'), 'address', '地址', 'Address', NULL, 'TEXT', NULL, '0', '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'oracle'), 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, '1', '主机名(Host)', 'Host', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'oracle'), 'port', '端口号(Port)', 'Port', NULL, 'TEXT', NULL, '1', '端口号(Port)', 'Port', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'oracle'), 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'oracle.jdbc.driver.OracleDriver', 'TEXT', NULL, '1', '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'oracle'), 'params', '连接参数(Connection params)', 'Connection params', NULL, 'TEXT', NULL, '0', '输入JSON格式(Input JSON format): {"param":"value"}', 'Input JSON format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'oracle'), 'username', '用户名(Username)', 'Username', NULL, 'TEXT', NULL, '1', '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'oracle'), 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, '1', '密码(Password)', 'Password', '', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'oracle'), 'sid', 'SID', 'SID', NULL, 'TEXT', NULL, '0', 'SID', 'SID', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'oracle'), 'serviceName', 'service_name', 'service_name', NULL, 'TEXT', NULL, '0', 'service_name', 'service_name', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'oracle'), 'server', 'server', 'server', NULL, 'TEXT', NULL, '0', 'server', 'server', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'oracle'), 'instance', '实例名(instance)', 'Instance', NULL, 'TEXT', NULL, '0', '实例名(instance)', 'Instance', NULL, NULL, NULL, NULL,  now(), now());

INSERT INTO "linkis_ps_dm_datasource_type_key"
    ("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time")
VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'dm'), 'address', '地址', 'Address', NULL, 'TEXT', NULL, '0', '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'dm'), 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, '1', '主机名(Host)', 'Host', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'dm'), 'port', '端口号(Port)', 'Port', NULL, 'TEXT', NULL, '1', '端口号(Port)', 'Port', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'dm'), 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'dm.jdbc.driver.DmDriver', 'TEXT', NULL, '1', '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'dm'), 'params', '连接参数(Connection params)', 'Connection params', NULL, 'TEXT', NULL, '0', '输入JSON格式(Input JSON format): {"param":"value"}', 'Input JSON format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'dm'), 'username', '用户名(Username)', 'Username', NULL, 'TEXT', NULL, '1', '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'dm'), 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, '1', '密码(Password)', 'Password', '', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'dm'), 'instance', '实例名(instance)', 'Instance', NULL, 'TEXT', NULL, '1', '实例名(instance)', 'Instance', NULL, NULL, NULL, NULL,  now(), now());

INSERT INTO "linkis_ps_dm_datasource_type_key"
    ("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time")
VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'kingbase'), 'address', '地址', 'Address', NULL, 'TEXT', NULL, '0', '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'kingbase'), 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, '1', '主机名(Host)', 'Host', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'kingbase'), 'port', '端口号(Port)', 'Port', NULL, 'TEXT', NULL, '1', '端口号(Port)', 'Port', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'kingbase'), 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'com.kingbase8.Driver', 'TEXT', NULL, '1', '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'kingbase'), 'params', '连接参数(Connection params)', 'Connection params', NULL, 'TEXT', NULL, '0', '输入JSON格式(Input JSON format): {"param":"value"}', 'Input JSON format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'kingbase'), 'username', '用户名(Username)', 'Username', NULL, 'TEXT', NULL, '1', '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'kingbase'), 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, '1', '密码(Password)', 'Password', '', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'kingbase'), 'instance', '实例名(instance)', 'Instance', NULL, 'TEXT', NULL, '1', '实例名(instance)', 'instance', NULL, NULL, NULL, NULL,  now(), now());

-- https://jdbc.postgresql.org/documentation/use/
INSERT INTO "linkis_ps_dm_datasource_type_key"
    ("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time")
VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'postgresql'), 'address', '地址', 'Address', NULL, 'TEXT', NULL, '0', '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'postgresql'), 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, '1', '主机名(Host)', 'Host', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'postgresql'), 'port', '端口号(Port)', 'Port', NULL, 'TEXT', NULL, '1', '端口号(Port)', 'Port', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'postgresql'), 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'org.postgresql.Driver', 'TEXT', NULL, '1', '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'postgresql'), 'params', '连接参数(Connection params)', 'Connection params', NULL, 'TEXT', NULL, '0', '输入JSON格式(Input JSON format): {"param":"value"}', 'Input JSON format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'postgresql'), 'username', '用户名(Username)', 'Username', NULL, 'TEXT', NULL, '1', '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'postgresql'), 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, '1', '密码(Password)', 'Password', '', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'postgresql'), 'instance', '实例名(instance)', 'Instance', NULL, 'TEXT', NULL, '1', '实例名(instance)', 'Instance', NULL, NULL, NULL, NULL,  now(), now());

-- https://learn.microsoft.com/zh-cn/sql/connect/jdbc/building-the-connection-url?redirectedfrom=MSDN&view=sql-server-ver16
INSERT INTO "linkis_ps_dm_datasource_type_key"
    ("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time")
VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'sqlserver'), 'address', '地址', 'Address', NULL, 'TEXT', NULL, '0', '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'sqlserver'), 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, '1', '主机名(Host)', 'Host', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'sqlserver'), 'port', '端口号(Port)', 'Port', NULL, 'TEXT', NULL, '1', '端口号(Port)', 'Port', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'sqlserver'), 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'com.microsoft.sqlserver.jdbc.SQLServerDriver', 'TEXT', NULL, '1', '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'sqlserver'), 'params', '连接参数(Connection params)', 'Connection params', NULL, 'TEXT', NULL, '0', '输入JSON格式(Input JSON format): {"param":"value"}', 'Input JSON format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'sqlserver'), 'username', '用户名(Username)', 'Username', NULL, 'TEXT', NULL, '1', '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'sqlserver'), 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, '1', '密码(Password)', 'Password', '', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'sqlserver'), 'instance', '实例名(instance)', 'Instance', NULL, 'TEXT', NULL, '1', '实例名(instance)', 'Instance', NULL, NULL, NULL, NULL,  now(), now());

-- https://www.ibm.com/docs/en/db2/11.5?topic=cdsudidsdjs-url-format-data-server-driver-jdbc-sqlj-type-4-connectivity
INSERT INTO "linkis_ps_dm_datasource_type_key"
    ("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time")
VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'db2'), 'address', '地址', 'Address', NULL, 'TEXT', NULL, '0', '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'db2'), 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, '1', '主机名(Host)', 'Host', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'db2'), 'port', '端口号(Port)', 'Port', NULL, 'TEXT', NULL, '1', '端口号(Port)', 'Port', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'db2'), 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'com.ibm.db2.jcc.DB2Driver', 'TEXT', NULL, '1', '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'db2'), 'params', '连接参数(Connection params)', 'Connection params', NULL, 'TEXT', NULL, '0', '输入JSON格式(Input JSON format): {"param":"value"}', 'Input JSON format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'db2'), 'username', '用户名(Username)', 'Username', NULL, 'TEXT', NULL, '1', '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'db2'), 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, '1', '密码(Password)', 'Password', '', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'db2'), 'instance', '实例名(instance)', 'Instance', NULL, 'TEXT', NULL, '1', '实例名(instance)', 'Instance', NULL, NULL, NULL, NULL,  now(), now());

-- https://greenplum.docs.pivotal.io/6-1/datadirect/datadirect_jdbc.html#topic_ylk_pbx_2bb
INSERT INTO "linkis_ps_dm_datasource_type_key"
    ("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time")
VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'greenplum'), 'address', '地址', 'Address', NULL, 'TEXT', NULL, '0', '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'greenplum'), 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, '1', '主机名(Host)', 'Host', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'greenplum'), 'port', '端口号(Port)', 'Port', NULL, 'TEXT', NULL, '1', '端口号(Port)', 'Port', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'greenplum'), 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'com.pivotal.jdbc.GreenplumDriver', 'TEXT', NULL, '1', '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'greenplum'), 'params', '连接参数(Connection params)', 'Connection params', NULL, 'TEXT', NULL, '0', '输入JSON格式(Input JSON format): {"param":"value"}', 'Input JSON format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'greenplum'), 'username', '用户名(Username)', 'Username', NULL, 'TEXT', NULL, '1', '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'greenplum'), 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, '1', '密码(Password)', 'Password', '', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'greenplum'), 'instance', '实例名(instance)', 'Instance', NULL, 'TEXT', NULL, '1', '实例名(instance)', 'Instance', NULL, NULL, NULL, NULL,  now(), now());

INSERT INTO "linkis_ps_dm_datasource_type_key"
    ("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time")
VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'doris'), 'address', '地址', 'Address', NULL, 'TEXT', NULL, '0', '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'doris'), 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, '1', '主机名(Host)', 'Host', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'doris'), 'port', '端口号(Port)', 'Port', NULL, 'TEXT', NULL, '1', '端口号(Port)', 'Port', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'doris'), 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'com.mysql.jdbc.Driver', 'TEXT', NULL, '1', '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'doris'), 'params', '连接参数(Connection params)', 'Connection params', NULL, 'TEXT', NULL, '0', '输入JSON格式(Input JSON format): {"param":"value"}', 'Input JSON format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'doris'), 'username', '用户名(Username)', 'Username', NULL, 'TEXT', NULL, '1', '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'doris'), 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, '0', '密码(Password)', 'Password', '', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'doris'), 'instance', '实例名(instance)', 'Instance', NULL, 'TEXT', NULL, '1', '实例名(instance)', 'Instance', NULL, NULL, NULL, NULL,  now(), now());

-- https://github.com/ClickHouse/clickhouse-jdbc/tree/master/clickhouse-jdbc
INSERT INTO "linkis_ps_dm_datasource_type_key"
    ("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time")
VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'clickhouse'), 'address', '地址', 'Address', NULL, 'TEXT', NULL, '0', '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'clickhouse'), 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, '1', '主机名(Host)', 'Host', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'clickhouse'), 'port', '端口号(Port)', 'Port', NULL, 'TEXT', NULL, '1', '端口号(Port)', 'Port', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'clickhouse'), 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'ru.yandex.clickhouse.ClickHouseDriver', 'TEXT', NULL, '1', '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'clickhouse'), 'params', '连接参数(Connection params)', 'Connection params', NULL, 'TEXT', NULL, '0', '输入JSON格式(Input JSON format): {"param":"value"}', 'Input JSON format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'clickhouse'), 'username', '用户名(Username)', 'Username', NULL, 'TEXT', NULL, '1', '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'clickhouse'), 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, '1', '密码(Password)', 'Password', '', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'clickhouse'), 'instance', '实例名(instance)', 'Instance', NULL, 'TEXT', NULL, '1', '实例名(instance)', 'Instance', NULL, NULL, NULL, NULL,  now(), now());


delete from linkis_ps_dm_datasource_env;
alter sequence linkis_ps_dm_datasource_env_id_seq restart with 1;
INSERT INTO "linkis_ps_dm_datasource_env" ("env_name", "env_desc", "datasource_type_id", "parameter", "create_time", "create_user", "modify_time", "modify_user") VALUES ('测试环境SIT', '测试环境SIT', (select id from "linkis_ps_dm_datasource_type" where "name" = 'hive'), '{"uris":"thrift://localhost:9083", "hadoopConf":{"hive.metastore.execute.setugi":"true"}}',  now(), NULL,  now(), NULL);
INSERT INTO "linkis_ps_dm_datasource_env" ("env_name", "env_desc", "datasource_type_id", "parameter", "create_time", "create_user", "modify_time", "modify_user") VALUES ('测试环境UAT', '测试环境UAT', (select id from "linkis_ps_dm_datasource_type" where "name" = 'hive'), '{"uris":"thrift://localhost:9083", "hadoopConf":{"hive.metastore.execute.setugi":"true"}}',  now(), NULL,  now(), NULL);
INSERT INTO "linkis_ps_dm_datasource_env" ("env_name", "env_desc", "datasource_type_id", "parameter", "create_time", "create_user", "modify_time", "modify_user") VALUES ('kafka测试环境SIT', '开源测试环境SIT', (select id from "linkis_ps_dm_datasource_type" where "name" = 'kafka'), '{"uris":"thrift://localhost:9092"}',  now(), NULL,  now(), NULL);


INSERT INTO "linkis_ps_dm_datasource_type_key"
("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time")
VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'tidb'), 'address', '地址', 'Address', NULL, 'TEXT', NULL, '0', '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'tidb'), 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, '1', '主机名(Host)', 'Host', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'tidb'), 'port', '端口号(Port)', 'Port', NULL, 'TEXT', NULL, '1', '端口号(Port)', 'Port', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'tidb'), 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'com.mysql.jdbc.Driver', 'TEXT', NULL, '1', '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'tidb'), 'params', '连接参数(Connection params)', 'Connection params', NULL, 'TEXT', NULL, '0', '输入JSON格式(Input JSON format): {"param":"value"}', 'Input JSON format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'tidb'), 'username', '用户名(Username)', 'Username', NULL, 'TEXT', NULL, '1', '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'tidb'), 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, '0', '密码(Password)', 'Password', '', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'tidb'), 'instance', '实例名(instance)', 'Instance', NULL, 'TEXT', NULL, '1', '实例名(instance)', 'Instance', NULL, NULL, NULL, NULL,  now(), now());

INSERT INTO "linkis_ps_dm_datasource_type_key"
("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time")
VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'starrocks'), 'address', '地址', 'Address', NULL, 'TEXT', NULL, '0', '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'starrocks'), 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, '1', '主机名(Host)', 'Host', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'starrocks'), 'port', '端口号(Port)', 'Port', NULL, 'TEXT', NULL, '1', '端口号(Port)', 'Port', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'starrocks'), 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'com.mysql.jdbc.Driver', 'TEXT', NULL, '1', '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'starrocks'), 'params', '连接参数(Connection params)', 'Connection params', NULL, 'TEXT', NULL, '0', '输入JSON格式(Input JSON format): {"param":"value"}', 'Input JSON format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'starrocks'), 'username', '用户名(Username)', 'Username', NULL, 'TEXT', NULL, '1', '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'starrocks'), 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, '0', '密码(Password)', 'Password', '', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'starrocks'), 'instance', '实例名(instance)', 'Instance', NULL, 'TEXT', NULL, '1', '实例名(instance)', 'Instance', NULL, NULL, NULL, NULL,  now(), now());

INSERT INTO "linkis_ps_dm_datasource_type_key"
("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time")
VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'gaussdb'), 'address', '地址', 'Address', NULL, 'TEXT', NULL, '0', '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'gaussdb'), 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, '1', '主机名(Host)', 'Host', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'gaussdb'), 'port', '端口号(Port)', 'Port', NULL, 'TEXT', NULL, '1', '端口号(Port)', 'Port', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'gaussdb'), 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'org.postgresql.Driver', 'TEXT', NULL, '1', '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'gaussdb'), 'params', '连接参数(Connection params)', 'Connection params', NULL, 'TEXT', NULL, '0', '输入JSON格式(Input JSON format): {"param":"value"}', 'Input JSON format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'gaussdb'), 'username', '用户名(Username)', 'Username', NULL, 'TEXT', NULL, '1', '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'gaussdb'), 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, '1', '密码(Password)', 'Password', '', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'gaussdb'), 'instance', '实例名(instance)', 'Instance', NULL, 'TEXT', NULL, '1', '实例名(instance)', 'Instance', NULL, NULL, NULL, NULL,  now(), now());

INSERT INTO "linkis_ps_dm_datasource_type_key"
("data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time")
VALUES ((select id from "linkis_ps_dm_datasource_type" where "name" = 'oceanbase'), 'address', '地址', 'Address', NULL, 'TEXT', NULL, '0', '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'oceanbase'), 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, '1', '主机名(Host)', 'Host', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'oceanbase'), 'port', '端口号(Port)', 'Port', NULL, 'TEXT', NULL, '1', '端口号(Port)', 'Port', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'oceanbase'), 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'com.mysql.jdbc.Driver', 'TEXT', NULL, '1', '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'oceanbase'), 'params', '连接参数(Connection params)', 'Connection params', NULL, 'TEXT', NULL, '0', '输入JSON格式(Input JSON format): {"param":"value"}', 'Input JSON format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'oceanbase'), 'username', '用户名(Username)', 'Username', NULL, 'TEXT', NULL, '1', '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'oceanbase'), 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, '1', '密码(Password)', 'Password', '', NULL, NULL, NULL,  now(), now()),
       ((select id from "linkis_ps_dm_datasource_type" where "name" = 'oceanbase'), 'instance', '实例名(instance)', 'Instance', NULL, 'TEXT', NULL, '1', '实例名(instance)', 'Instance', NULL, NULL, NULL, NULL,  now(), now());
