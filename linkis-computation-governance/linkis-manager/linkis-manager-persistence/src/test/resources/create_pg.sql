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


CREATE TABLE linkis_cg_manager_service_instance (
	id serial NOT NULL,
	"instance" varchar(128) NULL,
	"name" varchar(32) NULL,
	"owner" varchar(32) NULL,
	mark varchar(32) NULL,
	identifier varchar(32) NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	updator varchar(32) NULL,
	creator varchar(32) NULL,
	CONSTRAINT linkis_manager_service_instance_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_instance_msi ON linkis_cg_manager_service_instance (instance);


CREATE TABLE linkis_cg_manager_linkis_resources (
	id serial NOT NULL,
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


CREATE TABLE linkis_cg_manager_lock (
	id serial NOT NULL,
	lock_object varchar(255) NULL,
	time_out text NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_manager_lock_pkey PRIMARY KEY (id)
);



DROP TABLE IF EXISTS "linkis_cg_rm_external_resource_provider";
CREATE TABLE linkis_cg_rm_external_resource_provider (
	id serial NOT NULL,
	resource_type varchar(32) NOT NULL,
	"name" varchar(32) NOT NULL,
	labels varchar(32) NULL,
	config text NOT NULL,
	CONSTRAINT linkis_external_resource_provider_pkey PRIMARY KEY (id)
);


CREATE TABLE linkis_cg_manager_engine_em (
	id serial NOT NULL,
	engine_instance varchar(128) NULL,
	em_instance varchar(128) NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_manager_engine_em_pkey PRIMARY KEY (id)
);


DROP TABLE IF EXISTS "linkis_cg_manager_label";
CREATE TABLE linkis_cg_manager_label (
	id serial NOT NULL,
	label_key varchar(32) NOT NULL,
	label_value varchar(255) NOT NULL,
	label_feature varchar(16) NOT NULL,
	label_value_size int4 NOT NULL,
	update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_manager_label_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_lk_lv_ml ON linkis_cg_manager_label (label_key, label_value);


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


DROP TABLE IF EXISTS "linkis_cg_manager_label_resource";
CREATE TABLE linkis_cg_manager_label_resource (
	id serial NOT NULL,
	label_id int4 NULL,
	resource_id int4 NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_manager_label_resource_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_label_id_mlr ON linkis_cg_manager_label_resource (label_id);

CREATE TABLE linkis_cg_manager_label_service_instance (
	id serial NOT NULL,
	label_id int4 NULL,
	service_instance varchar(128) NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_manager_label_service_instance_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_lid_instance ON linkis_cg_manager_label_service_instance (label_id,service_instance);

CREATE TABLE linkis_cg_manager_label_user (
	id serial NOT NULL,
	username varchar(255) NULL,
	label_id int4 NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_manager_label_user_pkey PRIMARY KEY (id)
);

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

CREATE TABLE linkis_cg_ec_resource_info_record (
	id serial NOT NULL,
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
CREATE UNIQUE INDEX uniq_tid_lv ON linkis_cg_ec_resource_info_record (ticket_id, label_value);

INSERT INTO linkis_cg_manager_label_resource (label_id, resource_id, update_time, create_time) VALUES(2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO linkis_cg_manager_label_resource (label_id, resource_id, update_time, create_time) VALUES(1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO linkis_cg_manager_label_resource (label_id, resource_id, update_time, create_time) VALUES(3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO linkis_cg_manager_label_service_instance(label_id, service_instance, update_time, create_time) VALUES (2, 'instance1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO linkis_cg_manager_label_user(username, label_id, update_time, create_time)VALUES('testname', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO linkis_cg_manager_label (label_key,label_value,label_feature,label_value_size,update_time,create_time) VALUES ('combined_userCreator_engineType','*-LINKISCLI,*-*','OPTIONAL',2,'2022-03-28 01:31:08.0','2022-03-28 01:31:08.0');
INSERT INTO linkis_cg_manager_service_instance(instance, name, owner, mark, update_time, create_time, updator, creator)VALUES('instance1', 'testname', 'testowner', 'testmark', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'testupdator', 'testcreator');
INSERT INTO linkis_cg_manager_engine_em (engine_instance,em_instance,update_time,create_time) VALUES ('instance1','instance1','2022-02-26 14:54:05.0','2022-02-26 14:54:05.0');
INSERT INTO linkis_cg_manager_lock(lock_object, time_out, update_time, create_time)VALUES('testjson', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO linkis_cg_manager_linkis_resources ("max_resource","min_resource","used_resource","left_resource","expected_resource","locked_resource","resourceType","ticketId","update_time","create_time","updator","creator" )
VALUES ('testmax','mintest','user','left',null,null,'testtype',null,now(),now(),null,null);
INSERT INTO linkis_cg_manager_linkis_resources ("max_resource","min_resource","used_resource","left_resource","expected_resource","locked_resource","resourceType","ticketId","update_time","create_time","updator","creator" )
VALUES ('testmax2','mintest2','user2','left2',null,null,'testtype2',null,now(),now(),null,null);
INSERT INTO linkis_cg_manager_linkis_resources ("max_resource","min_resource","used_resource","left_resource","expected_resource","locked_resource","resourceType","ticketId","update_time","create_time","updator","creator" )
VALUES ('testmax3','mintest3','user3','left3',null,null,'testtype3','1',now(),now(),null,null);