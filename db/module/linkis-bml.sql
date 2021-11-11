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
 
drop table if exists `linkis_ps_bml_resources`;
CREATE TABLE if not exists `linkis_ps_bml_resources` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `resource_id` varchar(50) NOT NULL COMMENT 'resource uuid',
  `is_private` TINYINT(1) DEFAULT 0 COMMENT 'Whether the resource is private, 0 means private, 1 means public',
  `resource_header` TINYINT(1) DEFAULT 0 COMMENT 'Classification, 0 means unclassified, 1 means classified',
	`downloaded_file_name` varchar(200) DEFAULT NULL COMMENT 'File name when downloading',
	`sys` varchar(100) NOT NULL COMMENT 'Owning system',
	`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
	`owner` varchar(200) NOT NULL COMMENT 'Resource owner',
	`is_expire` TINYINT(1) DEFAULT 0 COMMENT 'Whether expired, 0 means not expired, 1 means expired',
	`expire_type` varchar(50) DEFAULT null COMMENT 'Expiration type, date refers to the expiration on the specified date, TIME refers to the time',
	`expire_time` varchar(50) DEFAULT null COMMENT 'Expiration time, one day by default',
	`max_version` int(20) DEFAULT 10 COMMENT 'The default is 10, which means to keep the latest 10 versions',
	`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Updated time',
	`updator` varchar(50) DEFAULT NULL COMMENT 'updator',
	`enable_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Status, 1: normal, 0: frozen',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4;

-- Modify the default value of expire_type to NULL
alter table linkis_ps_bml_resources alter column expire_type set default null;

-- Modify the default value of expire_time to NULL
alter table linkis_ps_bml_resources alter column expire_time set default null;


drop table if exists `linkis_ps_bml_resources_version`;
CREATE TABLE if not exists `linkis_ps_bml_resources_version` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `resource_id` varchar(50) NOT NULL COMMENT 'Resource uuid',
  `file_md5` varchar(32) NOT NULL COMMENT 'Md5 summary of the file',
  `version` varchar(20) NOT NULL COMMENT 'Resource version (v plus five digits)',
	`size` int(10) NOT NULL COMMENT 'File size',
	`start_byte` BIGINT(20) UNSIGNED NOT NULL DEFAULT 0,
	`end_byte` BIGINT(20) UNSIGNED NOT NULL DEFAULT 0,
	`resource` varchar(2000) NOT NULL COMMENT 'Resource content (file information including path and file name)',
	`description` varchar(2000) DEFAULT NULL COMMENT 'description',
	`start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Started time',
	`end_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Stoped time',
	`client_ip` varchar(200) NOT NULL COMMENT 'Client ip',
	`updator` varchar(50) DEFAULT NULL COMMENT 'updator',
	`enable_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Status, 1: normal, 0: frozen',
	unique key `resource_id_version`(`resource_id`, `version`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add start_byte and end_byte fields
ALTER TABLE `linkis_ps_bml_resources_version` ADD COLUMN `start_byte` BIGINT(20) UNSIGNED NOT NULL DEFAULT 0 AFTER `size`;

ALTER TABLE `linkis_ps_bml_resources_version` ADD COLUMN `end_byte` BIGINT(20) UNSIGNED NOT NULL DEFAULT 0 AFTER `start_byte`;

-- version field modification
alter table `linkis_ps_bml_resources_version` modify column `version` varchar(20) not null;

-- Add a joint unique constraint to resource_id and version
alter table `linkis_ps_bml_resources_version` add unique key `resource_id_version`(`resource_id`, `version`);


drop table if exists `linkis_ps_bml_resources_permission`;
CREATE TABLE if not exists `linkis_ps_bml_resources_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `resource_id` varchar(50) NOT NULL COMMENT 'Resource uuid',
  `permission` varchar(10) NOT NULL COMMENT 'permission',
	`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
	`system` varchar(50) default "dss" COMMENT 'creator',
	`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'updated time',
	`updator` varchar(50) NOT NULL COMMENT 'updator',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

drop table if exists `linkis_ps_resources_download_history`;
CREATE TABLE if not exists `linkis_ps_resources_download_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
	`start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'start time',
	`end_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'stop time',
	`client_ip` varchar(200) NOT NULL COMMENT 'client ip',
	`state` TINYINT(1) NOT NULL COMMENT 'Download status, 0 download successful, 1 download failed',
	 `resource_id` varchar(50) not null,
	 `version` varchar(20) not null,
	`downloader` varchar(50) NOT NULL COMMENT 'Downloader',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- Delete the resources_version_id field
alter table `linkis_ps_resources_download_history` drop column `resources_version_id`;

-- Add resource_id field
alter table `linkis_ps_resources_download_history` add column `resource_id` varchar(50) not null after `state`;

-- Add version field
alter table `linkis_ps_resources_download_history` add column `version` varchar(20) not null after `resource_id`;

create table dws_bml_resources_contentType (
	`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `ext` varchar(8) not null comment 'File extension',
  `content_type` varchar(16) not null comment 'file content-type',
  `range` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether it is necessary to resume the transmission with a breakpoint, 0 means not required, 1 means required',
	PRIMARY KEY (`id`),
  UNIQUE KEY `whitelist_contentType_uindex` (`content_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

drop table if exists `linkis_ps_bml_resources_task`;
CREATE TABLE if not exists `linkis_ps_bml_resources_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `resource_id` varchar(50) DEFAULT NULL COMMENT 'resource uuid',
  `version` varchar(20) DEFAULT NULL COMMENT 'Resource version number of the current operation',
  `operation` varchar(20) NOT NULL COMMENT 'Operation type. upload = 0, update = 1',
  `state` varchar(20) NOT NULL DEFAULT 'Schduled' COMMENT 'Current status of the task:Schduled, Running, Succeed, Failed,Cancelled',
  `submit_user` varchar(20) NOT NULL DEFAULT '' COMMENT 'Job submission user name',
  `system` varchar(20) DEFAULT 'dss' COMMENT 'Subsystem name: wtss',
  `instance` varchar(128) NOT NULL COMMENT 'Material library example',
  `client_ip` varchar(50) DEFAULT NULL COMMENT 'Request IP',
  `extra_params` text COMMENT 'Additional key information. Such as the resource IDs and versions that are deleted in batches, and all versions under the resource are deleted',
  `err_msg` varchar(2000) DEFAULT NULL COMMENT 'Task failure information.e.getMessage',
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Starting time',
  `end_time` datetime DEFAULT NULL COMMENT 'End Time',
  `last_update_time` datetime NOT NULL COMMENT 'Last update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;