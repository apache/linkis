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

-- ============================================
-- Linkis Database Index Definitions (MySQL)
-- ============================================
-- This file contains all index definitions extracted from linkis_ddl.sql
-- Index naming conventions:
--   - Non-unique indexes: idx_fieldname[_fieldname]
--   - Unique indexes: uniq_fieldname[_fieldname]
-- ============================================

-- Configuration Module Indexes
-- linkis_ps_configuration_config_key
ALTER TABLE `linkis_ps_configuration_config_key` ADD UNIQUE INDEX `uniq_key_ectype` (`key`,`engine_conn_type`);

-- linkis_ps_configuration_key_engine_relation
ALTER TABLE `linkis_ps_configuration_key_engine_relation` ADD UNIQUE INDEX `uniq_kid_lid` (`config_key_id`, `engine_type_label_id`);

-- linkis_ps_configuration_config_value
ALTER TABLE `linkis_ps_configuration_config_value` ADD UNIQUE INDEX `uniq_kid_lid` (`config_key_id`, `config_label_id`);

-- linkis_ps_configuration_category
ALTER TABLE `linkis_ps_configuration_category` ADD UNIQUE INDEX `uniq_label_id` (`label_id`);

-- linkis_ps_configuration_template_config_key
ALTER TABLE `linkis_ps_configuration_template_config_key` ADD UNIQUE INDEX `uniq_tid_kid` (`template_uuid`, `key_id`);
ALTER TABLE `linkis_ps_configuration_template_config_key` ADD UNIQUE INDEX `uniq_tname_kid` (`template_uuid`, `key_id`);

-- linkis_ps_configuration_key_limit_for_user
ALTER TABLE `linkis_ps_configuration_key_limit_for_user` ADD UNIQUE INDEX `uniq_com_label_kid` (`combined_label_value`, `key_id`);

-- linkis_ps_configutation_lm_across_cluster_rule
ALTER TABLE `linkis_ps_configutation_lm_across_cluster_rule` ADD UNIQUE KEY `idx_creator_username` (`creator`, `username`);

-- linkis_ps_configuration_across_cluster_rule
ALTER TABLE `linkis_ps_configuration_across_cluster_rule` ADD UNIQUE KEY `idx_creator_username` (`creator`, `username`);

-- Job History Module Indexes
-- linkis_ps_job_history_group_history
ALTER TABLE `linkis_ps_job_history_group_history` ADD KEY `idx_created_time` (`created_time`);
ALTER TABLE `linkis_ps_job_history_group_history` ADD KEY `idx_submit_user` (`submit_user`);

-- linkis_ps_job_history_diagnosis
ALTER TABLE `linkis_ps_job_history_diagnosis` ADD UNIQUE KEY `job_history_id` (`job_history_id`);

-- Common Lock Module Indexes
-- linkis_ps_common_lock
ALTER TABLE `linkis_ps_common_lock` ADD UNIQUE KEY `uniq_lock_object` (`lock_object`);

-- UDF Module Indexes
-- linkis_ps_udf_tree
ALTER TABLE `linkis_ps_udf_tree` ADD UNIQUE KEY `uniq_parent_name_uname_category` (`parent`,`name`,`user_name`,`category`);

-- linkis_ps_udf_user_load
ALTER TABLE `linkis_ps_udf_user_load` ADD UNIQUE KEY `uniq_uid_uname` (`udf_id`, `user_name`);

-- Variable Module Indexes
-- linkis_ps_variable_key_user
ALTER TABLE `linkis_ps_variable_key_user` ADD UNIQUE KEY `uniq_aid_kid_uname` (`application_id`,`key_id`,`user_name`);
ALTER TABLE `linkis_ps_variable_key_user` ADD KEY `idx_key_id` (`key_id`);
ALTER TABLE `linkis_ps_variable_key_user` ADD KEY `idx_aid` (`application_id`);

-- linkis_ps_variable_key
ALTER TABLE `linkis_ps_variable_key` ADD KEY `idx_aid` (`application_id`);

-- DataSource Module Indexes
-- linkis_ps_datasource_table
ALTER TABLE `linkis_ps_datasource_table` ADD UNIQUE KEY `uniq_db_name` (`database`,`name`);

-- Context Service Module Indexes
-- linkis_ps_cs_context_map
ALTER TABLE `linkis_ps_cs_context_map` ADD UNIQUE KEY `uniq_key_cid_ctype` (`key`,`context_id`,`context_type`);
ALTER TABLE `linkis_ps_cs_context_map` ADD KEY `idx_keywords` (`keywords`(191));

-- linkis_ps_cs_context_history
ALTER TABLE `linkis_ps_cs_context_history` ADD KEY `idx_keyword` (`keyword`(191));

-- linkis_ps_cs_context_id
ALTER TABLE `linkis_ps_cs_context_id` ADD KEY `idx_instance` (`instance`);
ALTER TABLE `linkis_ps_cs_context_id` ADD KEY `idx_backup_instance` (`backup_instance`);
ALTER TABLE `linkis_ps_cs_context_id` ADD KEY `idx_instance_bin` (`instance`,`backup_instance`);

-- BML Module Indexes
-- linkis_ps_bml_resources
ALTER TABLE `linkis_ps_bml_resources` ADD UNIQUE KEY `uniq_rid_eflag` (`resource_id`, `enable_flag`);

-- linkis_ps_bml_resources_version
ALTER TABLE `linkis_ps_bml_resources_version` ADD UNIQUE KEY `uniq_rid_version` (`resource_id`, `version`);

-- linkis_ps_bml_resources_task
ALTER TABLE `linkis_ps_bml_resources_task` ADD UNIQUE KEY `uniq_rid_version` (`resource_id`, `version`);

-- linkis_ps_bml_project
ALTER TABLE `linkis_ps_bml_project` ADD UNIQUE KEY `uniq_name` (`name`);

-- linkis_ps_bml_project_user
ALTER TABLE `linkis_ps_bml_project_user` ADD UNIQUE KEY `uniq_name_pid` (`username`, `project_id`);

-- linkis_ps_bml_project_resource
ALTER TABLE `linkis_ps_bml_project_resource` ADD INDEX `idx_resource_id` (`resource_id`);

-- linkis_ps_bml_cleaned_resources_version
ALTER TABLE `linkis_ps_bml_cleaned_resources_version` ADD UNIQUE KEY `resource_id_version` (`resource_id`,`version`);

-- Instance Label Module Indexes
-- linkis_ps_instance_label
ALTER TABLE `linkis_ps_instance_label` ADD UNIQUE KEY `uniq_lk_lv` (`label_key`,`label_value`);

-- linkis_ps_instance_label_value_relation
ALTER TABLE `linkis_ps_instance_label_value_relation` ADD UNIQUE KEY `uniq_lvk_lid` (`label_value_key`,`label_id`);

-- linkis_ps_instance_label_relation
ALTER TABLE `linkis_ps_instance_label_relation` ADD UNIQUE KEY `uniq_lid_instance` (`label_id`,`service_instance`);

-- linkis_ps_instance_info
ALTER TABLE `linkis_ps_instance_info` ADD UNIQUE KEY `uniq_instance` (`instance`);

-- Error Code Module Indexes
-- linkis_ps_error_code
ALTER TABLE `linkis_ps_error_code` ADD UNIQUE INDEX `idx_error_regex` (`error_regex`(191));

-- Computation Governance Manager Module Indexes
-- linkis_cg_manager_service_instance
ALTER TABLE `linkis_cg_manager_service_instance` ADD UNIQUE KEY `uniq_instance` (`instance`);
ALTER TABLE `linkis_cg_manager_service_instance` ADD INDEX `idx_instance_name` (`instance`, `name`);

-- linkis_cg_manager_label
ALTER TABLE `linkis_cg_manager_label` ADD UNIQUE KEY `uniq_lk_lv` (`label_key`,`label_value`);

-- linkis_cg_manager_label_value_relation
ALTER TABLE `linkis_cg_manager_label_value_relation` ADD UNIQUE KEY `uniq_lvk_lid` (`label_value_key`,`label_id`);
ALTER TABLE `linkis_cg_manager_label_value_relation` ADD UNIQUE KEY `unlid_lvk_lvc` (`label_id`,`label_value_key`,`label_value_content`);

-- linkis_cg_manager_label_resource
ALTER TABLE `linkis_cg_manager_label_resource` ADD UNIQUE KEY `uniq_label_id` (`label_id`);

-- linkis_cg_ec_resource_info_record
ALTER TABLE `linkis_cg_ec_resource_info_record` ADD KEY `idx_ticket_id` (`ticket_id`);
ALTER TABLE `linkis_cg_ec_resource_info_record` ADD UNIQUE KEY `uniq_tid_lv` (`ticket_id`,`label_value`);
ALTER TABLE `linkis_cg_ec_resource_info_record` ADD UNIQUE KEY `uniq_sinstance_status_cuser_ctime` (`service_instance`, `status`, `create_user`, `create_time`);

-- linkis_cg_manager_label_service_instance
ALTER TABLE `linkis_cg_manager_label_service_instance` ADD KEY `idx_lid_instance` (`label_id`,`service_instance`);

-- linkis_cg_tenant_label_config
ALTER TABLE `linkis_cg_tenant_label_config` ADD UNIQUE KEY `uniq_user_creator` (`user`,`creator`);

-- linkis_cg_user_ip_config
ALTER TABLE `linkis_cg_user_ip_config` ADD UNIQUE KEY `uniq_user_creator` (`user`,`creator`);

-- linkis_cg_tenant_department_config
ALTER TABLE `linkis_cg_tenant_department_config` ADD UNIQUE KEY `uniq_creator_department` (`creator`,`department`);

-- DataSource Manager Module Indexes
-- linkis_ps_dm_datasource
ALTER TABLE `linkis_ps_dm_datasource` ADD UNIQUE INDEX `uniq_datasource_name` (`datasource_name`);

-- linkis_ps_dm_datasource_env
ALTER TABLE `linkis_ps_dm_datasource_env` ADD UNIQUE KEY `uniq_env_name` (`env_name`);
ALTER TABLE `linkis_ps_dm_datasource_env` ADD UNIQUE INDEX `uniq_name_dtid` (`env_name`, `datasource_type_id`);

-- linkis_ps_dm_datasource_type
ALTER TABLE `linkis_ps_dm_datasource_type` ADD UNIQUE INDEX `uniq_name` (`name`);

-- linkis_ps_dm_datasource_type_key
ALTER TABLE `linkis_ps_dm_datasource_type_key` ADD UNIQUE KEY `uniq_dstid_key` (`data_source_type_id`, `key`);

-- Gateway Module Indexes
-- linkis_mg_gateway_auth_token
ALTER TABLE `linkis_mg_gateway_auth_token` ADD UNIQUE KEY `uniq_token_name` (`token_name`);

-- linkis_mg_gateway_whitelist_config
ALTER TABLE `linkis_mg_gateway_whitelist_config` ADD UNIQUE KEY `address_uniq` (`allowed_user`, `client_address`);
ALTER TABLE `linkis_mg_gateway_whitelist_config` ADD KEY `linkis_mg_gateway_whitelist_config_allowed_user` (`allowed_user`);

-- linkis_mg_gateway_whitelist_sensitive_user
ALTER TABLE `linkis_mg_gateway_whitelist_sensitive_user` ADD UNIQUE KEY `sensitive_username` (`sensitive_username`);

-- linkis_mg_gateway_ecc_userinfo
ALTER TABLE `linkis_mg_gateway_ecc_userinfo` ADD UNIQUE INDEX `apply_itsm_id` (`apply_itsm_id`,`user_id`);
