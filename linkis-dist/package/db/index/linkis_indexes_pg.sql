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
-- Linkis Database Index Definitions (PostgreSQL)
-- ============================================
-- This file contains all index definitions extracted from linkis_ddl_pg.sql
-- Index naming conventions:
--   - Non-unique indexes: idx_fieldname[_fieldname]
--   - Unique indexes: uniq_fieldname[_fieldname]
-- ============================================

-- Configuration Module Indexes
-- linkis_ps_configuration_config_key
CREATE UNIQUE INDEX IF NOT EXISTS uniq_key_ectype ON linkis_ps_configuration_config_key USING btree ("key", engine_conn_type);

-- linkis_ps_configuration_key_engine_relation
CREATE UNIQUE INDEX IF NOT EXISTS uniq_kid_lid ON linkis_ps_configuration_key_engine_relation USING btree (config_key_id, engine_type_label_id);

-- linkis_ps_configuration_config_value
CREATE UNIQUE INDEX IF NOT EXISTS uniq_kid_lid_config_value ON linkis_ps_configuration_config_value USING btree (config_key_id, config_label_id);

-- linkis_ps_configuration_category
CREATE UNIQUE INDEX IF NOT EXISTS uniq_label_id_category ON linkis_ps_configuration_category USING btree (label_id);

-- linkis_ps_configuration_template_config_key
CREATE UNIQUE INDEX IF NOT EXISTS uniq_tid_kid_template ON linkis_ps_configuration_template_config_key USING btree (template_uuid, key_id);
CREATE UNIQUE INDEX IF NOT EXISTS uniq_tname_kid_template ON linkis_ps_configuration_template_config_key USING btree (template_name, key_id);

-- linkis_ps_configuration_key_limit_for_user
CREATE UNIQUE INDEX IF NOT EXISTS uniq_com_label_kid_limit ON linkis_ps_configuration_key_limit_for_user USING btree (combined_label_value, key_id);

-- linkis_ps_configutation_lm_across_cluster_rule
CREATE UNIQUE INDEX IF NOT EXISTS idx_creator_username_lm ON linkis_ps_configutation_lm_across_cluster_rule USING btree (creator, username);

-- linkis_ps_configuration_across_cluster_rule
CREATE UNIQUE INDEX IF NOT EXISTS idx_creator_username_config ON linkis_ps_configuration_across_cluster_rule USING btree (creator, username);

-- Job History Module Indexes
-- linkis_ps_job_history_group_history
CREATE INDEX IF NOT EXISTS idx_created_time ON linkis_ps_job_history_group_history USING btree (created_time);
CREATE INDEX IF NOT EXISTS idx_submit_user ON linkis_ps_job_history_group_history USING btree (submit_user);

-- linkis_ps_job_history_diagnosis
CREATE UNIQUE INDEX IF NOT EXISTS job_history_id ON linkis_ps_job_history_diagnosis USING btree (job_history_id);

-- Common Lock Module Indexes
-- linkis_ps_common_lock
CREATE UNIQUE INDEX IF NOT EXISTS uniq_lock_object ON linkis_ps_common_lock USING btree (lock_object);

-- UDF Module Indexes
-- linkis_ps_udf_tree
CREATE UNIQUE INDEX IF NOT EXISTS uniq_parent_name_uname_category ON linkis_ps_udf_tree USING btree (parent, "name", user_name, category);

-- linkis_ps_udf_user_load
CREATE UNIQUE INDEX IF NOT EXISTS uniq_uid_uname ON linkis_ps_udf_user_load USING btree (udf_id, user_name);

-- Variable Module Indexes
-- linkis_ps_variable_key_user
CREATE UNIQUE INDEX IF NOT EXISTS uniq_aid_kid_uname ON linkis_ps_variable_key_user USING btree (application_id, key_id, user_name);
CREATE INDEX IF NOT EXISTS idx_key_id_variable_user ON linkis_ps_variable_key_user USING btree (key_id);
CREATE INDEX IF NOT EXISTS idx_aid_variable_user ON linkis_ps_variable_key_user USING btree (application_id);

-- linkis_ps_variable_key
CREATE INDEX IF NOT EXISTS idx_aid_variable_key ON linkis_ps_variable_key USING btree (application_id);

-- DataSource Module Indexes
-- linkis_ps_datasource_table
CREATE UNIQUE INDEX IF NOT EXISTS uniq_db_name ON linkis_ps_datasource_table USING btree (database, "name");

-- Context Service Module Indexes
-- linkis_ps_cs_context_map
CREATE UNIQUE INDEX IF NOT EXISTS uniq_key_cid_ctype ON linkis_ps_cs_context_map USING btree ("key", context_id, context_type);
CREATE INDEX IF NOT EXISTS idx_keywords ON linkis_ps_cs_context_map USING btree (substring(keywords, 1, 191));

-- linkis_ps_cs_context_history
CREATE INDEX IF NOT EXISTS idx_keyword ON linkis_ps_cs_context_history USING btree (substring(keyword, 1, 191));

-- linkis_ps_cs_context_id
CREATE INDEX IF NOT EXISTS idx_instance ON linkis_ps_cs_context_id USING btree (instance);
CREATE INDEX IF NOT EXISTS idx_backup_instance ON linkis_ps_cs_context_id USING btree (backup_instance);
CREATE INDEX IF NOT EXISTS idx_instance_bin ON linkis_ps_cs_context_id USING btree (instance, backup_instance);

-- BML Module Indexes
-- linkis_ps_bml_resources
CREATE UNIQUE INDEX IF NOT EXISTS uniq_rid_eflag ON linkis_ps_bml_resources USING btree (resource_id, enable_flag);

-- linkis_ps_bml_resources_version
CREATE UNIQUE INDEX IF NOT EXISTS uniq_rid_version_bml ON linkis_ps_bml_resources_version USING btree (resource_id, version);

-- linkis_ps_bml_resources_task
CREATE UNIQUE INDEX IF NOT EXISTS uniq_rid_version_task ON linkis_ps_bml_resources_task USING btree (resource_id, version);

-- linkis_ps_bml_project
CREATE UNIQUE INDEX IF NOT EXISTS uniq_name_project ON linkis_ps_bml_project USING btree ("name");

-- linkis_ps_bml_project_user
CREATE UNIQUE INDEX IF NOT EXISTS uniq_name_pid ON linkis_ps_bml_project_user USING btree (username, project_id);

-- linkis_ps_bml_project_resource
CREATE INDEX IF NOT EXISTS idx_resource_id ON linkis_ps_bml_project_resource USING btree (resource_id);

-- linkis_ps_bml_cleaned_resources_version
CREATE UNIQUE INDEX IF NOT EXISTS resource_id_version ON linkis_ps_bml_cleaned_resources_version USING btree (resource_id, version);

-- Instance Label Module Indexes
-- linkis_ps_instance_label
CREATE UNIQUE INDEX IF NOT EXISTS uniq_lk_lv_instance ON linkis_ps_instance_label USING btree (label_key, label_value);

-- linkis_ps_instance_label_value_relation
CREATE UNIQUE INDEX IF NOT EXISTS uniq_lvk_lid_instance ON linkis_ps_instance_label_value_relation USING btree (label_value_key, label_id);

-- linkis_ps_instance_label_relation
CREATE UNIQUE INDEX IF NOT EXISTS uniq_lid_instance ON linkis_ps_instance_label_relation USING btree (label_id, service_instance);

-- linkis_ps_instance_info
CREATE UNIQUE INDEX IF NOT EXISTS uniq_instance_info ON linkis_ps_instance_info USING btree (instance);

-- Error Code Module Indexes
-- linkis_ps_error_code
CREATE UNIQUE INDEX IF NOT EXISTS idx_error_regex ON linkis_ps_error_code USING btree (substring(error_regex, 1, 191));

-- Computation Governance Manager Module Indexes
-- linkis_cg_manager_service_instance
CREATE UNIQUE INDEX IF NOT EXISTS uniq_instance_manager ON linkis_cg_manager_service_instance USING btree (instance);
CREATE INDEX IF NOT EXISTS idx_instance_name ON linkis_cg_manager_service_instance USING btree (instance, name);

-- linkis_cg_manager_label
CREATE UNIQUE INDEX IF NOT EXISTS uniq_lk_lv_manager ON linkis_cg_manager_label USING btree (label_key, label_value);

-- linkis_cg_manager_label_value_relation
CREATE UNIQUE INDEX IF NOT EXISTS uniq_lvk_lid_manager ON linkis_cg_manager_label_value_relation USING btree (label_value_key, label_id);
CREATE UNIQUE INDEX IF NOT EXISTS unlid_lvk_lvc ON linkis_cg_manager_label_value_relation USING btree (label_id, label_value_key, label_value_content);

-- linkis_cg_manager_label_resource
CREATE UNIQUE INDEX IF NOT EXISTS uniq_label_id_resource ON linkis_cg_manager_label_resource USING btree (label_id);

-- linkis_cg_ec_resource_info_record
CREATE INDEX IF NOT EXISTS idx_ticket_id ON linkis_cg_ec_resource_info_record USING btree (ticket_id);
CREATE UNIQUE INDEX IF NOT EXISTS uniq_tid_lv ON linkis_cg_ec_resource_info_record USING btree (ticket_id, label_value);
CREATE UNIQUE INDEX IF NOT EXISTS uniq_sinstance_status_cuser_ctime ON linkis_cg_ec_resource_info_record USING btree (service_instance, status, create_user, create_time);

-- linkis_cg_manager_label_service_instance
CREATE INDEX IF NOT EXISTS idx_lid_instance ON linkis_cg_manager_label_service_instance USING btree (label_id, service_instance);

-- linkis_cg_tenant_label_config
CREATE UNIQUE INDEX IF NOT EXISTS uniq_user_creator_tenant ON linkis_cg_tenant_label_config USING btree ("user", creator);

-- linkis_cg_user_ip_config
CREATE UNIQUE INDEX IF NOT EXISTS uniq_user_creator_ip ON linkis_cg_user_ip_config USING btree ("user", creator);

-- linkis_cg_tenant_department_config
CREATE UNIQUE INDEX IF NOT EXISTS uniq_creator_department ON linkis_cg_tenant_department_config USING btree (creator, department);

-- DataSource Manager Module Indexes
-- linkis_ps_dm_datasource
CREATE UNIQUE INDEX IF NOT EXISTS uniq_datasource_name ON linkis_ps_dm_datasource USING btree (datasource_name);

-- linkis_ps_dm_datasource_env
CREATE UNIQUE INDEX IF NOT EXISTS uniq_env_name ON linkis_ps_dm_datasource_env USING btree (env_name);
CREATE UNIQUE INDEX IF NOT EXISTS uniq_name_dtid ON linkis_ps_dm_datasource_env USING btree (env_name, datasource_type_id);

-- linkis_ps_dm_datasource_type
CREATE UNIQUE INDEX IF NOT EXISTS uniq_name_datasource_type ON linkis_ps_dm_datasource_type USING btree ("name");

-- linkis_ps_dm_datasource_type_key
CREATE UNIQUE INDEX IF NOT EXISTS uniq_dstid_key ON linkis_ps_dm_datasource_type_key USING btree (data_source_type_id, "key");

-- Gateway Module Indexes
-- linkis_mg_gateway_auth_token
CREATE UNIQUE INDEX IF NOT EXISTS uniq_token_name ON linkis_mg_gateway_auth_token USING btree (token_name);

-- linkis_mg_gateway_whitelist_config
CREATE UNIQUE INDEX IF NOT EXISTS address_uniq ON linkis_mg_gateway_whitelist_config USING btree (allowed_user, client_address);
CREATE INDEX IF NOT EXISTS linkis_mg_gateway_whitelist_config_allowed_user ON linkis_mg_gateway_whitelist_config USING btree (allowed_user);

-- linkis_mg_gateway_whitelist_sensitive_user
CREATE UNIQUE INDEX IF NOT EXISTS sensitive_username ON linkis_mg_gateway_whitelist_sensitive_user USING btree (sensitive_username);

-- linkis_mg_gateway_ecc_userinfo
CREATE UNIQUE INDEX IF NOT EXISTS apply_itsm_id ON linkis_mg_gateway_ecc_userinfo USING btree (apply_itsm_id, user_id);
