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
 
SET FOREIGN_KEY_CHECKS = 0;
SET REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS linkis_ps_bml_project_resource;
CREATE TABLE   linkis_ps_bml_project_resource   (
    id   int(10) NOT NULL AUTO_INCREMENT,
    project_id   int(10) NOT NULL,
    resource_id   varchar(128),
  PRIMARY KEY (  id  )
);


DROP TABLE IF EXISTS linkis_ps_bml_project;
CREATE TABLE linkis_ps_bml_project (
    id   int(10) NOT NULL AUTO_INCREMENT,
    name   varchar(128)  DEFAULT NULL,
    system   varchar(64)  NOT NULL DEFAULT 'dss',
    source   varchar(1024)  DEFAULT NULL,
    description   varchar(1024)  DEFAULT NULL,
    creator   varchar(128) NOT NULL,
    enabled   tinyint(4) DEFAULT '1',
    create_time   datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (  id  ),
  UNIQUE KEY   name   (  name  )
);


DROP TABLE IF EXISTS linkis_ps_bml_project_user;
CREATE TABLE   linkis_ps_bml_project_user   (
    id   int(10) NOT NULL AUTO_INCREMENT,
    project_id   int(10) NOT NULL,
    username   varchar(64)  DEFAULT NULL,
    priv   int(10) NOT NULL DEFAULT '7',
    creator   varchar(128)  NOT NULL,
    create_time   datetime DEFAULT CURRENT_TIMESTAMP,
    expire_time   datetime DEFAULT NULL,
  PRIMARY KEY (  id  ),
  UNIQUE KEY   user_project   (  username  ,  project_id  )
);



DROP TABLE IF EXISTS linkis_ps_bml_resources_version;
CREATE TABLE   linkis_ps_bml_resources_version   (
    id   bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    resource_id   varchar(50) NOT NULL COMMENT 'Resource uuid',
    file_md5   varchar(32) NOT NULL COMMENT 'Md5 summary of the file',
    version   varchar(20) NOT NULL COMMENT 'Resource version (v plus five digits)',
    size   int(10) NOT NULL COMMENT 'File size',
    start_byte   bigint(20) unsigned NOT NULL DEFAULT '0',
    end_byte   bigint(20) unsigned NOT NULL DEFAULT '0',
    resource   varchar(2000) NOT NULL COMMENT 'Resource content (file information including path and file name)',
    description   varchar(2000) DEFAULT NULL COMMENT 'description',
    start_time   datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Started time',
    end_time   datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Stoped time',
    client_ip   varchar(200) NOT NULL COMMENT 'Client ip',
    updator   varchar(50) DEFAULT NULL COMMENT 'updator',
    enable_flag   tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Status, 1: normal, 0: frozen',
  PRIMARY KEY (  id  ),
  UNIQUE KEY   resource_id_version   (  resource_id  ,  version  )
);

DROP TABLE IF EXISTS linkis_ps_bml_resources_task;
CREATE TABLE   linkis_ps_bml_resources_task   (
    id   bigint(20) NOT NULL AUTO_INCREMENT,
    resource_id   varchar(50) DEFAULT NULL COMMENT 'resource uuid',
    version   varchar(20) DEFAULT NULL COMMENT 'Resource version number of the current operation',
    operation   varchar(20) NOT NULL COMMENT 'Operation type. upload = 0, update = 1',
    state   varchar(20) NOT NULL DEFAULT 'Schduled' COMMENT 'Current status of the task:Schduled, Running, Succeed, Failed,Cancelled',
    submit_user   varchar(20) NOT NULL DEFAULT '' COMMENT 'Job submission user name',
    system   varchar(20) DEFAULT 'dss' COMMENT 'Subsystem name: wtss',
    instance   varchar(128) NOT NULL COMMENT 'Material library example',
    client_ip   varchar(50) DEFAULT NULL COMMENT 'Request IP',
    extra_params   text COMMENT 'Additional key information. Such as the resource IDs and versions that are deleted in batches, and all versions under the resource are deleted',
    err_msg   varchar(2000) DEFAULT NULL COMMENT 'Task failure information.e.getMessage',
    start_time   datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Starting time',
    end_time   datetime DEFAULT NULL COMMENT 'End Time',
    last_update_time   datetime NOT NULL COMMENT 'Last update time',
  PRIMARY KEY (  id  )
);

DROP TABLE IF EXISTS linkis_ps_bml_resources;
CREATE TABLE   linkis_ps_bml_resources   (
    id   bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    resource_id   varchar(50) NOT NULL COMMENT 'resource uuid',
    is_private   tinyint(1) DEFAULT '0' COMMENT 'Whether the resource is private, 0 means private, 1 means public',
    resource_header   tinyint(1) DEFAULT '0' COMMENT 'Classification, 0 means unclassified, 1 means classified',
    downloaded_file_name   varchar(200) DEFAULT NULL COMMENT 'File name when downloading',
    sys   varchar(100) NOT NULL COMMENT 'Owning system',
    create_time   datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    owner   varchar(200) NOT NULL COMMENT 'Resource owner',
    is_expire   tinyint(1) DEFAULT '0' COMMENT 'Whether expired, 0 means not expired, 1 means expired',
    expire_type   varchar(50) DEFAULT NULL COMMENT 'Expiration type, date refers to the expiration on the specified date, TIME refers to the time',
    expire_time   varchar(50) DEFAULT NULL COMMENT 'Expiration time, one day by default',
    max_version   int(20) DEFAULT '10' COMMENT 'The default is 10, which means to keep the latest 10 versions',
    update_time   datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated time',
    updator   varchar(50) DEFAULT NULL COMMENT 'updator',
    enable_flag   tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Status, 1: normal, 0: frozen',
  PRIMARY KEY (  id  )
);

DROP TABLE IF EXISTS linkis_ps_resources_download_history;
CREATE TABLE   linkis_ps_resources_download_history   (
    id   bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    start_time   datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'start time',
    end_time   datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'stop time',
    client_ip   varchar(200) NOT NULL COMMENT 'client ip',
    state   tinyint(1) NOT NULL COMMENT 'Download status, 0 download successful, 1 download failed',
    resource_id   varchar(50) NOT NULL,
    version   varchar(20) NOT NULL,
    downloader   varchar(50) NOT NULL COMMENT 'Downloader',
  PRIMARY KEY (  id  )
);

insert ignore into  linkis_ps_bml_project_user(project_id, username, priv, creator, create_time) values ( 1, 'creCreatorUser', 2, 'creatorTest', now());
insert ignore into linkis_ps_bml_project(name, `system`, source, description, creator, enabled, create_time)values('testName', 'testSy','test', 'descTest','creCreatorUser', 1, now());
insert ignore into linkis_ps_bml_project_resource(project_id, resource_id) values(1, '123');