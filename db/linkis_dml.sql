INSERT INTO `linkis_application` (`id`, `name`, `chinese_name`, `description`) VALUES (0, '通用设置', NULL, NULL);
INSERT INTO `linkis_application` (`id`, `name`, `chinese_name`, `description`) VALUES (0, 'spark', NULL, NULL);
INSERT INTO `linkis_application` (`id`, `name`, `chinese_name`, `description`) VALUES (0, 'IDE', NULL, NULL);
INSERT INTO `linkis_application` (`id`, `name`, `chinese_name`, `description`) VALUES (0, 'hive', NULL, NULL);
INSERT INTO `linkis_application` (`id`, `name`, `chinese_name`, `description`) VALUES (0, 'storage', NULL, NULL);
INSERT INTO `linkis_application` (`id`, `name`, `chinese_name`, `description`) VALUES (0, 'python', NULL, NULL);
INSERT INTO `linkis_application` (`id`, `name`, `chinese_name`, `description`) VALUES (0, 'tidb', NULL, NULL);

SELECT @application_id := id from linkis_application where name = '通用设置';
INSERT INTO `linkis_config_tree` (`id`, `parent_id`, `name`, `description`, `application_id`) VALUES (0, '0', '队列资源', NULL, @application_id);
INSERT INTO `linkis_config_tree` (`id`, `parent_id`, `name`, `description`, `application_id`) VALUES (0, '0', '预热机制', NULL, @application_id);
INSERT INTO `linkis_config_tree` (`id`, `parent_id`, `name`, `description`, `application_id`) VALUES (0, '0', '清理机制', NULL, @application_id);
INSERT INTO `linkis_config_tree` (`id`, `parent_id`, `name`, `description`, `application_id`) VALUES (0, '0', '引擎设置', NULL, @application_id);
INSERT INTO `linkis_config_tree` (`id`, `parent_id`, `name`, `description`, `application_id`) VALUES (0, '0', '驱动器资源', NULL, @application_id);

SELECT @application_id := id from linkis_application where name = 'spark';
INSERT INTO `linkis_config_tree` (`id`, `parent_id`, `name`, `description`, `application_id`) VALUES (0, '0', 'spark资源设置', NULL, @application_id);
INSERT INTO `linkis_config_tree` (`id`, `parent_id`, `name`, `description`, `application_id`) VALUES (0, '0', 'spark引擎设置', NULL, @application_id);

SELECT @application_id := id from linkis_application where name = 'hive';
INSERT INTO `linkis_config_tree` (`id`, `parent_id`, `name`, `description`, `application_id`) VALUES (0, '0', 'hive引擎设置', NULL, @application_id);
INSERT INTO `linkis_config_tree` (`id`, `parent_id`, `name`, `description`, `application_id`) VALUES (0, '0', 'hive资源设置', NULL, @application_id);

SELECT @application_id := id from linkis_application where name = 'python';
INSERT INTO `linkis_config_tree` (`id`, `parent_id`, `name`, `description`, `application_id`) VALUES (0, '0', 'python引擎设置', NULL, @application_id);
INSERT INTO `linkis_config_tree` (`id`, `parent_id`, `name`, `description`, `application_id`) VALUES (0, '0', 'python资源设置', NULL, @application_id);

SELECT @application_id := id from linkis_application where name = 'tidb';
INSERT INTO `linkis_config_tree` (`id`, `parent_id`, `name`, `description`, `application_id`) VALUES (0, '0', 'tidb设置', NULL, @application_id);
INSERT INTO `linkis_config_tree` (`id`, `parent_id`, `name`, `description`, `application_id`) VALUES (0, '0', 'spark引擎设置', NULL, @application_id);
INSERT INTO `linkis_config_tree` (`id`, `parent_id`, `name`, `description`, `application_id`) VALUES (0, '0', 'spark资源设置', NULL, @application_id);



SELECT @application_id := id from linkis_application where name = '通用设置';
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'wds.linkis.yarnqueue', 'yarn队列名', 'yarn队列名', @application_id, 'default', 'None', NULL, '0', '0', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'wds.linkis.preheating.time', '预热时间', '预热时间', @application_id, '9:00', 'None', NULL, '0', '0', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'wds.linkis.tmpfile.clean.time', 'tmp文件清理时间', 'tmp文件清理时间', @application_id, '10:00', 'None', NULL, '0', '0', '1');

INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'wds.linkis.yarnqueue.cores.max', '取值范围：1-500，单位：个', '队列CPU使用上限', @application_id, '150', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|500)$', '0', '0', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'wds.linkis.yarnqueue.memory.max', '取值范围：1-1000，单位：G', '队列内存使用上限', @application_id, '300G', 'Regex', '^([1-9]\\d{0,2}|1000)(G|g)$', '0', '0', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'wds.linkis.client.memory.max', '取值范围：1-1000，单位：G', '驱动器内存使用上限', @application_id, '20G', 'Regex', '^([1-9]\\d{0,1}|100)(G|g)$', '0', '0', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'wds.linkis.instance', '范围：1-20，单位：个', '引擎最大并发数', @application_id, '10', 'NumInterval', '[1,20]', '0', '0', '1');


SELECT @application_id := id from linkis_application where name = 'IDE';
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'spark.executor.instances', '取值范围：1-40，单位：个', '执行器实例最大并发数', @application_id, '2', 'NumInterval', '[1,40]', '0', '0', '2');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'spark.executor.cores', '取值范围：1-8，单位：个', '执行器核心个数', @application_id, '2', 'NumInterval', '[1,2]', '1', '0', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'spark.executor.memory', '取值范围：3-15，单位：G', '执行器内存大小', @application_id, '3', 'NumInterval', '[3,15]', '0', '0', '3');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'spark.driver.cores', '取值范围：只能取1，单位：个', '驱动器核心个数', @application_id, '1', 'NumInterval', '[1,1]', '1', '1', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'spark.driver.memory', '取值范围：1-15，单位：G', '驱动器内存大小', @application_id, '2', 'NumInterval', '[1,15]', '0', '0', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'hive.client.memory', '取值范围：1-10，单位：G', 'hive引擎初始化内存大小', @application_id, '2', 'NumInterval', '[1,10]', '0', '0', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'hive.client.java.opts', 'hive客户端进程参数', 'hive引擎启动时jvm参数', @application_id, '', 'None', NULL, '1', '1', '1');

INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'wds.linkis.instance', '范围：1-3，单位：个', 'hive引擎最大并发数', @application_id, '3', 'NumInterval', '[1,3]', '0', '0', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'wds.linkis.instance', '范围：1-3，单位：个', 'spark引擎最大并发数', @application_id, '3', 'NumInterval', '[1,3]', '0', '0', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'wds.linkis.instance', '范围：1-3，单位：个', 'python引擎最大并发数', @application_id, '3', 'NumInterval', '[1,3]', '0', '0', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'mapred.reduce.tasks', '范围：1-20，单位：个', 'reduce数', @application_id, '10', 'NumInterval', '[1,20]', '0', '1', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'dfs.block.size', '取值范围：2-10，单位：G', 'map数据块大小', @application_id, '10', 'NumInterval', '[2,10]', '0', '1', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'hive.exec.reduce.bytes.per.reducer', '取值范围：2-10，单位：G', 'reduce处理的数据量', @application_id, '10', 'NumInterval', '[2,10]', '0', '1', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'python.java.client.memory', '取值范围：1-2，单位：G', 'python引擎初始化内存大小', @application_id, '1', 'NumInterval', '[1,2]', '0', '0', '1');


INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'wds.linkis.instance', '范围：1-3，单位：个', 'spark引擎最大并发数', @application_id, '1', 'NumInterval', '[1,3]', '0', '0', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'spark.executor.instances', '取值范围：1-40，单位：个', '执行器实例最大并发数', @application_id, '2', 'NumInterval', '[1,40]', '0', '0', '2');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'spark.executor.cores', '取值范围：1-8，单位：个', '执行器核心个数', @application_id, '2', 'NumInterval', '[1,2]', '1', '0', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'spark.executor.memory', '取值范围：3-15，单位：G', '执行器内存大小', @application_id, '3', 'NumInterval', '[3,15]', '0', '0', '3');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'spark.driver.cores', '取值范围：只能取1，单位：个', '驱动器核心个数 ', @application_id, '1', 'NumInterval', '[1,1]', '1', '1', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'spark.driver.memory', '取值范围：1-15，单位：G', '驱动器内存大小', @application_id, '2', 'NumInterval', '[1,15]', '0', '0', '1');

INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'spark.tispark.pd.addresses', NULL, NULL, @application_id, 'pd0:2379', 'None', NULL, '0', '0', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'spark.tispark.tidb.addr', NULL, NULL, @application_id, 'tidb', 'None', NULL, '0', '0', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'spark.tispark.tidb.password', NULL, NULL, @application_id, NULL, 'None', NULL, '0', '0', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'spark.tispark.tidb.port', NULL, NULL, @application_id, '4000', 'None', NULL, '0', '0', '1');
INSERT INTO `linkis_config_key` (`id`, `key`, `description`, `name`, `application_id`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`) VALUES (0, 'spark.tispark.tidb.user', NULL, NULL, @application_id, 'root', 'None', NULL, '0', '0', '1');


#---------------------------------------全局设置------------------

SELECT @key_id := id from linkis_config_key WHERE `key` = 'wds.linkis.yarnqueue';
SELECT @tree_id := id from linkis_config_tree WHERE `name` = '队列资源';
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id := id from linkis_config_key WHERE `key` = 'wds.linkis.yarnqueue.cores.max';
SELECT @tree_id := id from linkis_config_tree WHERE `name` = '队列资源';
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id := id from linkis_config_key WHERE `key` = 'wds.linkis.yarnqueue.memory.max';
SELECT @tree_id := id from linkis_config_tree WHERE `name` = '队列资源';
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id := id from linkis_config_key WHERE `key` = 'wds.linkis.preheating.time';
SELECT @tree_id := id from linkis_config_tree WHERE `name` = '预热机制';
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id := id from linkis_config_key WHERE `key` = 'wds.linkis.tmpfile.clean.time';
SELECT @tree_id := id from linkis_config_tree WHERE `name` = '清理机制';
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id := id from linkis_config_key WHERE `name` = '引擎最大并发数';
SELECT @tree_id := id from linkis_config_tree WHERE `name` = '引擎设置';
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id := id from linkis_config_key WHERE `key` = 'wds.linkis.client.memory.max';
SELECT @tree_id := id from linkis_config_tree WHERE `name` = '驱动器资源';
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

#---------------------------------------spark---------------

SELECT @key_id:=id from(SELECT k.id  as 'id'from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'spark.executor.instances' and a.name = 'IDE' ORDER BY k.id limit 1) as tmp;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'spark资源设置' and a.name = 'spark' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id:=id from(SELECT k.id  as 'id'from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`name` = 'spark引擎最大并发数' and a.name = 'IDE' ORDER BY k.id limit 1) as tmp;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'spark引擎设置' and a.name = 'spark' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id:=id from(SELECT k.id  as 'id'from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'spark.executor.cores' and a.name = 'IDE' ORDER BY k.id limit 1) as tmp;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'spark资源设置' and a.name = 'spark' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id:=id from(SELECT k.id  as 'id'from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'spark.executor.memory' and a.name = 'IDE' ORDER BY k.id limit 1) as tmp;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'spark资源设置' and a.name = 'spark' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id:=id from(SELECT k.id  as 'id'from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'spark.driver.cores' and a.name = 'IDE' ORDER BY k.id limit 1) as tmp;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'spark资源设置' and a.name = 'spark' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id:=id from(SELECT k.id  as 'id'from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'spark.driver.memory' and a.name = 'IDE' ORDER BY k.id limit 1) as tmp;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'spark资源设置' and a.name = 'spark' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

#---------------------------------------tidb------------------

SELECT @key_id:=id from(SELECT k.id  as 'id'from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'spark.executor.instances' and a.name = 'IDE' ORDER BY k.id desc limit 1) as tmp;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'spark资源设置' and a.name = 'tidb' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id:=id from(SELECT k.id  as 'id'from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`name` = 'spark引擎最大并发数' and a.name = 'IDE' ORDER BY k.id desc limit 1) as tmp;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'spark引擎设置' and a.name = 'tidb' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id:=id from(SELECT k.id  as 'id'from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'spark.executor.cores' and a.name = 'IDE' ORDER BY k.id desc limit 1) as tmp;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'spark资源设置' and a.name = 'tidb' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id:=id from(SELECT k.id  as 'id'from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'spark.executor.memory' and a.name = 'IDE' ORDER BY k.id desc limit 1) as tmp;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'spark资源设置' and a.name = 'tidb' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id:=id from(SELECT k.id  as 'id'from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'spark.driver.cores' and a.name = 'IDE' ORDER BY k.id desc limit 1) as tmp;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'spark资源设置' and a.name = 'tidb' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id:=id from(SELECT k.id  as 'id'from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'spark.driver.memory' and a.name = 'IDE' ORDER BY k.id desc limit 1) as tmp;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'spark资源设置' and a.name = 'tidb' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id := k.id from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'spark.tispark.pd.addresses' and a.name = 'IDE' ;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'tidb设置' and a.name = 'tidb' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id := k.id from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'spark.tispark.tidb.addr' and a.name = 'IDE' ;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'tidb设置' and a.name = 'tidb' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id := k.id from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'spark.tispark.tidb.password' and a.name = 'IDE' ;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'tidb设置' and a.name = 'tidb' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id := k.id from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'spark.tispark.tidb.port' and a.name = 'IDE' ;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'tidb设置' and a.name = 'tidb' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id := k.id from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'spark.tispark.tidb.user' and a.name = 'IDE' ;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'tidb设置' and a.name = 'tidb' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

#---------------------------hive-----------------
SELECT @key_id := k.id from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`name` = 'hive引擎最大并发数' and a.name = 'IDE' ;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'hive引擎设置' and a.name = 'hive' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id := k.id from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'hive.client.memory' and a.name = 'IDE' ;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'hive资源设置' and a.name = 'hive' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id := k.id from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'mapred.reduce.tasks' and a.name = 'IDE';
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'hive资源设置' and a.name = 'hive' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id := k.id from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'dfs.block.size' and a.name = 'IDE' ;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'hive资源设置' and a.name = 'hive' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id := k.id from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'hive.exec.reduce.bytes.per.reducer' and a.name = 'IDE' ;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'hive资源设置' and a.name = 'hive' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id := k.id from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'hive.client.java.opts' and a.name = 'IDE';
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'hive资源设置' and a.name = 'hive' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

#------------------------python---------------------

SELECT @key_id := k.id from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`name` = 'python引擎最大并发数' and a.name = 'IDE'  ;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'python引擎设置' and a.name = 'python' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);

SELECT @key_id := k.id from linkis_config_key k left join linkis_application a on k.application_id = a.id WHERE k.`key` = 'python.java.client.memory' and a.name = 'IDE' ;
SELECT @tree_id := t.id from linkis_config_tree t left join linkis_application a on t.application_id = a.id  WHERE t.`name` = 'python资源设置' and a.name = 'python' ;
INSERT INTO `linkis_config_key_tree` (`id`, `key_id`, `tree_id`) VALUES (0, @key_id, @tree_id);