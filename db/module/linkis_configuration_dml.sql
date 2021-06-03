

-- 变量：
SET @SPARK_LABEL="spark-2.4.3";
SET @HIVE_LABEL="hive-1.2.1";
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

-- Configuration的默认Key
-- 全局设置
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('wds.linkis.rm.yarnqueue', 'yarn队列名', 'yarn队列名', 'default', 'None', NULL, '0', '0', '1', '队列资源');
-- spark
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.yarnqueue.instance.max', '取值范围：1-128，单位：个', 'yarn队列实例最大个数', '30', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|128)$', '0', '0', '1', '队列资源', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.yarnqueue.cores.max', '取值范围：1-500，单位：个', '队列CPU使用上限', '150', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|500)$', '0', '0', '1', '队列资源', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.yarnqueue.memory.max', '取值范围：1-1000，单位：G', '队列内存使用上限', '300G', 'Regex', '^([1-9]\\d{0,2}|1000)(G|g)$', '0', '0', '1', '队列资源', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.client.memory.max', '取值范围：1-100，单位：G', '驱动器内存使用上限', '20G', 'Regex', '^([1-9]\\d{0,1}|100)(G|g)$', '0', '0', '1', '队列资源', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.client.core.max', '取值范围：1-128，单位：个', '驱动器核心个数上限', '10', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|128)$', '0', '0', '1', '队列资源', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.instance', '范围：1-20，单位：个', '引擎最大并发数', '10', 'NumInterval', '[1,20]', '0', '0', '1', '队列资源', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.executor.instances', '取值范围：1-40，单位：个', '执行器实例最大并发数', '2', 'NumInterval', '[1,40]', '0', '0', '2', 'spark资源设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.executor.cores', '取值范围：1-8，单位：个', '执行器核心个数',  '2', 'NumInterval', '[1,2]', '1', '0', '1','spark资源设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.executor.memory', '取值范围：3-15，单位：G', '执行器内存大小', '3', 'NumInterval', '[3,15]', '0', '0', '3', 'spark资源设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.driver.cores', '取值范围：只能取1，单位：个', '驱动器核心个数', '1', 'NumInterval', '[1,1]', '1', '1', '1', 'spark资源设置','spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.driver.memory', '取值范围：1-15，单位：G', '驱动器内存大小','2', 'NumInterval', '[1,15]', '0', '0', '1', 'spark资源设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.pd.addresses', NULL, NULL, 'pd0:2379', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.tidb.addr', NULL, NULL, 'tidb', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.tidb.password', NULL, NULL, NULL, 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.tidb.port', NULL, NULL, '4000', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.tidb.user', NULL, NULL, 'root', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.python.version', '取值范围：python2,python3', 'python版本','python2', 'OFT', '[\"python3\",\"python2\"]', '0', '0', '1', 'spark引擎设置', 'spark');
-- hive
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.client.memory.max', '取值范围：1-100，单位：G', '驱动器内存使用上限', '20G', 'Regex', '^([1-9]\\d{0,1}|100)(G|g)$', '0', '0', '1', '队列资源', 'hive');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.client.core.max', '取值范围：1-128，单位：个', '驱动器核心个数上限', '10', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|128)$', '0', '0', '1', '队列资源', 'hive');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.instance', '范围：1-20，单位：个', '引擎最大并发数', '10', 'NumInterval', '[1,20]', '0', '0', '1', '队列资源', 'hive');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('hive.client.memory', '取值范围：1-10，单位：G', 'hive引擎初始化内存大小','2', 'NumInterval', '[1,10]', '0', '0', '1', 'hive引擎设置', 'hive');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('hive.client.java.opts', 'hive客户端进程参数', 'hive引擎启动时jvm参数','', 'None', NULL, '1', '1', '1', 'hive引擎设置', 'hive');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('mapred.reduce.tasks', '范围：1-20，单位：个', 'reduce数', '10', 'NumInterval', '[1,20]', '0', '1', '1', 'hive资源设置', 'hive');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('dfs.block.size', '取值范围：2-10，单位：G', 'map数据块大小', '10', 'NumInterval', '[2,10]', '0', '1', '1', 'hive资源设置', 'hive');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('hive.exec.reduce.bytes.per.reducer', '取值范围：2-10，单位：G', 'reduce处理的数据量', '10', 'NumInterval', '[2,10]', '0', '1', '1', 'hive资源设置', 'hive');
-- python
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.client.memory.max', '取值范围：1-100，单位：G', '驱动器内存使用上限', '20G', 'Regex', '^([1-9]\\d{0,1}|100)(G|g)$', '0', '0', '1', '队列资源', 'python');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.client.core.max', '取值范围：1-128，单位：个', '驱动器核心个数上限', '10', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|128)$', '0', '0', '1', '队列资源', 'python');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.instance', '范围：1-20，单位：个', '引擎最大并发数', '10', 'NumInterval', '[1,20]', '0', '0', '1', '队列资源', 'python');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('python.java.client.memory', '取值范围：1-2，单位：G', 'python引擎初始化内存大小', '1', 'NumInterval', '[1,2]', '0', '0', '1', 'python引擎设置', 'python');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('python.version', '取值范围：python2,python3', 'python版本','python2', 'OFT', '[\"python3\",\"python2\"]', '0', '0', '1', 'python引擎设置', 'python');
-- pipeline
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('pipeline.output.mold', '取值范围：csv或excel', '结果集导出类型','csv', 'OFT', '[\"csv\",\"excel\"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('pipeline.field.split', '取值范围：，或\\t', 'csv分隔符',',', 'OFT', '[\",\",\"\\\\t\"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('pipeline.output.charset', '取值范围：utf-8或gbk', '结果集导出字符集','gbk', 'OFT', '[\"utf-8\",\"gbk\"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('pipeline.output.isoverwtite', '取值范围：true或false', '是否覆写','true', 'OFT', '[\"true\",\"false\"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.instance', '范围：1-3，单位：个', 'pipeline引擎最大并发数','3', 'NumInterval', '[1,3]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('pipeline.engine.memory', '取值范围：1-10，单位：G', 'pipeline引擎初始化内存大小','2', 'NumInterval', '[1,10]', '0', '0', '1', 'pipeline资源设置', 'pipeline');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('pipeline.output.shuffle.null.type', '取值范围：NULL或者BLANK', '空值替换','NULL', 'OFT', '[\"NULL\",\"BLANK\"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
-- jdbc
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.jdbc.connect.url', '例如:jdbc:hive2://127.0.0.1:10000', 'jdbc连接地址', 'jdbc:hive2://127.0.0.1:10000', 'Regex', '^\s*jdbc:\w+://([^:]+)(:\d+)(/[^\?]+)?(\?\S*)?$', '0', '0', '1', '数据源配置', 'jdbc');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.jdbc.version', '取值范围：jdbc3,jdbc4', 'jdbc版本','jdbc4', 'OFT', '[\"jdbc3\",\"jdbc4\"]', '0', '0', '1', '数据源配置', 'jdbc');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.jdbc.username', 'username', '数据库连接用户名', '', '', '', '0', '0', '1', '用户配置', 'jdbc');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.jdbc.password', 'password', '数据库连接密码', '', '', '', '0', '0', '1', '用户配置', 'jdbc');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.jdbc.connect.max', '范围：1-20，单位：个', 'jdbc引擎最大连接数', '10', 'NumInterval', '[1,20]', '0', '0', '1', '数据源配置', 'jdbc');
-- Configuration一级目录
INSERT INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType','*-全局设置,*-*', 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType','*-IDE,*-*', 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType','*-Visualis,*-*', 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType','*-nodeexecution,*-*', 'OPTIONAL', 2, now(), now());


-- 引擎级别默认配置
INSERT INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType','*-*,*-*', 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@SPARK_ALL, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@HIVE_ALL, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@PYTHON_ALL, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@PIPELINE_ALL, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@JDBC_ALL, 'OPTIONAL', 2, now(), now());

-- 自定义关联引擎(例如spark-2.4.3)和configKey值
-- 全局设置
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(SELECT config.id AS `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type is null and label.label_value = "*-*,*-*");

-- spark-2.4.3(此处选择将所有spark类型的Key值与spark2.4.3关联)
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(SELECT config.id AS `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'spark' and label.label_value = @SPARK_ALL);

-- hive-1.2.1
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(SELECT config.id AS `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'hive' and label_value = @HIVE_ALL);

-- python-python2
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(SELECT config.id AS `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'python' and label_value = @PYTHON_ALL);

-- pipeline-*
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(SELECT config.id AS `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'pipeline' and label_value = @PIPELINE_ALL);

-- jdbc-4
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(SELECT config.id AS `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'jdbc' and label_value = @JDBC_ALL);

------------------------------------------------------------------------
-- 如果需要自定义新增引擎的参数，以下配置无需编写SQL初始化
-- 只需要编写好上方的SQL，然后在管理台添加应用和引擎即可自动初始化配置
------------------------------------------------------------------------

-- Configuration二级目录(creator 级别的默认配置)
-- IDE
INSERT INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@SPARK_IDE, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@HIVE_IDE, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@PYTHON_IDE, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@PIPELINE_IDE, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@JDBC_IDE, 'OPTIONAL', 2, now(), now());

-- Visualis
INSERT INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@SPARK_VISUALIS, 'OPTIONAL', 2, now(), now());
-- nodeexecution
INSERT INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@SPARK_NODE, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@HIVE_NODE, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@PYTHON_NODE, 'OPTIONAL', 2, now(), now());


-- 关联一级二级目录
SELECT @label_id := id from linkis_cg_manager_label WHERE `label_value` = '*-全局设置,*-*';
INSERT INTO linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 1);

SELECT @label_id := id from linkis_cg_manager_label WHERE `label_value` = '*-IDE,*-*';
INSERT INTO linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 1);

SELECT @label_id := id from linkis_cg_manager_label WHERE `label_value` = '*-Visualis,*-*';
INSERT INTO linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 1);

SELECT @label_id := id from linkis_cg_manager_label WHERE `label_value` = '*-nodeexecution,*-*';
INSERT INTO linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 1);

SELECT @label_id := id from linkis_cg_manager_label WHERE `label_value` = @SPARK_IDE;
INSERT INTO linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

SELECT @label_id := id from linkis_cg_manager_label WHERE `label_value` = @HIVE_IDE;
INSERT INTO linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

SELECT @label_id := id from linkis_cg_manager_label WHERE `label_value` = @PYTHON_IDE;
INSERT INTO linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

SELECT @label_id := id from linkis_cg_manager_label WHERE `label_value` = @PIPELINE_IDE;
INSERT INTO linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

SELECT @label_id := id from linkis_cg_manager_label WHERE `label_value` = @JDBC_IDE;
INSERT INTO linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

SELECT @label_id := id from linkis_cg_manager_label WHERE `label_value` =  @SPARK_VISUALIS;
INSERT INTO linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

SELECT @label_id := id from linkis_cg_manager_label WHERE `label_value` = @SPARK_NODE;
INSERT INTO linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

SELECT @label_id := id from linkis_cg_manager_label WHERE `label_value` = @HIVE_NODE;
INSERT INTO linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

SELECT @label_id := id from linkis_cg_manager_label WHERE `label_value` = @PYTHON_NODE;
INSERT INTO linkis_ps_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

---- 关联label和默认配置
-- 全局默认配置(此处的'*-*,*-*'与一级目录'*-全局设置,*-*'相同，真正查询全局设置的label时应当查询*-*,*-*，而不是*-全局设置,*-*)
INSERT INTO `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(SELECT `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = '*-*,*-*');

-- spark2.4.3默认配置
INSERT INTO `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(SELECT `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = @SPARK_ALL);

-- hive1.2.1默认配置
INSERT INTO `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(SELECT `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = @HIVE_ALL);

-- python默认配置
INSERT INTO `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(SELECT `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = @PYTHON_ALL);

-- pipeline默认配置
INSERT INTO `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(SELECT `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = @PIPELINE_ALL);
