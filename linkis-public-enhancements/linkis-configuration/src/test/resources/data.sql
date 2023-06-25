/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

DELETE FROM linkis_cg_manager_label;

insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType','*-*,*-*', 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType','*-IDE,*-*', 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType','*-Visualis,*-*', 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType','*-nodeexecution,*-*', 'OPTIONAL', 2, now(), now());

INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`,`boundary_type`) VALUES ('wds.linkis.rm.yarnqueue', 'yarn队列名', 'yarn队列名', 'ide', 'None', NULL, '0', '0', '1', '队列资源',0);
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`,`boundary_type`) VALUES ('wds.linkis.rm.yarnqueue.instance.max', '取值范围：1-128，单位：个', '队列实例最大个数', '30', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|128)$', '0', '0', '1', '队列资源',3);
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`,`boundary_type`) VALUES ('wds.linkis.rm.yarnqueue.cores.max', '取值范围：1-500，单位：个', '队列CPU使用上限', '150', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|500)$', '0', '0', '1', '队列资源',3);
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`,`boundary_type`) VALUES ('wds.linkis.rm.yarnqueue.memory.max', '取值范围：1-1000，单位：G', '队列内存使用上限', '300G', 'Regex', '^([1-9]\\d{0,2}|1000)(G|g)$', '0', '0', '1', '队列资源',3);
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`,`boundary_type`) VALUES ('wds.linkis.rm.client.memory.max', '取值范围：1-100，单位：G', '全局各个引擎内存使用上限', '20G', 'Regex', '^([1-9]\\d{0,1}|100)(G|g)$', '0', '0', '1', '队列资源',3);
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`,`boundary_type`) VALUES ('wds.linkis.rm.client.core.max', '取值范围：1-128，单位：个', '全局各个引擎核心个数上限', '10', 'Regex', '^(?:[1-9]\\d?|[1][0-2][0-8])$', '0', '0', '1', '队列资源',3);
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`,`boundary_type`) VALUES ('wds.linkis.rm.instance', '范围：1-20，单位：个', '全局各个引擎最大并发数', '10', 'NumInterval', '[1,20]', '0', '0', '1', '队列资源',3);

insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`) values (1,1);
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`) values (2,1);
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`) values (3,1);
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`) values (4,1);
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`) values (5,1);
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`) values (6,1);
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`) values (7,1);

insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`) values (1,'1',1);
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`) values (2,'1',1);
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`) values (3,'1',1);
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`) values (4,'1',1);
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`) values (5,'1',1);
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`) values (6,'1',1);
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`) values (7,'1',1);

insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES (1, 1);
insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES (2, 1);
insert into linkis_ps_configuration_category (`label_id`, `level`) VALUES (3, 1);
