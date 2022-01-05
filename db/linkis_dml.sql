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
SET @SPARK_LABEL="spark-2.4.3";
SET @HIVE_LABEL="hive-2.3.3";
SET @PYTHON_LABEL="python-python2";
SET @PIPELINE_LABEL="pipeline-*";
SET @JDBC_LABEL="jdbc-4";

-- 衍生变量：
SET @SPARK_ALL=CONCAT('*-*,',@SPARK_LABEL);
SET @SPARK_IDE=CONCAT('*-IDE,',@SPARK_LABEL);
SET @SPARK_NODE=CONCAT('*-nodeexecution,',@SPARK_LABEL);
SET @SPARK_VISUALIS=CONCAT('*-Visualis,',@SPARK_LABEL);

SET @HIVE_ALL=CONCAT('*-*,',@HIVE_LABEL);
SET @HIVE_IDE=CONCAT('*-IDE,',@HIVE_LABEL);
SET @HIVE_NODE=CONCAT('*-nodeexecution,',@HIVE_LABEL);

SET @PYTHON_ALL=CONCAT('*-*,',@PYTHON_LABEL);
SET @PYTHON_IDE=CONCAT('*-IDE,',@PYTHON_LABEL);
SET @PYTHON_NODE=CONCAT('*-nodeexecution,',@PYTHON_LABEL);

SET @PIPELINE_ALL=CONCAT('*-*,',@PIPELINE_LABEL);
SET @PIPELINE_IDE=CONCAT('*-IDE,',@PIPELINE_LABEL);

SET @JDBC_ALL=CONCAT('*-*,',@JDBC_LABEL);
SET @JDBC_IDE=CONCAT('*-IDE,',@JDBC_LABEL);


-- Global Settings
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('wds.linkis.rm.yarnqueue', 'yarn队列名', 'yarn队列名', 'ide', 'None', NULL, '0', '0', '1', '队列资源');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('wds.linkis.rm.yarnqueue.instance.max', '取值范围：1-128，单位：个', '队列实例最大个数', '30', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|128)$', '0', '0', '1', '队列资源');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('wds.linkis.rm.yarnqueue.cores.max', '取值范围：1-500，单位：个', '队列CPU使用上限', '150', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|500)$', '0', '0', '1', '队列资源');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('wds.linkis.rm.yarnqueue.memory.max', '取值范围：1-1000，单位：G', '队列内存使用上限', '300G', 'Regex', '^([1-9]\\d{0,2}|1000)(G|g)$', '0', '0', '1', '队列资源');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('wds.linkis.rm.client.memory.max', '取值范围：1-100，单位：G', '全局各个引擎内存使用上限', '20G', 'Regex', '^([1-9]\\d{0,1}|100)(G|g)$', '0', '0', '1', '队列资源');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('wds.linkis.rm.client.core.max', '取值范围：1-128，单位：个', '全局各个引擎核心个数上限', '10', 'Regex', '^(?:[1-9]\\d?|[1][0-2][0-8])$', '0', '0', '1', '队列资源');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('wds.linkis.rm.instance', '范围：1-20，单位：个', '全局各个引擎最大并发数', '10', 'NumInterval', '[1,20]', '0', '0', '1', '队列资源');
-- spark
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.instance', '范围：1-20，单位：个', 'spark引擎最大并发数', '10', 'NumInterval', '[1,20]', '0', '0', '1', '队列资源', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.executor.instances', '取值范围：1-40，单位：个', 'spark执行器实例最大并发数', '1', 'NumInterval', '[1,40]', '0', '0', '2', 'spark资源设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.executor.cores', '取值范围：1-8，单位：个', 'spark执行器核心个数',  '1', 'NumInterval', '[1,2]', '0', '0', '1','spark资源设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.executor.memory', '取值范围：1-15，单位：G', 'spark执行器内存大小', '1g', 'Regex', '^([1-9]|1[0-5])(G|g)$', '0', '0', '3', 'spark资源设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.driver.cores', '取值范围：只能取1，单位：个', 'spark驱动器核心个数', '1', 'NumInterval', '[1,1]', '0', '1', '1', 'spark资源设置','spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.driver.memory', '取值范围：1-15，单位：G', 'spark驱动器内存大小','1g', 'Regex', '^([1-9]|1[0-5])(G|g)$', '0', '0', '1', 'spark资源设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.pd.addresses', NULL, NULL, 'pd0:2379', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.tidb.addr', NULL, NULL, 'tidb', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.tidb.password', NULL, NULL, NULL, 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.tidb.port', NULL, NULL, '4000', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.tidb.user', NULL, NULL, 'root', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.python.version', '取值范围：python2,python3', 'python版本','python2', 'OFT', '[\"python3\",\"python2\"]', '0', '0', '1', 'spark引擎设置', 'spark');
-- hive
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.instance', '范围：1-20，单位：个', 'hive引擎最大并发数', '10', 'NumInterval', '[1,20]', '0', '0', '1', '队列资源', 'hive');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.engineconn.java.driver.memory', '取值范围：1-10，单位：G', 'hive引擎初始化内存大小','1g', 'Regex', '^([1-9]|10)(G|g)$', '0', '0', '1', 'hive引擎设置', 'hive');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('hive.client.java.opts', 'hive客户端进程参数', 'hive引擎启动时jvm参数','', 'None', NULL, '1', '1', '1', 'hive引擎设置', 'hive');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('mapred.reduce.tasks', '范围：1-20，单位：个', 'reduce数', '10', 'NumInterval', '[1,20]', '0', '1', '1', 'hive资源设置', 'hive');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('dfs.block.size', '取值范围：2-10，单位：G', 'map数据块大小', '10', 'NumInterval', '[2,10]', '0', '1', '1', 'hive资源设置', 'hive');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('hive.exec.reduce.bytes.per.reducer', '取值范围：2-10，单位：G', 'reduce处理的数据量', '10', 'NumInterval', '[2,10]', '0', '1', '1', 'hive资源设置', 'hive');
-- python
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.client.memory.max', '取值范围：1-100，单位：G', 'python驱动器内存使用上限', '20G', 'Regex', '^([1-9]\\d{0,1}|100)(G|g)$', '0', '0', '1', '队列资源', 'python');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.client.core.max', '取值范围：1-128，单位：个', 'python驱动器核心个数上限', '10', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|128)$', '0', '0', '1', '队列资源', 'python');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.instance', '范围：1-20，单位：个', 'python引擎最大并发数', '10', 'NumInterval', '[1,20]', '0', '0', '1', '队列资源', 'python');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.engineconn.java.driver.memory', '取值范围：1-2，单位：G', 'python引擎初始化内存大小', '1g', 'Regex', '^([1-2])(G|g)$', '0', '0', '1', 'python引擎设置', 'python');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('python.version', '取值范围：python2,python3', 'python版本','python2', 'OFT', '[\"python3\",\"python2\"]', '0', '0', '1', 'python引擎设置', 'python');
-- pipeline
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('pipeline.output.mold', '取值范围：csv或excel', '结果集导出类型','csv', 'OFT', '[\"csv\",\"excel\"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('pipeline.field.split', '取值范围：，或\\t', 'csv分隔符',',', 'OFT', '[\",\",\"\\\\t\"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('pipeline.output.charset', '取值范围：utf-8或gbk', '结果集导出字符集','gbk', 'OFT', '[\"utf-8\",\"gbk\"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('pipeline.output.isoverwtite', '取值范围：true或false', '是否覆写','true', 'OFT', '[\"true\",\"false\"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.instance', '范围：1-3，单位：个', 'pipeline引擎最大并发数','3', 'NumInterval', '[1,3]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.engineconn.java.driver.memory', '取值范围：1-10，单位：G', 'pipeline引擎初始化内存大小','2g', 'Regex', '^([1-9]|10)(G|g)$', '0', '0', '1', 'pipeline资源设置', 'pipeline');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('pipeline.output.shuffle.null.type', '取值范围：NULL或者BLANK', '空值替换','NULL', 'OFT', '[\"NULL\",\"BLANK\"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
-- jdbc
insert into `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.jdbc.connect.url', '例如:jdbc:hive2://127.0.0.1:10000', 'jdbc连接地址', 'jdbc:hive2://127.0.0.1:10000', 'Regex', '^\\s*jdbc:\\w+://([^:]+)(:\\d+)(/[^\\?]+)?(\\?\\S*)?$', '0', '0', '1', '数据源配置', 'jdbc');
insert into `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.jdbc.version', '取值范围：jdbc3,jdbc4', 'jdbc版本','jdbc4', 'OFT', '[\"jdbc3\",\"jdbc4\"]', '0', '0', '1', '数据源配置', 'jdbc');
insert into `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.jdbc.username', 'username', '数据库连接用户名', '', 'None', '', '0', '0', '1', '用户配置', 'jdbc');
insert into `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.jdbc.password', 'password', '数据库连接密码', '', 'None', '', '0', '0', '1', '用户配置', 'jdbc');
insert into `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.jdbc.connect.max', '范围：1-20，单位：个', 'jdbc引擎最大连接数', '10', 'NumInterval', '[1,20]', '0', '0', '1', '数据源配置', 'jdbc');

-- Configuration first level directory
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType','*-全局设置,*-*', 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType','*-IDE,*-*', 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType','*-Visualis,*-*', 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType','*-nodeexecution,*-*', 'OPTIONAL', 2, now(), now());


-- Engine level default configuration
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType','*-*,*-*', 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@SPARK_ALL, 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@HIVE_ALL, 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@PYTHON_ALL, 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@PIPELINE_ALL, 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@JDBC_ALL, 'OPTIONAL', 2, now(), now());

-- Custom correlation engine (e.g. spark-2.4.3) and configKey value
-- Global Settings
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type is null and label.label_value = "*-*,*-*");

-- spark-2.4.3(Here choose to associate all spark type Key values with spark2.4.3)
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'spark' and label.label_value = @SPARK_ALL);

-- hive-1.2.1
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'hive' and label_value = @HIVE_ALL);

-- python-python2
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'python' and label_value = @PYTHON_ALL);

-- pipeline-*
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'pipeline' and label_value = @PIPELINE_ALL);

-- jdbc-4
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'jdbc' and label_value = @JDBC_ALL);


-- If you need to customize the parameters of the new engine, the following configuration does not need to write SQL initialization
-- Just write the SQL above, and then add applications and engines to the management console to automatically initialize the configuration


-- Configuration secondary directory (creator level default configuration)
-- IDE
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@SPARK_IDE, 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@HIVE_IDE, 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@PYTHON_IDE, 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@PIPELINE_IDE, 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@JDBC_IDE, 'OPTIONAL', 2, now(), now());

-- Visualis
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@SPARK_VISUALIS, 'OPTIONAL', 2, now(), now());
-- nodeexecution
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@SPARK_NODE, 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@HIVE_NODE, 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@PYTHON_NODE, 'OPTIONAL', 2, now(), now());


-- Associate first-level and second-level directories
select @label_id := id from linkis_cg_manager_label where `label_value` = '*-全局设置,*-*';
insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 1);

select @label_id := id from linkis_cg_manager_label where `label_value` = '*-IDE,*-*';
insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 1);

select @label_id := id from linkis_cg_manager_label where `label_value` = '*-Visualis,*-*';
insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 1);

select @label_id := id from linkis_cg_manager_label where `label_value` = '*-nodeexecution,*-*';
insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 1);

select @label_id := id from linkis_cg_manager_label where `label_value` = @SPARK_IDE;
insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

select @label_id := id from linkis_cg_manager_label where `label_value` = @HIVE_IDE;
insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

select @label_id := id from linkis_cg_manager_label where `label_value` = @PYTHON_IDE;
insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

select @label_id := id from linkis_cg_manager_label where `label_value` = @PIPELINE_IDE;
insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

select @label_id := id from linkis_cg_manager_label where `label_value` = @JDBC_IDE;
insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

select @label_id := id from linkis_cg_manager_label where `label_value` =  @SPARK_VISUALIS;
insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

select @label_id := id from linkis_cg_manager_label where `label_value` = @SPARK_NODE;
insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

select @label_id := id from linkis_cg_manager_label where `label_value` = @HIVE_NODE;
insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

select @label_id := id from linkis_cg_manager_label where `label_value` = @PYTHON_NODE;
insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

-- Associate label and default configuration
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(select `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = '*-*,*-*');

-- spark2.4.3 default configuration
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(select `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = @SPARK_ALL);

-- hive1.2.1 default configuration
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(select `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = @HIVE_ALL);

-- python default configuration
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(select `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = @PYTHON_ALL);

-- pipeline default configuration
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(select `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = @PIPELINE_ALL);

-- jdbc default configuration
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(select `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = @JDBC_ALL);


insert  into `linkis_cg_rm_external_resource_provider`(`id`,`resource_type`,`name`,`labels`,`config`) values
(1,'Yarn','sit',NULL,'{\r\n\"rmWebAddress\": \"@YARN_RESTFUL_URL\",\r\n\"hadoopVersion\": \"2.7.2\",\r\n\"authorEnable\":false,\r\n\"user\":\"hadoop\",\r\n\"pwd\":\"123456\",\r\n\"kerberosEnable\":@KERBEROS_ENABLE,\r\n\"principalName\":\"@PRINCIPAL_NAME\",\r\n\"keytabPath\":\"@KEYTAB_PATH\",\r\n\"krb5Path\":\"@KRB5_PATH\"\r\n}');
-- errorcode 
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('10001','会话创建失败，%s队列不存在，请检查队列设置是否正确','queue (\\S+) is not exists in YARN',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('10001','会话创建失败，用户%s不能提交应用到队列：%s，请联系提供队列给您的人员','User (\\S+) cannot submit applications to queue (\\S+)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('20001','Session创建失败，当前申请资源%s，队列可用资源%s,请检查资源配置是否合理','您本次向任务队列（[a-zA-Z_0-9\\.]+）请求资源（(.+)），任务队列最大可用资源（.+），任务队列剩余可用资源（(.+)）您已占用任务队列资源（.+）',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('20002','Session创建失败，服务器资源不足，请稍后再试','远程服务器没有足够资源实例化[a-zA-Z]+ Session，通常是由于您设置【驱动内存】或【客户端内存】过高导致的，建议kill脚本，调低参数后重新提交！等待下次调度...',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('20003','Session创建失败，队列资源不足，请稍后再试','request resources from ResourceManager has reached 560++ tries, give up and mark it as FAILED.',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('20003','文件%s不存在','Caused by:\\s*java.io.FileNotFoundException',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('20083','Java进程内存溢出，建议优化脚本内容','OutOfMemoryError',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('30001','%s无权限访问，请申请开通数据表权限，请联系您的数据管理人员','Permission denied:\\s*user=[a-zA-Z0-9_]+,\\s*access=[A-Z]+\\s*,\\s*inode="([a-zA-Z0-9/_\\.]+)"',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('40001','数据库%s不存在，请检查引用的数据库是否有误','Database ''([a-zA-Z_0-9]+)'' not found',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('40001','数据库%s不存在，请检查引用的数据库是否有误','Database does not exist: ([a-zA-Z_0-9]+)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('40002','表%s不存在，请检查引用的表是否有误','Table or view not found: ([`\\.a-zA-Z_0-9]+)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('40002','表%s不存在，请检查引用的表是否有误','Table not found ''([a-zA-Z_0-9]+)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('40003','字段%s不存在，请检查引用的字段是否有误','cannot resolve ''`(.+)`'' given input columns',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('40003','字段%s不存在，请检查引用的字段是否有误',' Invalid table alias or column reference ''(.+)'':',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('40004','分区字段%s不存在，请检查引用的表%s是否为分区表或分区字段有误','([a-zA-Z_0-9]+) is not a valid partition column in table ([`\\.a-zA-Z_0-9]+)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('40004','分区字段%s不存在，请检查引用的表是否为分区表或分区字段有误','Partition spec \\{(\\S+)\\} contains non-partition columns',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('40004','分区字段%s不存在，请检查引用的表是否为分区表或分区字段有误','table is not partitioned but partition spec exists:\\{(.+)\\}',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('40004','表对应的路径不存在，请联系您的数据管理人员','Path does not exist: viewfs',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50001','括号不匹配，请检查代码中括号是否前后匹配','extraneous input ''\\)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50002','非聚合函数%s必须写在group by中，请检查代码的group by语法','expression ''(\\S+)'' is neither present in the group by',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50002','非聚合函数%s必须写在group by中，请检查代码的group by语法','grouping expressions sequence is empty,\\s?and ''(\\S+)'' is not an aggregate function',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50002','非聚合函数%s必须写在group by中，请检查代码的group by语法','Expression not in GROUP BY key ''(\\S+)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50003','未知函数%s，请检查代码中引用的函数是否有误','Undefined function: ''(\\S+)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50003','未知函数%s，请检查代码中引用的函数是否有误','Invalid function ''(\\S+)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50004','字段%s存在名字冲突，请检查子查询内是否有同名字段','Reference ''(\\S+)'' is ambiguous',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50004','字段%s存在名字冲突，请检查子查询内是否有同名字段','Ambiguous column Reference ''(\\S+)'' in subquery',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50005','字段%s必须指定表或者子查询别名，请检查该字段来源','Column ''(\\S+)'' Found in more than One Tables/Subqueries',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50006','表%s在数据库%s中已经存在，请删除相应表后重试','Table or view ''(\\S+)'' already exists in database ''(\\S+)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50006','表%s在数据库中已经存在，请删除相应表后重试','Table (\\S+) already exists',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50006','表%s在数据库中已经存在，请删除相应表后重试','Table already exists',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50006','表%s在数据库中已经存在，请删除相应表后重试','AnalysisException: (S+) already exists',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('64001','找不到导入文件地址：%s','java.io.FileNotFoundException: (\\S+) \\(No such file or directory\\)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('64002','导出为excel时临时文件目录权限异常','java.io.IOException: Permission denied(.+)at org.apache.poi.xssf.streaming.SXSSFWorkbook.createAndRegisterSXSSFSheet',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('64003','导出文件时无法创建目录：%s','java.io.IOException: Mkdirs failed to create (\\S+) (.+)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50008','导入模块错误，系统没有%s模块，请联系运维人员安装','ImportError: No module named (S+)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50009','插入目标表字段数量不匹配,请检查代码！','requires that the data to be inserted have the same number of columns as the target table',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50011','数据类型不匹配，请检查代码！','due to data type mismatch: differing types in',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50012','字段%s引用有误，请检查字段是否存在！','Invalid column reference (S+)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50013','字段%s提取数据失败','Can''t extract value from (S+): need struct type but got string',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50014','括号或者关键字不匹配，请检查代码！','mismatched input ''(\\S+)'' expecting',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50015','group by 位置2不在select列表中，请检查代码！','GROUP BY position (S+) is not in select list',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50016','代码中存在NoneType空类型变量，请检查代码','''NoneType'' object',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50017','数组越界','IndexError:List index out of range',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50013','字段提取数据失败请检查字段类型','Can''t extract value from (S+): need struct type but got string',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50018','插入数据未指定目标表字段%s，请检查代码！','Cannot insert into target table because column number/types are different ''(S+)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50019','表别名%s错误，请检查代码！','Invalid table alias ''(\\S+)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50020','UDF函数未指定参数，请检查代码！','UDFArgumentException Argument expected',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50021','聚合函数%s不能写在group by 中，请检查代码！','aggregate functions are not allowed in GROUP BY',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50007','您的代码有语法错误，请您修改代码之后执行','SyntaxError',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('40002','表不存在，请检查引用的表是否有误','Table not found',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('40003','函数使用错误，请检查您使用的函数方式','No matching method',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50032','用户主动kill任务','is killed by user',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('60001','python代码变量%s未定义','name ''(S+)'' is not defined',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('60002','python udf %s 未定义','Undefined function:s+''(S+)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50007','您的sql代码可能有语法错误，请检查sql代码','FAILED: ParseException',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('50007','您的sql代码可能有语法错误，请检查sql代码','org.apache.spark.sql.catalyst.parser.ParseException',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('60003','脚本语法有误','ParseException:',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('60010','您可能没有相关权限','Permission denied',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('61027','python执行不能将%s和%s两种类型进行连接','cannot concatenate ''(S+)'' and ''(S+)''',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('60020','pyspark执行失败，可能是语法错误或stage失败','Py4JJavaError: An error occurred',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('61028','python代码缩进对齐有误','unexpected indent',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('60078','个人库超过限制','is exceeded',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('69582','python代码缩进有误','unexpected indent',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('69583','python代码反斜杠后面必须换行','unexpected character after line',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('60091','导出Excel表超过最大限制1048575','Invalid row number',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('60092','python save as table未指定格式，默认用parquet保存，hive查询报错','parquet.io.ParquetDecodingException',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('11011','远程服务器内存资源不足','errCode: 11011',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('11012','远程服务器CPU资源不足','errCode: 11012',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('11013','远程服务器实例资源不足','errCode: 11013',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('11014','队列CPU资源不足','errCode: 11014',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('11015','队列内存资源不足','errCode: 11015',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('11016','队列实例数超过限制','errCode: 11016',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('11017','超出全局计算引擎实例限制','errCode: 11017',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('60035','资源不足，启动引擎失败','资源不足',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('60075','获取Yarn队列信息异常,可能是您设置的yarn队列不存在','获取Yarn队列信息异常',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('95001','找不到变量值，请确认您是否设置相关变量','not find variable substitution for',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('95002','不存在的代理用户，请检查你是否申请过平台层（bdp或者bdap）用户','failed to change current working directory ownership',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('95003','请检查提交用户在WTSS内是否有该代理用户的权限，代理用户中是否存在特殊字符，是否用错了代理用户，OS层面是否有该用户，系统设置里面是否设置了该用户为代理用户','没有权限执行当前任务',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('95004','平台层不存在您的执行用户，请在ITSM申请平台层（bdp或者bdap）用户','使用chown命令修改',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('95005','未配置代理用户，请在ITSM走WTSS用户变更单，为你的用户授权改代理用户','请联系系统管理员为您的用户添加该代理用户',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('60079','所查库表无权限','Authorization failed:No privilege',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('30002','启动引擎超时，您可以进行任务重试','wait for DefaultEngineConn',0);
