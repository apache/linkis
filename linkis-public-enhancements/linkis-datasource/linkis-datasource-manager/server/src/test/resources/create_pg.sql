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
 
DROP TABLE IF EXISTS "linkis_ps_dm_datasource";
CREATE TABLE linkis_ps_dm_datasource (
	id serial NOT NULL,
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
CREATE UNIQUE INDEX uniq_datasource_name ON linkis_ps_dm_datasource (datasource_name);

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

DROP TABLE IF EXISTS "linkis_ps_dm_datasource_version";
CREATE TABLE linkis_ps_dm_datasource_version (
	version_id serial NOT NULL,
	datasource_id int8 NOT NULL,
	"parameter" varchar(2048) NULL,
	"comment" varchar(255) NULL,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_user varchar(255) NULL,
	CONSTRAINT linkis_ps_dm_datasource_version_pkey PRIMARY KEY (version_id,datasource_id)
);
CREATE UNIQUE INDEX uniq_vid_did ON linkis_ps_dm_datasource_version (version_id,"datasource_id");


delete from linkis_ps_dm_datasource_type;
INSERT INTO "linkis_ps_dm_datasource_type" ("name", "description", "option", "classifier", "icon", "layers")
VALUES ('mysql', 'mysql db', 'mysql db', 'relation database', '2000', 3);


delete from linkis_ps_dm_datasource_type_key;
INSERT INTO "linkis_ps_dm_datasource_type_key"
("data_source_type_id", "key", "name","name_en", "default_value", "value_type", "scope", "require", "description", "update_time", "create_time") VALUES
 (1,'host', 'Host', '127.0.0.1','127.0.0.1','TEXT', 'ENV','1','host name','2021-04-08 03:13:36','2021-04-08 03:13:36');

INSERT INTO "linkis_ps_dm_datasource_type_key"
("data_source_type_id", "key", "name", "name_en","default_value", "value_type", "scope", "require", "description", "update_time", "create_time") VALUES
 (1, 'port', 'Port', '3306','3306','TEXT', null,'1','port name','2021-04-08 03:13:36','2021-04-08 03:13:36');
