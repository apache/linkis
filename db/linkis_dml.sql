

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
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('wds.linkis.rm.yarnqueue', 'yarn队列名', 'yarn队列名', 'default', 'None', NULL, '0', '0', '1', '队列资源');
-- spark
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.yarnqueue.instance.max', '取值范围：1-128，单位：个', 'yarn队列实例最大个数', '30', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|128)$', '0', '0', '1', '队列资源', 'spark');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.yarnqueue.cores.max', '取值范围：1-500，单位：个', '队列CPU使用上限', '150', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|500)$', '0', '0', '1', '队列资源', 'spark');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.yarnqueue.memory.max', '取值范围：1-1000，单位：G', '队列内存使用上限', '300G', 'Regex', '^([1-9]\\d{0,2}|1000)(G|g)$', '0', '0', '1', '队列资源', 'spark');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.client.memory.max', '取值范围：1-100，单位：G', '驱动器内存使用上限', '20G', 'Regex', '^([1-9]\\d{0,1}|100)(G|g)$', '0', '0', '1', '队列资源', 'spark');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.client.core.max', '取值范围：1-128，单位：个', '驱动器核心个数上限', '10', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|128)$', '0', '0', '1', '队列资源', 'spark');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.instance', '范围：1-20，单位：个', '引擎最大并发数', '10', 'NumInterval', '[1,20]', '0', '0', '1', '队列资源', 'spark');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.executor.instances', '取值范围：1-40，单位：个', '执行器实例最大并发数', '2', 'NumInterval', '[1,40]', '0', '0', '2', 'spark资源设置', 'spark');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.executor.cores', '取值范围：1-8，单位：个', '执行器核心个数',  '2', 'NumInterval', '[1,2]', '1', '0', '1','spark资源设置', 'spark');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.executor.memory', '取值范围：3-15，单位：G', '执行器内存大小', '3', 'NumInterval', '[3,15]', '0', '0', '3', 'spark资源设置', 'spark');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.driver.cores', '取值范围：只能取1，单位：个', '驱动器核心个数', '1', 'NumInterval', '[1,1]', '1', '1', '1', 'spark资源设置','spark');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.driver.memory', '取值范围：1-15，单位：G', '驱动器内存大小','2', 'NumInterval', '[1,15]', '0', '0', '1', 'spark资源设置', 'spark');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.pd.addresses', NULL, NULL, 'pd0:2379', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.tidb.addr', NULL, NULL, 'tidb', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.tidb.password', NULL, NULL, NULL, 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.tidb.port', NULL, NULL, '4000', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.tispark.tidb.user', NULL, NULL, 'root', 'None', NULL, '0', '0', '1', 'tidb设置', 'spark');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('spark.python.version', '取值范围：python2,python3', 'python版本','python2', 'OFT', '[\"python3\",\"python2\"]', '0', '0', '1', 'spark引擎设置', 'spark');
-- hive
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.client.memory.max', '取值范围：1-100，单位：G', '驱动器内存使用上限', '20G', 'Regex', '^([1-9]\\d{0,1}|100)(G|g)$', '0', '0', '1', '队列资源', 'hive');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.client.core.max', '取值范围：1-128，单位：个', '驱动器核心个数上限', '10', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|128)$', '0', '0', '1', '队列资源', 'hive');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.instance', '范围：1-20，单位：个', '引擎最大并发数', '10', 'NumInterval', '[1,20]', '0', '0', '1', '队列资源', 'hive');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('hive.client.memory', '取值范围：1-10，单位：G', 'hive引擎初始化内存大小','2', 'NumInterval', '[1,10]', '0', '0', '1', 'hive引擎设置', 'hive');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('hive.client.java.opts', 'hive客户端进程参数', 'hive引擎启动时jvm参数','', 'None', NULL, '1', '1', '1', 'hive引擎设置', 'hive');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('mapred.reduce.tasks', '范围：1-20，单位：个', 'reduce数', '10', 'NumInterval', '[1,20]', '0', '1', '1', 'hive资源设置', 'hive');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('dfs.block.size', '取值范围：2-10，单位：G', 'map数据块大小', '10', 'NumInterval', '[2,10]', '0', '1', '1', 'hive资源设置', 'hive');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('hive.exec.reduce.bytes.per.reducer', '取值范围：2-10，单位：G', 'reduce处理的数据量', '10', 'NumInterval', '[2,10]', '0', '1', '1', 'hive资源设置', 'hive');
-- python
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.client.memory.max', '取值范围：1-100，单位：G', '驱动器内存使用上限', '20G', 'Regex', '^([1-9]\\d{0,1}|100)(G|g)$', '0', '0', '1', '队列资源', 'python');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.client.core.max', '取值范围：1-128，单位：个', '驱动器核心个数上限', '10', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|128)$', '0', '0', '1', '队列资源', 'python');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.rm.instance', '范围：1-20，单位：个', '引擎最大并发数', '10', 'NumInterval', '[1,20]', '0', '0', '1', '队列资源', 'python');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('python.java.client.memory', '取值范围：1-2，单位：G', 'python引擎初始化内存大小', '1', 'NumInterval', '[1,2]', '0', '0', '1', 'python引擎设置', 'python');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('python.version', '取值范围：python2,python3', 'python版本','python2', 'OFT', '[\"python3\",\"python2\"]', '0', '0', '1', 'python引擎设置', 'python');
-- pipeline
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('pipeline.output.mold', '取值范围：csv或excel', '结果集导出类型','csv', 'OFT', '[\"csv\",\"excel\"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('pipeline.field.split', '取值范围：，或\\t', 'csv分隔符',',', 'OFT', '[\",\",\"\\\\t\"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('pipeline.output.charset', '取值范围：utf-8或gbk', '结果集导出字符集','gbk', 'OFT', '[\"utf-8\",\"gbk\"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('pipeline.output.isoverwtite', '取值范围：true或false', '是否覆写','true', 'OFT', '[\"true\",\"false\"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.instance', '范围：1-3，单位：个', 'pipeline引擎最大并发数','3', 'NumInterval', '[1,3]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('pipeline.engine.memory', '取值范围：1-10，单位：G', 'pipeline引擎初始化内存大小','2', 'NumInterval', '[1,10]', '0', '0', '1', 'pipeline资源设置', 'pipeline');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('pipeline.output.shuffle.null.type', '取值范围：NULL或者BLANK', '空值替换','NULL', 'OFT', '[\"NULL\",\"BLANK\"]', '0', '0', '1', 'pipeline引擎设置', 'pipeline');
-- jdbc
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.jdbc.connect.url', '例如:jdbc:hive2://127.0.0.1:10000', 'jdbc连接地址', 'jdbc:hive2://127.0.0.1:10000', 'Regex', '^\s*jdbc:\w+://([^:]+)(:\d+)(/[^\?]+)?(\?\S*)?$', '0', '0', '1', '数据源配置', 'jdbc');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.jdbc.version', '取值范围：jdbc3,jdbc4', 'jdbc版本','jdbc4', 'OFT', '[\"jdbc3\",\"jdbc4\"]', '0', '0', '1', '数据源配置', 'jdbc');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.jdbc.username', 'username', '数据库连接用户名', '', '', '', '0', '0', '1', '用户配置', 'jdbc');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.jdbc.password', 'password', '数据库连接密码', '', '', '', '0', '0', '1', '用户配置', 'jdbc');
INSERT INTO `linkis_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.jdbc.connect.max', '范围：1-20，单位：个', 'jdbc引擎最大连接数', '10', 'NumInterval', '[1,20]', '0', '0', '1', '数据源配置', 'jdbc');
-- Configuration一级目录
INSERT INTO `linkis_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_engineType_userCreator','*-全局设置,*-*', 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_engineType_userCreator','*-IDE,*-*', 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_engineType_userCreator','*-Visualis,*-*', 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_engineType_userCreator','*-nodeexecution,*-*', 'OPTIONAL', 2, now(), now());


-- 引擎级别默认配置
INSERT INTO `linkis_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_engineType_userCreator',@SPARK_ALL, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_engineType_userCreator',@HIVE_ALL, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_engineType_userCreator',@PYTHON_ALL, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_engineType_userCreator',@PIPELINE_ALL, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_engineType_userCreator',@JDBC_ALL, 'OPTIONAL', 2, now(), now());


-- Configuration二级目录(creator 级别的默认配置)
-- IDE
INSERT INTO `linkis_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_engineType_userCreator',@SPARK_IDE, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_engineType_userCreator',@HIVE_IDE, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_engineType_userCreator',@PYTHON_IDE, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_engineType_userCreator',@PIPELINE_IDE, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_engineType_userCreator',@JDBC_IDE, 'OPTIONAL', 2, now(), now());

-- Visualis
INSERT INTO `linkis_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_engineType_userCreator',@SPARK_VISUALIS, 'OPTIONAL', 2, now(), now());
-- nodeexecution
INSERT INTO `linkis_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_engineType_userCreator',@SPARK_NODE, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_engineType_userCreator',@HIVE_NODE, 'OPTIONAL', 2, now(), now());
INSERT INTO `linkis_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_engineType_userCreator',@PYTHON_NODE, 'OPTIONAL', 2, now(), now());


-- 关联一级二级目录
SELECT @label_id := id from linkis_manager_label WHERE `label_value` = '*-全局设置,*-*';
INSERT INTO linkis_configuration_category (`label_id`, `level`) VALUES (@label_id, 1);

SELECT @label_id := id from linkis_manager_label WHERE `label_value` = '*-IDE,*-*';
INSERT INTO linkis_configuration_category (`label_id`, `level`) VALUES (@label_id, 1);

SELECT @label_id := id from linkis_manager_label WHERE `label_value` = '*-Visualis,*-*';
INSERT INTO linkis_configuration_category (`label_id`, `level`) VALUES (@label_id, 1);

SELECT @label_id := id from linkis_manager_label WHERE `label_value` = '*-nodeexecution,*-*';
INSERT INTO linkis_configuration_category (`label_id`, `level`) VALUES (@label_id, 1);

SELECT @label_id := id from linkis_manager_label WHERE `label_value` = @SPARK_IDE;
INSERT INTO linkis_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

SELECT @label_id := id from linkis_manager_label WHERE `label_value` = @HIVE_IDE;
INSERT INTO linkis_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

SELECT @label_id := id from linkis_manager_label WHERE `label_value` = @PYTHON_IDE;
INSERT INTO linkis_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

SELECT @label_id := id from linkis_manager_label WHERE `label_value` = @PIPELINE_IDE;
INSERT INTO linkis_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

SELECT @label_id := id from linkis_manager_label WHERE `label_value` = @JDBC_IDE;
INSERT INTO linkis_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

SELECT @label_id := id from linkis_manager_label WHERE `label_value` =  @SPARK_VISUALIS;
INSERT INTO linkis_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

SELECT @label_id := id from linkis_manager_label WHERE `label_value` = @SPARK_NODE;
INSERT INTO linkis_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

SELECT @label_id := id from linkis_manager_label WHERE `label_value` = @HIVE_NODE;
INSERT INTO linkis_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);

SELECT @label_id := id from linkis_manager_label WHERE `label_value` = @PYTHON_NODE;
INSERT INTO linkis_configuration_category (`label_id`, `level`) VALUES (@label_id, 2);


---- 关联label和默认配置
-- 全局默认配置(此处的'*-*,*-*'与一级目录'*-全局设置,*-*'相同，真正查询全局设置的label时应当查询*-*,*-*，而不是*-全局设置,*-*)
INSERT INTO `linkis_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_engineType_userCreator','*-*,*-*', 'OPTIONAL', 2, now(), now());

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'wds.linkis.rm.yarnqueue' AND `engine_conn_type` IS NULL;
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = '*-*,*-*';
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

-- spark默认配置
SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'wds.linkis.rm.yarnqueue.instance.max' AND `engine_conn_type` = 'spark';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @SPARK_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'wds.linkis.rm.yarnqueue.cores.max' AND `engine_conn_type` = 'spark';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @SPARK_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'wds.linkis.rm.yarnqueue.memory.max' AND `engine_conn_type` = 'spark';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @SPARK_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'wds.linkis.rm.client.memory.max' AND `engine_conn_type` = 'spark';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @SPARK_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'wds.linkis.rm.client.core.max' AND `engine_conn_type` = 'spark';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @SPARK_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'wds.linkis.rm.instance' AND `engine_conn_type` = 'spark';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @SPARK_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'spark.executor.instances' AND `engine_conn_type` = 'spark';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @SPARK_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'spark.executor.cores' AND `engine_conn_type` = 'spark';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @SPARK_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'spark.executor.memory' AND `engine_conn_type` = 'spark';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @SPARK_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'spark.driver.cores' AND `engine_conn_type` = 'spark';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @SPARK_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'spark.driver.memory' AND `engine_conn_type` = 'spark';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @SPARK_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'spark.python.version' AND `engine_conn_type` = 'spark';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @SPARK_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

-- hive默认配置
SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'wds.linkis.rm.client.memory.max' AND `engine_conn_type` = 'hive';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @HIVE_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

SELECT @configkey_id := id from linkis_configuration_config_key WHERE `key` = 'wds.linkis.rm.client.core.max' AND `engine_conn_type` = 'hive';
SELECT @config_label_id := id from linkis_manager_label WHERE `label_value` = @HIVE_ALL;
INSERT INTO `linkis_configuration_config_value` (`configkey_id`, `config_value`, `config_label_id`) VALUES (@configkey_id, '', @config_label_id);

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
