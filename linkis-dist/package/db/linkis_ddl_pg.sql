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


DROP TABLE IF EXISTS "linkis_ps_configuration_config_key";
CREATE TABLE linkis_ps_configuration_config_key (
	id bigserial NOT NULL,
	"key" varchar(50) NULL,
	description varchar(200) NULL,
	"name" varchar(50) NULL,
	default_value varchar(200) NULL,
	validate_type varchar(50) NULL,
	validate_range varchar(50) NULL,
	engine_conn_type varchar(50) NULL,
	is_hidden bool NULL,
	is_advanced bool NULL,
	"level" int2 NULL,
	"treeName" varchar(20) NULL,
	en_description varchar(200) NULL,
	en_name varchar(100) NULL,
	"en_treeName" varchar(100) NULL,
	CONSTRAINT linkis_configuration_config_key_pkey PRIMARY KEY (id)
);
COMMENT ON COLUMN "linkis_ps_configuration_config_key"."key" IS 'Set key, e.g. spark.executor.instances';
COMMENT ON COLUMN "linkis_ps_configuration_config_key"."engine_conn_type" IS 'engine type,such as spark,hive etc';
COMMENT ON COLUMN "linkis_ps_configuration_config_key"."default_value" IS 'Adopted when user does not set key';
COMMENT ON COLUMN "linkis_ps_configuration_config_key"."validate_type" IS 'Validate type, one of the following: None, NumInterval, FloatInterval, Include, Regex, OPF, Custom Rules';
COMMENT ON COLUMN "linkis_ps_configuration_config_key"."validate_range" IS 'Validate range';
COMMENT ON COLUMN "linkis_ps_configuration_config_key"."is_hidden" IS 'Whether it is hidden from user. If set to 1(true), then user cannot modify, however, it could still be used in back-end';
COMMENT ON COLUMN "linkis_ps_configuration_config_key"."is_advanced" IS 'Whether it is an advanced parameter. If set to 1(true), parameters would be displayed only when user choose to do so';
COMMENT ON COLUMN "linkis_ps_configuration_config_key"."level" IS 'Basis for displaying sorting in the front-end. Higher the level is, higher the rank the parameter gets';
COMMENT ON COLUMN "linkis_ps_configuration_config_key"."treeName" IS 'Reserved field, representing the subdirectory of engineType';
COMMENT ON COLUMN "linkis_ps_configuration_config_key"."treeName" IS 'english description';
COMMENT ON COLUMN "linkis_ps_configuration_config_key"."treeName" IS 'english name';
COMMENT ON COLUMN "linkis_ps_configuration_config_key"."treeName" IS 'english treeName';


DROP TABLE IF EXISTS "linkis_ps_configuration_key_engine_relation";
CREATE TABLE linkis_ps_configuration_key_engine_relation (
	id bigserial NOT NULL,
	config_key_id int4 NOT NULL,
	engine_type_label_id int4 NOT NULL,
	CONSTRAINT linkis_ps_configuration_key_engine_relation_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_ckid_etlid ON linkis_ps_configuration_key_engine_relation USING btree (config_key_id, engine_type_label_id);
COMMENT ON COLUMN "linkis_ps_configuration_key_engine_relation"."config_key_id" IS 'config key id';
COMMENT ON COLUMN "linkis_ps_configuration_key_engine_relation"."engine_type_label_id" IS 'engine label id';


DROP TABLE IF EXISTS "linkis_ps_configuration_config_value";
CREATE TABLE linkis_ps_configuration_config_value (
	id bigserial NOT NULL,
	config_key_id int4 NULL,
	config_value varchar(200) NULL,
	config_label_id int8 NULL,
	update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_configuration_config_value_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_ckid_clid ON linkis_ps_configuration_config_value USING btree (config_key_id, config_label_id);


DROP TABLE IF EXISTS "linkis_ps_configuration_category";
CREATE TABLE linkis_ps_configuration_category (
	id bigserial NOT NULL,
	label_id int4 NOT NULL,
	"level" int4 NOT NULL,
	description varchar(200) NULL,
	tag varchar(200) NULL,
	update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_configuration_category_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_label_id_cc ON linkis_ps_configuration_category USING btree (label_id);


DROP TABLE IF EXISTS "linkis_ps_job_history_group_history";
CREATE TABLE linkis_ps_job_history_group_history (
	id bigserial NOT NULL,
	job_req_id varchar(64) NULL,
	submit_user varchar(50) NULL,
	execute_user varchar(50) NULL,
	"source" text NULL,
	labels text NULL,
	params text NULL,
	progress varchar(32) NULL,
	status varchar(50) NULL,
	log_path varchar(200) NULL,
	error_code int4 NULL,
	error_desc varchar(1000) NULL,
	created_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	updated_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	instances varchar(250) NULL,
	metrics text NULL,
	engine_type varchar(32) NULL,
	execution_code text NULL,
	result_location varchar(500) NULL,
	observe_info varchar(500) NULL,
	CONSTRAINT linkis_ps_job_history_group_history_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_created_time ON linkis_ps_job_history_group_history USING btree (created_time);
CREATE INDEX idx_submit_user ON linkis_ps_job_history_group_history USING btree (submit_user);
COMMENT ON COLUMN "linkis_ps_job_history_group_history"."id" IS 'Primary Key, auto increment';
COMMENT ON COLUMN "linkis_ps_job_history_group_history"."job_req_id" IS 'job execId';
COMMENT ON COLUMN "linkis_ps_job_history_group_history"."submit_user" IS 'who submitted this Job';
COMMENT ON COLUMN "linkis_ps_job_history_group_history"."execute_user" IS 'who actually executed this Job';
COMMENT ON COLUMN "linkis_ps_job_history_group_history"."source" IS 'job source';
COMMENT ON COLUMN "linkis_ps_job_history_group_history"."labels" IS 'job labels';
COMMENT ON COLUMN "linkis_ps_job_history_group_history"."params" IS 'job labels';
COMMENT ON COLUMN "linkis_ps_job_history_group_history"."progress" IS 'Job execution progress';
COMMENT ON COLUMN "linkis_ps_job_history_group_history"."status" IS 'Script execution status, must be one of the following: Inited, WaitForRetry, Scheduled, Running, Succeed, Failed, Cancelled, Timeout';
COMMENT ON COLUMN "linkis_ps_job_history_group_history"."log_path" IS 'File path of the job log';
COMMENT ON COLUMN "linkis_ps_job_history_group_history"."error_code" IS 'Error code. Generated when the execution of the script fails';
COMMENT ON COLUMN "linkis_ps_job_history_group_history"."error_desc" IS 'Execution description. Generated when the execution of script fails';
COMMENT ON COLUMN "linkis_ps_job_history_group_history"."created_time" IS 'Creation time';
COMMENT ON COLUMN "linkis_ps_job_history_group_history"."updated_time" IS 'Update time';
COMMENT ON COLUMN "linkis_ps_job_history_group_history"."instances" IS 'Entrance instances';
COMMENT ON COLUMN "linkis_ps_job_history_group_history"."metrics" IS 'Job Metrics';
COMMENT ON COLUMN "linkis_ps_job_history_group_history"."engine_type" IS 'Engine type';
COMMENT ON COLUMN "linkis_ps_job_history_group_history"."execution_code" IS 'Job origin code or code path';
COMMENT ON COLUMN "linkis_ps_job_history_group_history"."observe_info" IS 'The notification information configuration of this job';


DROP TABLE IF EXISTS "linkis_ps_job_history_detail";
CREATE TABLE linkis_ps_job_history_detail (
	id bigserial NOT NULL,
	job_history_id int8 NOT NULL,
	result_location varchar(500) NULL,
	execution_content text NULL,
	result_array_size int4 NULL DEFAULT 0,
	job_group_info text NULL,
	created_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	updated_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	status varchar(32) NULL,
	priority int4 NULL DEFAULT 0,
	CONSTRAINT linkis_ps_job_history_detail_pkey PRIMARY KEY (id)
);
COMMENT ON COLUMN "linkis_ps_job_history_detail"."id" IS 'Primary Key, auto increment';
COMMENT ON COLUMN "linkis_ps_job_history_detail"."job_history_id" IS 'ID of JobHistory';
COMMENT ON COLUMN "linkis_ps_job_history_detail"."result_location" IS 'File path of the resultsets';
COMMENT ON COLUMN "linkis_ps_job_history_detail"."execution_content" IS 'The script code or other execution content executed by this Job';
COMMENT ON COLUMN "linkis_ps_job_history_detail"."result_array_size" IS 'size of result array';
COMMENT ON COLUMN "linkis_ps_job_history_detail"."job_group_info" IS 'Job group info/path';
COMMENT ON COLUMN "linkis_ps_job_history_detail"."created_time" IS 'Creation time';
COMMENT ON COLUMN "linkis_ps_job_history_detail"."updated_time" IS 'Update time';
COMMENT ON COLUMN "linkis_ps_job_history_detail"."status" IS 'status';
COMMENT ON COLUMN "linkis_ps_job_history_detail"."priority" IS 'order of subjob';


DROP TABLE IF EXISTS "linkis_ps_common_lock";
CREATE TABLE linkis_ps_common_lock (
	id bigserial NOT NULL,
	lock_object varchar(255) NULL,
    time_out text NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_ps_common_lock_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_lock_object ON linkis_ps_common_lock USING btree (lock_object);


DROP TABLE IF EXISTS "linkis_ps_udf_manager";
CREATE TABLE linkis_ps_udf_manager (
	id bigserial NOT NULL,
	user_name varchar(20) NULL,
	CONSTRAINT linkis_udf_manager_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_ps_udf_shared_group";
CREATE TABLE linkis_ps_udf_shared_group (
	id bigserial NOT NULL,
	udf_id int8 NOT NULL,
	shared_group varchar(50) NOT NULL,
	CONSTRAINT linkis_udf_shared_group_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_ps_udf_shared_info";
CREATE TABLE linkis_ps_udf_shared_info (
	id bigserial NOT NULL,
	udf_id int8 NOT NULL,
	user_name varchar(50) NOT NULL,
	CONSTRAINT linkis_ps_udf_shared_info_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_ps_udf_tree";
CREATE TABLE linkis_ps_udf_tree (
	id bigserial NOT NULL,
	parent int8 NOT NULL,
	"name" varchar(100) NULL,
	user_name varchar(50) NOT NULL,
	description varchar(255) NULL,
	create_time timestamp(6) NOT NULL,
	update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	category varchar(50) NULL,
	CONSTRAINT linkis_udf_tree_pkey PRIMARY KEY (id)
);
COMMENT ON COLUMN "linkis_ps_udf_tree"."name" IS 'Category name of the function. It would be displayed in the front-end';
COMMENT ON COLUMN "linkis_ps_udf_tree"."category" IS 'Used to distinguish between udf and function';


DROP TABLE IF EXISTS "linkis_ps_udf_user_load";
CREATE TABLE linkis_ps_udf_user_load (
    id bigserial NOT NULL,
	udf_id int4 NOT NULL,
	user_name varchar(50) NOT NULL,
	CONSTRAINT linkis_ps_udf_user_load_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_ps_udf_baseinfo";
CREATE TABLE linkis_ps_udf_baseinfo (
	id bigserial NOT NULL,
	create_user varchar(50) NOT NULL,
	udf_name varchar(255) NOT NULL,
	udf_type int4 NULL DEFAULT 0,
	tree_id int8 NOT NULL,
	create_time timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	update_time timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	"sys" varchar(255) NOT NULL DEFAULT 'ide',
	cluster_name varchar(255) NOT NULL,
	is_expire bool NULL,
	is_shared bool NULL,
	CONSTRAINT linkis_ps_udf_baseinfo_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_ps_udf_version";
CREATE TABLE linkis_ps_udf_version (
	id bigserial NOT NULL,
	udf_id int8 NOT NULL,
	"path" varchar(255) NOT NULL,
	bml_resource_id varchar(50) NOT NULL,
	bml_resource_version varchar(20) NOT NULL,
	is_published bool NULL,
	register_format varchar(255) NULL,
	use_format varchar(255) NULL,
	description varchar(255) NOT NULL,
	create_time timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	md5 varchar(100) NULL,
	CONSTRAINT linkis_ps_udf_version_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_ps_variable_key_user";
CREATE TABLE linkis_ps_variable_key_user (
	id bigserial NOT NULL,
	application_id int8 NULL,
	key_id int8 NULL,
	user_name varchar(50) NULL,
	value varchar(200) NULL,
	CONSTRAINT linkis_var_key_user_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_aid_vku ON linkis_ps_variable_key_user USING btree (application_id);
CREATE UNIQUE INDEX uniq_aid_kid_uname ON linkis_ps_variable_key_user USING btree (application_id, key_id, user_name);
CREATE INDEX idx_key_id ON linkis_ps_variable_key_user USING btree (key_id);
COMMENT ON COLUMN "linkis_ps_variable_key_user"."application_id" IS 'Reserved word';
COMMENT ON COLUMN "linkis_ps_variable_key_user"."value" IS 'Value of the global variable';


DROP TABLE IF EXISTS "linkis_ps_variable_key";
CREATE TABLE linkis_ps_variable_key (
	id bigserial NOT NULL,
	"key" varchar(50) NULL,
	description varchar(200) NULL,
	"name" varchar(50) NULL,
	application_id int8 NULL,
	default_value varchar(200) NULL,
	value_type varchar(50) NULL,
	value_regex varchar(100) NULL,
	CONSTRAINT linkis_var_key_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_aid_vk ON linkis_ps_variable_key USING btree (application_id);
COMMENT ON COLUMN "linkis_ps_variable_key"."key" IS 'Key of the global variable';
COMMENT ON COLUMN "linkis_ps_variable_key"."description" IS 'Reserved word';
COMMENT ON COLUMN "linkis_ps_variable_key"."name" IS 'Reserved word';
COMMENT ON COLUMN "linkis_ps_variable_key"."application_id" IS 'Reserved word';
COMMENT ON COLUMN "linkis_ps_variable_key"."default_value" IS 'Reserved word';
COMMENT ON COLUMN "linkis_ps_variable_key"."value_type" IS 'Reserved word';
COMMENT ON COLUMN "linkis_ps_variable_key"."value_regex" IS 'Reserved word';


DROP TABLE IF EXISTS "linkis_ps_datasource_access";
CREATE TABLE linkis_ps_datasource_access (
	id bigserial NOT NULL,
	table_id int8 NOT NULL,
	visitor varchar(16) NOT NULL,
	fields varchar(255) NULL,
	application_id int4 NOT NULL,
	access_time timestamp(6) NOT NULL,
	CONSTRAINT linkis_mdq_access_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_ps_datasource_field";
CREATE TABLE linkis_ps_datasource_field (
	id bigserial NOT NULL,
	table_id int8 NOT NULL,
	"name" varchar(64) NOT NULL,
	alias varchar(64) NULL,
	"type" varchar(64) NOT NULL,
	"comment" varchar(255) NULL,
	express varchar(255) NULL,
	"rule" varchar(128) NULL,
	is_partition_field bool NOT NULL,
	is_primary bool NOT NULL,
	length int4 NULL,
	mode_info varchar(128) NULL,
	CONSTRAINT linkis_mdq_field_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_ps_datasource_import";
CREATE TABLE linkis_ps_datasource_import (
	id bigserial NOT NULL,
	table_id int8 NOT NULL,
	import_type int4 NOT NULL,
	args varchar(255) NOT NULL,
	CONSTRAINT linkis_mdq_import_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_ps_datasource_lineage";
CREATE TABLE linkis_ps_datasource_lineage (
	id bigserial NOT NULL,
	table_id int8 NULL,
	source_table varchar(64) NULL,
	update_time timestamp(6) NULL,
	CONSTRAINT linkis_mdq_lineage_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_ps_datasource_table";
CREATE TABLE linkis_ps_datasource_table (
	id bigserial NOT NULL,
	"database" varchar(64) NOT NULL,
	"name" varchar(64) NOT NULL,
	alias varchar(64) NULL,
	creator varchar(16) NOT NULL,
	"comment" varchar(255) NULL,
	create_time timestamp(6) NOT NULL,
	product_name varchar(64) NULL,
	project_name varchar(255) NULL,
	"usage" varchar(128) NULL,
	lifecycle int4 NOT NULL,
	use_way int4 NOT NULL,
	is_import bool NOT NULL,
	model_level int4 NOT NULL,
	is_external_use bool NOT NULL,
	is_partition_table bool NOT NULL,
	is_available bool NOT NULL,
	CONSTRAINT linkis_mdq_table_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_db_name ON linkis_ps_datasource_table USING btree (database, name);


DROP TABLE IF EXISTS "linkis_ps_datasource_table_info";
CREATE TABLE linkis_ps_datasource_table_info (
	id bigserial NOT NULL,
	table_id int8 NOT NULL,
	table_last_update_time timestamp(6) NOT NULL,
	row_num int8 NOT NULL,
	file_num int4 NOT NULL,
	table_size varchar(32) NOT NULL,
	partitions_num int4 NOT NULL,
	update_time timestamp(6) NOT NULL,
	field_num int4 NOT NULL,
	CONSTRAINT linkis_mdq_table_info_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_ps_cs_context_map";
CREATE TABLE linkis_ps_cs_context_map (
	id serial4 NOT NULL,
	"key" varchar(128) NULL,
	context_scope varchar(32) NULL,
	context_type varchar(32) NULL,
	props text NULL,
	value text NULL,
	context_id int4 NULL,
	keywords varchar(255) NULL,
	update_time timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	access_time timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_cs_context_map_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_key_cid_ctype ON linkis_ps_cs_context_map USING btree (key, context_id, context_type);
CREATE INDEX idx_keywords ON linkis_ps_cs_context_map USING btree (keywords);


DROP TABLE IF EXISTS "linkis_ps_cs_context_map_listener";
CREATE TABLE linkis_ps_cs_context_map_listener (
	id serial4 NOT NULL,
	listener_source varchar(255) NULL,
	key_id int4 NULL,
	update_time timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	access_time timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_cs_context_map_listener_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_ps_cs_context_history";
CREATE TABLE linkis_ps_cs_context_history (
	id serial4 NOT NULL,
	context_id int4 NULL,
	"source" text NULL,
	context_type varchar(32) NULL,
	history_json text NULL,
	keyword varchar(255) NULL,
	update_time timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	access_time timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_cs_context_history_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_keyword ON linkis_ps_cs_context_history USING btree (keyword);


DROP TABLE IF EXISTS "linkis_ps_cs_context_id";
CREATE TABLE linkis_ps_cs_context_id (
	id serial4 NOT NULL,
	"user" varchar(32) NULL,
	application varchar(32) NULL,
	"source" varchar(255) NULL,
	expire_type varchar(32) NULL,
	expire_time timestamp(6) NULL,
	"instance" varchar(128) NULL,
	backup_instance varchar(255) NULL,
	update_time timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	access_time timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_cs_context_id_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_backup_instance ON linkis_ps_cs_context_id USING btree (backup_instance);
CREATE INDEX idx_instance ON linkis_ps_cs_context_id USING btree (instance);
CREATE INDEX idx_instance_bin ON linkis_ps_cs_context_id USING btree (instance, backup_instance);


DROP TABLE IF EXISTS "linkis_ps_cs_context_listener";
CREATE TABLE linkis_ps_cs_context_listener (
	id serial4 NOT NULL,
	listener_source varchar(255) NULL,
	context_id int4 NULL,
	update_time timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	access_time timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_cs_context_listener_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_ps_bml_resources";
CREATE TABLE linkis_ps_bml_resources (
	id bigserial NOT NULL,
	resource_id varchar(50) NOT NULL,
	is_private bool NULL DEFAULT false,
	resource_header int2 NULL DEFAULT 0,
	downloaded_file_name varchar(200) NULL,
	sys varchar(100) NOT NULL,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	"owner" varchar(200) NOT NULL,
	is_expire bool NULL DEFAULT false,
	expire_type varchar(50) NULL,
	expire_time varchar(50) NULL,
	max_version int4 NULL DEFAULT 10,
	update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updator varchar(50) NULL,
	enable_flag bool NOT NULL DEFAULT true,
	CONSTRAINT linkis_resources_pkey PRIMARY KEY (id)
);
COMMENT ON COLUMN "linkis_ps_bml_resources"."id" IS '主键';
COMMENT ON COLUMN "linkis_ps_bml_resources"."resource_id" IS '资源id，资源的uuid';
COMMENT ON COLUMN "linkis_ps_bml_resources"."is_private" IS '资源是否私有，0表示私有，1表示公开';
COMMENT ON COLUMN "linkis_ps_bml_resources"."resource_header" IS '分类，0表示未分类，1表示已分类';
COMMENT ON COLUMN "linkis_ps_bml_resources"."downloaded_file_name" IS '下载时的文件名';
COMMENT ON COLUMN "linkis_ps_bml_resources"."sys" IS '所属系统';
COMMENT ON COLUMN "linkis_ps_bml_resources"."create_time" IS '创建时间';
COMMENT ON COLUMN "linkis_ps_bml_resources"."owner" IS '资源所属者';
COMMENT ON COLUMN "linkis_ps_bml_resources"."is_expire" IS '是否过期，0表示不过期，1表示过期';
COMMENT ON COLUMN "linkis_ps_bml_resources"."expire_type" IS '过期类型，date指到指定日期过期，TIME指时间';
COMMENT ON COLUMN "linkis_ps_bml_resources"."expire_time" IS '过期时间，默认一天';
COMMENT ON COLUMN "linkis_ps_bml_resources"."max_version" IS '默认为10，指保留最新的10个版本';
COMMENT ON COLUMN "linkis_ps_bml_resources"."update_time" IS '更新时间';
COMMENT ON COLUMN "linkis_ps_bml_resources"."updator" IS '更新者';
COMMENT ON COLUMN "linkis_ps_bml_resources"."enable_flag" IS '状态，1：正常，0：冻结';


DROP TABLE IF EXISTS "linkis_ps_bml_resources_version";
CREATE TABLE linkis_ps_bml_resources_version (
	id bigserial NOT NULL,
	resource_id varchar(50) NOT NULL,
	file_md5 varchar(32) NOT NULL,
	"version" varchar(20) NOT NULL,
	"size" int4 NOT NULL,
	start_byte numeric(20) NOT NULL DEFAULT 0,
	end_byte numeric(20) NOT NULL DEFAULT 0,
	resource varchar(2000) NOT NULL,
	description varchar(2000) NULL,
	start_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	end_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	client_ip varchar(200) NOT NULL,
	updator varchar(50) NULL,
	enable_flag bool NOT NULL DEFAULT true,
	CONSTRAINT linkis_resources_version_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_rid_version ON linkis_ps_bml_resources_version USING btree (resource_id, version);
COMMENT ON COLUMN "linkis_ps_bml_resources_version"."id" IS '主键';
COMMENT ON COLUMN "linkis_ps_bml_resources_version"."resource_id" IS '资源id，资源的uuid';
COMMENT ON COLUMN "linkis_ps_bml_resources_version"."file_md5" IS '文件的md5摘要';
COMMENT ON COLUMN "linkis_ps_bml_resources_version"."version" IS '资源版本（v 加上 五位数字）';
COMMENT ON COLUMN "linkis_ps_bml_resources_version"."size" IS '文件大小';
COMMENT ON COLUMN "linkis_ps_bml_resources_version"."resource" IS '资源内容（文件信息 包括 路径和文件名）';
COMMENT ON COLUMN "linkis_ps_bml_resources_version"."description" IS '描述';
COMMENT ON COLUMN "linkis_ps_bml_resources_version"."start_time" IS '开始时间';
COMMENT ON COLUMN "linkis_ps_bml_resources_version"."end_time" IS '结束时间';
COMMENT ON COLUMN "linkis_ps_bml_resources_version"."client_ip" IS '客户端ip';
COMMENT ON COLUMN "linkis_ps_bml_resources_version"."updator" IS '修改者';
COMMENT ON COLUMN "linkis_ps_bml_resources_version"."enable_flag" IS '状态，1：正常，0：冻结';


DROP TABLE IF EXISTS "linkis_ps_bml_resources_permission";
CREATE TABLE linkis_ps_bml_resources_permission (
	id bigserial NOT NULL,
	resource_id varchar(50) NOT NULL,
	"permission" varchar(10) NOT NULL,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	"system" varchar(50) DEFAULT 'dss',
	update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updator varchar(50) NOT NULL,
	CONSTRAINT linkis_resources_permission_pkey PRIMARY KEY (id)
);
COMMENT ON COLUMN "linkis_ps_bml_resources_permission"."id" IS '主键';
COMMENT ON COLUMN "linkis_ps_bml_resources_permission"."resource_id" IS '资源id，资源的uuid';
COMMENT ON COLUMN "linkis_ps_bml_resources_permission"."permission" IS '权限代码';
COMMENT ON COLUMN "linkis_ps_bml_resources_permission"."create_time" IS '创建时间';
COMMENT ON COLUMN "linkis_ps_bml_resources_permission"."system" IS '创建者';
COMMENT ON COLUMN "linkis_ps_bml_resources_permission"."update_time" IS '更新时间';
COMMENT ON COLUMN "linkis_ps_bml_resources_permission"."updator" IS '更新者';


DROP TABLE IF EXISTS "linkis_ps_resources_download_history";
CREATE TABLE linkis_ps_resources_download_history (
	id bigserial NOT NULL,
	start_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	end_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	client_ip varchar(200) NOT NULL,
	state int2 NOT NULL,
	resource_id varchar(50) NOT NULL,
	"version" varchar(20) NOT NULL,
	downloader varchar(50) NOT NULL,
	CONSTRAINT linkis_resources_download_history_pkey PRIMARY KEY (id)
);
COMMENT ON COLUMN "linkis_ps_resources_download_history"."id" IS '主键';
COMMENT ON COLUMN "linkis_ps_resources_download_history"."start_time" IS '开始时间';
COMMENT ON COLUMN "linkis_ps_resources_download_history"."end_time" IS '结束时间';
COMMENT ON COLUMN "linkis_ps_resources_download_history"."client_ip" IS '客户端ip';
COMMENT ON COLUMN "linkis_ps_resources_download_history"."state" IS '下载状态，0下载成功，1下载失败';
COMMENT ON COLUMN "linkis_ps_resources_download_history"."downloader" IS '下载者';


DROP TABLE IF EXISTS "linkis_ps_bml_resources_task";
CREATE TABLE linkis_ps_bml_resources_task (
	id bigserial NOT NULL,
	resource_id varchar(50) NULL,
	"version" varchar(20) NULL,
	operation varchar(20) NOT NULL,
	state varchar(20) NOT NULL DEFAULT 'Schduled'::character varying,
	submit_user varchar(20) NOT NULL,
	"system" varchar(20) NULL DEFAULT 'dss'::character varying,
	"instance" varchar(128) NOT NULL,
	client_ip varchar(50) NULL,
	extra_params text NULL,
	err_msg varchar(2000) NULL,
	start_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	end_time timestamp(6) NULL,
	last_update_time timestamp(6) NOT NULL,
	CONSTRAINT linkis_resources_task_pkey PRIMARY KEY (id)
);
COMMENT ON COLUMN "linkis_ps_bml_resources_task"."resource_id" IS '资源id，资源的uuid';
COMMENT ON COLUMN "linkis_ps_bml_resources_task"."version" IS '当前操作的资源版本号';
COMMENT ON COLUMN "linkis_ps_bml_resources_task"."operation" IS '操作类型.upload = 0, update = 1';
COMMENT ON COLUMN "linkis_ps_bml_resources_task"."state" IS '任务当前状态:Schduled, Running, Succeed, Failed,Cancelled';
COMMENT ON COLUMN "linkis_ps_bml_resources_task"."submit_user" IS '任务提交用户名';
COMMENT ON COLUMN "linkis_ps_bml_resources_task"."system" IS '子系统名 wtss';
COMMENT ON COLUMN "linkis_ps_bml_resources_task"."instance" IS '物料库实例';
COMMENT ON COLUMN "linkis_ps_bml_resources_task"."client_ip" IS '请求IP';
COMMENT ON COLUMN "linkis_ps_bml_resources_task"."extra_params" IS '额外关键信息.如批量删除的资源IDs及versions,删除资源下的所有versions';
COMMENT ON COLUMN "linkis_ps_bml_resources_task"."err_msg" IS '任务失败信息.e.getMessage';
COMMENT ON COLUMN "linkis_ps_bml_resources_task"."start_time" IS '开始时间';
COMMENT ON COLUMN "linkis_ps_bml_resources_task"."end_time" IS '结束时间';
COMMENT ON COLUMN "linkis_ps_bml_resources_task"."last_update_time" IS '最后更新时间';


DROP TABLE IF EXISTS "linkis_ps_bml_project";
CREATE TABLE linkis_ps_bml_project (
	id bigserial NOT NULL,
	"name" varchar(128) NULL,
	"system" varchar(64) NOT NULL DEFAULT 'dss'::character varying,
	"source" varchar(1024) NULL,
	description varchar(1024) NULL,
	creator varchar(128) NOT NULL,
	enabled int2 NULL DEFAULT 1,
	create_time timestamp(6) NULL DEFAULT now(),
	CONSTRAINT linkis_bml_project_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_name_bp ON linkis_ps_bml_project USING btree (name);


DROP TABLE IF EXISTS "linkis_ps_bml_project_user";
CREATE TABLE linkis_ps_bml_project_user (
	id bigserial NOT NULL,
	project_id int4 NOT NULL,
	username varchar(64) NULL,
	priv int4 NOT NULL,
	creator varchar(128) NOT NULL,
	create_time timestamp(6) NULL DEFAULT now(),
	expire_time timestamp(6) NULL,
	CONSTRAINT linkis_bml_project_user_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_name_pid ON linkis_ps_bml_project_user USING btree (username, project_id);


DROP TABLE IF EXISTS "linkis_ps_bml_project_resource";
CREATE TABLE linkis_ps_bml_project_resource (
	id serial4 NOT NULL,
	project_id int4 NOT NULL,
	resource_id varchar(128) NULL,
	CONSTRAINT linkis_bml_project_resource_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_ps_instance_label";
CREATE TABLE linkis_ps_instance_label (
	id bigserial NOT NULL,
	label_key varchar(32) NOT NULL,
	label_value varchar(255) NOT NULL,
	label_feature varchar(16) NOT NULL,
	label_value_size int4 NOT NULL,
	update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_instance_label_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_lk_lv_il ON linkis_ps_instance_label USING btree (label_key, label_value);
COMMENT ON COLUMN "linkis_ps_instance_label"."label_key" IS 'string key';
COMMENT ON COLUMN "linkis_ps_instance_label"."label_value" IS 'string value';
COMMENT ON COLUMN "linkis_ps_instance_label"."label_feature" IS 'store the feature of label, but it may be redundant';
COMMENT ON COLUMN "linkis_ps_instance_label"."label_value_size" IS 'size of key -> value map';
COMMENT ON COLUMN "linkis_ps_instance_label"."update_time" IS 'update unix timestamp';
COMMENT ON COLUMN "linkis_ps_instance_label"."create_time" IS 'update unix timestamp';


DROP TABLE IF EXISTS "linkis_ps_instance_label_value_relation";
CREATE TABLE linkis_ps_instance_label_value_relation (
	id bigserial NOT NULL,
	label_value_key varchar(255) NOT NULL,
	label_value_content varchar(255) NULL,
	label_id int4 NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_ps_instance_label_value_relation_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_lvk_lid_ilvr ON linkis_ps_instance_label_value_relation USING btree (label_value_key, label_id);
COMMENT ON COLUMN "linkis_ps_instance_label_value_relation"."label_value_key" IS 'value key';
COMMENT ON COLUMN "linkis_ps_instance_label_value_relation"."label_value_content" IS 'value content';
COMMENT ON COLUMN "linkis_ps_instance_label_value_relation"."label_id" IS 'id reference linkis_ps_instance_label -> id';
COMMENT ON COLUMN "linkis_ps_instance_label_value_relation"."update_time" IS 'update unix timestamp';
COMMENT ON COLUMN "linkis_ps_instance_label_value_relation"."create_time" IS 'create unix timestamp';


DROP TABLE IF EXISTS "linkis_ps_instance_label_relation";
CREATE TABLE linkis_ps_instance_label_relation (
	id bigserial NOT NULL,
	label_id int4 NULL,
	service_instance varchar(128) NOT NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_instance_label_relation_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_lid_instance ON linkis_ps_instance_label_relation USING btree (label_id, service_instance);
COMMENT ON COLUMN "linkis_ps_instance_label_relation"."label_id" IS 'id reference linkis_ps_instance_label -> id';
COMMENT ON COLUMN "linkis_ps_instance_label_relation"."service_instance" IS 'structure like  ${host|machine}:${port}';
COMMENT ON COLUMN "linkis_ps_instance_label_relation"."update_time" IS 'update unix timestamp';
COMMENT ON COLUMN "linkis_ps_instance_label_relation"."create_time" IS 'create unix timestamp';


DROP TABLE IF EXISTS "linkis_ps_instance_info";
CREATE TABLE linkis_ps_instance_info (
	id bigserial NOT NULL,
	"instance" varchar(128) NULL,
	"name" varchar(128) NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_instance_info_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_instance_ii ON linkis_ps_instance_info USING btree (instance);
COMMENT ON COLUMN "linkis_ps_instance_info"."instance" IS 'structure like ${host|machine}:${port}';
COMMENT ON COLUMN "linkis_ps_instance_info"."name" IS 'equal application name in registry';
COMMENT ON COLUMN "linkis_ps_instance_info"."update_time" IS 'update unix timestamp';
COMMENT ON COLUMN "linkis_ps_instance_info"."create_time" IS 'create unix timestamp';


DROP TABLE IF EXISTS "linkis_ps_error_code";
CREATE TABLE linkis_ps_error_code (
	id bigserial NOT NULL,
	error_code varchar(50) NOT NULL,
	error_desc varchar(1024) NOT NULL,
	error_regex varchar(1024) NULL,
	error_type int4 NULL DEFAULT 0,
	CONSTRAINT linkis_ps_error_code_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_error_regex ON linkis_ps_error_code USING btree (error_regex);

DROP TABLE IF EXISTS "linkis_cg_manager_service_instance";
CREATE TABLE linkis_cg_manager_service_instance (
	id serial4 NOT NULL,
	"instance" varchar(128) NULL,
	"name" varchar(32) NULL,
	"owner" varchar(32) NULL,
	mark varchar(32) NULL,
	identifier varchar(32) NULL,
	ticketId varchar(255) NULL DEFAULT NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	updator varchar(32) NULL,
	creator varchar(32) NULL,
	CONSTRAINT linkis_manager_service_instance_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_instance_msi ON linkis_cg_manager_service_instance USING btree (instance);


DROP TABLE IF EXISTS "linkis_cg_manager_linkis_resources";
CREATE TABLE linkis_cg_manager_linkis_resources (
	id serial4 NOT NULL,
	max_resource varchar(1020) NULL,
	min_resource varchar(1020) NULL,
	used_resource varchar(1020) NULL,
	left_resource varchar(1020) NULL,
	expected_resource varchar(1020) NULL,
	locked_resource varchar(1020) NULL,
	"resourceType" varchar(255) NULL,
	"ticketId" varchar(255) NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	updator varchar(255) NULL,
	creator varchar(255) NULL,
	CONSTRAINT linkis_manager_linkis_resources_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_cg_manager_lock";
CREATE TABLE linkis_cg_manager_lock (
	id serial4 NOT NULL,
	lock_object varchar(255) NULL,
	time_out text NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_manager_lock_pkey PRIMARY KEY (id)
);



DROP TABLE IF EXISTS "linkis_cg_rm_external_resource_provider";
CREATE TABLE linkis_cg_rm_external_resource_provider (
	id serial4 NOT NULL,
	resource_type varchar(32) NOT NULL,
	"name" varchar(32) NOT NULL,
	labels varchar(32) NULL,
	config text NOT NULL,
	CONSTRAINT linkis_external_resource_provider_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_cg_manager_engine_em";
CREATE TABLE linkis_cg_manager_engine_em (
	id serial4 NOT NULL,
	engine_instance varchar(128) NULL,
	em_instance varchar(128) NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_manager_engine_em_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_cg_manager_label";
CREATE TABLE linkis_cg_manager_label (
	id serial4 NOT NULL,
	label_key varchar(32) NOT NULL,
	label_value varchar(255) NOT NULL,
	label_feature varchar(16) NOT NULL,
	label_value_size int4 NOT NULL,
	update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_manager_label_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_lk_lv_ml ON linkis_cg_manager_label USING btree (label_key, label_value);


DROP TABLE IF EXISTS "linkis_cg_manager_label_value_relation";
CREATE TABLE linkis_cg_manager_label_value_relation (
	id serial4 NOT NULL,
	label_value_key varchar(255) NOT NULL,
	label_value_content varchar(255) NULL,
	label_id int4 NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_cg_manager_label_value_relation_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_lvk_lid_mlvr ON linkis_cg_manager_label_value_relation USING btree (label_value_key, label_id);


DROP TABLE IF EXISTS "linkis_cg_manager_label_resource";
CREATE TABLE linkis_cg_manager_label_resource (
	id serial4 NOT NULL,
	label_id int4 NULL,
	resource_id int4 NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_manager_label_resource_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_label_id_mlr ON linkis_cg_manager_label_resource USING btree (label_id);


DROP TABLE IF EXISTS "linkis_cg_ec_resource_info_record";
CREATE TABLE linkis_cg_ec_resource_info_record (
	id serial4 NOT NULL,
	label_value varchar(255) NOT NULL,
	create_user varchar(128) NOT NULL,
	service_instance varchar(128) NULL,
	ecm_instance varchar(128) NULL,
	ticket_id varchar(100) NOT NULL,
	status varchar(50) NOT NULL,
	log_dir_suffix varchar(128) NULL,
	request_times int4 NULL,
	request_resource varchar(1020) NULL,
	used_times int4 NULL,
	used_resource varchar(1020) NULL,
	metrics TEXT NULL,
	release_times int4 NULL,
	released_resource varchar(1020) NULL,
	release_time timestamp(6) NULL,
	used_time timestamp(6) NULL,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_cg_ec_resource_info_record_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_tid_lv ON linkis_cg_ec_resource_info_record USING btree (ticket_id, label_value);
CREATE INDEX idx_ticket_id ON linkis_cg_ec_resource_info_record USING btree (ticket_id);
COMMENT ON COLUMN "linkis_cg_ec_resource_info_record"."label_value" IS 'ec labels stringValue';
COMMENT ON COLUMN "linkis_cg_ec_resource_info_record"."create_user" IS 'ec create user';
COMMENT ON COLUMN "linkis_cg_ec_resource_info_record"."service_instance" IS 'ec instance info';
COMMENT ON COLUMN "linkis_cg_ec_resource_info_record"."ecm_instance" IS 'ecm instance info';
COMMENT ON COLUMN "linkis_cg_ec_resource_info_record"."ticket_id" IS 'ec ticket id';
COMMENT ON COLUMN "linkis_cg_ec_resource_info_record"."status" IS 'EC status: Starting,Unlock,Locked,Idle,Busy,Running,ShuttingDown,Failed,Success';
COMMENT ON COLUMN "linkis_cg_ec_resource_info_record"."log_dir_suffix" IS 'log path';
COMMENT ON COLUMN "linkis_cg_ec_resource_info_record"."request_times" IS 'resource request times';
COMMENT ON COLUMN "linkis_cg_ec_resource_info_record"."request_resource" IS 'request resource';
COMMENT ON COLUMN "linkis_cg_ec_resource_info_record"."used_times" IS 'resource used time';
COMMENT ON COLUMN "linkis_cg_ec_resource_info_record"."used_resource" IS 'used resource';
COMMENT ON COLUMN "linkis_cg_ec_resource_info_record"."metrics" IS 'ec metrics';
COMMENT ON COLUMN "linkis_cg_ec_resource_info_record"."release_times" IS 'resource request times';
COMMENT ON COLUMN "linkis_cg_ec_resource_info_record"."released_resource" IS 'request resource';
COMMENT ON COLUMN "linkis_cg_ec_resource_info_record"."release_time" IS 'resource used time';
COMMENT ON COLUMN "linkis_cg_ec_resource_info_record"."used_time" IS 'used time';
COMMENT ON COLUMN "linkis_cg_ec_resource_info_record"."create_time" IS 'create time';


DROP TABLE IF EXISTS "linkis_cg_manager_label_service_instance";
CREATE TABLE linkis_cg_manager_label_service_instance (
	id serial4 NOT NULL,
	label_id int4 NULL,
	service_instance varchar(128) NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_manager_label_service_instance_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_lid_instance ON linkis_cg_manager_label_service_instance USING btree (label_id,service_instance);


DROP TABLE IF EXISTS "linkis_cg_manager_label_user";
CREATE TABLE linkis_cg_manager_label_user (
	id serial4 NOT NULL,
	username varchar(255) NULL,
	label_id int4 NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_manager_label_user_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_cg_manager_metrics_history";
CREATE TABLE linkis_cg_manager_metrics_history (
    id serial4 NOT NULL,
	instance_status int4 NULL,
	overload varchar(255) NULL,
	heartbeat_msg varchar(255) NULL,
	healthy_status int4 NULL,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	creator varchar(255) NULL,
	"ticketID" varchar(255) NULL,
	"serviceName" varchar(255) NULL,
	"instance" varchar(255) NULL,
	CONSTRAINT linkis_cg_manager_metrics_history_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_cg_manager_service_instance_metrics";
CREATE TABLE linkis_cg_manager_service_instance_metrics (
	"instance" varchar(128) NOT NULL,
	instance_status int4 NULL,
	overload varchar(255) NULL,
	heartbeat_msg text NULL,
	healthy_status varchar(255) NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_manager_service_instance_metrics_pkey PRIMARY KEY (instance)
);


DROP TABLE IF EXISTS "linkis_cg_engine_conn_plugin_bml_resources";
CREATE TABLE linkis_cg_engine_conn_plugin_bml_resources (
	id bigserial NOT NULL,
	engine_conn_type varchar(100) NOT NULL,
	"version" varchar(100) NULL,
	file_name varchar(255) NULL,
	file_size int8 NOT NULL DEFAULT 0,
	last_modified int8 NULL,
	bml_resource_id varchar(100) NOT NULL,
	bml_resource_version varchar(200) NOT NULL,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	last_update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_engine_conn_plugin_bml_resources_pkey PRIMARY KEY (id)
);
COMMENT ON COLUMN "linkis_cg_engine_conn_plugin_bml_resources"."id" IS '主键';
COMMENT ON COLUMN "linkis_cg_engine_conn_plugin_bml_resources"."engine_conn_type" IS 'Engine type';
COMMENT ON COLUMN "linkis_cg_engine_conn_plugin_bml_resources"."version" IS 'version';
COMMENT ON COLUMN "linkis_cg_engine_conn_plugin_bml_resources"."file_name" IS 'file name';
COMMENT ON COLUMN "linkis_cg_engine_conn_plugin_bml_resources"."file_size" IS 'file size';
COMMENT ON COLUMN "linkis_cg_engine_conn_plugin_bml_resources"."last_modified" IS 'File update time';
COMMENT ON COLUMN "linkis_cg_engine_conn_plugin_bml_resources"."bml_resource_id" IS 'Owning system';
COMMENT ON COLUMN "linkis_cg_engine_conn_plugin_bml_resources"."bml_resource_version" IS 'Resource owner';
COMMENT ON COLUMN "linkis_cg_engine_conn_plugin_bml_resources"."create_time" IS 'created time';
COMMENT ON COLUMN "linkis_cg_engine_conn_plugin_bml_resources"."last_update_time" IS 'update time';



DROP TABLE IF EXISTS "linkis_ps_dm_datasource";
CREATE TABLE linkis_ps_dm_datasource (
	id bigserial NOT NULL,
	datasource_name varchar(255) NOT NULL,
	"datasource_desc" varchar(255) NULL,
	datasource_type_id int8 NOT NULL,
	create_identify varchar(255) NULL,
	create_system varchar(255) NULL,
	"parameter" varchar(1024) NULL,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	modify_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_user varchar(255) NULL,
	modify_user varchar(255) NULL,
	labels varchar(255) NULL,
	version_id int8 NULL,
	expire boolean DEFAULT '0',
	published_version_id int8 NULL,
	CONSTRAINT linkis_ps_dm_datasource_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_datasource_name ON linkis_ps_dm_datasource USING btree (datasource_name);


DROP TABLE IF EXISTS "linkis_ps_dm_datasource_env";
CREATE TABLE linkis_ps_dm_datasource_env (
	id bigserial NOT NULL,
	env_name varchar(32) NOT NULL,
	"env_desc" varchar(255) NULL,
	datasource_type_id int8 NOT NULL,
	"parameter" varchar(1024) NULL,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_user varchar(255) NULL,
	modify_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	modify_user varchar(255) NULL,
	CONSTRAINT linkis_ps_dm_datasource_env_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_name_dtid ON linkis_ps_dm_datasource_env USING btree (env_name,datasource_type_id);
CREATE UNIQUE INDEX uniq_env_name ON linkis_ps_dm_datasource_env USING btree (env_name);


DROP TABLE IF EXISTS "linkis_ps_dm_datasource_type";
CREATE TABLE linkis_ps_dm_datasource_type (
	id bigserial NOT NULL,
	"name" varchar(32) NOT NULL,
	description varchar(255) NULL,
	"option" varchar(32) NULL,
	"classifier" varchar(32) NOT NULL,
	"icon" varchar(255) NULL,
	layers int4 NOT NULL,
	description_en varchar(255) NULL,
	option_en varchar(32) NULL,
	classifier_en varchar(32) NULL,
	CONSTRAINT linkis_ps_dm_datasource_type_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_name_ddt ON linkis_ps_dm_datasource_type USING btree (name);
COMMENT ON COLUMN "linkis_ps_dm_datasource_type"."description_en" IS 'english description';
COMMENT ON COLUMN "linkis_ps_dm_datasource_type"."option_en" IS 'english option';
COMMENT ON COLUMN "linkis_ps_dm_datasource_type"."classifier_en" IS 'english classifier';


DROP TABLE IF EXISTS "linkis_ps_dm_datasource_type_key";
CREATE TABLE linkis_ps_dm_datasource_type_key (
	id bigserial NOT NULL,
	data_source_type_id int8 NOT NULL,
	"key" varchar(32) NOT NULL,
	"name" varchar(32) NOT NULL,
    "name_en" varchar(32)  NOT NULL,
	default_value varchar(50) NULL,
	value_type varchar(50) NOT NULL,
	"scope" varchar(50) NULL,
	"require" boolean DEFAULT '0',
	description varchar(200) NULL,
	description_en varchar(200) NULL,
	value_regex varchar(200) NULL,
	ref_id int8 NULL,
	"ref_value" varchar(50) NULL,
	"data_source" varchar(200) NULL,
	update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_ps_dm_datasource_type_key_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_dstid_key ON linkis_ps_dm_datasource_type_key USING btree (data_source_type_id,"key");


DROP TABLE IF EXISTS "linkis_ps_dm_datasource_version";
CREATE TABLE linkis_ps_dm_datasource_version (
	version_id bigserial NOT NULL,
	datasource_id int8 NOT NULL,
	"parameter" varchar(2048) NULL,
	"comment" varchar(255) NULL,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_user varchar(255) NULL,
	CONSTRAINT linkis_ps_dm_datasource_version_pkey PRIMARY KEY (version_id,datasource_id)
);
CREATE UNIQUE INDEX uniq_vid_did ON linkis_ps_dm_datasource_version USING btree (version_id,"datasource_id");


DROP TABLE IF EXISTS "linkis_mg_gateway_auth_token";
CREATE TABLE linkis_mg_gateway_auth_token (
	id bigserial NOT NULL,
	"token_name" varchar(128) NOT NULL,
	legal_users text NULL,
	legal_hosts text NULL,
	"business_owner" varchar(32) NULL,
	create_time timestamp(6) NULL,
	update_time timestamp(6) NULL,
	elapse_day int8 NULL,
	update_by varchar(32) NULL,
	CONSTRAINT linkis_mg_gateway_auth_token_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_token_name ON linkis_mg_gateway_auth_token USING btree (token_name);


DROP TABLE IF EXISTS "linkis_cg_tenant_label_config";
CREATE TABLE "linkis_cg_tenant_label_config" (
  "id" bigserial NOT NULL,
  "user" varchar(50) NOT NULL,
  "creator" varchar(50) NOT NULL,
  "tenant_value" varchar(128) NOT NULL,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "desc" varchar(100) NOT NULL,
  "bussiness_user" varchar(50) NOT NULL,
  CONSTRAINT linkis_cg_tenant_label_config_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_user_creator_tlc ON linkis_cg_tenant_label_config USING btree ("user",creator);


DROP TABLE IF EXISTS "linkis_cg_user_ip_config";
CREATE TABLE "linkis_cg_user_ip_config" (
  "id" bigserial NOT NULL,
  "user" varchar(50) NOT NULL,
  "creator" varchar(50) NOT NULL,
  "ip_list" text NOT NULL,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "desc" varchar(100) NOT NULL,
  "bussiness_user" varchar(50) NOT NULL,
  CONSTRAINT linkis_cg_user_ip_config_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_user_creator_uic ON linkis_cg_user_ip_config USING btree ("user",creator);
