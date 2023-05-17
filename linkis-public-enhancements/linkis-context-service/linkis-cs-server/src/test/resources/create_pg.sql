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

DROP TABLE IF EXISTS "linkis_ps_cs_context_history";
CREATE TABLE linkis_ps_cs_context_history (
	id serial NOT NULL,
	context_id int4 NULL,
	"source" text NULL,
	context_type varchar(32) NULL,
	history_json text NULL,
	keyword varchar(255) NULL,
	update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	access_time timestamp(6) DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_cs_context_history_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_keyword ON linkis_ps_cs_context_history (keyword);

DROP TABLE IF EXISTS "linkis_ps_cs_context_listener";
CREATE TABLE linkis_ps_cs_context_listener (
	id serial NOT NULL,
	listener_source varchar(255) NULL,
	context_id int4 NULL,
	update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	access_time timestamp(6) DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_cs_context_listener_pkey PRIMARY KEY (id)
);

DROP TABLE IF EXISTS "linkis_ps_cs_context_id";
CREATE TABLE linkis_ps_cs_context_id (
	id serial NOT NULL,
	"user" varchar(32) NULL,
	application varchar(32) NULL,
	"source" varchar(255) NULL,
	expire_type varchar(32) NULL,
	expire_time timestamp(6) NULL,
	"instance" varchar(128) NULL,
	backup_instance varchar(255) NULL,
	update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	access_time timestamp(6) DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_cs_context_id_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_backup_instance ON linkis_ps_cs_context_id (backup_instance);
CREATE INDEX idx_instance ON linkis_ps_cs_context_id (instance);
CREATE INDEX idx_instance_bin ON linkis_ps_cs_context_id (instance, backup_instance);

DROP TABLE IF EXISTS "linkis_ps_cs_context_map_listener";
CREATE TABLE linkis_ps_cs_context_map_listener (
	id serial NOT NULL,
	listener_source varchar(255) NULL,
	key_id int4 NULL,
	update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	access_time timestamp(6) DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_cs_context_map_listener_pkey PRIMARY KEY (id)
);

DROP TABLE IF EXISTS "linkis_ps_cs_context_map";
CREATE TABLE linkis_ps_cs_context_map (
	id serial NOT NULL,
	"key" varchar(128) NULL,
	context_scope varchar(32) NULL,
	context_type varchar(32) NULL,
	props text NULL,
	value text NULL,
	context_id int4 NULL,
	keywords varchar(255) NULL,
	update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	access_time timestamp(6) DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_cs_context_map_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_key_cid_ctype ON linkis_ps_cs_context_map (key, context_id, context_type);
CREATE INDEX idx_keywords ON linkis_ps_cs_context_map (keywords);