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


-- Index naming convention(索引命名规范)
-- Ordinary index: inx_field name(普通索引：inx_字段名)
-- Unique index: uni_ field name(唯一索引：uni_字段名)
-- Composite index: ain_ field name(复合索引：ain_字段名)
-- The index name should not exceed 50 characters(索引名尽量不超过50个字符)
-- The index name is modified according to the index naming convention(索引名称按索引命名规范修改)

ALTER TABLE `linkis_ps_configuration_key_engine_relation`
        DROP INDEX IF EXISTS `config_key_id`,
        ADD UNIQUE INDEX `ain_lpcker_kid_lid` (`config_key_id`, `engine_type_label_id`);

ALTER TABLE `linkis_ps_configuration_config_value`
        DROP INDEX IF EXISTS `config_key_id`,
        ADD UNIQUE INDEX `ain_lpccv_kid_lid` (`config_key_id`, `config_label_id`);

ALTER TABLE `linkis_ps_configuration_category`
        DROP INDEX IF EXISTS `label_id`,
        ADD UNIQUE INDEX `uni_label_id` (`label_id`);


ALTER TABLE `linkis_ps_job_history_group_history`
        DROP INDEX IF EXISTS `created_time`,
        DROP INDEX IF EXISTS `submit_user`,
        ADD KEY `inx_created_time` (`created_time`),
        ADD KEY `inx_submit_user` (`submit_user`);

ALTER TABLE `linkis_ps_common_lock`
        DROP INDEX IF EXISTS `lock_object`,
        ADD UNIQUE KEY `uni_lock_object` (`lock_object`);

ALTER TABLE `linkis_ps_variable_key_user`
        DROP INDEX  IF EXISTS `key_id`,
        DROP INDEX  IF EXISTS `application_id`,
        ADD KEY `inx_key_id` (`key_id`),
        ADD KEY `inx_lpvku_aid` (`application_id`);

ALTER TABLE `linkis_ps_variable_key`
        DROP INDEX IF EXISTS `application_id`,
        ADD KEY `inx_lpvk_aid` (`application_id`);

ALTER TABLE `linkis_ps_datasource_table`
        DROP INDEX IF EXISTS `database`,
        ADD UNIQUE KEY `ain_db_name` (`database`,`name`);

ALTER TABLE `linkis_ps_cs_context_map`
        DROP INDEX IF EXISTS `key`,
        DROP INDEX IF EXISTS `keywords`,
        ADD UNIQUE KEY `ain_key_cid_ctype` (`key`,`context_id`,`context_type`),
        ADD KEY `inx_lpccm_keywords` (`keywords`(191));

ALTER TABLE `linkis_ps_cs_context_history`
        DROP INDEX IF EXISTS `keyword`,
        ADD KEY `inx_lpcch_keyword` (`keyword`(191));

ALTER TABLE `linkis_ps_cs_context_id`
        DROP INDEX IF EXISTS `instance`,
        DROP INDEX IF EXISTS `backup_instance`,
        DROP INDEX IF EXISTS `instance_2`,
        ADD  KEY `inx_instance` (`instance`(128)),
        ADD KEY `inx_backup_instance` (`backup_instance`(191)),
        ADD KEY `ain_instance_bin` (`instance`(128),`backup_instance`(128));

ALTER TABLE `linkis_ps_bml_resources_version`
        DROP INDEX IF EXISTS `resource_id_version`,
        ADD UNIQUE KEY `ain_rid_version` (`resource_id`, `version`);

ALTER TABLE `linkis_ps_bml_project`
        DROP INDEX IF EXISTS `name`,
        ADD UNIQUE KEY `uni_name` (`name`);

ALTER TABLE `linkis_ps_bml_project_user`
        DROP INDEX IF EXISTS `user_project`,
        ADD UNIQUE KEY `ain_name_pid` (`username`, `project_id`);

ALTER TABLE `linkis_ps_instance_label`
        DROP INDEX IF EXISTS `label_key_value`,
        ADD UNIQUE KEY `ain_lpil_lk_lv` (`label_key`,`label_value`);

ALTER TABLE `linkis_ps_instance_label_value_relation`
        DROP INDEX IF EXISTS `label_value_key_label_id`,
        ADD UNIQUE KEY `ain_lpilvr_lvk_lid` (`label_value_key`,`label_id`);

ALTER TABLE `linkis_ps_instance_label_relation`
        DROP INDEX IF EXISTS `label_instance`,
        ADD UNIQUE KEY `ain_lpilr_lid_instance` (`label_id`,`service_instance`);

ALTER TABLE `linkis_ps_instance_info`
        DROP INDEX IF EXISTS `instance`,
        ADD UNIQUE KEY `uni_lpii_instance` (`instance`);

ALTER TABLE `linkis_cg_manager_service_instance`
        DROP INDEX IF EXISTS `instance`,
        ADD UNIQUE KEY `uni_lcmsi_instance` (`instance`);

ALTER TABLE `linkis_cg_manager_label`
        DROP INDEX IF EXISTS `label_key_value`,
        ADD UNIQUE KEY `uni_lcml_lk_lv` (`label_key`,`label_value`);

ALTER TABLE `linkis_cg_manager_label_value_relation`
        DROP INDEX IF EXISTS `label_value_key_label_id`,
        ADD UNIQUE KEY `ain_lclvr_lvk_lid` (`label_value_key`,`label_id`);

ALTER TABLE `linkis_cg_manager_label_resource`
        DROP INDEX IF EXISTS `label_id`,
        ADD UNIQUE KEY `uni_label_id` (`label_id`);

ALTER TABLE `linkis_cg_ec_resource_info_record`
        DROP INDEX IF EXISTS `ticket_id`,
        DROP INDEX IF EXISTS `label_value_ticket_id`,
        ADD  KEY `inx_ticket_id` (`ticket_id`),
        ADD UNIQUE KEY `ain_tid_lv` (`ticket_id`,`label_value`);

ALTER TABLE `linkis_cg_manager_label_service_instance`
        DROP INDEX IF EXISTS `label_serviceinstance`,
        ADD  KEY `ain_lcmlsi_lid_instance` (`label_id`,`service_instance`);

ALTER TABLE `linkis_cg_manager_label_service_instance`
        DROP INDEX IF EXISTS `label_serviceinstance`,
        ADD  KEY `ain_lcmlsi_lid_instance` (`label_id`,`service_instance`);

ALTER TABLE `linkis_ps_dm_datasource_env`
        DROP INDEX IF EXISTS `env_name`,
        DROP INDEX IF EXISTS `env_name_datasource_type_id`,
        ADD  UNIQUE KEY `uni_env_name` (`env_name`),
        ADD  UNIQUE INDEX `ain_name_dtid` (`env_name`, `datasource_type_id`);

ALTER TABLE `linkis_ps_dm_datasource_type_key`
        DROP INDEX IF EXISTS `data_source_type_id_key`,
        ADD  UNIQUE KEY `ain_dstid_key` (`data_source_type_id`, `key`);

ALTER TABLE `linkis_mg_gateway_auth_token`
        DROP INDEX IF EXISTS `token_name`,
        ADD  UNIQUE KEY `uni_token_name` (`token_name`);

ALTER TABLE `linkis_cg_user_ip_config`
        DROP INDEX IF EXISTS `user_creator`,
        ADD  UNIQUE KEY `ain_lctlc_user_creator` (`user`,`creator`);

ALTER TABLE `linkis_cg_tenant_label_config`
        DROP INDEX IF EXISTS `user_creator`,
        ADD  UNIQUE KEY `ain_lcuic_user_creator` (`user`,`creator`);