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
 
DROP TABLE IF EXISTS "linkis_ps_bml_project_resource";
CREATE TABLE linkis_ps_bml_project_resource (
	id serial NOT NULL,
	project_id int4 NOT NULL,
	resource_id varchar(128) NULL,
	CONSTRAINT linkis_bml_project_resource_pkey PRIMARY KEY (id)
);

DROP TABLE IF EXISTS "linkis_ps_bml_project";
CREATE TABLE linkis_ps_bml_project (
	id serial NOT NULL,
	"name" varchar(128) NULL,
	"system" varchar(64) NOT NULL DEFAULT 'dss',
	"source" varchar(1024) NULL,
	description varchar(1024) NULL,
	creator varchar(128) NOT NULL,
	enabled int2 NULL DEFAULT 1,
	create_time timestamp(6) NULL DEFAULT now(),
	CONSTRAINT linkis_bml_project_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_name_bp ON linkis_ps_bml_project (name);

DROP TABLE IF EXISTS "linkis_ps_bml_project_user";
CREATE TABLE linkis_ps_bml_project_user (
	id serial NOT NULL,
	project_id int4 NOT NULL,
	username varchar(64) NULL,
	priv int4 NOT NULL,
	creator varchar(128) NOT NULL,
	create_time timestamp(6) NULL DEFAULT now(),
	expire_time timestamp(6) NULL,
	CONSTRAINT linkis_bml_project_user_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_name_pid ON linkis_ps_bml_project_user (username, project_id);

DROP TABLE IF EXISTS "linkis_ps_bml_resources_version";
CREATE TABLE linkis_ps_bml_resources_version (
	id serial NOT NULL,
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
CREATE UNIQUE INDEX uniq_rid_version ON linkis_ps_bml_resources_version (resource_id, version);

DROP TABLE IF EXISTS "linkis_ps_bml_resources_task";
CREATE TABLE linkis_ps_bml_resources_task (
	id serial NOT NULL,
	resource_id varchar(50) NULL,
	"version" varchar(20) NULL,
	operation varchar(20) NOT NULL,
	state varchar(20) NOT NULL DEFAULT 'Schduled',
	submit_user varchar(20) NOT NULL,
	"system" varchar(20) NULL DEFAULT 'dss',
	"instance" varchar(128) NOT NULL,
	client_ip varchar(50) NULL,
	extra_params text NULL,
	err_msg varchar(2000) NULL,
	start_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	end_time timestamp(6) NULL,
	last_update_time timestamp(6) NOT NULL,
	CONSTRAINT linkis_resources_task_pkey PRIMARY KEY (id)
);

DROP TABLE IF EXISTS "linkis_ps_bml_resources";
CREATE TABLE linkis_ps_bml_resources (
	id serial NOT NULL,
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

DROP TABLE IF EXISTS "linkis_ps_resources_download_history";
CREATE TABLE linkis_ps_resources_download_history (
	id serial NOT NULL,
	start_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	end_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	client_ip varchar(200) NOT NULL,
	state int2 NOT NULL,
	resource_id varchar(50) NOT NULL,
	"version" varchar(20) NOT NULL,
	downloader varchar(50) NOT NULL,
	CONSTRAINT linkis_resources_download_history_pkey PRIMARY KEY (id)
);
``


insert  into linkis_ps_bml_project_user(project_id, username, priv, creator, create_time) values ( 1, 'creCreatorUser', 2, 'creatorTest', now());
insert  into linkis_ps_bml_project(name, "system", source, description, creator, enabled, create_time)values('testName', 'testSy','test', 'descTest','creCreatorUser', 1, now());
insert  into linkis_ps_bml_project_resource(project_id, resource_id) values(1, '123');