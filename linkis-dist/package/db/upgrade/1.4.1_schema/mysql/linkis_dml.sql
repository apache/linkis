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
('spark.locality.wait', '范围：0-3000，单位：毫秒', '任务调度本地等待时间', '3000', 'OFT', '[\"0\",\"1000\",\"2000\",\"3000\"]', 'spark', 0, 1, 1, 'spark资源设置', 0, 'Spark Resource Settings', 'Range: 0-3000, Unit: millisecond', 'Task Scheduling Local Waiting Time');


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


