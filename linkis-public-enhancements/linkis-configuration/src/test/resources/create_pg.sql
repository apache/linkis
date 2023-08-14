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

DROP TABLE IF EXISTS "linkis_ps_configuration_category";
CREATE TABLE linkis_ps_configuration_category (
	id serial NOT NULL,
	label_id int4 NOT NULL,
	"level" int4 NOT NULL,
	description varchar(200) NULL,
	tag varchar(200) NULL,
	update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_configuration_category_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_label_id_cc ON linkis_ps_configuration_category (label_id);

DROP TABLE IF EXISTS "linkis_cg_tenant_label_config";
CREATE TABLE "linkis_cg_tenant_label_config" (
  id serial NOT NULL,
  "user" varchar(50) NOT NULL,
  "creator" varchar(50) NOT NULL,
  "tenant_value" varchar(128) NOT NULL,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "desc" varchar(100) NOT NULL,
  "bussiness_user" varchar(50) NOT NULL,
  CONSTRAINT linkis_cg_tenant_label_config_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_user_creator_tlc ON linkis_cg_tenant_label_config ("user",creator);


DROP TABLE IF EXISTS "linkis_cg_user_ip_config";
CREATE TABLE "linkis_cg_user_ip_config" (
  id serial NOT NULL,
  "user" varchar(50) NOT NULL,
  "creator" varchar(50) NOT NULL,
  "ip_list" text NOT NULL,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "desc" varchar(100) NOT NULL,
  "bussiness_user" varchar(50) NOT NULL,
  CONSTRAINT linkis_cg_user_ip_config_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_user_creator_uic ON linkis_cg_user_ip_config ("user",creator);


DELETE FROM linkis_cg_manager_label;

insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType','*-*,*-*', 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType','*-IDE,*-*', 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType','*-Visualis,*-*', 'OPTIONAL', 2, now(), now());
insert into "linkis_cg_manager_label" ("label_key", "label_value", "label_feature", "label_value_size", "update_time", "create_time") VALUES ('combined_userCreator_engineType','*-nodeexecution,*-*', 'OPTIONAL', 2, now(), now());

INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName") VALUES ('wds.linkis.rm.yarnqueue', 'yarn队列名', 'yarn队列名', 'ide', 'None', NULL, '0', '0', '1', '队列资源');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName") VALUES ('wds.linkis.rm.yarnqueue.instance.max', '取值范围：1-128，单位：个', '队列实例最大个数', '30', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|128)$', '0', '0', '1', '队列资源');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName") VALUES ('wds.linkis.rm.yarnqueue.cores.max', '取值范围：1-500，单位：个', '队列CPU使用上限', '150', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|500)$', '0', '0', '1', '队列资源');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName") VALUES ('wds.linkis.rm.yarnqueue.memory.max', '取值范围：1-1000，单位：G', '队列内存使用上限', '300G', 'Regex', '^([1-9]\\d{0,2}|1000)(G|g)$', '0', '0', '1', '队列资源');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName") VALUES ('wds.linkis.rm.client.memory.max', '取值范围：1-100，单位：G', '全局各个引擎内存使用上限', '20G', 'Regex', '^([1-9]\\d{0,1}|100)(G|g)$', '0', '0', '1', '队列资源');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName") VALUES ('wds.linkis.rm.client.core.max', '取值范围：1-128，单位：个', '全局各个引擎核心个数上限', '10', 'Regex', '^(?:[1-9]\\d?|[1][0-2][0-8])$', '0', '0', '1', '队列资源');
INSERT INTO "linkis_ps_configuration_config_key" ("key", "description", "name", "default_value", "validate_type", "validate_range", "is_hidden", "is_advanced", "level", "treeName") VALUES ('wds.linkis.rm.instance', '范围：1-20，单位：个', '全局各个引擎最大并发数', '10', 'NumInterval', '[1,20]', '0', '0', '1', '队列资源');

insert into "linkis_ps_configuration_key_engine_relation" ("config_key_id", "engine_type_label_id") values (1,1);
insert into "linkis_ps_configuration_key_engine_relation" ("config_key_id", "engine_type_label_id") values (2,1);
insert into "linkis_ps_configuration_key_engine_relation" ("config_key_id", "engine_type_label_id") values (3,1);
insert into "linkis_ps_configuration_key_engine_relation" ("config_key_id", "engine_type_label_id") values (4,1);
insert into "linkis_ps_configuration_key_engine_relation" ("config_key_id", "engine_type_label_id") values (5,1);
insert into "linkis_ps_configuration_key_engine_relation" ("config_key_id", "engine_type_label_id") values (6,1);
insert into "linkis_ps_configuration_key_engine_relation" ("config_key_id", "engine_type_label_id") values (7,1);

insert into "linkis_ps_configuration_config_value" ("config_key_id", "config_value", "config_label_id") values (1,'1',1);
insert into "linkis_ps_configuration_config_value" ("config_key_id", "config_value", "config_label_id") values (2,'1',1);
insert into "linkis_ps_configuration_config_value" ("config_key_id", "config_value", "config_label_id") values (3,'1',1);
insert into "linkis_ps_configuration_config_value" ("config_key_id", "config_value", "config_label_id") values (4,'1',1);
insert into "linkis_ps_configuration_config_value" ("config_key_id", "config_value", "config_label_id") values (5,'1',1);
insert into "linkis_ps_configuration_config_value" ("config_key_id", "config_value", "config_label_id") values (6,'1',1);
insert into "linkis_ps_configuration_config_value" ("config_key_id", "config_value", "config_label_id") values (7,'1',1);

insert into linkis_ps_configuration_category ("label_id", "level") VALUES (1, 1);
insert into linkis_ps_configuration_category ("label_id", "level") VALUES (2, 1);
insert into linkis_ps_configuration_category ("label_id", "level") VALUES (3, 1);
