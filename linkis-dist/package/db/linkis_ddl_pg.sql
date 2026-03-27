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


-- Non-unique indexes are named according to "idx_fieldname[_fieldname]". For example idx_age_name
-- The unique index is named according to "uniq_field name[_field name]". For example uniq_age_name
-- It is recommended to include all field names for composite indexes, and the long field names can be abbreviated. For example idx_age_name_add
-- The index name should not exceed 50 characters, and the name should be lowercase
--
-- 非唯一索引按照“idx_字段名称[_字段名称]”进用行命名。例如idx_age_name
-- 唯一索引按照“uniq_字段名称[_字段名称]”进用行命名。例如uniq_age_name
-- 组合索引建议包含所有字段名,过长的字段名可以采用缩写形式。例如idx_age_name_add
-- 索引名尽量不超过50个字符，命名应该使用小写


-- 注意事项
-- 1. TDSQL层面做了硬性规定，对于varchar索引，字段总长度不能超过768个字节，建议组合索引的列的长度根据实际列数值的长度定义，比如身份证号定义长度为varchar(20)，不要定位为varchar(100)，
--   同时，由于TDSQL默认采用UTF8字符集，一个字符3个字节，因此，实际索引所包含的列的长度要小于768/3=256字符长度。
-- 2. AOMP 执行sql 语句 create table 可以带反撇号，alter 语句不能带反撇号
-- 3. 使用 alter 添加、修改字段时请带要字符集和排序规则 CHARSET utf8mb4 COLLATE utf8mb4_bin

DROP TABLE IF EXISTS "linkis_ps_configuration_config_key";
CREATE TABLE linkis_ps_configuration_config_key (
  id bigserial NOT NULL,
  "key" varchar(50)  DEFAULT NULL,
  description varchar(200) DEFAULT NULL,
  "name" varchar(50)  DEFAULT NULL,
  default_value varchar(200) DEFAULT NULL,
  validate_type varchar(50)  DEFAULT NULL,
  validate_range varchar(150)  DEFAULT NULL,
  engine_conn_type varchar(50)  DEFAULT '',
  is_hidden boolean   DEFAULT NULL,
  is_advanced boolean   DEFAULT NULL,
  "level" smallint   DEFAULT NULL,
  treeName varchar(20)  DEFAULT NULL,
  boundary_type smallint NOT NULL DEFAULT '0',
  en_description varchar(200) DEFAULT NULL,
  en_name varchar(100) DEFAULT NULL,
  en_treeName varchar(100) DEFAULT NULL,
  template_required boolean DEFAULT false,
  CONSTRAINT linkis_ps_configuration_config_key_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_key_ectype ON linkis_ps_configuration_config_key USING btree ("key", engine_conn_type);
COMMENT ON COLUMN linkis_ps_configuration_config_key."key" IS 'Set key, e.g. spark.executor.instances';
COMMENT ON COLUMN linkis_ps_configuration_config_key.default_value IS 'Adopted when user does not set key';
COMMENT ON COLUMN linkis_ps_configuration_config_key.validate_type IS 'Validate type, one of the following: None, NumInterval, FloatInterval, Include, Regex, OPF, Custom Rules';
COMMENT ON COLUMN linkis_ps_configuration_config_key.validate_range IS 'Validate range';
COMMENT ON COLUMN linkis_ps_configuration_config_key.engine_conn_type IS 'engine type,such as spark,hive etc';
COMMENT ON COLUMN linkis_ps_configuration_config_key.is_hidden IS 'Whether it is hidden from user. If set to 1(true), then user cannot modify, however, it could still be used in back-end';
COMMENT ON COLUMN linkis_ps_configuration_config_key.is_advanced IS 'Whether it is an advanced parameter. If set to 1(true), parameters would be displayed only when user choose to do so';
COMMENT ON COLUMN linkis_ps_configuration_config_key."level" IS 'Basis for displaying sorting in the front-end. Higher the level is, higher the rank the parameter gets';
COMMENT ON COLUMN linkis_ps_configuration_config_key.treeName IS 'Reserved field, representing the subdirectory of engineType';
COMMENT ON COLUMN linkis_ps_configuration_config_key.boundary_type IS '0  none/ 1 with mix /2 with max / 3 min and max both';
COMMENT ON COLUMN linkis_ps_configuration_config_key.en_description IS 'english description';
COMMENT ON COLUMN linkis_ps_configuration_config_key.en_name IS 'english name';
COMMENT ON COLUMN linkis_ps_configuration_config_key.en_treeName IS 'english treeName';
COMMENT ON COLUMN linkis_ps_configuration_config_key.template_required IS 'template required 0 none / 1 must';
DROP TABLE IF EXISTS "linkis_ps_configuration_key_engine_relation";
CREATE TABLE linkis_ps_configuration_key_engine_relation (
  id bigserial NOT NULL,
  config_key_id bigint NOT NULL,
  engine_type_label_id bigint NOT NULL,
  CONSTRAINT linkis_ps_configuration_key_engine_relation_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_kid_lid ON linkis_ps_configuration_key_engine_relation USING btree (config_key_id, engine_type_label_id);
COMMENT ON COLUMN linkis_ps_configuration_key_engine_relation.config_key_id IS 'config key id';
COMMENT ON COLUMN linkis_ps_configuration_key_engine_relation.engine_type_label_id IS 'engine label id';
DROP TABLE IF EXISTS "linkis_ps_configuration_config_value";
CREATE TABLE linkis_ps_configuration_config_value (
  id bigserial NOT NULL,
  config_key_id bigint,
  config_value varchar(500),
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_ps_configuration_config_value_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_kid_lid ON linkis_ps_configuration_config_value USING btree (config_key_id, config_label_id);
DROP TABLE IF EXISTS "linkis_ps_configuration_category";
CREATE TABLE linkis_ps_configuration_category (
  id serial NOT NULL,
  label_id integer NOT NULL,
  "level" integer NOT NULL,
  description varchar(200),
  tag varchar(200),
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_ps_configuration_category_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_label_id ON linkis_ps_configuration_category USING btree (label_id);
DROP TABLE IF EXISTS "linkis_ps_configuration_template_config_key";
CREATE TABLE linkis_ps_configuration_template_config_key (
  id bigserial NOT NULL,
  template_name VARCHAR(200) NOT NULL,
  template_uuid VARCHAR(36) NOT NULL,
  key_id bigint NOT NULL,
  config_value VARCHAR(200) NULL DEFAULT NULL,
  max_value VARCHAR(50) NULL DEFAULT NULL,
  min_value VARCHAR(50) NULL DEFAULT NULL,
  validate_range VARCHAR(50) NULL DEFAULT NULL,
  is_valid VARCHAR(2) DEFAULT 'Y',
  create_by VARCHAR(50) NOT NULL,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(50) NULL DEFAULT NULL,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_ps_configuration_template_config_key_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_tid_kid ON linkis_ps_configuration_template_config_key USING btree (template_uuid, key_id);
CREATE UNIQUE INDEX uniq_tname_kid ON linkis_ps_configuration_template_config_key USING btree (template_uuid, key_id);
COMMENT ON COLUMN linkis_ps_configuration_template_config_key.template_name IS 'Configuration template name redundant storage';
COMMENT ON COLUMN linkis_ps_configuration_template_config_key.template_uuid IS 'uuid template id recorded by the third party';
COMMENT ON COLUMN linkis_ps_configuration_template_config_key.key_id IS 'id of linkis_ps_configuration_config_key';
COMMENT ON COLUMN linkis_ps_configuration_template_config_key.config_value IS 'configuration value';
COMMENT ON COLUMN linkis_ps_configuration_template_config_key.max_value IS 'upper limit value';
COMMENT ON COLUMN linkis_ps_configuration_template_config_key.min_value IS 'Lower limit value (reserved)';
COMMENT ON COLUMN linkis_ps_configuration_template_config_key.validate_range IS 'Verification regularity (reserved)';
COMMENT ON COLUMN linkis_ps_configuration_template_config_key.is_valid IS 'Is it valid? Reserved Y/N';
COMMENT ON COLUMN linkis_ps_configuration_template_config_key.create_by IS 'Creator';
COMMENT ON COLUMN linkis_ps_configuration_template_config_key.create_time IS 'create time';
COMMENT ON COLUMN linkis_ps_configuration_template_config_key.update_by IS 'Update by';
COMMENT ON COLUMN linkis_ps_configuration_template_config_key.update_time IS 'update time';
DROP TABLE IF EXISTS "linkis_ps_configuration_key_limit_for_user";
CREATE TABLE linkis_ps_configuration_key_limit_for_user (
  id bigserial NOT NULL,
  user_name VARCHAR(50) NOT NULL,
  combined_label_value VARCHAR(128) NOT NULL,
  key_id bigint NOT NULL,
  config_value VARCHAR(200) NULL DEFAULT NULL,
  max_value VARCHAR(50) NULL DEFAULT NULL,
  min_value VARCHAR(50) NULL DEFAULT NULL,
  latest_update_template_uuid VARCHAR(36) NOT NULL,
  is_valid VARCHAR(2) DEFAULT 'Y',
  create_by VARCHAR(50) NOT NULL,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(50) NULL DEFAULT NULL,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_ps_configuration_key_limit_for_user_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_com_label_kid ON linkis_ps_configuration_key_limit_for_user USING btree (combined_label_value, key_id);
COMMENT ON COLUMN linkis_ps_configuration_key_limit_for_user.user_name IS 'username';
COMMENT ON COLUMN linkis_ps_configuration_key_limit_for_user.combined_label_value IS 'Combined label combined_userCreator_engineType such as hadoop-IDE,spark-2.4.3';
COMMENT ON COLUMN linkis_ps_configuration_key_limit_for_user.key_id IS 'id of linkis_ps_configuration_config_key';
COMMENT ON COLUMN linkis_ps_configuration_key_limit_for_user.config_value IS 'configuration value';
COMMENT ON COLUMN linkis_ps_configuration_key_limit_for_user.max_value IS 'upper limit value';
COMMENT ON COLUMN linkis_ps_configuration_key_limit_for_user.min_value IS 'Lower limit value (reserved)';
COMMENT ON COLUMN linkis_ps_configuration_key_limit_for_user.latest_update_template_uuid IS 'uuid template id recorded by the third party';
COMMENT ON COLUMN linkis_ps_configuration_key_limit_for_user.is_valid IS 'Is it valid? Reserved Y/N';
COMMENT ON COLUMN linkis_ps_configuration_key_limit_for_user.create_by IS 'Creator';
COMMENT ON COLUMN linkis_ps_configuration_key_limit_for_user.create_time IS 'create time';
COMMENT ON COLUMN linkis_ps_configuration_key_limit_for_user.update_by IS 'Update by';
COMMENT ON COLUMN linkis_ps_configuration_key_limit_for_user.update_time IS 'update time';
DROP TABLE IF EXISTS "linkis_ps_configutation_lm_across_cluster_rule";
CREATE TABLE linkis_ps_configutation_lm_across_cluster_rule (
  id INT AUTO_INCREMENT,
  cluster_name char(32) NOT NULL,
  creator char(32) NOT NULL,
  username char(32) NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_by char(32) NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by char(32) NOT NULL,
  rules varchar(256) NOT NULL,
  is_valid VARCHAR(2) DEFAULT 'N',
  CONSTRAINT linkis_ps_configutation_lm_across_cluster_rule_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_creator_username ON linkis_ps_configutation_lm_across_cluster_rule USING btree (creator, username);
COMMENT ON COLUMN linkis_ps_configutation_lm_across_cluster_rule.id IS 'Rule ID, auto-increment primary key';
COMMENT ON COLUMN linkis_ps_configutation_lm_across_cluster_rule.cluster_name IS 'Cluster name, cannot be empty';
COMMENT ON COLUMN linkis_ps_configutation_lm_across_cluster_rule.creator IS 'Creator, cannot be empty';
COMMENT ON COLUMN linkis_ps_configutation_lm_across_cluster_rule.username IS 'User, cannot be empty';
COMMENT ON COLUMN linkis_ps_configutation_lm_across_cluster_rule.create_time IS 'Creation time, cannot be empty';
COMMENT ON COLUMN linkis_ps_configutation_lm_across_cluster_rule.create_by IS 'Creator, cannot be empty';
COMMENT ON COLUMN linkis_ps_configutation_lm_across_cluster_rule.update_time IS 'Modification time, cannot be empty';
COMMENT ON COLUMN linkis_ps_configutation_lm_across_cluster_rule.update_by IS 'Updater, cannot be empty';
COMMENT ON COLUMN linkis_ps_configutation_lm_across_cluster_rule.rules IS 'Rule content, cannot be empty';
COMMENT ON COLUMN linkis_ps_configutation_lm_across_cluster_rule.is_valid IS 'Is it valid Y/N';
DROP TABLE IF EXISTS "linkis_ps_job_history_group_history";
CREATE TABLE linkis_ps_job_history_group_history (
  id bigserial NOT NULL,
  job_req_id varchar(64) DEFAULT NULL,
  submit_user varchar(50) DEFAULT NULL,
  execute_user varchar(50) DEFAULT NULL,
  "source" text DEFAULT NULL,
  labels text DEFAULT NULL,
  params text DEFAULT NULL,
  progress varchar(32) DEFAULT NULL,
  status varchar(50) DEFAULT NULL,
  log_path varchar(200) DEFAULT NULL,
  error_code int DEFAULT NULL,
  error_desc varchar(1000) DEFAULT NULL,
  created_time timestamp(3) DEFAULT CURRENT_TIMESTAMP,
  updated_time timestamp(3) DEFAULT CURRENT_TIMESTAMP,
  instances varchar(250) DEFAULT NULL,
  metrics text DEFAULT NULL,
  engine_type varchar(32) DEFAULT NULL,
  execution_code text DEFAULT NULL,
  result_location varchar(500) DEFAULT NULL,
  observe_info varchar(500) DEFAULT NULL,
  CONSTRAINT linkis_ps_job_history_group_history_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_created_time ON linkis_ps_job_history_group_history USING btree (created_time);
CREATE INDEX idx_submit_user ON linkis_ps_job_history_group_history USING btree (submit_user);
COMMENT ON COLUMN linkis_ps_job_history_group_history.id IS 'Primary Key, auto increment';
COMMENT ON COLUMN linkis_ps_job_history_group_history.job_req_id IS 'job execId';
COMMENT ON COLUMN linkis_ps_job_history_group_history.submit_user IS 'who submitted this Job';
COMMENT ON COLUMN linkis_ps_job_history_group_history.execute_user IS 'who actually executed this Job';
COMMENT ON COLUMN linkis_ps_job_history_group_history."source" IS 'job source';
COMMENT ON COLUMN linkis_ps_job_history_group_history.labels IS 'job labels';
COMMENT ON COLUMN linkis_ps_job_history_group_history.params IS 'job params';
COMMENT ON COLUMN linkis_ps_job_history_group_history.progress IS 'Job execution progress';
COMMENT ON COLUMN linkis_ps_job_history_group_history.status IS 'Script execution status, must be one of the following: Inited, WaitForRetry, Scheduled, Running, Succeed, Failed, Cancelled, Timeout';
COMMENT ON COLUMN linkis_ps_job_history_group_history.log_path IS 'File path of the job log';
COMMENT ON COLUMN linkis_ps_job_history_group_history.error_code IS 'Error code. Generated when the execution of the script fails';
COMMENT ON COLUMN linkis_ps_job_history_group_history.error_desc IS 'Execution description. Generated when the execution of script fails';
COMMENT ON COLUMN linkis_ps_job_history_group_history.created_time IS 'Creation time';
COMMENT ON COLUMN linkis_ps_job_history_group_history.updated_time IS 'Update time';
COMMENT ON COLUMN linkis_ps_job_history_group_history.instances IS 'Entrance instances';
COMMENT ON COLUMN linkis_ps_job_history_group_history.metrics IS 'Job Metrics';
COMMENT ON COLUMN linkis_ps_job_history_group_history.engine_type IS 'Engine type';
COMMENT ON COLUMN linkis_ps_job_history_group_history.execution_code IS 'Job origin code or code path';
COMMENT ON COLUMN linkis_ps_job_history_group_history.result_location IS 'File path of the resultsets';
COMMENT ON COLUMN linkis_ps_job_history_group_history.observe_info IS 'The notification information configuration of this job';
DROP TABLE IF EXISTS "linkis_ps_job_history_detail";
CREATE TABLE linkis_ps_job_history_detail (
  id bigserial NOT NULL,
  job_history_id bigint NOT NULL,
  result_location varchar(500) DEFAULT NULL,
  execution_content text DEFAULT NULL,
  result_array_size smallint DEFAULT 0,
  job_group_info text DEFAULT NULL,
  created_time timestamp(3) DEFAULT CURRENT_TIMESTAMP,
  updated_time timestamp(3) DEFAULT CURRENT_TIMESTAMP,
  status varchar(32) DEFAULT NULL,
  priority smallint DEFAULT 0,
  CONSTRAINT linkis_ps_job_history_detail_pkey PRIMARY KEY (id)
);
COMMENT ON COLUMN linkis_ps_job_history_detail.id IS 'Primary Key, auto increment';
COMMENT ON COLUMN linkis_ps_job_history_detail.job_history_id IS 'ID of JobHistory';
COMMENT ON COLUMN linkis_ps_job_history_detail.result_location IS 'File path of the resultsets';
COMMENT ON COLUMN linkis_ps_job_history_detail.execution_content IS 'The script code or other execution content executed by this Job';
COMMENT ON COLUMN linkis_ps_job_history_detail.result_array_size IS 'size of result array';
COMMENT ON COLUMN linkis_ps_job_history_detail.job_group_info IS 'Job group info/path';
COMMENT ON COLUMN linkis_ps_job_history_detail.created_time IS 'Creation time';
COMMENT ON COLUMN linkis_ps_job_history_detail.updated_time IS 'Update time';
COMMENT ON COLUMN linkis_ps_job_history_detail.status IS 'status';
COMMENT ON COLUMN linkis_ps_job_history_detail.priority IS 'order of subjob';
DROP TABLE IF EXISTS "linkis_ps_common_lock";
CREATE TABLE linkis_ps_common_lock (
  id serial NOT NULL,
  lock_object varchar(255) DEFAULT NULL,
  locker VARCHAR(255) DEFAULT NULL,
  time_out text,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_ps_common_lock_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_lock_object ON linkis_ps_common_lock USING btree (lock_object);
COMMENT ON COLUMN linkis_ps_common_lock.locker IS 'locker';
DROP TABLE IF EXISTS "linkis_ps_udf_manager";
CREATE TABLE linkis_ps_udf_manager (
  id bigserial NOT NULL,
  user_name varchar(20) DEFAULT NULL,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_ps_udf_manager_pkey PRIMARY KEY (id)
);
DROP TABLE IF EXISTS "linkis_ps_udf_shared_group";
CREATE TABLE linkis_ps_udf_shared_group (
  id bigserial NOT NULL,
  udf_id bigint NOT NULL,
  shared_group varchar(50) NOT NULL,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_ps_udf_shared_group_pkey PRIMARY KEY (id)
);
DROP TABLE IF EXISTS "linkis_ps_udf_shared_info";
CREATE TABLE linkis_ps_udf_shared_info (
  id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
  udf_id bigint NOT NULL,
  user_name varchar(50) NOT NULL,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP
);
DROP TABLE IF EXISTS "linkis_ps_udf_tree";
CREATE TABLE linkis_ps_udf_tree (
  id bigserial NOT NULL,
  parent bigint NOT NULL,
  "name" varchar(50) DEFAULT NULL,
  user_name varchar(50) NOT NULL,
  description varchar(255) DEFAULT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  category varchar(50) DEFAULT NULL,
  CONSTRAINT linkis_ps_udf_tree_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_parent_name_uname_category ON linkis_ps_udf_tree USING btree (parent, "name", user_name, category);
COMMENT ON COLUMN linkis_ps_udf_tree."name" IS 'Category name of the function. It would be displayed in the front-end';
COMMENT ON COLUMN linkis_ps_udf_tree.category IS 'Used to distinguish between udf and function';
DROP TABLE IF EXISTS "linkis_ps_udf_user_load";
CREATE TABLE linkis_ps_udf_user_load (
  id bigserial NOT NULL,
  udf_id bigint NOT NULL,
  user_name varchar(50) NOT NULL,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_ps_udf_user_load_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_uid_uname ON linkis_ps_udf_user_load USING btree (udf_id, user_name);
DROP TABLE IF EXISTS "linkis_ps_udf_baseinfo";
CREATE TABLE linkis_ps_udf_baseinfo (
  id bigserial NOT NULL,
  create_user varchar(50) NOT NULL,
  udf_name varchar(255) NOT NULL,
  udf_type integer DEFAULT '0',
  tree_id bigint NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  sys varchar(255) NOT NULL DEFAULT 'ide',
  cluster_name varchar(255) NOT NULL,
  is_expire boolean DEFAULT NULL,
  is_shared boolean DEFAULT NULL,
  CONSTRAINT linkis_ps_udf_baseinfo_pkey PRIMARY KEY (id)
);
COMMENT ON COLUMN linkis_ps_udf_baseinfo.sys IS 'source system';
DROP TABLE IF EXISTS "linkis_ps_udf_version";
CREATE TABLE linkis_ps_udf_version (
  id bigserial NOT NULL,
  udf_id bigint NOT NULL,
  path varchar(255) NOT NULL,
  bml_resource_id varchar(50) NOT NULL,
  bml_resource_version varchar(20) NOT NULL,
  is_published boolean DEFAULT NULL,
  register_format varchar(255) DEFAULT NULL,
  use_format varchar(255) DEFAULT NULL,
  description varchar(255) NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  md5 varchar(100) DEFAULT NULL,
  CONSTRAINT linkis_ps_udf_version_pkey PRIMARY KEY (id)
);
COMMENT ON COLUMN linkis_ps_udf_version.path IS 'Source path for uploading files';
COMMENT ON COLUMN linkis_ps_udf_version.is_published IS 'is published';
COMMENT ON COLUMN linkis_ps_udf_version.description IS 'version desc';
DROP TABLE IF EXISTS "linkis_ps_variable_key_user";
CREATE TABLE linkis_ps_variable_key_user (
  id bigserial NOT NULL,
  application_id bigint DEFAULT NULL,
  key_id bigint DEFAULT NULL,
  user_name varchar(50) DEFAULT NULL,
  value varchar(200) DEFAULT NULL,
  CONSTRAINT linkis_ps_variable_key_user_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_aid_kid_uname ON linkis_ps_variable_key_user USING btree (application_id, key_id, user_name);
CREATE INDEX idx_key_id ON linkis_ps_variable_key_user USING btree (key_id);
CREATE INDEX idx_aid ON linkis_ps_variable_key_user USING btree (application_id);
COMMENT ON COLUMN linkis_ps_variable_key_user.application_id IS 'Reserved word';
COMMENT ON COLUMN linkis_ps_variable_key_user.value IS 'Value of the global variable';
DROP TABLE IF EXISTS "linkis_ps_variable_key";
CREATE TABLE linkis_ps_variable_key (
  id bigserial NOT NULL,
  "key" varchar(50) DEFAULT NULL,
  description varchar(200) DEFAULT NULL,
  "name" varchar(50) DEFAULT NULL,
  application_id bigint DEFAULT NULL,
  default_value varchar(200) DEFAULT NULL,
  value_type varchar(50) DEFAULT NULL,
  value_regex varchar(100) DEFAULT NULL,
  CONSTRAINT linkis_ps_variable_key_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_aid ON linkis_ps_variable_key USING btree (application_id);
COMMENT ON COLUMN linkis_ps_variable_key."key" IS 'Key of the global variable';
COMMENT ON COLUMN linkis_ps_variable_key.description IS 'Reserved word';
COMMENT ON COLUMN linkis_ps_variable_key."name" IS 'Reserved word';
COMMENT ON COLUMN linkis_ps_variable_key.application_id IS 'Reserved word';
COMMENT ON COLUMN linkis_ps_variable_key.default_value IS 'Reserved word';
COMMENT ON COLUMN linkis_ps_variable_key.value_type IS 'Reserved word';
COMMENT ON COLUMN linkis_ps_variable_key.value_regex IS 'Reserved word';
DROP TABLE IF EXISTS "linkis_ps_datasource_access";
CREATE TABLE linkis_ps_datasource_access (
  id bigserial NOT NULL,
  table_id bigint NOT NULL,
  visitor varchar(16) NOT NULL,
  fields varchar(255) DEFAULT NULL,
  application_id smallint NOT NULL,
  access_time timestamp NOT NULL,
  CONSTRAINT linkis_ps_datasource_access_pkey PRIMARY KEY (id)
);
DROP TABLE IF EXISTS "linkis_ps_datasource_field";
CREATE TABLE linkis_ps_datasource_field (
  id bigserial NOT NULL,
  table_id bigint NOT NULL,
  "name" varchar(64) NOT NULL,
  alias varchar(64) DEFAULT NULL,
  "type" varchar(64) NOT NULL,
  comment varchar(255) DEFAULT NULL,
  express varchar(255) DEFAULT NULL,
  rule varchar(128) DEFAULT NULL,
  is_partition_field boolean NOT NULL,
  is_primary boolean NOT NULL,
  length integer DEFAULT NULL,
  mode_info varchar(128) DEFAULT NULL,
  CONSTRAINT linkis_ps_datasource_field_pkey PRIMARY KEY (id)
);
DROP TABLE IF EXISTS "linkis_ps_datasource_import";
CREATE TABLE linkis_ps_datasource_import (
  id bigserial NOT NULL,
  table_id bigint NOT NULL,
  import_type smallint NOT NULL,
  args varchar(255) NOT NULL,
  CONSTRAINT linkis_ps_datasource_import_pkey PRIMARY KEY (id)
);
DROP TABLE IF EXISTS "linkis_ps_datasource_lineage";
CREATE TABLE linkis_ps_datasource_lineage (
  id bigserial NOT NULL,
  table_id bigint DEFAULT NULL,
  source_table varchar(64) DEFAULT NULL,
  update_time timestamp DEFAULT NULL,
  CONSTRAINT linkis_ps_datasource_lineage_pkey PRIMARY KEY (id)
);
DROP TABLE IF EXISTS "linkis_ps_datasource_table";
CREATE TABLE linkis_ps_datasource_table (
  id bigserial NOT NULL,
  database varchar(64) NOT NULL,
  "name" varchar(64) NOT NULL,
  alias varchar(64) DEFAULT NULL,
  creator varchar(16) NOT NULL,
  comment varchar(255) DEFAULT NULL,
  create_time timestamp NOT NULL,
  product_name varchar(64) DEFAULT NULL,
  project_name varchar(255) DEFAULT NULL,
  usage varchar(128) DEFAULT NULL,
  lifecycle smallint NOT NULL,
  use_way smallint NOT NULL,
  is_import boolean NOT NULL,
  model_level smallint NOT NULL,
  is_external_use boolean NOT NULL,
  is_partition_table boolean NOT NULL,
  is_available boolean NOT NULL,
  CONSTRAINT linkis_ps_datasource_table_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_db_name ON linkis_ps_datasource_table USING btree (database, "name");
DROP TABLE IF EXISTS "linkis_ps_datasource_table_info";
CREATE TABLE linkis_ps_datasource_table_info (
  id bigserial NOT NULL,
  table_id bigint NOT NULL,
  table_last_update_time timestamp NOT NULL,
  row_num bigint NOT NULL,
  file_num integer NOT NULL,
  table_size varchar(32) NOT NULL,
  partitions_num integer NOT NULL,
  update_time timestamp NOT NULL,
  field_num integer NOT NULL,
  CONSTRAINT linkis_ps_datasource_table_info_pkey PRIMARY KEY (id)
);
DROP TABLE IF EXISTS "linkis_ps_cs_context_map";
CREATE TABLE linkis_ps_cs_context_map (
  id serial NOT NULL,
  "key" varchar(128) DEFAULT NULL,
  context_scope varchar(32) DEFAULT NULL,
  context_type varchar(32) DEFAULT NULL,
  props text,
  value text,
  context_id integer DEFAULT NULL,
  keywords varchar(255) DEFAULT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  access_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_ps_cs_context_map_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_key_cid_ctype ON linkis_ps_cs_context_map USING btree ("key", context_id, context_type);
CREATE INDEX idx_keywords ON linkis_ps_cs_context_map USING btree (keywords(191);
COMMENT ON COLUMN linkis_ps_cs_context_map.update_time IS 'update unix timestamp';
COMMENT ON COLUMN linkis_ps_cs_context_map.create_time IS 'create time';
COMMENT ON COLUMN linkis_ps_cs_context_map.access_time IS 'last access time';
DROP TABLE IF EXISTS "linkis_ps_cs_context_map_listener";
CREATE TABLE linkis_ps_cs_context_map_listener (
  id serial NOT NULL,
  listener_source varchar(255) DEFAULT NULL,
  key_id integer DEFAULT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  access_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_ps_cs_context_map_listener_pkey PRIMARY KEY (id)
);
COMMENT ON COLUMN linkis_ps_cs_context_map_listener.update_time IS 'update unix timestamp';
COMMENT ON COLUMN linkis_ps_cs_context_map_listener.create_time IS 'create time';
COMMENT ON COLUMN linkis_ps_cs_context_map_listener.access_time IS 'last access time';
DROP TABLE IF EXISTS "linkis_ps_cs_context_history";
CREATE TABLE linkis_ps_cs_context_history (
  id serial NOT NULL,
  context_id integer DEFAULT NULL,
  "source" text,
  context_type varchar(32) DEFAULT NULL,
  history_json text,
  keyword varchar(255) DEFAULT NULL,
  CONSTRAINT linkis_ps_cs_context_history_pkey PRIMARY KEY (id),
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  access_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_keyword ON linkis_ps_cs_context_history USING btree (keyword(191);
COMMENT ON COLUMN linkis_ps_cs_context_history.update_time IS 'update unix timestamp';
COMMENT ON COLUMN linkis_ps_cs_context_history.create_time IS 'create time';
COMMENT ON COLUMN linkis_ps_cs_context_history.access_time IS 'last access time';
DROP TABLE IF EXISTS "linkis_ps_cs_context_id";
CREATE TABLE linkis_ps_cs_context_id (
  id serial NOT NULL,
  "user" varchar(32) DEFAULT NULL,
  application varchar(32) DEFAULT NULL,
  "source" varchar(255) DEFAULT NULL,
  expire_type varchar(32) DEFAULT NULL,
  expire_time timestamp DEFAULT NULL,
  instance varchar(64) DEFAULT NULL,
  backup_instance varchar(64) DEFAULT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  access_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_ps_cs_context_id_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_instance ON linkis_ps_cs_context_id USING btree (instance);
CREATE INDEX idx_backup_instance ON linkis_ps_cs_context_id USING btree (backup_instance);
CREATE INDEX idx_instance_bin ON linkis_ps_cs_context_id USING btree (instance, backup_instance);
COMMENT ON COLUMN linkis_ps_cs_context_id.update_time IS 'update unix timestamp';
COMMENT ON COLUMN linkis_ps_cs_context_id.create_time IS 'create time';
COMMENT ON COLUMN linkis_ps_cs_context_id.access_time IS 'last access time';
DROP TABLE IF EXISTS "linkis_ps_cs_context_listener";
CREATE TABLE linkis_ps_cs_context_listener (
  id serial NOT NULL,
  listener_source varchar(255) DEFAULT NULL,
  context_id integer DEFAULT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  access_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_ps_cs_context_listener_pkey PRIMARY KEY (id)
);
COMMENT ON COLUMN linkis_ps_cs_context_listener.update_time IS 'update unix timestamp';
COMMENT ON COLUMN linkis_ps_cs_context_listener.create_time IS 'create time';
COMMENT ON COLUMN linkis_ps_cs_context_listener.access_time IS 'last access time';
DROP TABLE IF EXISTS "linkis_ps_bml_resources";
CREATE TABLE linkis_ps_bml_resources (
  id bigserial NOT NULL,
  resource_id varchar(50) NOT NULL,
  is_private boolean DEFAULT false,
  resource_header smallint DEFAULT 0,
  downloaded_file_name varchar(200) DEFAULT NULL,
  sys varchar(100) NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  owner varchar(200) NOT NULL,
  is_expire boolean DEFAULT false,
  expire_type varchar(50) DEFAULT null,
  expire_time varchar(50) DEFAULT null,
  max_version integer DEFAULT 10,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updator varchar(50) DEFAULT NULL,
  enable_flag boolean NOT NULL DEFAULT true,
  CONSTRAINT linkis_ps_bml_resources_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_rid_eflag ON linkis_ps_bml_resources USING btree (resource_id, enable_flag);
COMMENT ON COLUMN linkis_ps_bml_resources.id IS 'Primary key';
COMMENT ON COLUMN linkis_ps_bml_resources.resource_id IS 'resource uuid';
COMMENT ON COLUMN linkis_ps_bml_resources.is_private IS 'Whether the resource is private, 0 means private, 1 means public';
COMMENT ON COLUMN linkis_ps_bml_resources.resource_header IS 'Classification, 0 means unclassified, 1 means classified';
COMMENT ON COLUMN linkis_ps_bml_resources.downloaded_file_name IS 'File name when downloading';
COMMENT ON COLUMN linkis_ps_bml_resources.sys IS 'Owning system';
COMMENT ON COLUMN linkis_ps_bml_resources.create_time IS 'Created time';
COMMENT ON COLUMN linkis_ps_bml_resources.owner IS 'Resource owner';
COMMENT ON COLUMN linkis_ps_bml_resources.is_expire IS 'Whether expired, 0 means not expired, 1 means expired';
COMMENT ON COLUMN linkis_ps_bml_resources.expire_type IS 'Expiration type, date refers to the expiration on the specified date, TIME refers to the time';
COMMENT ON COLUMN linkis_ps_bml_resources.expire_time IS 'Expiration time, one day by default';
COMMENT ON COLUMN linkis_ps_bml_resources.max_version IS 'The default is 10, which means to keep the latest 10 versions';
COMMENT ON COLUMN linkis_ps_bml_resources.update_time IS 'Updated time';
COMMENT ON COLUMN linkis_ps_bml_resources.updator IS 'updator';
COMMENT ON COLUMN linkis_ps_bml_resources.enable_flag IS 'Status, 1: normal, 0: frozen';
DROP TABLE IF EXISTS "linkis_ps_bml_resources_version";
CREATE TABLE linkis_ps_bml_resources_version (
  id bigserial NOT NULL,
  resource_id varchar(50) NOT NULL,
  file_md5 varchar(32) NOT NULL,
  version varchar(20) NOT NULL,
  size integer NOT NULL,
  start_byte bigint UNSIGNED NOT NULL DEFAULT 0,
  end_byte bigint UNSIGNED NOT NULL DEFAULT 0,
  resource varchar(2000) NOT NULL,
  description varchar(2000) DEFAULT NULL,
  start_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  end_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  client_ip varchar(200) NOT NULL,
  updator varchar(50) DEFAULT NULL,
  enable_flag boolean NOT NULL DEFAULT true,
  CONSTRAINT linkis_ps_bml_resources_version_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_rid_version ON linkis_ps_bml_resources_version USING btree (resource_id, version);
COMMENT ON COLUMN linkis_ps_bml_resources_version.id IS 'Primary key';
COMMENT ON COLUMN linkis_ps_bml_resources_version.resource_id IS 'Resource uuid';
COMMENT ON COLUMN linkis_ps_bml_resources_version.file_md5 IS 'Md5 summary of the file';
COMMENT ON COLUMN linkis_ps_bml_resources_version.version IS 'Resource version (v plus five digits)';
COMMENT ON COLUMN linkis_ps_bml_resources_version.size IS 'File size';
COMMENT ON COLUMN linkis_ps_bml_resources_version.resource IS 'Resource content (file information including path and file name)';
COMMENT ON COLUMN linkis_ps_bml_resources_version.description IS 'description';
COMMENT ON COLUMN linkis_ps_bml_resources_version.start_time IS 'Started time';
COMMENT ON COLUMN linkis_ps_bml_resources_version.end_time IS 'Stoped time';
COMMENT ON COLUMN linkis_ps_bml_resources_version.client_ip IS 'Client ip';
COMMENT ON COLUMN linkis_ps_bml_resources_version.updator IS 'updator';
COMMENT ON COLUMN linkis_ps_bml_resources_version.enable_flag IS 'Status, 1: normal, 0: frozen';
DROP TABLE IF EXISTS "linkis_ps_bml_resources_permission";
CREATE TABLE linkis_ps_bml_resources_permission (
  id bigserial NOT NULL,
  resource_id varchar(50) NOT NULL,
  permission varchar(10) NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "system" varchar(50) default "dss",
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updator varchar(50) NOT NULL,
  CONSTRAINT linkis_ps_bml_resources_permission_pkey PRIMARY KEY (id)
);
COMMENT ON COLUMN linkis_ps_bml_resources_permission.id IS 'Primary key';
COMMENT ON COLUMN linkis_ps_bml_resources_permission.resource_id IS 'Resource uuid';
COMMENT ON COLUMN linkis_ps_bml_resources_permission.permission IS 'permission';
COMMENT ON COLUMN linkis_ps_bml_resources_permission.create_time IS 'created time';
COMMENT ON COLUMN linkis_ps_bml_resources_permission."system" IS 'creator';
COMMENT ON COLUMN linkis_ps_bml_resources_permission.update_time IS 'updated time';
COMMENT ON COLUMN linkis_ps_bml_resources_permission.updator IS 'updator';
DROP TABLE IF EXISTS "linkis_ps_resources_download_history";
CREATE TABLE linkis_ps_resources_download_history (
  id bigserial NOT NULL,
  start_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  end_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  client_ip varchar(200) NOT NULL,
  state smallint NOT NULL,
  resource_id varchar(50) not null,
  version varchar(20) not null,
  downloader varchar(50) NOT NULL,
  CONSTRAINT linkis_ps_resources_download_history_pkey PRIMARY KEY (id)
);
COMMENT ON COLUMN linkis_ps_resources_download_history.id IS 'primary key';
COMMENT ON COLUMN linkis_ps_resources_download_history.start_time IS 'start time';
COMMENT ON COLUMN linkis_ps_resources_download_history.end_time IS 'stop time';
COMMENT ON COLUMN linkis_ps_resources_download_history.client_ip IS 'client ip';
COMMENT ON COLUMN linkis_ps_resources_download_history.state IS 'Download status, 0 download successful, 1 download failed';
COMMENT ON COLUMN linkis_ps_resources_download_history.downloader IS 'Downloader';
DROP TABLE IF EXISTS "linkis_ps_bml_resources_task";
CREATE TABLE linkis_ps_bml_resources_task (
  id bigserial NOT NULL,
  resource_id varchar(50) DEFAULT NULL,
  version varchar(20) DEFAULT NULL,
  operation varchar(20) NOT NULL,
  state varchar(20) NOT NULL DEFAULT 'Schduled',
  submit_user varchar(20) NOT NULL DEFAULT '',
  "system" varchar(20) DEFAULT 'dss',
  instance varchar(128) NOT NULL,
  client_ip varchar(50) DEFAULT NULL,
  extra_params text,
  err_msg varchar(2000) DEFAULT NULL,
  start_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  end_time timestamp DEFAULT NULL,
  last_update_time timestamp NOT NULL,
  CONSTRAINT linkis_ps_bml_resources_task_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_rid_version ON linkis_ps_bml_resources_task USING btree (resource_id, version);
COMMENT ON COLUMN linkis_ps_bml_resources_task.resource_id IS 'resource uuid';
COMMENT ON COLUMN linkis_ps_bml_resources_task.version IS 'Resource version number of the current operation';
COMMENT ON COLUMN linkis_ps_bml_resources_task.operation IS 'Operation type. upload = 0, update = 1';
COMMENT ON COLUMN linkis_ps_bml_resources_task.state IS 'Current status of the task:Schduled, Running, Succeed, Failed,Cancelled';
COMMENT ON COLUMN linkis_ps_bml_resources_task.submit_user IS 'Job submission user name';
COMMENT ON COLUMN linkis_ps_bml_resources_task."system" IS 'Subsystem name: wtss';
COMMENT ON COLUMN linkis_ps_bml_resources_task.instance IS 'Material library example';
COMMENT ON COLUMN linkis_ps_bml_resources_task.client_ip IS 'Request IP';
COMMENT ON COLUMN linkis_ps_bml_resources_task.extra_params IS 'Additional key information. Such as the resource IDs and versions that are deleted in batches, and all versions under the resource are deleted';
COMMENT ON COLUMN linkis_ps_bml_resources_task.err_msg IS 'Task failure information.e.getMessage';
COMMENT ON COLUMN linkis_ps_bml_resources_task.start_time IS 'Starting time';
COMMENT ON COLUMN linkis_ps_bml_resources_task.end_time IS 'End Time';
COMMENT ON COLUMN linkis_ps_bml_resources_task.last_update_time IS 'Last update time';
DROP TABLE IF EXISTS "linkis_ps_bml_project";
CREATE TABLE linkis_ps_bml_project (
  id serial NOT NULL,
  "name" varchar(128) DEFAULT NULL,
  "system" varchar(64) not null default "dss",
  "source" varchar(1024) default null,
  description varchar(1024) default null,
  creator varchar(128) not null,
  enabled smallint default 1,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_ps_bml_project_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_name ON linkis_ps_bml_project USING btree ("name");
DROP TABLE IF EXISTS "linkis_ps_bml_project_user";
CREATE TABLE linkis_ps_bml_project_user (
  id serial NOT NULL,
  project_id integer NOT NULL,
  username varchar(64) DEFAULT NULL,
  priv integer not null default 7, -- rwx 421 The permission value is 7. 8 is the administrator, which can authorize other users,
  creator varchar(128) not null,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  expire_time timestamp default null,
  CONSTRAINT linkis_ps_bml_project_user_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_name_pid ON linkis_ps_bml_project_user USING btree (username, project_id);
DROP TABLE IF EXISTS "linkis_ps_bml_project_resource";
CREATE TABLE linkis_ps_bml_project_resource (
  id serial NOT NULL,
  project_id integer NOT NULL,
  resource_id varchar(128) DEFAULT NULL,
  CONSTRAINT linkis_ps_bml_project_resource_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_resource_id ON linkis_ps_bml_project_resource USING btree (resource_id);
DROP TABLE IF EXISTS "linkis_ps_instance_label";
CREATE TABLE linkis_ps_instance_label (
  id serial NOT NULL,
  label_key varchar(32) NOT NULL,
  label_value varchar(128) NOT NULL,
  label_feature varchar(16) NOT NULL,
  label_value_size integer NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_ps_instance_label_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_lk_lv ON linkis_ps_instance_label USING btree (label_key, label_value);
COMMENT ON COLUMN linkis_ps_instance_label.label_key IS 'string key';
COMMENT ON COLUMN linkis_ps_instance_label.label_value IS 'string value';
COMMENT ON COLUMN linkis_ps_instance_label.label_feature IS 'store the feature of label, but it may be redundant';
COMMENT ON COLUMN linkis_ps_instance_label.label_value_size IS 'size of key -> value map';
COMMENT ON COLUMN linkis_ps_instance_label.update_time IS 'update unix timestamp';
COMMENT ON COLUMN linkis_ps_instance_label.create_time IS 'update unix timestamp';
DROP TABLE IF EXISTS "linkis_ps_instance_label_value_relation";
CREATE TABLE linkis_ps_instance_label_value_relation (
  id serial NOT NULL,
  label_value_key varchar(128) NOT NULL,
  label_value_content varchar(255) DEFAULT NULL,
  label_id integer DEFAULT NULL,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_ps_instance_label_value_relation_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_lvk_lid ON linkis_ps_instance_label_value_relation USING btree (label_value_key, label_id);
COMMENT ON COLUMN linkis_ps_instance_label_value_relation.label_value_key IS 'value key';
COMMENT ON COLUMN linkis_ps_instance_label_value_relation.label_value_content IS 'value content';
COMMENT ON COLUMN linkis_ps_instance_label_value_relation.label_id IS 'id reference linkis_ps_instance_label -> id';
COMMENT ON COLUMN linkis_ps_instance_label_value_relation.update_time IS 'update unix timestamp';
COMMENT ON COLUMN linkis_ps_instance_label_value_relation.create_time IS 'create unix timestamp';
DROP TABLE IF EXISTS "linkis_ps_instance_label_relation";
CREATE TABLE linkis_ps_instance_label_relation (
  id serial NOT NULL,
  label_id integer DEFAULT NULL,
  service_instance varchar(128) NOT NULL,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_ps_instance_label_relation_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_lid_instance ON linkis_ps_instance_label_relation USING btree (label_id, service_instance);
COMMENT ON COLUMN linkis_ps_instance_label_relation.label_id IS 'id reference linkis_ps_instance_label -> id';
COMMENT ON COLUMN linkis_ps_instance_label_relation.service_instance IS 'structure like ${host|machine}:${port}';
COMMENT ON COLUMN linkis_ps_instance_label_relation.update_time IS 'update unix timestamp';
COMMENT ON COLUMN linkis_ps_instance_label_relation.create_time IS 'create unix timestamp';
DROP TABLE IF EXISTS "linkis_ps_instance_info";
CREATE TABLE linkis_ps_instance_info (
  id serial NOT NULL,
  instance varchar(128) DEFAULT NULL,
  "name" varchar(128) DEFAULT NULL,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_ps_instance_info_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_instance ON linkis_ps_instance_info USING btree (instance);
COMMENT ON COLUMN linkis_ps_instance_info.instance IS 'structure like ${host|machine}:${port}';
COMMENT ON COLUMN linkis_ps_instance_info."name" IS 'equal application name in registry';
COMMENT ON COLUMN linkis_ps_instance_info.update_time IS 'update unix timestamp';
COMMENT ON COLUMN linkis_ps_instance_info.create_time IS 'create unix timestamp';
DROP TABLE IF EXISTS "linkis_ps_error_code";
CREATE TABLE linkis_ps_error_code (
  id bigserial NOT NULL,
  error_code varchar(50) NOT NULL,
  error_desc varchar(1024) NOT NULL,
  error_regex varchar(1024) DEFAULT NULL,
  error_type int(3) DEFAULT 0,
  CONSTRAINT linkis_ps_error_code_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_error_regex ON linkis_ps_error_code USING btree (error_regex(191);
DROP TABLE IF EXISTS "linkis_cg_manager_service_instance";
CREATE TABLE linkis_cg_manager_service_instance (
  id serial NOT NULL,
  instance varchar(128) DEFAULT NULL,
  "name" varchar(32) DEFAULT NULL,
  owner varchar(32) DEFAULT NULL,
  mark varchar(32) DEFAULT NULL,
  identifier varchar(32) DEFAULT NULL,
  ticketId varchar(255) DEFAULT NULL,
  mapping_host varchar(128) DEFAULT NULL,
  mapping_ports varchar(128) DEFAULT NULL,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  updator varchar(32) DEFAULT NULL,
  creator varchar(32) DEFAULT NULL,
  params text DEFAULT NULL,
  CONSTRAINT linkis_cg_manager_service_instance_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_instance ON linkis_cg_manager_service_instance USING btree (instance);
CREATE INDEX idx_instance_name ON linkis_cg_manager_service_instance USING btree (instance, "name");
DROP TABLE IF EXISTS "linkis_cg_manager_linkis_resources";
CREATE TABLE linkis_cg_manager_linkis_resources (
  id serial NOT NULL,
  max_resource varchar(1020) DEFAULT NULL,
  min_resource varchar(1020) DEFAULT NULL,
  used_resource varchar(1020) DEFAULT NULL,
  left_resource varchar(1020) DEFAULT NULL,
  expected_resource varchar(1020) DEFAULT NULL,
  locked_resource varchar(1020) DEFAULT NULL,
  resourceType varchar(255) DEFAULT NULL,
  ticketId varchar(255) DEFAULT NULL,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  updator varchar(255) DEFAULT NULL,
  creator varchar(255) DEFAULT NULL,
  CONSTRAINT linkis_cg_manager_linkis_resources_pkey PRIMARY KEY (id)
);
DROP TABLE IF EXISTS "linkis_cg_manager_lock";
CREATE TABLE linkis_cg_manager_lock (
  id serial NOT NULL,
  lock_object varchar(255) DEFAULT NULL,
  time_out text,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_cg_manager_lock_pkey PRIMARY KEY (id)
);
DROP TABLE IF EXISTS "linkis_cg_rm_external_resource_provider";
CREATE TABLE linkis_cg_rm_external_resource_provider (
  id serial NOT NULL,
  resource_type varchar(32) NOT NULL,
  "name" varchar(32) NOT NULL,
  labels varchar(32) DEFAULT NULL,
  config text NOT NULL,
  CONSTRAINT linkis_cg_rm_external_resource_provider_pkey PRIMARY KEY (id)
);
DROP TABLE IF EXISTS "linkis_cg_manager_engine_em";
CREATE TABLE linkis_cg_manager_engine_em (
  id serial NOT NULL,
  engine_instance varchar(128) DEFAULT NULL,
  em_instance varchar(128) DEFAULT NULL,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_cg_manager_engine_em_pkey PRIMARY KEY (id)
);
DROP TABLE IF EXISTS "linkis_cg_manager_label";
CREATE TABLE linkis_cg_manager_label (
  id serial NOT NULL,
  label_key varchar(32) NOT NULL,
  label_value varchar(128) NOT NULL,
  label_feature varchar(16) NOT NULL,
  label_value_size integer NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_cg_manager_label_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_lk_lv ON linkis_cg_manager_label USING btree (label_key, label_value);
DROP TABLE IF EXISTS "linkis_cg_manager_label_value_relation";
CREATE TABLE linkis_cg_manager_label_value_relation (
  id serial NOT NULL,
  label_value_key varchar(128) NOT NULL,
  label_value_content varchar(255) DEFAULT NULL,
  label_id integer DEFAULT NULL,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_cg_manager_label_value_relation_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_lvk_lid ON linkis_cg_manager_label_value_relation USING btree (label_value_key, label_id);
CREATE UNIQUE INDEX unlid_lvk_lvc ON linkis_cg_manager_label_value_relation USING btree (label_id, label_value_key, label_value_content);
DROP TABLE IF EXISTS "linkis_cg_manager_label_resource";
CREATE TABLE linkis_cg_manager_label_resource (
  id serial NOT NULL,
  label_id integer DEFAULT NULL,
  resource_id integer DEFAULT NULL,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_cg_manager_label_resource_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_label_id ON linkis_cg_manager_label_resource USING btree (label_id);
DROP TABLE IF EXISTS "linkis_cg_ec_resource_info_record";
CREATE TABLE linkis_cg_ec_resource_info_record (
  id serial NOT NULL,
  label_value VARCHAR(128) NOT NULL,
  create_user VARCHAR(128) NOT NULL,
  service_instance varchar(128) DEFAULT NULL,
  ecm_instance varchar(128) DEFAULT NULL,
  ticket_id VARCHAR(36) NOT NULL,
  status varchar(50) DEFAULT NULL,
  log_dir_suffix varchar(128) DEFAULT NULL,
  request_times INT(8),
  request_resource VARCHAR(1020),
  used_times INT(8),
  used_resource VARCHAR(1020),
  metrics TEXT DEFAULT NULL,
  release_times INT(8),
  released_resource VARCHAR(1020),
  release_time timestamp DEFAULT NULL,
  used_time timestamp DEFAULT NULL,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_cg_ec_resource_info_record_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_ticket_id ON linkis_cg_ec_resource_info_record USING btree (ticket_id);
CREATE UNIQUE INDEX uniq_tid_lv ON linkis_cg_ec_resource_info_record USING btree (ticket_id, label_value);
CREATE UNIQUE INDEX uniq_sinstance_status_cuser_ctime ON linkis_cg_ec_resource_info_record USING btree (service_instance, status, create_user, create_time);
COMMENT ON COLUMN linkis_cg_ec_resource_info_record.label_value IS 'ec labels stringValue';
COMMENT ON COLUMN linkis_cg_ec_resource_info_record.create_user IS 'ec create user';
COMMENT ON COLUMN linkis_cg_ec_resource_info_record.service_instance IS 'ec instance info';
COMMENT ON COLUMN linkis_cg_ec_resource_info_record.ecm_instance IS 'ecm instance info ';
COMMENT ON COLUMN linkis_cg_ec_resource_info_record.ticket_id IS 'ec ticket id';
COMMENT ON COLUMN linkis_cg_ec_resource_info_record.status IS 'EC status: Starting,Unlock,Locked,Idle,Busy,Running,ShuttingDown,Failed,Success';
COMMENT ON COLUMN linkis_cg_ec_resource_info_record.log_dir_suffix IS 'log path';
COMMENT ON COLUMN linkis_cg_ec_resource_info_record.request_times IS 'resource request times';
COMMENT ON COLUMN linkis_cg_ec_resource_info_record.request_resource IS 'request resource';
COMMENT ON COLUMN linkis_cg_ec_resource_info_record.used_times IS 'resource used times';
COMMENT ON COLUMN linkis_cg_ec_resource_info_record.used_resource IS 'used resource';
COMMENT ON COLUMN linkis_cg_ec_resource_info_record.metrics IS 'ec metrics';
COMMENT ON COLUMN linkis_cg_ec_resource_info_record.release_times IS 'resource released times';
COMMENT ON COLUMN linkis_cg_ec_resource_info_record.released_resource IS 'released resource';
COMMENT ON COLUMN linkis_cg_ec_resource_info_record.release_time IS 'released time';
COMMENT ON COLUMN linkis_cg_ec_resource_info_record.used_time IS 'used time';
COMMENT ON COLUMN linkis_cg_ec_resource_info_record.create_time IS 'create time';
DROP TABLE IF EXISTS "linkis_cg_manager_label_service_instance";
CREATE TABLE linkis_cg_manager_label_service_instance (
  id serial NOT NULL,
  label_id integer DEFAULT NULL,
  service_instance varchar(128) DEFAULT NULL,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_cg_manager_label_service_instance_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_lid_instance ON linkis_cg_manager_label_service_instance USING btree (label_id, service_instance);
DROP TABLE IF EXISTS "linkis_cg_manager_label_user";
CREATE TABLE linkis_cg_manager_label_user (
  id serial NOT NULL,
  username varchar(255) DEFAULT NULL,
  label_id integer DEFAULT NULL,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_cg_manager_label_user_pkey PRIMARY KEY (id)
);
DROP TABLE IF EXISTS "linkis_cg_manager_metrics_history";
CREATE TABLE linkis_cg_manager_metrics_history (
  id serial NOT NULL,
  instance_status integer DEFAULT NULL,
  overload varchar(255) DEFAULT NULL,
  heartbeat_msg varchar(255) DEFAULT NULL,
  healthy_status integer DEFAULT NULL,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  creator varchar(255) DEFAULT NULL,
  ticketID varchar(255) DEFAULT NULL,
  serviceName varchar(255) DEFAULT NULL,
  instance varchar(255) DEFAULT NULL,
  CONSTRAINT linkis_cg_manager_metrics_history_pkey PRIMARY KEY (id)
);
DROP TABLE IF EXISTS "linkis_cg_manager_service_instance_metrics";
CREATE TABLE linkis_cg_manager_service_instance_metrics (
  instance varchar(128) NOT NULL,
  instance_status integer DEFAULT NULL,
  overload varchar(255) DEFAULT NULL,
  heartbeat_msg text DEFAULT NULL,
  healthy_status varchar(255) DEFAULT NULL,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
  description varchar(256) NOT NULL DEFAULT '',
  CONSTRAINT linkis_cg_manager_service_instance_metrics_pkey PRIMARY KEY (instance)
);
DROP TABLE IF EXISTS "linkis_cg_engine_conn_plugin_bml_resources";
CREATE TABLE linkis_cg_engine_conn_plugin_bml_resources (
  id bigserial NOT NULL,
  engine_conn_type varchar(100) NOT NULL,
  version varchar(100),
  file_name varchar(255),
  file_size bigint  DEFAULT 0 NOT NULL,
  last_modified bigint,
  bml_resource_id varchar(100) NOT NULL,
  bml_resource_version varchar(200) NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_cg_engine_conn_plugin_bml_resources_pkey PRIMARY KEY (id)
);
COMMENT ON COLUMN linkis_cg_engine_conn_plugin_bml_resources.id IS 'Primary key';
COMMENT ON COLUMN linkis_cg_engine_conn_plugin_bml_resources.engine_conn_type IS 'Engine type';
COMMENT ON COLUMN linkis_cg_engine_conn_plugin_bml_resources.version IS 'version';
COMMENT ON COLUMN linkis_cg_engine_conn_plugin_bml_resources.file_name IS 'file name';
COMMENT ON COLUMN linkis_cg_engine_conn_plugin_bml_resources.file_size IS 'file size';
COMMENT ON COLUMN linkis_cg_engine_conn_plugin_bml_resources.last_modified IS 'File update time';
COMMENT ON COLUMN linkis_cg_engine_conn_plugin_bml_resources.bml_resource_id IS 'Owning system';
COMMENT ON COLUMN linkis_cg_engine_conn_plugin_bml_resources.bml_resource_version IS 'Resource owner';
COMMENT ON COLUMN linkis_cg_engine_conn_plugin_bml_resources.create_time IS 'created time';
COMMENT ON COLUMN linkis_cg_engine_conn_plugin_bml_resources.last_update_time IS 'updated time';
DROP TABLE IF EXISTS "linkis_ps_dm_datasource";
CREATE TABLE linkis_ps_dm_datasource (
  id serial NOT NULL,
  datasource_name varchar(255) NOT NULL,
  datasource_desc varchar(255)      DEFAULT NULL,
  datasource_type_id integer                       NOT NULL,
  create_identify varchar(255)      DEFAULT NULL,
  create_system varchar(255)      DEFAULT NULL,
  parameter varchar(2048) NULL DEFAULT NULL,
  create_time timestamp                      NULL DEFAULT CURRENT_TIMESTAMP,
  modify_time timestamp                      NULL DEFAULT CURRENT_TIMESTAMP,
  create_user varchar(255)      DEFAULT NULL,
  modify_user varchar(255)      DEFAULT NULL,
  labels varchar(255)      DEFAULT NULL,
  version_id integer                            DEFAULT NULL,
  expire boolean                         DEFAULT false,
  published_version_id integer                            DEFAULT NULL,
  CONSTRAINT linkis_ps_dm_datasource_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_datasource_name ON linkis_ps_dm_datasource USING btree (datasource_name);
COMMENT ON COLUMN linkis_ps_dm_datasource.version_id IS 'current version id';
DROP TABLE IF EXISTS "linkis_ps_dm_datasource_env";
CREATE TABLE linkis_ps_dm_datasource_env (
  id serial NOT NULL,
  env_name varchar(32)  NOT NULL,
  env_desc varchar(255)          DEFAULT NULL,
  datasource_type_id integer                       NOT NULL,
  parameter varchar(2048)          DEFAULT NULL,
  create_time timestamp                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_user varchar(255) NULL     DEFAULT NULL,
  modify_time timestamp                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modify_user varchar(255) NULL     DEFAULT NULL,
  CONSTRAINT linkis_ps_dm_datasource_env_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_env_name ON linkis_ps_dm_datasource_env USING btree (env_name);
CREATE UNIQUE INDEX uniq_name_dtid ON linkis_ps_dm_datasource_env USING btree (env_name, datasource_type_id);
DROP TABLE IF EXISTS "linkis_ps_dm_datasource_type";
CREATE TABLE linkis_ps_dm_datasource_type (
  id serial NOT NULL,
  "name" varchar(32) NOT NULL,
  description varchar(255) DEFAULT NULL,
  "option" varchar(32)  DEFAULT NULL,
  classifier varchar(32) NOT NULL,
  icon varchar(255) DEFAULT NULL,
  layers int(3)                       NOT NULL,
  description_en varchar(255) DEFAULT NULL,
  option_en varchar(32) DEFAULT NULL,
  classifier_en varchar(32) DEFAULT NULL,
  CONSTRAINT linkis_ps_dm_datasource_type_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_name ON linkis_ps_dm_datasource_type USING btree ("name");
COMMENT ON COLUMN linkis_ps_dm_datasource_type.description_en IS 'english description';
COMMENT ON COLUMN linkis_ps_dm_datasource_type.option_en IS 'english option';
COMMENT ON COLUMN linkis_ps_dm_datasource_type.classifier_en IS 'english classifier';
DROP TABLE IF EXISTS "linkis_ps_dm_datasource_type_key";
CREATE TABLE linkis_ps_dm_datasource_type_key (
  id serial NOT NULL,
  data_source_type_id integer                       NOT NULL,
  "key" varchar(32)  NOT NULL,
  "name" varchar(32)  NOT NULL,
  name_en varchar(32)  NULL     DEFAULT NULL,
  default_value varchar(50)  NULL     DEFAULT NULL,
  value_type varchar(50)  NOT NULL,
  scope varchar(50)  NULL     DEFAULT NULL,
  require boolean                    NULL     DEFAULT false,
  description varchar(200) NULL     DEFAULT NULL,
  description_en varchar(200) NULL     DEFAULT NULL,
  value_regex varchar(200) NULL     DEFAULT NULL,
  ref_id bigint                    NULL     DEFAULT NULL,
  ref_value varchar(50)  NULL     DEFAULT NULL,
  data_source varchar(200) NULL     DEFAULT NULL,
  update_time timestamp                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_time timestamp                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_ps_dm_datasource_type_key_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_dstid_key ON linkis_ps_dm_datasource_type_key USING btree (data_source_type_id, "key");
DROP TABLE IF EXISTS "linkis_ps_dm_datasource_version";
CREATE TABLE linkis_ps_dm_datasource_version (
  version_id serial NOT NULL,
  datasource_id integer                        NOT NULL,
  parameter varchar(2048) NULL DEFAULT NULL,
  comment varchar(255)  NULL DEFAULT NULL,
  create_time timestamp                    NULL DEFAULT CURRENT_TIMESTAMP,
  create_user varchar(255)  NULL DEFAULT NULL
);
DROP TABLE IF EXISTS "linkis_mg_gateway_auth_token";
CREATE TABLE linkis_mg_gateway_auth_token (
  id serial NOT NULL,
  token_name varchar(128) NOT NULL,
  token_sign TEXT DEFAULT NULL,
  legal_users text,
  legal_hosts text,
  business_owner varchar(32),
  create_time DATE DEFAULT NULL,
  update_time DATE DEFAULT NULL,
  elapse_day BIGINT DEFAULT NULL,
  update_by varchar(32),
  CONSTRAINT linkis_mg_gateway_auth_token_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_token_name ON linkis_mg_gateway_auth_token USING btree (token_name);
DROP TABLE IF EXISTS "linkis_cg_tenant_label_config";
CREATE TABLE linkis_cg_tenant_label_config (
  id serial NOT NULL,
  "user" varchar(50) NOT NULL,
  creator varchar(50) NOT NULL,
  tenant_value varchar(128) NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "desc" varchar(100) NOT NULL,
  bussiness_user varchar(50) NOT NULL,
  is_valid varchar(1) NOT NULL DEFAULT 'Y',
  CONSTRAINT linkis_cg_tenant_label_config_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_user_creator ON linkis_cg_tenant_label_config USING btree ("user", creator);
COMMENT ON COLUMN linkis_cg_tenant_label_config.is_valid IS 'is valid';
DROP TABLE IF EXISTS "linkis_cg_user_ip_config";
CREATE TABLE linkis_cg_user_ip_config (
  id serial NOT NULL,
  "user" varchar(50) NOT NULL,
  creator varchar(50) NOT NULL,
  ip_list text NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "desc" varchar(100) NOT NULL,
  bussiness_user varchar(50) NOT NULL,
  CONSTRAINT linkis_cg_user_ip_config_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_user_creator ON linkis_cg_user_ip_config USING btree ("user", creator);
DROP TABLE IF EXISTS "linkis_org_user";
CREATE TABLE linkis_org_user (
  cluster_code varchar(16),
  user_type varchar(64),
  user_name varchar(128),
  org_id varchar(16),
  org_name varchar(64),
  queue_name varchar(64),
  db_name varchar(64),
  interface_user varchar(64),
  is_union_analyse varchar(64),
  create_time varchar(64),
  user_itsm_no varchar(64),
  CONSTRAINT linkis_org_user_pkey PRIMARY KEY (user_name)
);
COMMENT ON COLUMN linkis_org_user.cluster_code IS 'cluster code';
COMMENT ON COLUMN linkis_org_user.user_type IS 'user type';
COMMENT ON COLUMN linkis_org_user.user_name IS 'username';
COMMENT ON COLUMN linkis_org_user.org_id IS 'org id';
COMMENT ON COLUMN linkis_org_user.org_name IS 'org name';
COMMENT ON COLUMN linkis_org_user.queue_name IS 'yarn queue name';
COMMENT ON COLUMN linkis_org_user.db_name IS 'default db name';
COMMENT ON COLUMN linkis_org_user.interface_user IS 'interface user';
COMMENT ON COLUMN linkis_org_user.is_union_analyse IS 'is union analyse';
COMMENT ON COLUMN linkis_org_user.create_time IS 'create time';
COMMENT ON COLUMN linkis_org_user.user_itsm_no IS 'user itsm no';
DROP TABLE IF EXISTS "linkis_cg_synckey";
CREATE TABLE linkis_cg_synckey (
  username char(32) NOT NULL,
  synckey char(32) NOT NULL,
  instance varchar(32) NOT NULL,
  create_time timestamp(3) NOT NULL
);
DROP TABLE IF EXISTS "linkis_et_validator_checkinfo";
CREATE TABLE linkis_et_validator_checkinfo (
  id bigserial NOT NULL,
  execute_user varchar(64) NOT NULL,
  db_name varchar(64) DEFAULT NULL,
  params text,
  code_type varchar(32) NOT NULL,
  operation_type varchar(32) NOT NULL,
  status smallint(4) DEFAULT NULL,
  code text,
  msg text,
  risk_level varchar(32) DEFAULT NULL,
  hit_rules text,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT linkis_et_validator_checkinfo_pkey PRIMARY KEY (id)
);
COMMENT ON COLUMN linkis_et_validator_checkinfo.create_time IS 'create time';
DROP TABLE IF EXISTS "linkis_ps_bml_cleaned_resources_version";
CREATE TABLE linkis_ps_bml_cleaned_resources_version (
  id bigserial NOT NULL,
  resource_id varchar(50) NOT NULL,
  file_md5 varchar(32) NOT NULL,
  version varchar(20) NOT NULL,
  size integer NOT NULL,
  start_byte bigint unsigned NOT NULL DEFAULT '0',
  end_byte bigint unsigned NOT NULL DEFAULT '0',
  resource varchar(2000) NOT NULL,
  description varchar(2000) DEFAULT NULL,
  start_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  end_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  client_ip varchar(200) NOT NULL,
  updator varchar(50) DEFAULT NULL,
  enable_flag boolean NOT NULL DEFAULT true,
  old_resource varchar(2000) NOT NULL,
  CONSTRAINT linkis_ps_bml_cleaned_resources_version_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX resource_id_version ON linkis_ps_bml_cleaned_resources_version USING btree (resource_id, version);
COMMENT ON COLUMN linkis_ps_bml_cleaned_resources_version.id IS '主键';
COMMENT ON COLUMN linkis_ps_bml_cleaned_resources_version.resource_id IS '资源id，资源的uuid';
COMMENT ON COLUMN linkis_ps_bml_cleaned_resources_version.file_md5 IS '文件的md5摘要';
COMMENT ON COLUMN linkis_ps_bml_cleaned_resources_version.version IS '资源版本（v 加上 五位数字）';
COMMENT ON COLUMN linkis_ps_bml_cleaned_resources_version.size IS '文件大小';
COMMENT ON COLUMN linkis_ps_bml_cleaned_resources_version.resource IS '资源内容（文件信息 包括 路径和文件名）';
COMMENT ON COLUMN linkis_ps_bml_cleaned_resources_version.description IS '描述';
COMMENT ON COLUMN linkis_ps_bml_cleaned_resources_version.start_time IS '开始时间';
COMMENT ON COLUMN linkis_ps_bml_cleaned_resources_version.end_time IS '结束时间';
COMMENT ON COLUMN linkis_ps_bml_cleaned_resources_version.client_ip IS '客户端ip';
COMMENT ON COLUMN linkis_ps_bml_cleaned_resources_version.updator IS '修改者';
COMMENT ON COLUMN linkis_ps_bml_cleaned_resources_version.enable_flag IS '状态，1：正常，0：冻结';
COMMENT ON COLUMN linkis_ps_bml_cleaned_resources_version.old_resource IS '旧的路径';
DROP TABLE IF EXISTS "linkis_ps_configuration_across_cluster_rule";
CREATE TABLE linkis_ps_configuration_across_cluster_rule (
  id INT AUTO_INCREMENT,
  cluster_name char(32) NOT NULL,
  creator char(32) NOT NULL,
  username char(32) NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_by char(32) NOT NULL,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_by char(32) NOT NULL,
  rules varchar(512) NOT NULL,
  is_valid VARCHAR(2) DEFAULT 'N',
  CONSTRAINT linkis_ps_configuration_across_cluster_rule_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_creator_username ON linkis_ps_configuration_across_cluster_rule USING btree (creator, username);
COMMENT ON COLUMN linkis_ps_configuration_across_cluster_rule.id IS '规则ID，自增主键';
COMMENT ON COLUMN linkis_ps_configuration_across_cluster_rule.cluster_name IS '集群名称，不能为空';
COMMENT ON COLUMN linkis_ps_configuration_across_cluster_rule.creator IS '创建者，不能为空';
COMMENT ON COLUMN linkis_ps_configuration_across_cluster_rule.username IS '用户，不能为空';
COMMENT ON COLUMN linkis_ps_configuration_across_cluster_rule.create_time IS '创建时间，不能为空';
COMMENT ON COLUMN linkis_ps_configuration_across_cluster_rule.create_by IS '创建者，不能为空';
COMMENT ON COLUMN linkis_ps_configuration_across_cluster_rule.update_time IS '修改时间，不能为空';
COMMENT ON COLUMN linkis_ps_configuration_across_cluster_rule.update_by IS '更新者，不能为空';
COMMENT ON COLUMN linkis_ps_configuration_across_cluster_rule.rules IS '规则内容，不能为空';
COMMENT ON COLUMN linkis_ps_configuration_across_cluster_rule.is_valid IS '是否有效 Y/N';
DROP TABLE IF EXISTS "linkis_org_user_sync";
CREATE TABLE linkis_org_user_sync (
  cluster_code varchar(16),
  user_type varchar(64),
  user_name varchar(128),
  org_id varchar(16),
  org_name varchar(64),
  queue_name varchar(64),
  db_name varchar(64),
  interface_user varchar(64),
  is_union_analyse varchar(64),
  create_time varchar(64),
  user_itsm_no varchar(64),
  CONSTRAINT linkis_org_user_sync_pkey PRIMARY KEY (user_name)
);
COMMENT ON COLUMN linkis_org_user_sync.cluster_code IS '集群';
COMMENT ON COLUMN linkis_org_user_sync.user_type IS '用户类型';
COMMENT ON COLUMN linkis_org_user_sync.user_name IS '授权用户';
COMMENT ON COLUMN linkis_org_user_sync.org_id IS '部门ID';
COMMENT ON COLUMN linkis_org_user_sync.org_name IS '部门名字';
COMMENT ON COLUMN linkis_org_user_sync.queue_name IS '默认资源队列';
COMMENT ON COLUMN linkis_org_user_sync.db_name IS '默认操作数据库';
COMMENT ON COLUMN linkis_org_user_sync.interface_user IS '接口人';
COMMENT ON COLUMN linkis_org_user_sync.is_union_analyse IS '是否联合分析人';
COMMENT ON COLUMN linkis_org_user_sync.create_time IS '用户创建时间';
COMMENT ON COLUMN linkis_org_user_sync.user_itsm_no IS '用户创建单号';
DROP TABLE IF EXISTS "linkis_cg_tenant_department_config";
CREATE TABLE linkis_cg_tenant_department_config (
  id serial NOT NULL,
  creator varchar(50) NOT NULL,
  department varchar(64) NOT NULL,
  department_id varchar(16) NOT NULL,
  tenant_value varchar(128) NOT NULL,
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_by varchar(50) NOT NULL,
  is_valid varchar(1) NOT NULL DEFAULT 'Y',
  CONSTRAINT linkis_cg_tenant_department_config_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_creator_department ON linkis_cg_tenant_department_config USING btree (creator, department);
COMMENT ON COLUMN linkis_cg_tenant_department_config.id IS 'ID';
COMMENT ON COLUMN linkis_cg_tenant_department_config.creator IS '应用';
COMMENT ON COLUMN linkis_cg_tenant_department_config.department IS '部门名称';
COMMENT ON COLUMN linkis_cg_tenant_department_config.department_id IS '部门ID';
COMMENT ON COLUMN linkis_cg_tenant_department_config.tenant_value IS '部门租户标签';
COMMENT ON COLUMN linkis_cg_tenant_department_config.create_time IS '创建时间';
COMMENT ON COLUMN linkis_cg_tenant_department_config.update_time IS '更新时间';
COMMENT ON COLUMN linkis_cg_tenant_department_config.create_by IS '创建用户';
COMMENT ON COLUMN linkis_cg_tenant_department_config.is_valid IS '是否有效';
DROP TABLE IF EXISTS "linkis_mg_gateway_whitelist_config";
CREATE TABLE linkis_mg_gateway_whitelist_config (
  id serial NOT NULL,
  allowed_user varchar(128) NOT NULL,
  client_address varchar(128) NOT NULL,
  create_time timestamp DEFAULT NULL,
  update_time timestamp DEFAULT NULL,
  CONSTRAINT linkis_mg_gateway_whitelist_config_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX address_uniq ON linkis_mg_gateway_whitelist_config USING btree (allowed_user, client_address);
CREATE INDEX linkis_mg_gateway_whitelist_config_allowed_user ON linkis_mg_gateway_whitelist_config USING btree (allowed_user);
DROP TABLE IF EXISTS "linkis_mg_gateway_whitelist_sensitive_user";
CREATE TABLE linkis_mg_gateway_whitelist_sensitive_user (
  id serial NOT NULL,
  sensitive_username varchar(128) NOT NULL,
  create_time timestamp DEFAULT NULL,
  update_time timestamp DEFAULT NULL,
  CONSTRAINT linkis_mg_gateway_whitelist_sensitive_user_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX sensitive_username ON linkis_mg_gateway_whitelist_sensitive_user USING btree (sensitive_username);
DROP TABLE IF EXISTS "linkis_ps_python_module_info";
CREATE TABLE linkis_ps_python_module_info (
  id bigserial NOT NULL,
  "name" varchar(255) NOT NULL,
  description text,
  path varchar(255) NOT NULL,
  engine_type varchar(50) NOT NULL,
  create_user varchar(50) NOT NULL,
  update_user varchar(50) NOT NULL,
  is_load boolean NOT NULL DEFAULT false,
  is_expire boolean DEFAULT NULL,
  python_module varchar(200) DEFAULT NULL,
  create_time timestamp NOT NULL,
  update_time timestamp NOT NULL,
  CONSTRAINT linkis_ps_python_module_info_pkey PRIMARY KEY (id)
);
COMMENT ON COLUMN linkis_ps_python_module_info.id IS '自增id';
COMMENT ON COLUMN linkis_ps_python_module_info."name" IS 'python模块名称';
COMMENT ON COLUMN linkis_ps_python_module_info.description IS 'python模块描述';
COMMENT ON COLUMN linkis_ps_python_module_info.path IS 'hdfs路径';
COMMENT ON COLUMN linkis_ps_python_module_info.engine_type IS '引擎类型，python/spark/all';
COMMENT ON COLUMN linkis_ps_python_module_info.create_user IS '创建用户';
COMMENT ON COLUMN linkis_ps_python_module_info.update_user IS '修改用户';
COMMENT ON COLUMN linkis_ps_python_module_info.is_load IS '是否加载，0-未加载，1-已加载';
COMMENT ON COLUMN linkis_ps_python_module_info.is_expire IS '是否过期，0-未过期，1-已过期）';
COMMENT ON COLUMN linkis_ps_python_module_info.python_module IS '依赖python模块';
COMMENT ON COLUMN linkis_ps_python_module_info.create_time IS '创建时间';
COMMENT ON COLUMN linkis_ps_python_module_info.update_time IS '修改时间';
DROP TABLE IF EXISTS "linkis_ps_job_history_diagnosis";
CREATE TABLE linkis_ps_job_history_diagnosis (
  id bigserial NOT NULL,
  job_history_id bigint NOT NULL,
  diagnosis_content text,
  created_time timestamp(3) DEFAULT CURRENT_TIMESTAMP,
  updated_time timestamp(3) DEFAULT CURRENT_TIMESTAMP,
  only_read varchar(5) DEFAULT NULL,
  CONSTRAINT linkis_ps_job_history_diagnosis_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX job_history_id ON linkis_ps_job_history_diagnosis USING btree (job_history_id);
COMMENT ON COLUMN linkis_ps_job_history_diagnosis.id IS 'Primary Key, auto increment';
COMMENT ON COLUMN linkis_ps_job_history_diagnosis.job_history_id IS 'ID of JobHistory';
COMMENT ON COLUMN linkis_ps_job_history_diagnosis.diagnosis_content IS 'Diagnosis failed task information';
COMMENT ON COLUMN linkis_ps_job_history_diagnosis.created_time IS 'Creation time';
COMMENT ON COLUMN linkis_ps_job_history_diagnosis.updated_time IS 'Update time';
COMMENT ON COLUMN linkis_ps_job_history_diagnosis.only_read IS '1 just read,can not update';
DROP TABLE IF EXISTS "linkis_mg_gateway_ecc_userinfo";
CREATE TABLE linkis_mg_gateway_ecc_userinfo (
CREATE UNIQUE INDEX apply_itsm_id ON linkis_mg_gateway_ecc_userinfo USING btree (apply_itsm_id, user_id);
COMMENT ON COLUMN linkis_mg_gateway_ecc_userinfo.id IS '主键ID，自增';
COMMENT ON COLUMN linkis_mg_gateway_ecc_userinfo.om_tool IS '工具系统';
COMMENT ON COLUMN linkis_mg_gateway_ecc_userinfo.user_id IS '申请授权用户';
COMMENT ON COLUMN linkis_mg_gateway_ecc_userinfo.op_user_id IS '协助运维账号';
COMMENT ON COLUMN linkis_mg_gateway_ecc_userinfo.roles IS '角色列表，多个逗号,分隔';
COMMENT ON COLUMN linkis_mg_gateway_ecc_userinfo.auth_system_id IS '授权子系统名称ID，多个逗号,分隔';
COMMENT ON COLUMN linkis_mg_gateway_ecc_userinfo.apply_itsm_id IS 'ITSM申请单号，唯一，重复推送时根据这个字段做更新';
COMMENT ON COLUMN linkis_mg_gateway_ecc_userinfo.effective_datetime IS '生效时间，允许登录的最早时间';
COMMENT ON COLUMN linkis_mg_gateway_ecc_userinfo.expire_datetime IS '失效时间，根据这个时间计算cookie的有效期';
COMMENT ON COLUMN linkis_mg_gateway_ecc_userinfo.created_at IS '创建时间，默认当前时间';
COMMENT ON COLUMN linkis_mg_gateway_ecc_userinfo.updated_at IS '更新时间，默认当前时间，更新时修改';
