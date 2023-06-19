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

DROP TABLE IF EXISTS "linkis_ps_datasource_table";
CREATE TABLE linkis_ps_datasource_table (
	id serial NOT NULL,
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
CREATE UNIQUE INDEX uniq_db_name ON linkis_ps_datasource_table (database, name);

DROP TABLE IF EXISTS "linkis_ps_datasource_field";
CREATE TABLE linkis_ps_datasource_field (
	id serial NOT NULL,
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
	id serial NOT NULL,
	table_id int8 NOT NULL,
	import_type int4 NOT NULL,
	args varchar(255) NOT NULL,
	CONSTRAINT linkis_mdq_import_pkey PRIMARY KEY (id)
);

DROP TABLE IF EXISTS "linkis_ps_datasource_lineage";
CREATE TABLE linkis_ps_datasource_lineage (
	id serial NOT NULL,
	table_id int8 NULL,
	source_table varchar(64) NULL,
	update_time timestamp(6) NULL,
	CONSTRAINT linkis_mdq_lineage_pkey PRIMARY KEY (id)
);


INSERT INTO linkis_ps_datasource_table (database,name,alias,creator,comment,create_time,product_name,project_name,usage,lifecycle,use_way,is_import,model_level,is_external_use,is_partition_table,is_available) VALUES
	 ('ods_user_md_ind','t_student_temp','t_student_temp','hadoop','','2022-04-14 18:53:20','','','测试',0,2,'0',0,'0','1','1');
INSERT INTO linkis_ps_datasource_field (table_id,name,alias,type,comment,express,rule,is_partition_field,is_primary,length) VALUES
     (1,'name','','string','',null,null,'0','0',255);
INSERT INTO linkis_ps_datasource_import (table_id,import_type,args) VALUES (1,1,'where 1=1');
INSERT INTO linkis_ps_datasource_lineage (table_id,source_table) VALUES (1,'db_test');

DROP TABLE IF EXISTS "DBS";
CREATE TABLE "DBS" (
	"DB_ID" int8 NOT NULL,
	"DESC" varchar(4000) NULL DEFAULT NULL,
	"DB_LOCATION_URI" varchar(4000) NOT NULL,
	"NAME" varchar(128) NULL DEFAULT NULL,
	"OWNER_NAME" varchar(128) NULL DEFAULT NULL,
	"OWNER_TYPE" varchar(10) NULL DEFAULT NULL,
	"CTLG_NAME" varchar(256) NULL,
	CONSTRAINT "DBS_pkey" PRIMARY KEY ("DB_ID")
);

DROP TABLE IF EXISTS "SDS";
CREATE TABLE "SDS" (
	"SD_ID" int8 NOT NULL,
	"INPUT_FORMAT" varchar(4000) NULL DEFAULT NULL,
	"IS_COMPRESSED" bool NOT NULL,
	"LOCATION" varchar(4000) NULL DEFAULT NULL,
	"NUM_BUCKETS" int8 NOT NULL,
	"OUTPUT_FORMAT" varchar(4000) NULL DEFAULT NULL,
	"SERDE_ID" int8 NULL,
	"CD_ID" int8 NULL,
	"IS_STOREDASSUBDIRECTORIES" bool NOT NULL,
	CONSTRAINT "SDS_pkey" PRIMARY KEY ("SD_ID")
);

DROP TABLE IF EXISTS "TBLS";
CREATE TABLE "TBLS" (
	"TBL_ID" int8 NOT NULL,
	"CREATE_TIME" int8 NOT NULL,
	"DB_ID" int8 NULL,
	"LAST_ACCESS_TIME" int8 NOT NULL,
	"OWNER" varchar(767) NULL DEFAULT NULL,
	"OWNER_TYPE" varchar(10) NULL DEFAULT NULL,
	"RETENTION" int8 NOT NULL,
	"SD_ID" int8 NULL,
	"TBL_NAME" varchar(256) NULL DEFAULT NULL,
	"TBL_TYPE" varchar(128) NULL DEFAULT NULL,
	"VIEW_EXPANDED_TEXT" text NULL,
	"VIEW_ORIGINAL_TEXT" text NULL,
	"IS_REWRITE_ENABLED" bool NOT NULL DEFAULT false,
	"WRITE_ID" int8 NULL DEFAULT 0,
	CONSTRAINT "TBLS_pkey" PRIMARY KEY ("TBL_ID")
);

INSERT INTO "DBS" ("DB_ID","DESC","DB_LOCATION_URI","NAME","OWNER_NAME","OWNER_TYPE","CTLG_NAME") VALUES
	 (1,'Default Hive database','hdfs://hadoops/user/hive/warehouse','default','public','ROLE','hive');
INSERT INTO "TBLS" ("TBL_ID","CREATE_TIME","DB_ID","LAST_ACCESS_TIME","OWNER","OWNER_TYPE","RETENTION","SD_ID","TBL_NAME","TBL_TYPE","VIEW_EXPANDED_TEXT","VIEW_ORIGINAL_TEXT","IS_REWRITE_ENABLED") VALUES
	 (1,1648518600,1,0,'hadoop','USER',0,1,'employee','MANAGED_TABLE',NULL,NULL,'0');
INSERT INTO "SDS" ("SD_ID","CD_ID","INPUT_FORMAT","IS_COMPRESSED","IS_STOREDASSUBDIRECTORIES","LOCATION","NUM_BUCKETS","OUTPUT_FORMAT","SERDE_ID") VALUES
	 (1,1,'org.apache.hadoop.mapred.TextInputFormat','0','0','hdfs://hadoops/user/hive/warehouse/hivedemo.db/employee',-1,'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat',1);

DROP TABLE IF EXISTS "ROLES";
CREATE TABLE public."ROLES" (
	"ROLE_ID" int8 NOT NULL,
	"CREATE_TIME" int8 NOT NULL,
	"OWNER_NAME" varchar(128) NULL DEFAULT NULL,
	"ROLE_NAME" varchar(128) NULL DEFAULT NULL
);


DROP TABLE IF EXISTS "ROLE_MAP";
CREATE TABLE "ROLE_MAP" (
	"ROLE_GRANT_ID" int8 NOT NULL,
	"ADD_TIME" int8 NOT NULL,
	"GRANT_OPTION" int2 NOT NULL,
	"GRANTOR" varchar(128) NULL DEFAULT NULL,
	"GRANTOR_TYPE" varchar(128) NULL DEFAULT NULL,
	"PRINCIPAL_NAME" varchar(128) NULL DEFAULT NULL,
	"PRINCIPAL_TYPE" varchar(128) NULL DEFAULT NULL,
	"ROLE_ID" int8 NULL,
	CONSTRAINT "ROLE_MAP_pkey" PRIMARY KEY ("ROLE_GRANT_ID")
);

DROP TABLE IF EXISTS "DB_PRIVS";
CREATE TABLE "DB_PRIVS" (
	"DB_GRANT_ID" int8 NOT NULL,
	"CREATE_TIME" int8 NOT NULL,
	"DB_ID" int8 NULL,
	"GRANT_OPTION" int2 NOT NULL,
	"GRANTOR" varchar(128) NULL DEFAULT NULL,
	"GRANTOR_TYPE" varchar(128) NULL DEFAULT NULL,
	"PRINCIPAL_NAME" varchar(128) NULL DEFAULT NULL,
	"PRINCIPAL_TYPE" varchar(128) NULL DEFAULT NULL,
	"DB_PRIV" varchar(128) NULL DEFAULT NULL,
	"AUTHORIZER" varchar(128) NULL DEFAULT NULL,
	CONSTRAINT "DB_PRIVS_pkey" PRIMARY KEY ("DB_GRANT_ID")
);

DROP TABLE IF EXISTS "TBL_PRIVS";
CREATE TABLE "TBL_PRIVS" (
	"TBL_GRANT_ID" int8 NOT NULL,
	"CREATE_TIME" int8 NOT NULL,
	"GRANT_OPTION" int2 NOT NULL,
	"GRANTOR" varchar(128) NULL DEFAULT NULL,
	"GRANTOR_TYPE" varchar(128) NULL DEFAULT NULL,
	"PRINCIPAL_NAME" varchar(128) NULL DEFAULT NULL,
	"PRINCIPAL_TYPE" varchar(128) NULL DEFAULT NULL,
	"TBL_PRIV" varchar(128) NULL DEFAULT NULL,
	"TBL_ID" int8 NULL,
	"AUTHORIZER" varchar(128) NULL DEFAULT NULL,
	CONSTRAINT "TBL_PRIVS_pkey" PRIMARY KEY ("TBL_GRANT_ID")
);


DROP TABLE IF EXISTS "PARTITION_PARAMS";
CREATE TABLE "PARTITION_PARAMS" (
	"PART_ID" int8 NOT NULL,
	"PARAM_KEY" varchar(256) NOT NULL,
	"PARAM_VALUE" text NULL,
	CONSTRAINT "PARTITION_PARAMS_pkey" PRIMARY KEY ("PART_ID", "PARAM_KEY")
);

DROP TABLE IF EXISTS "PARTITIONS";
CREATE TABLE "PARTITIONS" (
	"PART_ID" int8 NOT NULL,
	"CREATE_TIME" int8 NOT NULL,
	"LAST_ACCESS_TIME" int8 NOT NULL,
	"PART_NAME" varchar(767),
	"SD_ID" int8 NULL,
	"TBL_ID" int8 NULL,
	"WRITE_ID" int8 NULL DEFAULT 0
);

DROP TABLE IF EXISTS "COLUMNS_V2";
CREATE TABLE "COLUMNS_V2" (
	"CD_ID" int8 NOT NULL,
	"COMMENT" varchar(4000) NULL,
	"COLUMN_NAME" varchar(767) NOT NULL,
	"TYPE_NAME" text NULL,
	"INTEGER_IDX" int4 NOT NULL,
	CONSTRAINT "COLUMNS_V2_pkey" PRIMARY KEY ("CD_ID", "COLUMN_NAME")
);

DROP TABLE IF EXISTS "PARTITION_KEYS";
CREATE TABLE "PARTITION_KEYS" (
	"TBL_ID" int8 NOT NULL,
	"PKEY_COMMENT" varchar(4000),
	"PKEY_NAME" varchar(128) NOT NULL,
	"PKEY_TYPE" varchar(767) NOT NULL,
	"INTEGER_IDX" int8 NOT NULL
);

INSERT INTO "ROLES" ("ROLE_ID","CREATE_TIME","OWNER_NAME","ROLE_NAME") VALUES (2,1647872356,'public','public');
INSERT INTO "PARTITION_PARAMS" ("PART_ID","PARAM_KEY","PARAM_VALUE") VALUES (3,'totalSize',3);
INSERT INTO "PARTITIONS" ("PART_ID","CREATE_TIME","LAST_ACCESS_TIME","PART_NAME","SD_ID","TBL_ID") VALUES
 (3,1650266917,0,'ds=202202',1,1);
INSERT INTO "COLUMNS_V2" ("CD_ID","COMMENT","COLUMN_NAME","TYPE_NAME","INTEGER_IDX") VALUES (1,'','destination','string',0);
INSERT INTO "PARTITION_KEYS" ("TBL_ID","PKEY_COMMENT","PKEY_NAME","PKEY_TYPE","INTEGER_IDX") VALUES
 (1,'','ds','string',0);