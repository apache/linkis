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

SET FOREIGN_KEY_CHECKS=0;
SET REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS linkis_ps_datasource_table CASCADE;
CREATE TABLE linkis_ps_datasource_table (
  id bigint(255)  AUTO_INCREMENT,
  database varchar(64)  ,
  name varchar(64)  ,
  alias varchar(64)  DEFAULT NULL,
  creator varchar(16)  ,
  comment varchar(255)  DEFAULT NULL,
  create_time datetime ,
  product_name varchar(64)  DEFAULT NULL,
  project_name varchar(255)  DEFAULT NULL,
  usage varchar(128)  DEFAULT NULL,
  lifecycle int(4) ,
  use_way int(4) ,
  is_import tinyint(1) ,
  model_level int(4) ,
  is_external_use tinyint(1) ,
  is_partition_table tinyint(1) ,
  is_available tinyint(1) ,
  PRIMARY KEY (id),
  UNIQUE KEY database (database,name)
) ;

DROP TABLE IF EXISTS linkis_ps_datasource_field CASCADE;
CREATE TABLE linkis_ps_datasource_field (
  id bigint(20)  AUTO_INCREMENT,
  table_id bigint(20) ,
  name varchar(64)  ,
  alias varchar(64)  DEFAULT NULL,
  type varchar(64)  ,
  comment varchar(255)  DEFAULT NULL,
  express varchar(255)  DEFAULT NULL,
  rule varchar(128)  DEFAULT NULL,
  is_partition_field tinyint(1) ,
  is_primary tinyint(1) ,
  length int(11) DEFAULT NULL,
  mode_info varchar(128)  DEFAULT NULL,
  PRIMARY KEY (id)
) ;

DROP TABLE IF EXISTS linkis_ps_datasource_import CASCADE;
CREATE TABLE linkis_ps_datasource_import (
  id bigint(20)  AUTO_INCREMENT,
  table_id bigint(20) ,
  import_type int(4) ,
  args varchar(255)  ,
  PRIMARY KEY (id)
) ;

DROP TABLE IF EXISTS linkis_ps_datasource_lineage CASCADE;
CREATE TABLE linkis_ps_datasource_lineage (
  id bigint(20)  AUTO_INCREMENT,
  table_id bigint(20) DEFAULT NULL,
  source_table varchar(64)  DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  PRIMARY KEY (id)
) ;


INSERT INTO linkis_ps_datasource_table (database,name,alias,creator,comment,create_time,product_name,project_name,usage,lifecycle,use_way,is_import,model_level,is_external_use,is_partition_table,is_available) VALUES
	 ('ods_user_md_ind','t_student_temp','t_student_temp','hadoop','','2022-04-14 18:53:20','','','测试',0,2,0,0,0,1,1);
INSERT INTO linkis_ps_datasource_field (table_id,name,alias,type,comment,express,rule,is_partition_field,is_primary,length) VALUES
     (1,'name','','string','',null,null,0,0,255);
INSERT INTO linkis_ps_datasource_import (table_id,import_type,args) VALUES (1,1,'where 1=1');
INSERT INTO linkis_ps_datasource_lineage (table_id,source_table) VALUES (1,'db_test');

DROP TABLE IF EXISTS DBS CASCADE;
CREATE TABLE DBS (
  DB_ID bigint(20) ,
  DESC varchar(256)  DEFAULT NULL,
  DB_LOCATION_URI varchar(4000) ,
  NAME varchar(128)   DEFAULT NULL,
  OWNER_NAME varchar(128)   DEFAULT NULL,
  OWNER_TYPE varchar(10)   DEFAULT NULL,
  CTLG_NAME varchar(256) DEFAULT 'hive',
  PRIMARY KEY (DB_ID)
) ;

DROP TABLE IF EXISTS SDS CASCADE;
CREATE TABLE SDS (
  SD_ID bigint(20) ,
  CD_ID bigint(20) DEFAULT NULL,
  INPUT_FORMAT varchar(4000)  DEFAULT NULL,
  IS_COMPRESSED bit(1) ,
  IS_STOREDASSUBDIRECTORIES bit(1) ,
  LOCATION varchar(4000)  DEFAULT NULL,
  NUM_BUCKETS int(11) ,
  OUTPUT_FORMAT varchar(4000)  DEFAULT NULL,
  SERDE_ID bigint(20) DEFAULT NULL,
  PRIMARY KEY (SD_ID)
) ;

DROP TABLE IF EXISTS TBLS CASCADE;
CREATE TABLE TBLS (
  TBL_ID bigint(20) ,
  CREATE_TIME int(11) ,
  DB_ID bigint(20) DEFAULT NULL,
  LAST_ACCESS_TIME int(11) ,
  OWNER varchar(767)  DEFAULT NULL,
  OWNER_TYPE varchar(10)  DEFAULT NULL,
  RETENTION int(11) ,
  SD_ID bigint(20) DEFAULT NULL,
  TBL_NAME varchar(256)  DEFAULT NULL,
  TBL_TYPE varchar(128)  DEFAULT NULL,
  VIEW_EXPANDED_TEXT mediumtext,
  VIEW_ORIGINAL_TEXT mediumtext,
  IS_REWRITE_ENABLED bit(1) ,
  PRIMARY KEY (TBL_ID)
) ;

INSERT INTO DBS (DB_ID,DESC,DB_LOCATION_URI,NAME,OWNER_NAME,OWNER_TYPE,CTLG_NAME) VALUES
	 (1,'Default Hive database','hdfs://hadoops/user/hive/warehouse','default','public','ROLE','hive');
INSERT INTO TBLS (TBL_ID,CREATE_TIME,DB_ID,LAST_ACCESS_TIME,OWNER,OWNER_TYPE,RETENTION,SD_ID,TBL_NAME,TBL_TYPE,VIEW_EXPANDED_TEXT,VIEW_ORIGINAL_TEXT,IS_REWRITE_ENABLED) VALUES
	 (1,1648518600,1,0,'hadoop','USER',0,1,'employee','MANAGED_TABLE',NULL,NULL,0);
INSERT INTO SDS (SD_ID,CD_ID,INPUT_FORMAT,IS_COMPRESSED,IS_STOREDASSUBDIRECTORIES,LOCATION,NUM_BUCKETS,OUTPUT_FORMAT,SERDE_ID) VALUES
	 (1,1,'org.apache.hadoop.mapred.TextInputFormat',0,0,'hdfs://hadoops/user/hive/warehouse/hivedemo.db/employee',-1,'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat',1);

DROP TABLE IF EXISTS ROLES CASCADE;
CREATE TABLE ROLES (
  ROLE_ID bigint(20) ,
  CREATE_TIME int(11) ,
  OWNER_NAME varchar(128)  DEFAULT NULL,
  ROLE_NAME varchar(128)  DEFAULT NULL,
  PRIMARY KEY (ROLE_ID)
) ;

DROP TABLE IF EXISTS ROLE_MAP CASCADE;
CREATE TABLE ROLE_MAP (
  ROLE_GRANT_ID bigint(20) ,
  ADD_TIME int(11) ,
  GRANT_OPTION smallint(6) ,
  GRANTOR varchar(128)  DEFAULT NULL,
  GRANTOR_TYPE varchar(128)  DEFAULT NULL,
  PRINCIPAL_NAME varchar(128)  DEFAULT NULL,
  PRINCIPAL_TYPE varchar(128)  DEFAULT NULL,
  ROLE_ID bigint(20) DEFAULT NULL,
  PRIMARY KEY (ROLE_GRANT_ID)
) ;

DROP TABLE IF EXISTS DB_PRIVS CASCADE;
CREATE TABLE DB_PRIVS (
  DB_GRANT_ID bigint(20) ,
  CREATE_TIME int(11) ,
  DB_ID bigint(20) DEFAULT NULL,
  GRANT_OPTION smallint(6) ,
  GRANTOR varchar(128)  DEFAULT NULL,
  GRANTOR_TYPE varchar(128)  DEFAULT NULL,
  PRINCIPAL_NAME varchar(128)  DEFAULT NULL,
  PRINCIPAL_TYPE varchar(128)  DEFAULT NULL,
  DB_PRIV varchar(128)  DEFAULT NULL,
  AUTHORIZER varchar(128)  DEFAULT NULL,
  PRIMARY KEY (DB_GRANT_ID)
) ;

DROP TABLE IF EXISTS TBL_PRIVS CASCADE;
CREATE TABLE TBL_PRIVS (
  TBL_GRANT_ID bigint(20) ,
  CREATE_TIME int(11) ,
  GRANT_OPTION smallint(6) ,
  GRANTOR varchar(128)  DEFAULT NULL,
  GRANTOR_TYPE varchar(128)  DEFAULT NULL,
  PRINCIPAL_NAME varchar(128)  DEFAULT NULL,
  PRINCIPAL_TYPE varchar(128)  DEFAULT NULL,
  TBL_PRIV varchar(128)  DEFAULT NULL,
  TBL_ID bigint(20) DEFAULT NULL,
  AUTHORIZER varchar(128)  DEFAULT NULL,
  PRIMARY KEY (TBL_GRANT_ID)
) ;

DROP TABLE IF EXISTS PARTITION_PARAMS CASCADE;
CREATE TABLE PARTITION_PARAMS (
  PART_ID bigint(20) ,
  PARAM_KEY varchar(256) ,
  PARAM_VALUE varchar(4000)  DEFAULT NULL,
  PRIMARY KEY (PART_ID,PARAM_KEY)
) ;

DROP TABLE IF EXISTS PARTITIONS CASCADE;
CREATE TABLE PARTITIONS (
  PART_ID bigint(20) ,
  CREATE_TIME int(11) ,
  LAST_ACCESS_TIME int(11) ,
  PART_NAME varchar(767)  DEFAULT NULL,
  SD_ID bigint(20) DEFAULT NULL,
  TBL_ID bigint(20) DEFAULT NULL,
  PRIMARY KEY (PART_ID)
) ;

DROP TABLE IF EXISTS COLUMNS_V2 CASCADE;
CREATE TABLE COLUMNS_V2 (
  CD_ID bigint(20) ,
  COMMENT varchar(256) DEFAULT NULL,
  COLUMN_NAME varchar(767) ,
  TYPE_NAME mediumtext ,
  INTEGER_IDX int(11) ,
  PRIMARY KEY (CD_ID,COLUMN_NAME)
) ;

DROP TABLE IF EXISTS PARTITION_KEYS CASCADE;
CREATE TABLE PARTITION_KEYS (
  TBL_ID bigint(20) ,
  PKEY_COMMENT varchar(4000)  DEFAULT NULL,
  PKEY_NAME varchar(128),
  PKEY_TYPE varchar(767) ,
  INTEGER_IDX int(11) ,
  PRIMARY KEY (TBL_ID,PKEY_NAME)
) ;

INSERT INTO ROLES (ROLE_ID,CREATE_TIME,OWNER_NAME,ROLE_NAME) VALUES (2,1647872356,'public','public');
INSERT INTO PARTITION_PARAMS (PART_ID,PARAM_KEY,PARAM_VALUE) VALUES (3,'totalSize',3);
INSERT INTO PARTITIONS (PART_ID,CREATE_TIME,LAST_ACCESS_TIME,PART_NAME,SD_ID,TBL_ID) VALUES
 (3,1650266917,0,'ds=202202',1,1);
INSERT INTO COLUMNS_V2 (CD_ID,COMMENT,COLUMN_NAME,TYPE_NAME,INTEGER_IDX) VALUES (1,'','destination','string',0);
INSERT INTO PARTITION_KEYS (TBL_ID,PKEY_COMMENT,PKEY_NAME,PKEY_TYPE,INTEGER_IDX) VALUES
 (1,'','ds','string',0);