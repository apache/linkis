

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
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.executor.instances', '取值范围：1-40，单位：个', 'spark执行器实例最大并发数', '2', 'NumInterval', '[1,40]', '0', '0', '2', 'spark资源设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.executor.cores', '取值范围：1-8，单位：个', 'spark执行器核心个数',  '2', 'NumInterval', '[1,2]', '0', '0', '1','spark资源设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.executor.memory', '取值范围：3-15，单位：G', 'spark执行器内存大小', '3g', 'Regex', '^([3-9]|1[0-5])(G|g)$', '0', '0', '3', 'spark资源设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.driver.cores', '取值范围：只能取1，单位：个', 'spark驱动器核心个数', '1', 'NumInterval', '[1,1]', '0', '1', '1', 'spark资源设置','spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.driver.memory', '取值范围：1-15，单位：G', 'spark驱动器内存大小','2g', 'Regex', '^([3-9]|1[0-5])(G|g)$', '0', '0', '1', 'spark资源设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.pd.addresses', NULL, NULL, 'pd0:2379', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.tidb.addr', NULL, NULL, 'tidb', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.tidb.password', NULL, NULL, NULL, 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.tidb.port', NULL, NULL, '4000', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.tidb.user', NULL, NULL, 'root', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.python.version', '取值范围：python2,python3', 'python版本','python2', 'OFT', '[\"python3\",\"python2\"]', '0', '0', '1', 'spark引擎设置', 'spark');
-- hive
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.instance', '范围：1-20，单位：个', 'hive引擎最大并发数', '10', 'NumInterval', '[1,20]', '0', '0', '1', '队列资源', 'hive');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.engineconn.java.driver.memory', '取值范围：1-10，单位：G', 'hive引擎初始化内存大小','2g', 'Regex', '^([1-9]|10)(G|g)$', '0', '0', '1', 'hive引擎设置', 'hive');
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
(1,'Yarn','sit',NULL,'{\r\n\"rmWebAddress\": \"@YARN_RESTFUL_URL\",\r\n\"hadoopVersion\": \"2.7.2\",\r\n\"authorEnable\":false,\r\n\"user\":\"hadoop\",\r\n\"pwd\":\"123456\"\r\n}');

INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('10001','会话创建失败，%s队列不存在，请检查队列设置是否正确','queue (\\S+) is not exists in YARN',0);
SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'wds.linkis.rm.instance' AND `engine_conn_type` = 'hive';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @HIVE_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'hive.client.memory' AND `engine_conn_type` = 'hive';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @HIVE_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'hive.client.java.opts' AND `engine_conn_type` = 'hive';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @HIVE_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'mapred.reduce.tasks' AND `engine_conn_type` = 'hive';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @HIVE_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'dfs.block.size' AND `engine_conn_type` = 'hive';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @HIVE_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'hive.exec.reduce.bytes.per.reducer' AND `engine_conn_type` = 'hive';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @HIVE_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

-- python默认配置
SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'wds.linkis.rm.client.memory.max' AND `engine_conn_type` = 'python';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @PYTHON_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'wds.linkis.rm.client.core.max' AND `engine_conn_type` = 'python';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` =  @PYTHON_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'wds.linkis.rm.instance' AND `engine_conn_type` = 'python';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` =  @PYTHON_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'python.java.client.memory' AND `engine_conn_type` = 'python';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` =  @PYTHON_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'python.version' AND `engine_conn_type` = 'python';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` =  @PYTHON_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

-- pipeline默认配置
SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'pipeline.output.mold' AND `engine_conn_type` = 'pipeline';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` =  @PIPELINE_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'pipeline.field.split' AND `engine_conn_type` = 'pipeline';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` =  @PIPELINE_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'pipeline.output.charset' AND `engine_conn_type` = 'pipeline';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @PIPELINE_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'pipeline.output.isoverwtite' AND `engine_conn_type` = 'pipeline';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @PIPELINE_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'pipeline.engine.memory' AND `engine_conn_type` = 'pipeline';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @PIPELINE_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'pipeline.output.shuffle.null.type' AND `engine_conn_type` = 'pipeline';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @PIPELINE_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'wds.linkis.instance' AND `engine_conn_type` = 'pipeline';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @PIPELINE_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

-- jdbc默认配置
SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'wds.linkis.jdbc.connect.url' AND `engine_conn_type` = 'jdbc';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @JDBC_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'wds.linkis.jdbc.version' AND `engine_conn_type` = 'jdbc';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @JDBC_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'wds.linkis.jdbc.username' AND `engine_conn_type` = 'jdbc';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @JDBC_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'wds.linkis.jdbc.password' AND `engine_conn_type` = 'jdbc';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @JDBC_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'wds.linkis.jdbc.connect.max' AND `engine_conn_type` = 'jdbc';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @JDBC_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);



insert  into `linkis_external_resource_provider`(`id`,`resource_type`,`name`,`labels`,`config`) values
(1,'Yarn','sit',NULL,'{\r\n\"rmWebAddress\": \"@YARN_RESTFUL_URL\",\r\n\"hadoopVersion\": \"2.7.2\",\r\n\"authorEnable\":true,\r\n\"user\":\"hadoop\",\r\n\"pwd\":\"897ede66a860\"\r\n}');

-- ----------------------------
-- Records of linkis_datasource_env, this data is for test
-- ----------------------------
INSERT INTO `linkis_datasource_env` VALUES (1, '测试环境', '测试', 2, '{\"ip\":\"192.168.1.1\", \"port\":\"3308\"}', '2021-04-07 22:40:54', 'Adam', '2021-04-07 22:41:02', 'Adam');
INSERT INTO `linkis_datasource_env` VALUES (2, '生产环境mysql', '测试', 2, '{\"ip\":\"192.168.1.1\"}', '2021-04-07 22:40:54', 'Adam', '2021-04-07 22:40:54', 'Adam');
INSERT INTO `linkis_datasource_env` VALUES (3, '测试环境hive', '测试', 2, '{\"wds.linkis.server.mdm.service.hive.uris\":\"192.168.1.1\",\"wds.linkis.server.mdm.service.hive.principle\":\"td\'d\'d\",\"wds.linkis.server.mdm.service.hive.keytab\":\"keytab\"}', '2021-04-07 22:40:54', 'Adam', '2021-04-07 22:40:54', 'Adam');
INSERT INTO `linkis_datasource_env` VALUES (4, '测试环境es', '测试', 3, '{\"wds.linkis.server.mdm.service.es.urls\":\"\",\"wds.linkis.server.mdm.service.es.username\":\"\",\"wds.linkis.server.mdm.service.es.password\":\"\"}', NULL, NULL, NULL, 'Adam');


-- ----------------------------
-- Records of linkis_datasource_type_key
-- ----------------------------
INSERT INTO `linkis_datasource_type_key` VALUES (1, 1, 'host', 'Host', NULL, 'TEXT', NULL, 1, 'mysql Host ', NULL, NULL, NULL, NULL, '2021-04-08 03:13:36', '2021-04-08 03:13:36');
INSERT INTO `linkis_datasource_type_key` VALUES (2, 1, 'port', '端口', NULL, 'TEXT', NULL, 1, '端口', NULL, NULL, NULL, NULL, '2021-04-17 03:10:28', '2021-04-17 03:10:28');
INSERT INTO `linkis_datasource_type_key` VALUES (4, 1, 'username', '用户名', NULL, 'TEXT', NULL, 1, '用户名', '^[A-Za-z]+$', NULL, NULL, NULL, '2021-04-12 01:54:39', '2021-04-12 01:54:39');
INSERT INTO `linkis_datasource_type_key` VALUES (5, 1, 'password', '密码', NULL, 'PASSWORD', NULL, 1, '密码', '^[a-zA-Z0-9]{6,16}$', NULL, NULL, NULL, '2021-04-12 01:54:39', '2021-04-12 01:54:39');
INSERT INTO `linkis_datasource_type_key` VALUES (6, 2, 'file', 'keytab', NULL, 'FILE', NULL, 0, 'keytab文件', NULL, NULL, NULL, NULL, '2021-04-12 02:38:11', '2021-04-12 02:38:11');
INSERT INTO `linkis_datasource_type_key` VALUES (7, 2, 'envId', '集群环境', '', 'SELECT', NULL, 1, '集群配置', NULL, NULL, NULL, 'http://localhost:8080/api/rest_j/v1/data_source/env_list/all/type/2', '2021-04-13 01:53:57', '2021-04-13 01:53:57');


-- ----------------------------
-- Records of linkis_datasource_type
-- ----------------------------
INSERT INTO `linkis_datasource_type` VALUES (1, 'mysql', 'mysql数据库', 'mysql数据库', '关系型数据库', 'https://img.alicdn.com/imgextra/i4/O1CN01uLYwgg1zS93Aq9W8C_!!6000000006712-2-tps-280-176.png');
INSERT INTO `linkis_datasource_type` VALUES (2, 'hive', 'hive数据库', 'hive', '大数据存储', 'https://img.alicdn.com/imgextra/i4/O1CN01uLYwgg1zS93Aq9W8C_!!6000000006712-2-tps-280-176.png');
INSERT INTO `linkis_datasource_type` VALUES (3, 'presto', 'presto SQL', 'presto', '大数据存储', 'https://img.alicdn.com/imgextra/i4/O1CN01uLYwgg1zS93Aq9W8C_!!6000000006712-2-tps-280-176.png');
