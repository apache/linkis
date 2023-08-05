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

DROP TABLE IF EXISTS "linkis_ps_datasource_access";
CREATE TABLE linkis_ps_datasource_access (
	id serial NOT NULL,
	table_id int8 NOT NULL,
	visitor varchar(16) NOT NULL,
	fields varchar(255) NULL,
	application_id int4 NOT NULL,
	access_time timestamp(6) NOT NULL,
	CONSTRAINT linkis_mdq_access_pkey PRIMARY KEY (id)
);

DROP TABLE IF EXISTS "linkis_ps_dm_datasource_env";
CREATE TABLE linkis_ps_dm_datasource_env (
	id serial NOT NULL,
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
CREATE UNIQUE INDEX uniq_name_dtid ON linkis_ps_dm_datasource_env (env_name,datasource_type_id);
CREATE UNIQUE INDEX uniq_env_name ON linkis_ps_dm_datasource_env (env_name);

DROP TABLE IF EXISTS "linkis_ps_dm_datasource_type_key";
CREATE TABLE linkis_ps_dm_datasource_type_key (
	id serial NOT NULL,
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
CREATE UNIQUE INDEX uniq_dstid_key ON linkis_ps_dm_datasource_type_key (data_source_type_id,"key");

DROP TABLE IF EXISTS "linkis_ps_dm_datasource_type";
CREATE TABLE linkis_ps_dm_datasource_type (
	id serial NOT NULL,
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
CREATE UNIQUE INDEX uniq_name_ddt ON linkis_ps_dm_datasource_type (name);

DROP TABLE IF EXISTS "linkis_ps_error_code";
CREATE TABLE linkis_ps_error_code (
	id serial NOT NULL,
	error_code varchar(50) NOT NULL,
	error_desc varchar(1024) NOT NULL,
	error_regex varchar(1024) NULL,
	error_type int4 NULL DEFAULT 0,
	CONSTRAINT linkis_ps_error_code_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_error_regex ON linkis_ps_error_code (error_regex);


DROP TABLE IF EXISTS "linkis_mg_gateway_auth_token";
CREATE TABLE linkis_mg_gateway_auth_token (
	id serial NOT NULL,
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
CREATE UNIQUE INDEX uniq_token_name ON linkis_mg_gateway_auth_token (token_name);

DROP TABLE IF EXISTS "linkis_cg_rm_external_resource_provider";
CREATE TABLE linkis_cg_rm_external_resource_provider (
	id serial NOT NULL,
	resource_type varchar(32) NOT NULL,
	"name" varchar(32) NOT NULL,
	labels varchar(32) NULL,
	config text NOT NULL,
	CONSTRAINT linkis_external_resource_provider_pkey PRIMARY KEY (id)
);

DROP TABLE IF EXISTS "linkis_ps_udf_manager";
CREATE TABLE linkis_ps_udf_manager (
	id serial NOT NULL,
	user_name varchar(20) NULL,
	CONSTRAINT linkis_udf_manager_pkey PRIMARY KEY (id)
);

DROP TABLE IF EXISTS "linkis_ps_udf_tree";
CREATE TABLE linkis_ps_udf_tree (
	id serial NOT NULL,
	parent int8 NOT NULL,
	"name" varchar(100) NULL,
	user_name varchar(50) NOT NULL,
	description varchar(255) NULL,
	create_time timestamp(6) NOT NULL,
	update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	category varchar(50) NULL,
	CONSTRAINT linkis_udf_tree_pkey PRIMARY KEY (id)
);

DROP TABLE IF EXISTS "linkis_cg_manager_label";
CREATE TABLE linkis_cg_manager_label (
	id serial NOT NULL,
	label_key varchar(50) NOT NULL,
	label_value varchar(255) NOT NULL,
	label_feature varchar(16) NOT NULL,
	label_value_size int4 NOT NULL,
	update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_manager_label_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_lk_lv_ml ON linkis_cg_manager_label (label_key, label_value);

DROP TABLE IF EXISTS "linkis_cg_manager_label_value_relation";
CREATE TABLE linkis_cg_manager_label_value_relation (
	id serial NOT NULL,
	label_value_key varchar(255) NOT NULL,
	label_value_content varchar(255) NULL,
	label_id int4 NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_cg_manager_label_value_relation_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_lvk_lid_mlvr ON linkis_cg_manager_label_value_relation (label_value_key, label_id);

DROP TABLE IF EXISTS "linkis_ps_configuration_config_key";
CREATE TABLE linkis_ps_configuration_config_key (
	id serial NOT NULL,
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

DROP TABLE IF EXISTS "linkis_ps_configuration_config_value";
CREATE TABLE linkis_ps_configuration_config_value (
	id serial NOT NULL,
	config_key_id int4 NULL,
	config_value varchar(200) NULL,
	config_label_id int8 NULL,
	update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_configuration_config_value_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_ckid_clid ON linkis_ps_configuration_config_value (config_key_id, config_label_id);

DROP TABLE IF EXISTS "linkis_ps_configuration_key_engine_relation";
CREATE TABLE linkis_ps_configuration_key_engine_relation (
	id serial NOT NULL,
	config_key_id int4 NOT NULL,
	engine_type_label_id int4 NOT NULL,
	CONSTRAINT linkis_ps_configuration_key_engine_relation_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_ckid_etlid ON linkis_ps_configuration_key_engine_relation (config_key_id, engine_type_label_id);

DROP TABLE IF EXISTS "linkis_cg_engine_conn_plugin_bml_resources";
CREATE TABLE linkis_cg_engine_conn_plugin_bml_resources (
	id serial NOT NULL,
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

INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (1, 'combined_userCreator_engineType', '*-全局设置,*-*', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (2, 'combined_userCreator_engineType', '*-IDE,*-*', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (3, 'combined_userCreator_engineType', '*-Visualis,*-*', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (4, 'combined_userCreator_engineType', '*-nodeexecution,*-*', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (5, 'combined_userCreator_engineType', '*-*,*-*', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (6, 'combined_userCreator_engineType', '*-*,spark-3.2.1', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (7, 'combined_userCreator_engineType', '*-*,hive-3.1.3', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (8, 'combined_userCreator_engineType', '*-*,python-python2', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (9, 'combined_userCreator_engineType', '*-*,pipeline-1', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (10, 'combined_userCreator_engineType', '*-*,jdbc-4', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (11, 'combined_userCreator_engineType', '*-*,openlookeng-1.5.0', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (12, 'combined_userCreator_engineType', '*-IDE,spark-3.2.1', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (13, 'combined_userCreator_engineType', '*-IDE,hive-3.1.3', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (14, 'combined_userCreator_engineType', '*-IDE,python-python2', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (15, 'combined_userCreator_engineType', '*-IDE,pipeline-1', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (16, 'combined_userCreator_engineType', '*-IDE,jdbc-4', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (17, 'combined_userCreator_engineType', '*-IDE,openlookeng-1.5.0', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (18, 'combined_userCreator_engineType', '*-Visualis,spark-3.2.1', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (19, 'combined_userCreator_engineType', '*-nodeexecution,spark-3.2.1', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (20, 'combined_userCreator_engineType', '*-nodeexecution,hive-3.1.3', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO "linkis_cg_manager_label" ("id", "label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES (21, 'combined_userCreator_engineType', '*-nodeexecution,python-python2', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');

delete from linkis_ps_dm_datasource_type_key;
INSERT INTO "linkis_ps_dm_datasource_type_key" ("id", "data_source_type_id", "key", "name", "name_en", "default_value", "value_type", "scope", "require", "description", "description_en", "value_regex", "ref_id", "ref_value", "data_source", "update_time", "create_time") VALUES (1, 1, 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, '0', '主机名(Host)', 'Host1', NULL, NULL, NULL, NULL, '2022-11-24 20:46:21', '2022-11-24 20:46:21');

DELETE FROM linkis_ps_dm_datasource_type;
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers") VALUES ('kafka', 'kafka', 'kafka', '消息队列', '', 2);
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers") VALUES ('hive', 'hive数据库', 'hive', '大数据存储', '', 3);
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers") VALUES ('elasticsearch','elasticsearch数据源','es无结构化存储','分布式全文索引','',3);


