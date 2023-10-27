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

DROP TABLE IF EXISTS "linkis_ps_instance_info";
CREATE TABLE linkis_ps_instance_info (
	id serial NOT NULL,
	"instance" varchar(128) NULL,
	"name" varchar(128) NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_instance_info_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_instance_ii ON linkis_ps_instance_info USING btree (instance);


DROP TABLE IF EXISTS "linkis_ps_instance_label";
CREATE TABLE linkis_ps_instance_label (
	id serial NOT NULL,
	label_key varchar(32) NOT NULL,
	label_value varchar(255) NOT NULL,
	label_feature varchar(16) NOT NULL,
	label_value_size int4 NOT NULL,
	update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_instance_label_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_lk_lv_il ON linkis_ps_instance_label USING btree (label_key, label_value);

DROP TABLE IF EXISTS "linkis_ps_instance_label_relation";
CREATE TABLE linkis_ps_instance_label_relation (
	id serial NOT NULL,
	label_id int4 NULL,
	service_instance varchar(128) NOT NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_instance_label_relation_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_lid_instance ON linkis_ps_instance_label_relation USING btree (label_id, service_instance);


DROP TABLE IF EXISTS "linkis_ps_instance_label_value_relation";
CREATE TABLE linkis_ps_instance_label_value_relation (
	id serial NOT NULL,
	label_value_key varchar(255) NOT NULL,
	label_value_content varchar(255) NULL,
	label_id int4 NULL,
	update_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	create_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_ps_instance_label_value_relation_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_lvk_lid_ilvr ON linkis_ps_instance_label_value_relation USING btree (label_value_key, label_id);
