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

DROP TABLE IF EXISTS "linkis_ps_variable_key_user";
CREATE TABLE linkis_ps_variable_key_user (
	id serial NOT NULL,
	application_id int8 NULL,
	key_id int8 NULL,
	user_name varchar(50) NULL,
	value varchar(200) NULL,
	CONSTRAINT linkis_var_key_user_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_aid_vku ON linkis_ps_variable_key_user (application_id);
CREATE UNIQUE INDEX uniq_aid_kid_uname ON linkis_ps_variable_key_user (application_id, key_id, user_name);
CREATE INDEX idx_key_id ON linkis_ps_variable_key_user (key_id);

DROP TABLE IF EXISTS "linkis_ps_variable_key";
CREATE TABLE linkis_ps_variable_key (
	id serial NOT NULL,
	"key" varchar(50) NULL,
	description varchar(200) NULL,
	"name" varchar(50) NULL,
	application_id int8 NULL,
	default_value varchar(200) NULL,
	value_type varchar(50) NULL,
	value_regex varchar(100) NULL,
	CONSTRAINT linkis_var_key_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_aid_vk ON linkis_ps_variable_key (application_id);

DELETE FROM linkis_ps_variable_key;

insert into linkis_ps_variable_key("key","application_id") values ('mywork1',-1);
insert into linkis_ps_variable_key("key","application_id") values ('mywork2',-1);
insert into linkis_ps_variable_key("key","application_id") values ('mywork3',-1);
insert into linkis_ps_variable_key("key","application_id") values ('mywork4',-1);
insert into linkis_ps_variable_key("key","application_id") values ('mywork5',-1);

DELETE FROM linkis_ps_variable_key_user;

insert into linkis_ps_variable_key_user("application_id","key_id","user_name",value) values (-1,1,'tom1','stu');
insert into linkis_ps_variable_key_user("application_id","key_id","user_name",value) values (-1,2,'tom2','tea');
insert into linkis_ps_variable_key_user("application_id","key_id","user_name",value) values (-1,3,'tom3','bob');
insert into linkis_ps_variable_key_user("application_id","key_id","user_name",value) values (-1,4,'tom4','swim');
insert into linkis_ps_variable_key_user("application_id","key_id","user_name",value) values (-1,5,'tom5','smile');