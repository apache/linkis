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

update  linkis_ps_configuration_config_key set engine_conn_type = "" where engine_conn_type is NULL;

INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('13008','任务产生的序列化结果总大小超过了配置的spark.driver.maxResultSize限制。请检查您的任务，看看是否有可能减小任务产生的结果大小，或则可以考虑压缩或合并结果，以减少传输的数据量','is bigger than spark.driver.maxResultSize',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('13009','您的任务因为引擎退出（退出可能是引擎进程OOM或者主动kill引擎）导致失败','ERROR EC exits unexpectedly and actively kills the task',0);

update  linkis_ps_configuration_config_key set template_required = 1 where `key` in (
"spark.executor.instances",
"spark.executor.memory",
"spark.driver.memory",
"wds.linkis.engineconn.java.driver.memory",
"mapreduce.job.running.map.limit",
"mapreduce.job.running.reduce.limit",
)
update  linkis_ps_configuration_config_key set template_required = 1 where `key` = "wds.linkis.rm.instance" and  engine_conn_type in ("spark","hive");
-- spark.conf
INSERT INTO linkis_ps_configuration_config_key
(`key`, description, name,
default_value, validate_type, validate_range, engine_conn_type,
is_hidden, is_advanced, `level`,
treeName, boundary_type, en_treeName,
en_description, en_name)
VALUES(
'spark.conf', '多个参数使用分号[;]分隔 例如spark.shuffle.spill=true;', 'spark自定义配置参数',
null, 'None', NULL, 'spark',
0, 1, 1,
'spark资源设置', 0, 'Spark Resource Settings',
'Multiple parameters are separated by semicolons [;] For example, spark.shuffle.compress=ture;', 'Spark Custom Configuration Parameters');

INSERT INTO `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(
        SELECT config.id AS `config_key_id`, label.id AS `engine_type_label_id`
        FROM (
                select * from linkis_ps_configuration_config_key
                where `key`="spark.conf"
                and `engine_conn_type`="spark") config
   INNER JOIN linkis_cg_manager_label label ON label.label_value ="*-*,spark-2.4.3"
);



INSERT INTO `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(
    SELECT `relation`.`config_key_id` AS `config_key_id`, NULL AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id`
    FROM linkis_ps_configuration_key_engine_relation relation
    INNER JOIN ( select * from linkis_ps_configuration_config_key  where `key`="spark.conf" and `engine_conn_type`="spark") config on relation.config_key_id=config.id
    INNER JOIN ( select * from linkis_cg_manager_label   where label_value ="*-*,spark-2.4.3") label on label.id=relation.engine_type_label_id
);


-- spark.locality.wait

INSERT INTO `linkis_ps_configuration_config_key`
(`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`, `boundary_type`, `en_treeName`, `en_description`, `en_name`)
VALUES
('spark.locality.wait', '范围：0-3，单位：秒', '任务调度本地等待时间', '3s', 'OFT', '[\"0s\",\"1s\",\"2s\",\"3s\"]', 'spark', 0, 1, 1, 'spark资源设置', 0, 'Spark Resource Settings', 'Range: 0-3, Unit: second', 'Task Scheduling Local Waiting Time');


-- all 默认
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(
        SELECT config.id AS `config_key_id`, label.id AS `engine_type_label_id`
        FROM (
                select * from linkis_ps_configuration_config_key
                where `key`="spark.locality.wait"
                and `engine_conn_type`="spark") config
   INNER JOIN linkis_cg_manager_label label ON label.label_value ="*-*,spark-2.4.3"
);



INSERT INTO `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(
    SELECT `relation`.`config_key_id` AS `config_key_id`, NULL AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id`
    FROM linkis_ps_configuration_key_engine_relation relation
    INNER JOIN ( select * from linkis_ps_configuration_config_key  where `key`="spark.locality.wait" and `engine_conn_type`="spark") config on relation.config_key_id=config.id
    INNER JOIN ( select * from linkis_cg_manager_label   where label_value ="*-*,spark-2.4.3") label on label.id=relation.engine_type_label_id
);


-- spark.memory.fraction
INSERT INTO `linkis_ps_configuration_config_key`
(`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`, `boundary_type`, `en_treeName`, `en_description`, `en_name`)
VALUES
('spark.memory.fraction', '范围：0.4,0.5,0.6，单位：百分比', '执行内存和存储内存的百分比', '0.6', 'OFT', '[\"0.4\",\"0.5\",\"0.6\"]', 'spark', 0, 1, 1, 'spark资源设置', 0, 'Spark Resource Settings', 'Range: 0.4, 0.5, 0.6, in percentage', 'Percentage Of Execution Memory And Storage Memory');


-- all 默认
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(
        SELECT config.id AS `config_key_id`, label.id AS `engine_type_label_id`
        FROM (
                select * from linkis_ps_configuration_config_key
                where `key`="spark.memory.fraction"
                and `engine_conn_type`="spark") config
   INNER JOIN linkis_cg_manager_label label ON label.label_value ="*-*,spark-2.4.3"
);

INSERT INTO `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(
    SELECT `relation`.`config_key_id` AS `config_key_id`, NULL AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id`
    FROM linkis_ps_configuration_key_engine_relation relation
    INNER JOIN ( select * from linkis_ps_configuration_config_key  where `key`="spark.memory.fraction" and `engine_conn_type`="spark") config on relation.config_key_id=config.id
    INNER JOIN ( select * from linkis_cg_manager_label   where label_value ="*-*,spark-2.4.3") label on label.id=relation.engine_type_label_id
);


UPDATE linkis_ps_error_code SET error_regex = "User (\\S+) cannot submit applications to queue ([A-Za-z._0-9]+)" WHERE  error_code =  "21001";

INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43016','模块 %s 没有属性 %s ，请确认代码引用是否正常','AttributeError: \'(\\S+)\' object has no attribute \'(\\S+)\'',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43017','存在参数无效或拼写错误，请确认 %s 参数正确性','KeyError: (\\(.+\\))',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43018','文件未找到，请确认该路径( %s )是否存在','FileNotFoundError.*No such file or directory\\:\\s\'(\\S+)\'',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('01006','没有健康可用的ecm节点，可能任务量大,导致节点资源处于不健康状态，尝试kill空闲引擎释放资源','There are corresponding ECM tenant labels',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('01007','文件编码格式异常,请联系管理人员处理','UnicodeEncodeError.*characters',0);
UPDATE linkis_ps_error_code SET error_regex = "KeyError: (.*)" WHERE  error_code =  "43017";

UPDATE linkis_ps_error_code SET error_desc = "任务实际运行内存超过了设置的内存限制，请在管理台增加executor内存或在提交任务时通过spark.executor.memory增加内存。更多细节请参考Linkis常见问题Q60" WHERE  error_code =  "13002";
update  linkis_ps_configuration_config_key   set validate_range ='[\",\",\"\\\\t\",\"\\\\;\",\"\\\\|\"]',description ="取值范围：,或\\t或;或|" WHERE `key`= "pipeline.field.split";

DELETE FROM  linkis_ps_error_code WHERE  error_code  = "43007";

UPDATE linkis_ps_error_code SET  error_regex='Permission denied:\\s*user=[a-zA-Z0-9_]+[,，]\\s*access=[a-zA-Z0-9_]+\\s*[,，]\\s*inode="([a-zA-Z0-9/_\\.]+)"'  WHERE  error_code  = "22001";

INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('13010','任务实际运行内存超过了设置的内存限制，请在管理台增加executor内存或在提交任务时通过spark.executor.memory增加内存','Container exited with a non-zero exit code',0);

INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43042','插入数据表动态分区数超过配置值 %s ，请优化sql或调整配置hive.exec.max.dynamic.partitions后重试','Maximum was set to (\\S+) partitions per node',0);

INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43043','执行任务消耗内存超过限制，hive任务请修改map或reduce的内存，spark任务请修改executor端内存','Error：java heap space',0);

INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43044','表 %s 分区数超过阈值 %s，需要分批删除分区，再删除表','the partitions of table (\\S+) exceeds threshold (\\S+)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43045','查询/操作的表 %s 分区数为 %s ，超过阈值 %s ，需要限制查询/操作的分区数量','Number of partitions scanned \\(=(\\d+)\\) on table (\\S+) exceeds limit \\(=(\\d+)\\)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43046','动态分区一次性写入分区数 %s ，超过阈值  %s,请减少一次性写入的分区数','Number of dynamic partitions created is (\\S+), which is more than (\\S+)',0);
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43047','动态分区一次性写入分区数 %s ，超过阈值  %s,请减少一次性写入的分区数','Maximum was set to (\\S+) partitions per node, number of dynamic partitions on this node: (\\S+)',0);

INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`,`en_description`, `en_name`, `en_treeName`, `template_required`) VALUES ('mapreduce.job.reduce.slowstart.completedmaps', '取值范围：0-1', 'Map任务数与总Map任务数之间的比例','0.05', 'Regex', '^(0(\\.\\d{1,2})?|1(\\.0{1,2})?)$', '0', '0', '1', 'hive引擎设置', 'hive', 'Value Range: 0-1', 'The Ratio Between The Number Of Map Tasks And The Total Number Of Map Tasks', 'Hive Engine Settings', '1');

insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = 'hive' and config.`key` = "mapreduce.job.reduce.slowstart.completedmaps" and label_value = "*-*,hive-2.3.3");


insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(select `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id and relation.config_key_id = (select id FROM  linkis_ps_configuration_config_key where `key`="mapreduce.job.reduce.slowstart.completedmaps")AND label.label_value = '*-*,hive-2.3.3');


update linkis_ps_dm_datasource_type_key set value_regex='^[0-9A-Za-z_-\\s]+$' where data_source_type_id=18 and `key`='username';

UPDATE linkis_ps_error_code SET error_desc = "任务运行内存超过设置内存限制，请在管理台增加executor内存或在提交任务时通过spark.executor.memory调整内存。更多细节请参考Linkis常见问题Q60" WHERE  error_code = "13002";

UPDATE linkis_ps_error_code SET error_desc = "任务运行内存超过设置内存限制，请在管理台增加executor内存或在提交任务时通过spark.executor.memory调整内存。更多细节请参考Linkis常见问题Q60" WHERE  error_code = "13010";

UPDATE linkis_ps_error_code SET error_desc = "任务运行内存超过设置内存限制，请在管理台增加executor内存或调优sql后执行" WHERE  error_code = "13003";

UPDATE linkis_ps_error_code SET error_desc = "任务运行内存超过设置内存限制，导致引擎意外退出，请在管理台增加executor内存或在提交任务时通过spark.executor.memory调整内存。更多细节请参考Linkis常见问题Q60" WHERE  error_code = "13004";

UPDATE linkis_ps_error_code SET error_desc = "任务运行内存超过设置内存限制，导致Spark app应用退出，请在管理台增加executor内存或在提交任务时通过spark.executor.memory调整内存。更多细节请参考Linkis常见问题Q60" WHERE  error_code = "13005";

UPDATE linkis_ps_error_code SET error_desc = "任务运行内存超过设置内存限制，导致Spark context应用退出，请在管理台增加executor内存或在提交任务时通过spark.executor.memory调整内存。更多细节请参考Linkis常见问题Q60" WHERE  error_code = "13006";

UPDATE linkis_ps_error_code SET error_desc = "任务运行内存超过设置内存限制，导致Pyspark子进程退出，请在管理台增加executor内存或在提交任务时通过spark.executor.memory调整内存。更多细节请参考Linkis常见问题Q60" WHERE  error_code = "13007";

UPDATE linkis_ps_error_code SET error_desc = "任务运行内存超过设置内存限制，导致Linkis服务负载过高，请在管理台调整executor内存或联系管理员扩容" WHERE  error_code = "01002";

UPDATE linkis_ps_error_code SET error_desc = "任务运行内存超过设置内存限制，导致Linkis服务负载过高，请在管理台调整executor内存或联系管理员扩容" WHERE  error_code = "01003";


INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('43020','Python 进程已停止，查询失败！','python process has stopped',0);