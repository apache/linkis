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


update linkis_ps_configuration_config_key  set `key` = 'linkis.trino.url' where `key` = 'wds.linkis.trino.url' ;
update linkis_ps_configuration_config_key  set `key` = 'linkis.trino.catalog' where `key` = 'wds.linkis.trino.catalog' ;
update linkis_ps_configuration_config_key  set `key` = 'linkis.trino.schema' where `key` = 'wds.linkis.trino.schema' ;
update linkis_ps_configuration_config_key  set `key` = 'linkis.trino.source' where `key` = 'wds.linkis.trino.source' ;


update `linkis_ps_configuration_config_key`  set `validate_range` = '[\"1h\",\"2h\",\"6h\",\"12h\",\"30m\",\"15m\",\"3m\"]' where `key` = 'wds.linkis.engineconn.max.free.time' and `engine_conn_type` = 'spark';
update `linkis_ps_configuration_config_key`  set `description` = '取值范围：3m,15m,30m,1h,2h,6h,12h' where `key` = 'wds.linkis.engineconn.max.free.time' and `engine_conn_type` = 'spark' ;


INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`, `en_description`, `en_name`, `en_treeName`) VALUES ("spark.external.default.jars", '取值范围：file:///xxx.jar 多个路径时 逗号分隔', 'spark 支持额外的jar包列表', NULL, 'Regex', '^file:\/\/\/[\u4e00-\u9fa5_a-zA-Z0-9-.\/]*\.jar(?:,\s*file:\/\/\/[\u4e00-\u9fa5_a-zA-Z0-9-.\/]*\.jar)*?$', '0', '1', '1', 'spark资源设置', 'spark','Value Range: file:///xxx.jar', 'Spark External Default Jars', 'Spark Resource Settings');

INSERT INTO `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(
	SELECT config.id AS `config_key_id`, label.id AS `engine_type_label_id`
	FROM (
		select * from linkis_ps_configuration_config_key
		where `key`="spark.external.default.jars"
		and `engine_conn_type`="spark") config
   INNER JOIN linkis_cg_manager_label label ON label.label_value ="*-*,spark-2.4.3"
);

INSERT INTO `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(
    SELECT `relation`.`config_key_id` AS `config_key_id`, NULL AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id`
    FROM linkis_ps_configuration_key_engine_relation relation
    INNER JOIN ( select * from linkis_ps_configuration_config_key  where `key`="spark.external.default.jars" and `engine_conn_type`="spark") config on relation.config_key_id=config.id
    INNER JOIN ( select * from linkis_cg_manager_label   where label_value ="*-*,spark-2.4.3") label on label.id=relation.engine_type_label_id
);


UPDATE  linkis_ps_configuration_config_key  SET validate_range ='[\",\",\"\\\\t\",\"\\\\;\",\"\\\\|\"]',description ="取值范围:,或\\t或;或|" WHERE `key`= "pipeline.field.split";

DELETE FROM  linkis_ps_error_code WHERE  error_code  = "43007";

UPDATE linkis_ps_error_code SET  error_regex='Permission denied:\\s*user=[a-zA-Z0-9_]+[,，]\\s*access=[a-zA-Z0-9_]+\\s*[,，]\\s*inode="([a-zA-Z0-9/_\\.]+)"'  WHERE  error_code  = "22001";

INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('13010','任务实际运行内存超过了设置的内存限制，请在管理台增加executor内存或在提交任务时通过spark.executor.memory增加内存','Container exited with a non-zero exit code',0);

UPDATE linkis_ps_configuration_config_key  SET  `key`="pipeline.output.isoverwrite" where `key` = "pipeline.output.isoverwtite";

INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43042','插入数据表动态分区数超过配置值 %s ，请优化sql或调整配置hive.exec.max.dynamic.partitions后重试','Maximum was set to (\\S+) partitions per node',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43043','执行任务消耗内存超过限制，hive任务请修改map或reduce的内存，spark任务请修改executor端内存','Error：java heap space',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43044','表 %s 分区数超过阈值 %s，需要分批删除分区，再删除表','the partitions of table (\\S+) exceeds threshold (\\S+)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43045','查询/操作的表 %s 分区数为 %s ，超过阈值 %s ，需要限制查询/操作的分区数量','Number of partitions scanned \\(=(\\d+)\\) on table (\\S+) exceeds limit \\(=(\\d+)\\)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43046','动态分区一次性写入分区数 %s ，超过阈值  %s,请减少一次性写入的分区数','Number of dynamic partitions created is (\\S+), which is more than (\\S+)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43047','动态分区一次性写入分区数 %s ，超过阈值  %s,请减少一次性写入的分区数','Maximum was set to (\\S+) partitions per node, number of dynamic partitions on this node: (\\S+)',0);

INSERT INTO linkis_ps_dm_datasource_type_key (data_source_type_id, `key`, name, default_value, value_type, `scope`, `require`, description, value_regex, ref_id, ref_value, data_source, update_time, create_time, name_en, description_en) VALUES(5, 'userClientIp', 'userClientIp', NULL, 'TEXT', 'ENV', 0, 'userClientIp', NULL, NULL, NULL, NULL, now(),now(), 'user client ip', 'user client ip');

INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43019','执行表在元数据库中存在meta缓存，meta信息与缓存不一致导致，请增加参数(--conf spark.sql.hive.convertMetastoreOrc=false)后重试','Unable to alter table.*Table is not allowed to be altered',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('13011','广播表过大导致driver内存溢出，请在执行sql前增加参数后重试：set spark.sql.autoBroadcastJoinThreshold=-1;','dataFrame to local exception',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43048','参数引用错误，请检查参数 %s 是否正常引用','UnboundLocalError.*local variable (\\S+) referenced before assignment',0);

UPDATE linkis_ps_error_code SET error_desc = "任务运行内存超过设置内存限制，导致Linkis服务负载过高，请在管理台调整Driver内存或联系管理员扩容" WHERE  error_code = "01002";
UPDATE linkis_ps_error_code SET error_desc = "任务运行内存超过设置内存限制，导致Linkis服务负载过高，请在管理台调整Driver内存或联系管理员扩容" WHERE  error_code = "01003";
UPDATE linkis_ps_error_code SET error_desc = "任务运行内存超过设置内存限制，导致Spark app应用退出，请在管理台增加Driver内存或在提交任务时通过spark.driver.memory调整内存。更多细节请参考Linkis常见问题Q60" WHERE  error_code = "13005";
UPDATE linkis_ps_error_code SET error_desc = "任务运行内存超过设置内存限制，导致Spark context应用退出，请在管理台增加Driver内存或在提交任务时通过spark.driver.memory调整内存。更多细节请参考Linkis常见问题Q60" WHERE  error_code = "13006";
UPDATE linkis_ps_error_code SET error_desc = "任务运行内存超过设置内存限制，导致Pyspark子进程退出，请在管理台增加Driver内存或在提交任务时通过spark.driver.memory调整内存。更多细节请参考Linkis常见问题Q60" WHERE  error_code = "13007";
UPDATE linkis_ps_error_code SET error_desc = "您的任务因为引擎退出（退出可能是引擎进程OOM或者主动kill引擎）导致失败" WHERE  error_code = "13009";

INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('13012','driver内存不足，请增加driver内存后重试','Failed to allocate a page (\\S+.*\\)), try again.',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('13013','使用spark默认变量sc导致后续代码执行失败','sc.setJobGroup(\\S+.*\\))',0);
DELETE FROM linkis_ps_error_code WHERE error_code  = "43016";
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43016','模块 %s 没有属性 %s ，请确认代码引用是否正常','AttributeError: \'(\\S+)\' object has no attribute \'(\\S+)\'',0);
UPDATE linkis_ps_error_code SET error_desc = "任务运行内存超过设置内存限制，导致引擎意外退出，请在管理台调整内存参数。" WHERE  error_code = "13004";
INSERT INTO linkis_cg_manager_label (label_key,label_value,label_feature,label_value_size,update_time,create_time) VALUES ('combined_userCreator_engineType','*-IDE,nebula-3.0.0','OPTIONAL',2,now(),now());
INSERT INTO linkis_cg_manager_label (label_key,label_value,label_feature,label_value_size,update_time,create_time) VALUES ('combined_userCreator_engineType','*-*,nebula-3.0.0','OPTIONAL',2,now(),now());

insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES ((select id from linkis_cg_manager_label where `label_value` = '*-IDE,nebula-3.0.0'), 2);

INSERT INTO linkis_ps_configuration_config_key (`key`,description,name,default_value,validate_type,validate_range,engine_conn_type,is_hidden,is_advanced,`level`,treeName,boundary_type,en_treeName,en_description,en_name,template_required) VALUES
('linkis.nebula.host','Nebula 连接地址','Nebula 连接地址',NULL,'None',NULL,'nebula',0,0,1,'Necula引擎设置',0,'Nebula Engine Settings','Nebula Host','Nebula Host',0);
INSERT INTO linkis_ps_configuration_config_key (`key`,description,name,default_value,validate_type,validate_range,engine_conn_type,is_hidden,is_advanced,`level`,treeName,boundary_type,en_treeName,en_description,en_name,template_required) VALUES
('linkis.nebula.port','Nebula 连接端口','Nebula 连接端口',NULL,'None',NULL,'nebula',0,0,1,'Necula引擎设置',0,'Nebula Engine Settings','Nebula Port','Nebula Port',0);
INSERT INTO linkis_ps_configuration_config_key (`key`,description,name,default_value,validate_type,validate_range,engine_conn_type,is_hidden,is_advanced,`level`,treeName,boundary_type,en_treeName,en_description,en_name,template_required) VALUES
('linkis.nebula.username','Nebula 连接用户名','Nebula 连接用户名',NULL,'None',NULL,'nebula',0,0,1,'Necula引擎设置',0,'Nebula Engine Settings','Nebula Username','Nebula Username',0);
INSERT INTO linkis_ps_configuration_config_key (`key`,description,name,default_value,validate_type,validate_range,engine_conn_type,is_hidden,is_advanced,`level`,treeName,boundary_type,en_treeName,en_description,en_name,template_required) VALUES
('linkis.nebula.password','Nebula 连接密码','Nebula 连接密码',NULL,'None',NULL,'nebula',0,0,1,'Necula引擎设置',0,'Nebula Engine Settings','Nebula Password','Nebula Password',0);

insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`) (select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'nebula' and config.`key` = 'linkis.nebula.host' and label_value = '*-*,nebula-3.0.0');
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`) (select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'nebula' and config.`key` = 'linkis.nebula.port' and label_value = '*-*,nebula-3.0.0');
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`) (select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'nebula' and config.`key` = 'linkis.nebula.username' and label_value = '*-*,nebula-3.0.0');
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`) (select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'nebula' and config.`key` = 'linkis.nebula.password' and label_value = '*-*,nebula-3.0.0');

insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`) ( select `relation`.`config_key_id` AS `config_key_id`, '127.0.0.1' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id and relation.config_key_id = ( select id FROM linkis_ps_configuration_config_key where `key` = 'linkis.nebula.host') AND label.label_value = '*-*,nebula-3.0.0');
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`) ( select `relation`.`config_key_id` AS `config_key_id`, '9669' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id and relation.config_key_id = ( select id FROM linkis_ps_configuration_config_key where `key` = 'linkis.nebula.port') AND label.label_value = '*-*,nebula-3.0.0');
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`) ( select `relation`.`config_key_id` AS `config_key_id`, 'nebula' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id and relation.config_key_id = ( select id FROM linkis_ps_configuration_config_key where `key` = 'linkis.nebula.username') AND label.label_value = '*-*,nebula-3.0.0');
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`) ( select `relation`.`config_key_id` AS `config_key_id`, 'nebula' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id and relation.config_key_id = ( select id FROM linkis_ps_configuration_config_key where `key` = 'linkis.nebula.password') AND label.label_value = '*-*,nebula-3.0.0');

INSERT INTO `linkis_mg_gateway_auth_token`(`token_name`,`legal_users`,`legal_hosts`,`business_owner`,`create_time`,`update_time`,`elapse_day`,`update_by`) VALUES ('DOCTOR-AUTH-LEstzFKwKkrALsDOuGg', '*', '*', 'BDP', DATE_FORMAT(NOW(), '%Y-%m-%d'), DATE_FORMAT(NOW(), '%Y-%m-%d'), -1, 'LINKIS');


INSERT INTO linkis_ps_configuration_config_key (`key`, description, name, default_value, validate_type, validate_range, engine_conn_type, is_hidden, is_advanced, `level`, treeName, boundary_type, en_treeName, en_description, en_name, template_required) VALUES( 'wds.linkis.jdbc.driver', '例如:com.mysql.jdbc.Driver', 'jdbc连接驱动', '', 'None', '', 'jdbc', 0, 0, 1, '用户配置', 0, 'User Configuration', 'For Example: com.mysql.jdbc.Driver', 'JDBC Connection Driver', 0);
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`) (select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'jdbc' and config.`key` = 'wds.linkis.jdbc.driver' and label_value = '*-*,jdbc-4');
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`) ( select `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id and relation.config_key_id = ( select id FROM linkis_ps_configuration_config_key where `key` = 'wds.linkis.jdbc.driver') AND label.label_value = '*-*,jdbc-4');

INSERT INTO linkis_ps_configuration_config_key (`key`, description, name, default_value, validate_type, validate_range, engine_conn_type, is_hidden, is_advanced, `level`, treeName, boundary_type, en_treeName, en_description, en_name, template_required) VALUES( 'linkis.jdbc.task.timeout.alert.time', '单位：分钟', 'jdbc任务任务超时告警时间', '', 'Regex', '^[1-9]\\d*$', 'jdbc', 0, 0, 1, '超时告警配置', 0, 'Timeout Alert Configuration', 'Unit: Minutes', 'JDBC Task Timeout Alert Time', 0);
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`) (select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'jdbc' and config.`key` = 'linkis.jdbc.task.timeout.alert.time' and label_value = '*-*,jdbc-4');
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`) ( select `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id and relation.config_key_id = ( select id FROM linkis_ps_configuration_config_key where `key` = 'linkis.jdbc.task.timeout.alert.time') AND label.label_value = '*-*,jdbc-4');

INSERT INTO linkis_ps_configuration_config_key (`key`, description, name, default_value, validate_type, validate_range, engine_conn_type, is_hidden, is_advanced, `level`, treeName, boundary_type, en_treeName, en_description, en_name, template_required) VALUES( 'linkis.jdbc.task.timeout.alert.user', '多人用英文逗号分隔', 'jdbc任务任务超时告警人', '', 'Regex', '^[a-zA-Z0-9,_-]+$', 'jdbc', 0, 0, 1, '超时告警配置', 0, 'Timeout Alert Configuration', 'Multiple People Separated By Commas In English', 'JDBC Task Timeout Alert Person', 0);
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`) (select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'jdbc' and config.`key` = 'linkis.jdbc.task.timeout.alert.user' and label_value = '*-*,jdbc-4');
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`) ( select `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id and relation.config_key_id = ( select id FROM linkis_ps_configuration_config_key where `key` = 'linkis.jdbc.task.timeout.alert.user') AND label.label_value = '*-*,jdbc-4');

INSERT INTO linkis_ps_configuration_config_key (`key`, description, name, default_value, validate_type, validate_range, engine_conn_type, is_hidden, is_advanced, `level`, treeName, boundary_type, en_treeName, en_description, en_name, template_required) VALUES( 'linkis.jdbc.task.timeout.alert.level', '超时告警级别:1 critical,2 major,3 minor,4 warning,5 info', 'jdbc任务任务超时告警级别', '3', 'NumInterval', '[1,5]', 'jdbc', 0, 0, 1, '超时告警配置', 0, 'Timeout Alert Configuration', 'Timeout Alert Levels: 1 Critical, 2 Major, 3 Minor, 4 Warning, 5 Info', 'JDBC Task Timeout Alert Level', 0);
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`) (select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'jdbc' and config.`key` = 'linkis.jdbc.task.timeout.alert.level' and label_value = '*-*,jdbc-4');
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`) ( select `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id and relation.config_key_id = ( select id FROM linkis_ps_configuration_config_key where `key` = 'linkis.jdbc.task.timeout.alert.level') AND label.label_value = '*-*,jdbc-4');

INSERT INTO linkis_ps_configuration_config_key (`key`, description, name, default_value, validate_type, validate_range, engine_conn_type, is_hidden, is_advanced, `level`, treeName, boundary_type, en_treeName, en_description, en_name, template_required) VALUES( 'linkis.jdbc.task.timeout.alert.datasource.type', '多个数据源用英文逗号分隔', '超时告警支持数据源类型', 'starrocks', 'Regex', '^[a-zA-Z0-9,]+$', 'jdbc', 0, 0, 1, '超时告警配置', 0, 'Timeout Alert Configuration', 'Separate Multiple Data Sources With Commas In English', 'Timeout Alert Supports Data Source Types', 0);
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`) (select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'jdbc' and config.`key` = 'linkis.jdbc.task.timeout.alert.datasource.type' and label_value = '*-*,jdbc-4');
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`) ( select `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id and relation.config_key_id = ( select id FROM linkis_ps_configuration_config_key where `key` = 'linkis.jdbc.task.timeout.alert.datasource.type') AND label.label_value = '*-*,jdbc-4');


INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `default_value`, `value_type`, `scope`, `require`, `description`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`, `name_en`, `description_en`) VALUES ((select id from `linkis_ps_dm_datasource_type` where `name` = 'starrocks'), 'kill_task_time', '超时kill任务时间', NULL, 'TEXT', NULL, 0, '配置任务超时时间，满足配置执行kill，单位：分钟', '^[1-9]\\d*$', NULL, NULL, NULL, now(), now(), 'Timeout Kill Task Time', 'Configure Task Timeout To Meet The Requirement Of Executing The Kill Action');

UPDATE  linkis_ps_error_code set error_desc ="任务运行内存超过设置内存限制，导致引擎意外退出，请在管理台调整内存后使用",error_regex="failed because the (hive|python|shell|jdbc|io_file|io_hdfs|fps|pipeline|presto|nebula|flink|appconn|sqoop|datax|openlookeng|trino|elasticsearch|seatunnel|hbase|jobserver) engine quitted unexpectedly" WHERE error_code = "13004";
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('13014','任务运行内存超过设置内存限制，导致引擎意外退出，请在管理台增加executor内存或在提交任务时通过spark.executor.memory或spark.executor.memoryOverhead调整内存','failed because the spark engine quitted unexpectedly',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43050','特殊UDF不支持在非sql脚本中使用','Not support spacial udf in non-SQL script',0);

INSERT INTO linkis_ps_configuration_config_key (`key`, description, name, default_value, validate_type, validate_range, engine_conn_type, is_hidden, is_advanced, `level`, treeName, boundary_type, en_treeName, en_description, en_name, template_required) VALUES( 'wds.linkis.engineconn.java.driver.memory', '取值范围：1-10，单位：G', 'jdbc引擎初始化内存大小', '1g', 'Regex', '^([1-9]|10)(G|g)$', 'jdbc', 0, 0, 1, '用户配置', 0, 'Value range: 1-10, Unit: G', 'JDBC Engine Initialization Memory Size', 'User Configuration', 0);
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`) (select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'jdbc' and config.`key` = 'wds.linkis.engineconn.java.driver.memory' and label_value = '*-*,jdbc-4');
INSERT INTO `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`) ( select `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id and relation.config_key_id = (select id FROM linkis_ps_configuration_config_key where `key` = 'wds.linkis.engineconn.java.driver.memory'and engine_conn_type = 'jdbc') AND label.label_value = '*-*,jdbc-4');


update linkis_ps_configuration_config_key set description ="取值范围：1-8000，单位：个",validate_range ="^(?:[1-9]\\d{0,2}|[1-7]\\d{3}|8000)$",en_description ="Value Range: 1-8000, Unit: Piece" where `key` = 'wds.linkis.rm.yarnqueue.cores.max';

update linkis_ps_configuration_config_key set description ="取值范围：1-20000，单位：G",validate_range ="^(?:[1-9]\\d{0,3}|[1]\\d{4}|20000)(G|g)$",en_description ="Value Range: 1-20000, Unit: G" where `key` = 'wds.linkis.rm.yarnqueue.memory.max';

update linkis_ps_configuration_config_key set description ="范围：1-100，单位：个",validate_range ="[1,100]",en_description ="Range: 1-100, unit: piece" where `key` = 'wds.linkis.rm.instance' and engine_conn_type ="spark";

update linkis_ps_configuration_key_limit_for_user set max_value ='8000' where key_id  = (SELECT  id  FROM linkis_ps_configuration_config_key where `key` = 'wds.linkis.rm.yarnqueue.cores.max');

update linkis_ps_configuration_key_limit_for_user set max_value ='20000g' where key_id  = (SELECT  id  FROM linkis_ps_configuration_config_key where `key` = 'wds.linkis.rm.yarnqueue.memory.max');

update linkis_ps_configuration_key_limit_for_user set max_value ='100' where key_id  = (SELECT  id  FROM linkis_ps_configuration_config_key where `key` = 'wds.linkis.rm.instance' and engine_conn_type ='spark');

INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('21004','Hive Metastore存在问题，生产请联系生产服务助手进行处理，测试请联系Hive开发','Unable to instantiate org.apache.hadoop.hive.ql.metadata.SessionHiveMetaStoreClient',0);


update linkis_ps_configuration_config_key set validate_range ="^(?!root\\.).*",validate_type ="Regex" where  `key`="wds.linkis.rm.yarnqueue";


insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',CONCAT('*-*,',"spark-3.4.4"), 'OPTIONAL', 2, now(), now());

insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'spark' and label.label_value = CONCAT('*-*,',"spark-3.4.4"));


insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',CONCAT('*-IDE,',"spark-3.4.4"), 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',CONCAT('*-Visualis,',"spark-3.4.4"), 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',CONCAT('*-nodeexecution,',"spark-3.4.4"), 'OPTIONAL', 2, now(), now());


insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES ((select id from linkis_cg_manager_label where `label_value` = CONCAT('*-IDE,',"spark-3.4.4")), 2);
insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES ((select id from linkis_cg_manager_label where `label_value` = CONCAT('*-Visualis,',"spark-3.4.4")), 2);
insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES ((select id from linkis_cg_manager_label where `label_value` = CONCAT('*-nodeexecution,',"spark-3.4.4")), 2);


insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(select `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = CONCAT('*-*,',"spark-3.4.4"));


INSERT IGNORE INTO linkis_cg_manager_label (label_key, label_value, label_feature, label_value_size, update_time, create_time)
SELECT label_key, REPLACE(label_value, 'spark-2.4.3', 'spark-3.4.4') AS label_value, label_feature, label_value_size, NOW() , NOW()
FROM linkis_cg_manager_label
WHERE label_key = 'combined_userCreator_engineType'
  AND label_value LIKE '%-IDE,spark-2.4.3'
  AND label_value != "*-IDE,spark-2.4.3";

INSERT IGNORE INTO linkis_ps_configuration_config_value (config_key_id, config_value, config_label_id, update_time, create_time)
SELECT config_key.id as config_key_id,"python3" as config_value ,label.id as config_label_id, NOW() , NOW()  FROM linkis_cg_manager_label label,linkis_ps_configuration_config_key config_key
WHERE config_key.`key` ="spark.python.version"
AND label.label_key = 'combined_userCreator_engineType'
AND label.label_value LIKE '%-IDE,spark-3.4.4'


INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('42003','未知函数%s，请检查代码中引用的函数是否有误','Cannot resolve function `(\\S+)',0);


update linkis_ps_configuration_config_key set default_value  = "com.mysql.jdbc.Driver"  where  engine_conn_type  = "jdbc" and `key`="wds.linkis.jdbc.driver";

insert IGNORE into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`)
select 'combined_userCreator_engineType',REPLACE(label_value, '2.4.3', '3.4.4'), 'OPTIONAL', 	2, 	now(), 	now()
from 	linkis_cg_manager_label
where  	label_value like "%spark-2.4.3%";

insert IGNORE into linkis_ps_configuration_category (`label_id`, `level`)
select 	id as `label_id`,	2 as `level`
from 	linkis_cg_manager_label
where 	label_value in (select REPLACE(label_value, '2.4.3', '3.4.4')
					from linkis_cg_manager_label
					where id in (
							select cate.label_id
							from  linkis_ps_configuration_category cate, linkis_cg_manager_label label
							where cate.label_id = label .id  and label.label_value like "%spark-2.4.3%"));

INSERT INTO linkis_ps_configuration_config_value (config_key_id, config_value, config_label_id, update_time, create_time)
SELECT     cofig.config_key_id AS config_key_id,    cofig.config_value AS config_value,    res.sp3 AS config_label_id,    NOW() AS update_time,    NOW() AS create_time
FROM
    (SELECT * FROM linkis_ps_configuration_config_value
     WHERE config_label_id IN
        (SELECT id FROM linkis_cg_manager_label WHERE label_value LIKE "%spark-2.4.3%")
     AND config_key_id IN
        (SELECT id FROM linkis_ps_configuration_config_key WHERE engine_conn_type = "spark")) cofig,
    (SELECT * FROM
        (SELECT id AS sp3, label_value AS lp3 FROM linkis_cg_manager_label WHERE label_value LIKE "%spark-3.4.4%") a,
        (SELECT id AS sp2, label_value AS lp2 FROM linkis_cg_manager_label WHERE label_value LIKE "%spark-2.4.3%") b
     WHERE SUBSTRING_INDEX(a.lp3, ',', 1) = SUBSTRING_INDEX(b.lp2, ',', 1)) res
WHERE cofig.config_label_id = res.sp2
ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    update_time = NOW();


UPDATE      linkis_ps_configuration_config_value
SET     config_value = "python3"
WHERE     config_label_id IN (
        SELECT id
        FROM linkis_cg_manager_label
        WHERE label_value LIKE "%spark-3.4.4%"
    )
AND config_key_id = (select id from linkis_ps_configuration_config_key where `key`= "spark.python.version");


-- JDBC 配置项
INSERT IGNORE INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`, `boundary_type`, `en_treeName`, `en_description`, `en_name`, `template_required`) VALUES('wds.linkis.jdbc.version', '取值范围：jdbc3,jdbc4', 'jdbc版本', 'jdbc4', 'OFT', '["jdbc3","jdbc4"]', 'jdbc', 0, 0, 1, '数据源配置', 0, 'DataSource Configuration', 'Value range: jdbc3, jdbc4', 'jdbc version', 0);
INSERT IGNORE INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`, `boundary_type`, `en_treeName`, `en_description`, `en_name`, `template_required`) VALUES('wds.linkis.jdbc.connect.max', '范围：1-20，单位：个', 'jdbc引擎最大连接数', '10', 'NumInterval', '[1,20]', 'jdbc', 0, 0, 1, '数据源配置', 3, 'DataSource Configuration', 'Range: 1-20, unit: piece', 'Maximum connections of jdbc engine', 0);
INSERT IGNORE INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`, `boundary_type`, `en_treeName`, `en_description`, `en_name`, `template_required`) VALUES('wds.linkis.jdbc.username', 'username', '数据库连接用户名', '', 'None', '', 'jdbc', 0, 0, 1, '用户配置', 0, 'User Configuration', 'username', 'Database connection user name', 0);
INSERT IGNORE INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`, `boundary_type`, `en_treeName`, `en_description`, `en_name`, `template_required`) VALUES('wds.linkis.jdbc.password', 'password', '数据库连接密码', '', 'None', '', 'jdbc', 0, 0, 1, '用户配置', 0, 'User Configuration', 'password', 'Database connection password', 0);
INSERT IGNORE INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`, `boundary_type`, `en_treeName`, `en_description`, `en_name`, `template_required`) VALUES('wds.linkis.jdbc.driver', '例如:com.mysql.jdbc.Driver', 'jdbc连接驱动', NULL, 'None', NULL, 'jdbc', 0, 0, 1, '用户配置', 0, 'User Configuration', 'For Example: com.mysql.jdbc.Driver', 'JDBC Connection Driver', 0);
INSERT IGNORE INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`, `boundary_type`, `en_treeName`, `en_description`, `en_name`, `template_required`) VALUES('wds.linkis.engineconn.java.driver.memory', '取值范围：1-10，单位：G', 'jdbc引擎初始化内存大小', '1g', 'Regex', '^([1-9]|10)(G|g)$', 'jdbc', 0, 0, 1, '用户配置', 0, 'Value range: 1-10, Unit: G', 'JDBC Engine Initialization Memory Size', 'User Configuration', 0);
INSERT IGNORE INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`, `boundary_type`, `en_treeName`, `en_description`, `en_name`, `template_required`) VALUES('linkis.jdbc.task.timeout.alert.time', '单位：分钟', 'jdbc任务超时告警时间', '', 'Regex', '^[1-9]\\d*$', 'jdbc', 0, 0, 1, '超时告警配置', 0, 'Timeout Alarm Configuration', 'Unit: Minutes', 'JDBC Task Timeout Alarm Time', 0);
INSERT IGNORE INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`, `boundary_type`, `en_treeName`, `en_description`, `en_name`, `template_required`) VALUES('linkis.jdbc.task.timeout.alert.user', '多人用英文逗号分隔', 'jdbc任务超时告警人', '', 'Regex', '^[a-zA-Z0-9,_-]+$', 'jdbc', 0, 0, 1, '超时告警配置', 0, 'Timeout Alarm Configuration', 'Multiple People Separated By Commas In English', 'JDBC Task Timeout Alert Person', 0);
INSERT IGNORE INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`, `boundary_type`, `en_treeName`, `en_description`, `en_name`, `template_required`) VALUES('linkis.jdbc.task.timeout.alert.level', '超时告警级别:1 critical,2 major,3 minor,4 warning,5 info', 'jdbc任务超时告警级别', '3', 'NumInterval', '[1,5]', 'jdbc', 0, 0, 1, '超时告警配置', 0, 'Timeout Alarm Configuration', 'Timeout Alarm Levels: 1 Critical, 2 Major, 3 Minor, 4 Warning, 5 Info', 'JDBC Task Timeout Alarm Level', 0);
INSERT IGNORE INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`, `boundary_type`, `en_treeName`, `en_description`, `en_name`, `template_required`) VALUES('linkis.jdbc.task.timeout.alert.datasource.type', '多个数据源用英文逗号分隔', '超时告警支持数据源类型', 'starrocks', 'Regex', '^[a-zA-Z0-9,]+$', 'jdbc', 0, 0, 1, '超时告警配置', 0, 'Timeout Alarm Configuration', 'Separate multiple data sources with commas in English', 'Timeout alarm supports data source types', 0);
INSERT IGNORE INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`, `boundary_type`, `en_treeName`, `en_description`, `en_name`, `template_required`) VALUES('linkisJDBCPoolAbandonedTimeout', '范围：1-21600，单位：秒', '数据源链接超时自动关闭时间', '300', 'NumInterval', '[1,21600]', 'jdbc', 0, 0, 1, '数据源配置', 0, 'Data Source Configuration', 'Range: 1-21600, Unit: seconds', 'Data Source Auto Close Link Time', 0);
INSERT IGNORE INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`, `boundary_type`, `en_treeName`, `en_description`, `en_name`, `template_required`) VALUES('wds.linkis.engineconn.max.free.time', '取值范围：3m,15m,30m,1h,2h', '引擎空闲退出时间', '15m', 'OFT', '["1h","2h","30m","15m","3m"]', 'jdbc', 0, 0, 1, '用户配置', 0, 'User Configuration', 'Value range: 3m, 15m, 30m, 1h, 2h', 'Engine unlock exit time', 0);

-- JDBC 标签和关系配置
INSERT IGNORE INTO `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',CONCAT('*-*,',"jdbc-4"), 'OPTIONAL', 2, now(), now());

INSERT IGNORE INTO `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'jdbc' and label_value = CONCAT('*-*,',"jdbc-4"));

INSERT IGNORE INTO linkis_ps_configuration_category (`label_id`, `level`) VALUES ((select id from linkis_cg_manager_label where `label_value` = CONCAT('*-IDE,',"jdbc-4")), 2);

-- jdbc default configuration
INSERT IGNORE INTO `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(select `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = CONCAT('*-*,',"jdbc-4"));

INSERT INTO `linkis_ps_configuration_config_key` (`key`,description,name,default_value,validate_type,validate_range,engine_conn_type,is_hidden,is_advanced,`level`,treeName,boundary_type,en_treeName,en_description,en_name,template_required) VALUES
('wds.linkis.engine.running.job.max', '引擎运行最大任务数', '引擎运行最大任务数', '30', 'None', NULL, 'shell', 0, 0, 1, 'shell引擎设置', 0, 'Maximum Number Of Tasks The Engine Can Run', 'Maximum Number For Engine', 'shell Engine Settings', 0);

insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`) ( select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'shell' and config.`key` = 'wds.linkis.engine.running.job.max' and label_value = '*-*,shell-1');


insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`) ( select `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id and relation.config_key_id = ( select id FROM linkis_ps_configuration_config_key where `key` = 'wds.linkis.engine.running.job.max') AND label.label_value = '*-*,shell-1');

-- 更新 Shell 引擎运行最大任务数默认值从 30 改为 60
UPDATE `linkis_ps_configuration_config_key` SET default_value = '60' WHERE `key` = 'wds.linkis.engine.running.job.max' AND engine_conn_type = 'shell';

-- 更新错误码正则表达式
UPDATE linkis_ps_error_code SET error_regex = "The ecm of labels" WHERE error_code = "01001";

