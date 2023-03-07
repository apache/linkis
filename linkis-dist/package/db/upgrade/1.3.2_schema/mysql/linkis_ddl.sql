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

-- ----------------------------
-- CREATE TABLE linkis_cg_tenant_label_config
-- ----------------------------
CREATE TABLE `linkis_cg_tenant_label_config` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `user` varchar(50) COLLATE utf8_bin NOT NULL,
  `creator` varchar(50) COLLATE utf8_bin NOT NULL,
  `tenant_value` varchar(128) COLLATE utf8_bin NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `desc` varchar(100) COLLATE utf8_bin NOT NULL,
  `bussiness_user` varchar(50) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_creator` (`user`,`creator`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


-- ----------------------------
-- CREATE TABLE linkis_cg_user_ip_config
-- ----------------------------
CREATE TABLE `linkis_cg_user_ip_config` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `user` varchar(50) COLLATE utf8_bin NOT NULL,
  `creator` varchar(50) COLLATE utf8_bin NOT NULL,
  `ip_list` text COLLATE utf8_bin NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `desc` varchar(100) COLLATE utf8_bin NOT NULL,
  `bussiness_user` varchar(50) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_creator` (`user`,`creator`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


-- Non-unique indexes are named according to "idx_fieldname[_fieldname]". For example idx_age_name
-- The unique index is named according to "uniq_field name[_field name]". For example uniq_age_name
-- It is recommended to include all field names for composite indexes, and the long field names can be abbreviated. For example idx_age_name_add
-- The index name should not exceed 50 characters, and the name should be lowercase
--
-- 非唯一索引按照“idx_字段名称[_字段名称]”进用行命名。例如idx_age_name
-- 唯一索引按照“uniq_字段名称[_字段名称]”进用行命名。例如uniq_age_name
-- 组合索引建议包含所有字段名,过长的字段名可以采用缩写形式。例如idx_age_name_add
-- 索引名尽量不超过50个字符，命名应该使用小写

ALTER TABLE `linkis_ps_configuration_key_engine_relation`
        DROP INDEX IF EXISTS `config_key_id`,
        ADD UNIQUE INDEX `uniq_kid_lid` (`config_key_id`, `engine_type_label_id`);

ALTER TABLE `linkis_ps_configuration_config_value`
        DROP INDEX IF EXISTS `config_key_id`,
        ADD UNIQUE INDEX `uniq_kid_lid` (`config_key_id`, `config_label_id`);

ALTER TABLE `linkis_ps_configuration_category`
        DROP INDEX IF EXISTS `label_id`,
        ADD UNIQUE INDEX `uniq_label_id` (`label_id`);


ALTER TABLE `linkis_ps_job_history_group_history`
        DROP INDEX IF EXISTS `created_time`,
        DROP INDEX IF EXISTS `submit_user`,
        ADD KEY `idx_created_time` (`created_time`),
        ADD KEY `idx_submit_user` (`submit_user`);

ALTER TABLE `linkis_ps_common_lock`
        DROP INDEX IF EXISTS `lock_object`,
        ADD UNIQUE KEY `uniq_lock_object` (`lock_object`);

ALTER TABLE `linkis_ps_variable_key_user`
        DROP INDEX  IF EXISTS `key_id`,
        DROP INDEX  IF EXISTS `application_id`,
        DROP INDEX  IF EXISTS `application_id_2`,
        ADD KEY `idx_key_id` (`key_id`),
        ADD UNIQUE KEY `uniq_aid_kid_uname` (`application_id`,`key_id`,`user_name`),
        ADD KEY `idx_aid` (`application_id`);

ALTER TABLE `linkis_ps_variable_key`
        DROP INDEX IF EXISTS `application_id`,
        ADD KEY `idx_aid` (`application_id`);

ALTER TABLE `linkis_ps_datasource_table`
        DROP INDEX IF EXISTS `database`,
        ADD UNIQUE KEY `uniq_db_name` (`database`,`name`);

ALTER TABLE `linkis_ps_cs_context_map`
        DROP INDEX IF EXISTS `key`,
        DROP INDEX IF EXISTS `keywords`,
        ADD UNIQUE KEY `uniq_key_cid_ctype` (`key`,`context_id`,`context_type`),
        ADD KEY `idx_keywords` (`keywords`(191));

ALTER TABLE `linkis_ps_cs_context_history`
        DROP INDEX IF EXISTS `keyword`,
        ADD KEY `idx_keyword` (`keyword`(191));

ALTER TABLE `linkis_ps_cs_context_id`
        DROP INDEX IF EXISTS `instance`,
        DROP INDEX IF EXISTS `backup_instance`,
        DROP INDEX IF EXISTS `instance_2`,
        ADD  KEY `idx_instance` (`instance`(128)),
        ADD KEY `idx_backup_instance` (`backup_instance`(191)),
        ADD KEY `idx_instance_bin` (`instance`(128),`backup_instance`(128));

ALTER TABLE `linkis_ps_bml_resources_version`
        DROP INDEX IF EXISTS `resource_id_version`,
        ADD UNIQUE KEY `uniq_rid_version` (`resource_id`, `version`);

ALTER TABLE `linkis_ps_bml_project`
        DROP INDEX IF EXISTS `name`,
        ADD UNIQUE KEY `uniq_name` (`name`);

ALTER TABLE `linkis_ps_bml_project_user`
        DROP INDEX IF EXISTS `user_project`,
        ADD UNIQUE KEY `uniq_name_pid` (`username`, `project_id`);

ALTER TABLE `linkis_ps_instance_label`
        DROP INDEX IF EXISTS `label_key_value`,
        ADD UNIQUE KEY `uniq_lk_lv` (`label_key`,`label_value`);

ALTER TABLE `linkis_ps_instance_label_value_relation`
        DROP INDEX IF EXISTS `label_value_key_label_id`,
        ADD UNIQUE KEY `uniq_lvk_lid` (`label_value_key`,`label_id`);

ALTER TABLE `linkis_ps_instance_label_relation`
        DROP INDEX IF EXISTS `label_instance`,
        ADD UNIQUE KEY `uniq_lid_instance` (`label_id`,`service_instance`);

ALTER TABLE `linkis_ps_instance_info`
        DROP INDEX IF EXISTS `instance`,
        ADD UNIQUE KEY `uniq_instance` (`instance`);

ALTER TABLE `linkis_cg_manager_service_instance`
        DROP INDEX IF EXISTS `instance`,
        ADD UNIQUE KEY `uniq_instance` (`instance`);

ALTER TABLE `linkis_cg_manager_label`
        DROP INDEX IF EXISTS `label_key_value`,
        ADD UNIQUE KEY `uniq_lk_lv` (`label_key`,`label_value`);

ALTER TABLE `linkis_cg_manager_label_value_relation`
        DROP INDEX IF EXISTS `label_value_key_label_id`,
        ADD UNIQUE KEY `uniq_lvk_lid` (`label_value_key`,`label_id`);

ALTER TABLE `linkis_cg_manager_label_resource`
        DROP INDEX IF EXISTS `label_id`,
        ADD UNIQUE KEY `uniq_label_id` (`label_id`);

ALTER TABLE `linkis_cg_ec_resource_info_record`
        DROP INDEX IF EXISTS `ticket_id`,
        DROP INDEX IF EXISTS `label_value_ticket_id`,
        ADD  KEY `idx_tid` (`ticket_id`),
        ADD UNIQUE KEY `uniq_tid_lv` (`ticket_id`,`label_value`);

ALTER TABLE `linkis_cg_manager_label_service_instance`
        DROP INDEX IF EXISTS `label_serviceinstance`,
        ADD  KEY `idx_lid_instance` (`label_id`,`service_instance`);

ALTER TABLE `linkis_cg_manager_label_service_instance`
        DROP INDEX IF EXISTS `label_serviceinstance`,
        ADD  KEY `uniq_lid_instance` (`label_id`,`service_instance`);

ALTER TABLE `linkis_ps_dm_datasource_env`
        DROP INDEX IF EXISTS `env_name`,
        DROP INDEX IF EXISTS `env_name_datasource_type_id`,
        ADD  UNIQUE KEY `uniq_env_name` (`env_name`),
        ADD  UNIQUE INDEX `uniq_name_dtid` (`env_name`, `datasource_type_id`);

ALTER TABLE `linkis_ps_dm_datasource_type_key`
        DROP INDEX IF EXISTS `data_source_type_id_key`,
        ADD  UNIQUE KEY `uniq_dstid_key` (`data_source_type_id`, `key`);

ALTER TABLE `linkis_mg_gateway_auth_token`
        DROP INDEX IF EXISTS `token_name`,
        ADD  UNIQUE KEY `uniq_token_name` (`token_name`);

ALTER TABLE `linkis_ps_dm_datasource`
        DROP INDEX IF EXISTS `datasource_name_un`,
        ADD  UNIQUE KEY `uniq_datasource_name` (`datasource_name`);

ALTER TABLE `linkis_ps_dm_datasource_type`
        DROP INDEX IF EXISTS `name_un`,
        ADD UNIQUE INDEX `uniq_name` (`name`);

ALTER TABLE `linkis_cg_user_ip_config`
        DROP INDEX IF EXISTS `user_creator`,
        ADD  UNIQUE KEY `uniq_user_creator` (`user`,`creator`);

ALTER TABLE `linkis_cg_tenant_label_config`
        DROP INDEX IF EXISTS `user_creator`,
        ADD  UNIQUE KEY `uniq_user_creator` (`user`,`creator`);



ALTER TABLE `linkis_ps_configuration_config_key` ADD COLUMN `en_description` varchar(200) DEFAULT NULL COMMENT 'english description';
ALTER TABLE `linkis_ps_configuration_config_key` ADD COLUMN `en_name` varchar(100) DEFAULT NULL COMMENT 'english name';
ALTER TABLE `linkis_ps_configuration_config_key` ADD COLUMN `en_treeName` varchar(100) DEFAULT NULL COMMENT 'english treeName';


ALTER TABLE `linkis_ps_dm_datasource_type` ADD COLUMN `description_en` varchar(255) DEFAULT NULL COMMENT 'english description';
ALTER TABLE `linkis_ps_dm_datasource_type` ADD COLUMN `option_en` varchar(32) DEFAULT NULL COMMENT 'english option';
ALTER TABLE `linkis_ps_dm_datasource_type` ADD COLUMN `classifier_en` varchar(32) DEFAULT NULL COMMENT 'english classifier';

ALTER TABLE `linkis_ps_dm_datasource_type_key` ADD COLUMN `name_en` varchar(32) DEFAULT NULL COMMENT 'english name';
ALTER TABLE `linkis_ps_dm_datasource_type_key` ADD COLUMN `description_en` varchar(200) DEFAULT NULL COMMENT 'english description';


