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


-- Non-unique indexes are named according to "idx_fieldname[_fieldname]". For example idx_age_name
-- The unique index is named according to "uniq_field name[_field name]". For example uniq_age_name
-- It is recommended to include all field names for composite indexes, and the long field names can be abbreviated. For example idx_age_name_add
-- The index name should not exceed 50 characters, and the name should be lowercase
--
-- 非唯一索引按照“idx_字段名称[_字段名称]”进用行命名。例如idx_age_name
-- 唯一索引按照“uniq_字段名称[_字段名称]”进用行命名。例如uniq_age_name
-- 组合索引建议包含所有字段名,过长的字段名可以采用缩写形式。例如idx_age_name_add
-- 索引名尽量不超过50个字符，命名应该使用小写

SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `linkis_ps_configuration_config_key`;
CREATE TABLE `linkis_ps_configuration_config_key`(
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `key` varchar(50) DEFAULT NULL COMMENT 'Set key, e.g. spark.executor.instances',
  `description` varchar(200) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `default_value` varchar(200) DEFAULT NULL COMMENT 'Adopted when user does not set key',
  `validate_type` varchar(50) DEFAULT NULL COMMENT 'Validate type, one of the following: None, NumInterval, FloatInterval, Include, Regex, OPF, Custom Rules',
  `validate_range` varchar(50) DEFAULT NULL COMMENT 'Validate range',
  `engine_conn_type` varchar(50) DEFAULT NULL COMMENT 'engine type,such as spark,hive etc',
  `is_hidden` tinyint(1) DEFAULT NULL COMMENT 'Whether it is hidden from user. If set to 1(true), then user cannot modify, however, it could still be used in back-end',
  `is_advanced` tinyint(1) DEFAULT NULL COMMENT 'Whether it is an advanced parameter. If set to 1(true), parameters would be displayed only when user choose to do so',
  `level` tinyint(1) DEFAULT NULL COMMENT 'Basis for displaying sorting in the front-end. Higher the level is, higher the rank the parameter gets',
  `treeName` varchar(20) DEFAULT NULL COMMENT 'Reserved field, representing the subdirectory of engineType',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


DROP TABLE IF EXISTS `linkis_ps_configuration_key_engine_relation`;
CREATE TABLE `linkis_ps_configuration_key_engine_relation`(
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_key_id` bigint(20) NOT NULL COMMENT 'config key id',
  `engine_type_label_id` bigint(20) NOT NULL COMMENT 'engine label id',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uniq_kid_lid` (`config_key_id`, `engine_type_label_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


DROP TABLE IF EXISTS `linkis_ps_configuration_config_value`;
CREATE TABLE `linkis_ps_configuration_config_value`(
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_key_id` bigint(20),
  `config_value` varchar(200),
  `config_label_id`int(20),
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uniq_kid_lid` (`config_key_id`, `config_label_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_ps_configuration_category`;
CREATE TABLE `linkis_ps_configuration_category` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_id` int(20) NOT NULL,
  `level` int(20) NOT NULL,
  `description` varchar(200),
  `tag` varchar(200),
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uniq_label_id` (`label_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- New linkis job
--

DROP  TABLE IF EXISTS `linkis_ps_job_history_group_history`;
CREATE TABLE `linkis_ps_job_history_group_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key, auto increment',
  `job_req_id` varchar(64) DEFAULT NULL COMMENT 'job execId',
  `submit_user` varchar(50) DEFAULT NULL COMMENT 'who submitted this Job',
  `execute_user` varchar(50) DEFAULT NULL COMMENT 'who actually executed this Job',
  `source` text DEFAULT NULL COMMENT 'job source',
  `labels` text DEFAULT NULL COMMENT 'job labels',
  `params` text DEFAULT NULL COMMENT 'job params',
  `progress` varchar(32) DEFAULT NULL COMMENT 'Job execution progress',
  `status` varchar(50) DEFAULT NULL COMMENT 'Script execution status, must be one of the following: Inited, WaitForRetry, Scheduled, Running, Succeed, Failed, Cancelled, Timeout',
  `log_path` varchar(200) DEFAULT NULL COMMENT 'File path of the job log',
  `error_code` int DEFAULT NULL COMMENT 'Error code. Generated when the execution of the script fails',
  `error_desc` varchar(1000) DEFAULT NULL COMMENT 'Execution description. Generated when the execution of script fails',
  `created_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Creation time',
  `updated_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Update time',
  `instances` varchar(250) DEFAULT NULL COMMENT 'Entrance instances',
  `metrics` text DEFAULT NULL COMMENT   'Job Metrics',
  `engine_type` varchar(32) DEFAULT NULL COMMENT 'Engine type',
  `execution_code` text DEFAULT NULL COMMENT 'Job origin code or code path',
  `result_location` varchar(500) DEFAULT NULL COMMENT 'File path of the resultsets',
  `observe_info` varchar(500) DEFAULT NULL COMMENT 'The notification information configuration of this job',
  PRIMARY KEY (`id`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_submit_user` (`submit_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP  TABLE IF EXISTS `linkis_ps_job_history_detail`;
CREATE TABLE `linkis_ps_job_history_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key, auto increment',
  `job_history_id` bigint(20) NOT NULL COMMENT 'ID of JobHistory',
  `result_location` varchar(500) DEFAULT NULL COMMENT 'File path of the resultsets',
  `execution_content` text DEFAULT NULL COMMENT 'The script code or other execution content executed by this Job',
  `result_array_size` int(4) DEFAULT 0 COMMENT 'size of result array',
  `job_group_info` text DEFAULT NULL COMMENT 'Job group info/path',
  `created_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Creation time',
  `updated_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Update time',
  `status` varchar(32) DEFAULT NULL COMMENT 'status',
  `priority` int(4) DEFAULT 0 COMMENT 'order of subjob',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP  TABLE IF EXISTS `linkis_ps_common_lock`;
CREATE TABLE `linkis_ps_common_lock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lock_object` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `time_out` longtext COLLATE utf8_bin,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_lock_object` (`lock_object`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for linkis_ps_udf_manager
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_udf_manager`;
CREATE TABLE `linkis_ps_udf_manager` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for linkis_ps_udf_shared_group
-- An entry would be added when a user share a function to other user group
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_udf_shared_group`;
CREATE TABLE `linkis_ps_udf_shared_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `udf_id` bigint(20) NOT NULL,
  `shared_group` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `linkis_ps_udf_shared_info`;
CREATE TABLE `linkis_ps_udf_shared_info`
(
   `id` bigint(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
   `udf_id` bigint(20) NOT NULL,
   `user_name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for linkis_ps_udf_tree
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_udf_tree`;
CREATE TABLE `linkis_ps_udf_tree` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent` bigint(20) NOT NULL,
  `name` varchar(100) DEFAULT NULL COMMENT 'Category name of the function. It would be displayed in the front-end',
  `user_name` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `category` varchar(50) DEFAULT NULL COMMENT 'Used to distinguish between udf and function',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for linkis_ps_udf_user_load
-- Used to store the function a user selects in the front-end
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_udf_user_load`;
CREATE TABLE `linkis_ps_udf_user_load` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `udf_id` bigint(20) NOT NULL,
  `user_name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `linkis_ps_udf_baseinfo`;
CREATE TABLE `linkis_ps_udf_baseinfo` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_user` varchar(50) NOT NULL,
  `udf_name` varchar(255) NOT NULL,
  `udf_type` int(11) DEFAULT '0',
  `tree_id` bigint(20) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `sys` varchar(255) NOT NULL DEFAULT 'ide' COMMENT 'source system',
  `cluster_name` varchar(255) NOT NULL,
  `is_expire` bit(1) DEFAULT NULL,
  `is_shared` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- bdp_easy_ide.linkis_ps_udf_version definition
DROP TABLE IF EXISTS `linkis_ps_udf_version`;
CREATE TABLE `linkis_ps_udf_version` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `udf_id` bigint(20) NOT NULL,
  `path` varchar(255) NOT NULL COMMENT 'Source path for uploading files',
  `bml_resource_id` varchar(50) NOT NULL,
  `bml_resource_version` varchar(20) NOT NULL,
  `is_published` bit(1) DEFAULT NULL COMMENT 'is published',
  `register_format` varchar(255) DEFAULT NULL,
  `use_format` varchar(255) DEFAULT NULL,
  `description` varchar(255) NOT NULL COMMENT 'version desc',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `md5` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for linkis_ps_variable_key_user
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_variable_key_user`;
CREATE TABLE `linkis_ps_variable_key_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application_id` bigint(20) DEFAULT NULL COMMENT 'Reserved word',
  `key_id` bigint(20) DEFAULT NULL,
  `user_name` varchar(50) DEFAULT NULL,
  `value` varchar(200) DEFAULT NULL COMMENT 'Value of the global variable',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_aid_kid_uname` (`application_id`,`key_id`,`user_name`),
  KEY `idx_key_id` (`key_id`),
  KEY `idx_aid` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ----------------------------
-- Table structure for linkis_ps_variable_key
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_variable_key`;
CREATE TABLE `linkis_ps_variable_key` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `key` varchar(50) DEFAULT NULL COMMENT 'Key of the global variable',
  `description` varchar(200) DEFAULT NULL COMMENT 'Reserved word',
  `name` varchar(50) DEFAULT NULL COMMENT 'Reserved word',
  `application_id` bigint(20) DEFAULT NULL COMMENT 'Reserved word',
  `default_value` varchar(200) DEFAULT NULL COMMENT 'Reserved word',
  `value_type` varchar(50) DEFAULT NULL COMMENT 'Reserved word',
  `value_regex` varchar(100) DEFAULT NULL COMMENT 'Reserved word',
  PRIMARY KEY (`id`),
  KEY `idx_aid` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for linkis_ps_datasource_access
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_datasource_access`;
CREATE TABLE `linkis_ps_datasource_access` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `table_id` bigint(20) NOT NULL,
  `visitor` varchar(16) COLLATE utf8_bin NOT NULL,
  `fields` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `application_id` int(4) NOT NULL,
  `access_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for linkis_ps_datasource_field
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_datasource_field`;
CREATE TABLE `linkis_ps_datasource_field` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `table_id` bigint(20) NOT NULL,
  `name` varchar(64) COLLATE utf8_bin NOT NULL,
  `alias` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `type` varchar(64) COLLATE utf8_bin NOT NULL,
  `comment` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `express` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `rule` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `is_partition_field` tinyint(1) NOT NULL,
  `is_primary` tinyint(1) NOT NULL,
  `length` int(11) DEFAULT NULL,
  `mode_info` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for linkis_ps_datasource_import
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_datasource_import`;
CREATE TABLE `linkis_ps_datasource_import` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `table_id` bigint(20) NOT NULL,
  `import_type` int(4) NOT NULL,
  `args` varchar(255) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for linkis_ps_datasource_lineage
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_datasource_lineage`;
CREATE TABLE `linkis_ps_datasource_lineage` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `table_id` bigint(20) DEFAULT NULL,
  `source_table` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for linkis_ps_datasource_table
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_datasource_table`;
CREATE TABLE `linkis_ps_datasource_table` (
  `id` bigint(255) NOT NULL AUTO_INCREMENT,
  `database` varchar(64) COLLATE utf8_bin NOT NULL,
  `name` varchar(64) COLLATE utf8_bin NOT NULL,
  `alias` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `creator` varchar(16) COLLATE utf8_bin NOT NULL,
  `comment` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `product_name` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `project_name` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `usage` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `lifecycle` int(4) NOT NULL,
  `use_way` int(4) NOT NULL,
  `is_import` tinyint(1) NOT NULL,
  `model_level` int(4) NOT NULL,
  `is_external_use` tinyint(1) NOT NULL,
  `is_partition_table` tinyint(1) NOT NULL,
  `is_available` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_db_name` (`database`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for linkis_ps_datasource_table_info
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_datasource_table_info`;
CREATE TABLE `linkis_ps_datasource_table_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `table_id` bigint(20) NOT NULL,
  `table_last_update_time` datetime NOT NULL,
  `row_num` bigint(20) NOT NULL,
  `file_num` int(11) NOT NULL,
  `table_size` varchar(32) COLLATE utf8_bin NOT NULL,
  `partitions_num` int(11) NOT NULL,
  `update_time` datetime NOT NULL,
  `field_num` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;




-- ----------------------------
-- Table structure for linkis_ps_cs_context_map
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_cs_context_map`;
CREATE TABLE `linkis_ps_cs_context_map` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `key` varchar(128) DEFAULT NULL,
  `context_scope` varchar(32) DEFAULT NULL,
  `context_type` varchar(32) DEFAULT NULL,
  `props` text,
  `value` mediumtext,
  `context_id` int(11) DEFAULT NULL,
  `keywords` varchar(255) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `access_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'last access time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_key_cid_ctype` (`key`,`context_id`,`context_type`),
  KEY `idx_keywords` (`keywords`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for linkis_ps_cs_context_map_listener
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_cs_context_map_listener`;
CREATE TABLE `linkis_ps_cs_context_map_listener` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `listener_source` varchar(255) DEFAULT NULL,
  `key_id` int(11) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `access_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'last access time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for linkis_ps_cs_context_history
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_cs_context_history`;
CREATE TABLE `linkis_ps_cs_context_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `context_id` int(11) DEFAULT NULL,
  `source` text,
  `context_type` varchar(32) DEFAULT NULL,
  `history_json` text,
  `keyword` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `access_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'last access time',
  KEY `idx_keyword` (`keyword`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for linkis_ps_cs_context_id
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_cs_context_id`;
CREATE TABLE `linkis_ps_cs_context_id` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` varchar(32) DEFAULT NULL,
  `application` varchar(32) DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `expire_type` varchar(32) DEFAULT NULL,
  `expire_time` datetime DEFAULT NULL,
  `instance` varchar(128) DEFAULT NULL,
  `backup_instance` varchar(255) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `access_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'last access time',
  PRIMARY KEY (`id`),
  KEY `idx_instance` (`instance`(128)),
  KEY `idx_backup_instance` (`backup_instance`(191)),
  KEY `idx_instance_bin` (`instance`(128),`backup_instance`(128))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for linkis_ps_cs_context_listener
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_cs_context_listener`;
CREATE TABLE `linkis_ps_cs_context_listener` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `listener_source` varchar(255) DEFAULT NULL,
  `context_id` int(11) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `access_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'last access time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `linkis_ps_bml_resources`;
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


DROP TABLE IF EXISTS `linkis_ps_bml_resources_version`;
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
	unique key `uniq_rid_version`(`resource_id`, `version`),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



DROP TABLE IF EXISTS `linkis_ps_bml_resources_permission`;
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



DROP TABLE IF EXISTS `linkis_ps_resources_download_history`;
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




-- 创建资源任务表,包括上传,更新,下载
DROP TABLE IF EXISTS `linkis_ps_bml_resources_task`;
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



DROP TABLE IF EXISTS `linkis_ps_bml_project`;
create table if not exists linkis_ps_bml_project(
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) DEFAULT NULL,
  `system` varchar(64) not null default "dss",
  `source` varchar(1024) default null,
  `description` varchar(1024) default null,
  `creator` varchar(128) not null,
  `enabled` tinyint default 1,
  `create_time` datetime DEFAULT now(),
  unique key `uniq_name` (`name`),
PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPACT;



DROP TABLE IF EXISTS `linkis_ps_bml_project_user`;
create table if not exists linkis_ps_bml_project_user(
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `project_id` int(10) NOT NULL,
  `username` varchar(64) DEFAULT NULL,
  `priv` int(10) not null default 7, -- rwx 421 The permission value is 7. 8 is the administrator, which can authorize other users
  `creator` varchar(128) not null,
  `create_time` datetime DEFAULT now(),
  `expire_time` datetime default null,
  unique key `uniq_name_pid`(`username`, `project_id`),
PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPACT;


DROP TABLE IF EXISTS `linkis_ps_bml_project_resource`;
create table if not exists linkis_ps_bml_project_resource(
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `project_id` int(10) NOT NULL,
  `resource_id` varchar(128) DEFAULT NULL,
PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPACT;


DROP TABLE IF EXISTS `linkis_ps_instance_label`;
CREATE TABLE `linkis_ps_instance_label` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_key` varchar(32) COLLATE utf8_bin NOT NULL COMMENT 'string key',
  `label_value` varchar(255) COLLATE utf8_bin NOT NULL COMMENT 'string value',
  `label_feature` varchar(16) COLLATE utf8_bin NOT NULL COMMENT 'store the feature of label, but it may be redundant',
  `label_value_size` int(20) NOT NULL COMMENT 'size of key -> value map',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_lk_lv` (`label_key`,`label_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


DROP TABLE IF EXISTS `linkis_ps_instance_label_value_relation`;
CREATE TABLE `linkis_ps_instance_label_value_relation` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_value_key` varchar(255) COLLATE utf8_bin NOT NULL COMMENT 'value key',
  `label_value_content` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT 'value content',
  `label_id` int(20) DEFAULT NULL COMMENT 'id reference linkis_ps_instance_label -> id',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create unix timestamp',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_lvk_lid` (`label_value_key`,`label_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_ps_instance_label_relation`;
CREATE TABLE `linkis_ps_instance_label_relation` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_id` int(20) DEFAULT NULL COMMENT 'id reference linkis_ps_instance_label -> id',
  `service_instance` varchar(128) NOT NULL COLLATE utf8_bin COMMENT 'structure like ${host|machine}:${port}',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create unix timestamp',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_lid_instance` (`label_id`,`service_instance`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


DROP TABLE IF EXISTS `linkis_ps_instance_info`;
CREATE TABLE `linkis_ps_instance_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `instance` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT 'structure like ${host|machine}:${port}',
  `name` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT 'equal application name in registry',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'update unix timestamp',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create unix timestamp',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_instance` (`instance`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_ps_error_code`;
CREATE TABLE `linkis_ps_error_code` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `error_code` varchar(50) NOT NULL,
  `error_desc` varchar(1024) NOT NULL,
  `error_regex` varchar(1024) DEFAULT NULL,
  `error_type` int(3) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_error_regex` (error_regex(255))
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_service_instance`;
CREATE TABLE `linkis_cg_manager_service_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `instance` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `name` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `owner` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `mark` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updator` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `creator` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_instance` (`instance`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_linkis_resources`;
CREATE TABLE `linkis_cg_manager_linkis_resources` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `max_resource` varchar(1020) COLLATE utf8_bin DEFAULT NULL,
  `min_resource` varchar(1020) COLLATE utf8_bin DEFAULT NULL,
  `used_resource` varchar(1020) COLLATE utf8_bin DEFAULT NULL,
  `left_resource` varchar(1020) COLLATE utf8_bin DEFAULT NULL,
  `expected_resource` varchar(1020) COLLATE utf8_bin DEFAULT NULL,
  `locked_resource` varchar(1020) COLLATE utf8_bin DEFAULT NULL,
  `resourceType` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ticketId` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updator` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `creator` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_lock`;
CREATE TABLE `linkis_cg_manager_lock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lock_object` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `time_out` longtext COLLATE utf8_bin,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_rm_external_resource_provider`;
CREATE TABLE `linkis_cg_rm_external_resource_provider` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `resource_type` varchar(32) NOT NULL,
  `name` varchar(32) NOT NULL,
  `labels` varchar(32) DEFAULT NULL,
  `config` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `linkis_cg_manager_engine_em`;
CREATE TABLE `linkis_cg_manager_engine_em` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `engine_instance` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `em_instance` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_label`;
CREATE TABLE `linkis_cg_manager_label` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_key` varchar(32) COLLATE utf8_bin NOT NULL,
  `label_value` varchar(255) COLLATE utf8_bin NOT NULL,
  `label_feature` varchar(16) COLLATE utf8_bin NOT NULL,
  `label_value_size` int(20) NOT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_lk_lv` (`label_key`,`label_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_label_value_relation`;
CREATE TABLE `linkis_cg_manager_label_value_relation` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_value_key` varchar(255) COLLATE utf8_bin NOT NULL,
  `label_value_content` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `label_id` int(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_lvk_lid` (`label_value_key`,`label_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_label_resource`;
CREATE TABLE `linkis_cg_manager_label_resource` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_id` int(20) DEFAULT NULL,
  `resource_id` int(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_label_id` (`label_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_ec_resource_info_record`;
CREATE TABLE `linkis_cg_ec_resource_info_record` (
    `id` INT(20) NOT NULL AUTO_INCREMENT,
    `label_value` VARCHAR(255) NOT NULL COMMENT 'ec labels stringValue',
    `create_user` VARCHAR(128) NOT NULL COMMENT 'ec create user',
    `service_instance` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT 'ec instance info',
    `ecm_instance` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT 'ecm instance info ',
    `ticket_id` VARCHAR(100) NOT NULL COMMENT 'ec ticket id',
    `status` varchar(50) DEFAULT NULL COMMENT 'EC status: Starting,Unlock,Locked,Idle,Busy,Running,ShuttingDown,Failed,Success',
    `log_dir_suffix` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT 'log path',
    `request_times` INT(8) COMMENT 'resource request times',
    `request_resource` VARCHAR(1020) COMMENT 'request resource',
    `used_times` INT(8) COMMENT 'resource used times',
    `used_resource` VARCHAR(1020) COMMENT 'used resource',
    `release_times` INT(8) COMMENT 'resource released times',
    `released_resource` VARCHAR(1020)  COMMENT 'released resource',
    `release_time` datetime DEFAULT NULL COMMENT 'released time',
    `used_time` datetime DEFAULT NULL COMMENT 'used time',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    PRIMARY KEY (`id`),
    KEY `idx_ticket_id` (`ticket_id`),
    UNIQUE KEY `uniq_tid_lv` (`ticket_id`,`label_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_label_service_instance`;
CREATE TABLE `linkis_cg_manager_label_service_instance` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_id` int(20) DEFAULT NULL,
  `service_instance` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_lid_instance` (`label_id`,`service_instance`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


DROP TABLE IF EXISTS `linkis_cg_manager_label_user`;
CREATE TABLE `linkis_cg_manager_label_user` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `label_id` int(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


DROP TABLE IF EXISTS `linkis_cg_manager_metrics_history`;
CREATE TABLE `linkis_cg_manager_metrics_history` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `instance_status` int(20) DEFAULT NULL,
  `overload` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `heartbeat_msg` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `healthy_status` int(20) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `creator` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ticketID` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `serviceName` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `instance` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_service_instance_metrics`;
CREATE TABLE `linkis_cg_manager_service_instance_metrics` (
  `instance` varchar(128) COLLATE utf8_bin NOT NULL,
  `instance_status` int(11) DEFAULT NULL,
  `overload` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `heartbeat_msg` text COLLATE utf8_bin DEFAULT NULL,
  `healthy_status` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`instance`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_engine_conn_plugin_bml_resources`;
CREATE TABLE `linkis_cg_engine_conn_plugin_bml_resources` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `engine_conn_type` varchar(100) NOT NULL COMMENT 'Engine type',
  `version` varchar(100) COMMENT 'version',
  `file_name` varchar(255) COMMENT 'file name',
  `file_size` bigint(20)  DEFAULT 0 NOT NULL COMMENT 'file size',
  `last_modified` bigint(20)  COMMENT 'File update time',
  `bml_resource_id` varchar(100) NOT NULL COMMENT 'Owning system',
  `bml_resource_version` varchar(200) NOT NULL COMMENT 'Resource owner',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  `last_update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'updated time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for linkis_ps_dm_datasource
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_dm_datasource`;
CREATE TABLE `linkis_ps_dm_datasource`
(
    `id`                   int(11)                       NOT NULL AUTO_INCREMENT,
    `datasource_name`      varchar(255) COLLATE utf8_bin NOT NULL,
    `datasource_desc`      varchar(255) COLLATE utf8_bin      DEFAULT NULL,
    `datasource_type_id`   int(11)                       NOT NULL,
    `create_identify`      varchar(255) COLLATE utf8_bin      DEFAULT NULL,
    `create_system`        varchar(255) COLLATE utf8_bin      DEFAULT NULL,
    `parameter`            varchar(1024) COLLATE utf8_bin NULL DEFAULT NULL,
    `create_time`          datetime                      NULL DEFAULT CURRENT_TIMESTAMP,
    `modify_time`          datetime                      NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user`          varchar(255) COLLATE utf8_bin      DEFAULT NULL,
    `modify_user`          varchar(255) COLLATE utf8_bin      DEFAULT NULL,
    `labels`               varchar(255) COLLATE utf8_bin      DEFAULT NULL,
    `version_id`           int(11)                            DEFAULT NULL COMMENT 'current version id',
    `expire`               tinyint(1)                         DEFAULT 0,
    `published_version_id` int(11)                            DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uniq_datasource_name` (`datasource_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for linkis_ps_dm_datasource_env
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_dm_datasource_env`;
CREATE TABLE `linkis_ps_dm_datasource_env`
(
    `id`                 int(11)                       NOT NULL AUTO_INCREMENT,
    `env_name`           varchar(32) COLLATE utf8_bin  NOT NULL,
    `env_desc`           varchar(255) COLLATE utf8_bin          DEFAULT NULL,
    `datasource_type_id` int(11)                       NOT NULL,
    `parameter`          varchar(1024) COLLATE utf8_bin          DEFAULT NULL,
    `create_time`        datetime                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user`        varchar(255) COLLATE utf8_bin NULL     DEFAULT NULL,
    `modify_time`        datetime                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modify_user`        varchar(255) COLLATE utf8_bin NULL     DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_env_name` (`env_name`),
    UNIQUE INDEX `uniq_name_dtid` (`env_name`, `datasource_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


-- ----------------------------
-- Table structure for linkis_ps_dm_datasource_type
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_dm_datasource_type`;
CREATE TABLE `linkis_ps_dm_datasource_type`
(
    `id`          int(11)                      NOT NULL AUTO_INCREMENT,
    `name`        varchar(32) COLLATE utf8_bin NOT NULL,
    `description` varchar(255) COLLATE utf8_bin DEFAULT NULL,
    `option`      varchar(32) COLLATE utf8_bin  DEFAULT NULL,
    `classifier`  varchar(32) COLLATE utf8_bin NOT NULL,
    `icon`        varchar(255) COLLATE utf8_bin DEFAULT NULL,
    `layers`      int(3)                       NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uniq_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for linkis_ps_dm_datasource_type_key
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_dm_datasource_type_key`;
CREATE TABLE `linkis_ps_dm_datasource_type_key`
(
    `id`                  int(11)                       NOT NULL AUTO_INCREMENT,
    `data_source_type_id` int(11)                       NOT NULL,
    `key`                 varchar(32) COLLATE utf8_bin  NOT NULL,
    `name`                varchar(32) COLLATE utf8_bin  NOT NULL,
    `name_en`             varchar(32) COLLATE utf8_bin  NOT NULL,
    `default_value`       varchar(50) COLLATE utf8_bin  NULL     DEFAULT NULL,
    `value_type`          varchar(50) COLLATE utf8_bin  NOT NULL,
    `scope`               varchar(50) COLLATE utf8_bin  NULL     DEFAULT NULL,
    `require`             tinyint(1)                    NULL     DEFAULT 0,
    `description`         varchar(200) COLLATE utf8_bin NULL     DEFAULT NULL,
    `description_en`      varchar(200) COLLATE utf8_bin NULL     DEFAULT NULL,
    `value_regex`         varchar(200) COLLATE utf8_bin NULL     DEFAULT NULL,
    `ref_id`              bigint(20)                    NULL     DEFAULT NULL,
    `ref_value`           varchar(50) COLLATE utf8_bin  NULL     DEFAULT NULL,
    `data_source`         varchar(200) COLLATE utf8_bin NULL     DEFAULT NULL,
    `update_time`         datetime                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_time`         datetime                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_dstid_key` (`data_source_type_id`, `key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ----------------------------
-- Table structure for linkis_ps_dm_datasource_version
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_dm_datasource_version`;
CREATE TABLE `linkis_ps_dm_datasource_version`
(
    `version_id`    int(11)                        NOT NULL AUTO_INCREMENT,
    `datasource_id` int(11)                        NOT NULL,
    `parameter`     varchar(2048) COLLATE utf8_bin NULL DEFAULT NULL,
    `comment`       varchar(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    `create_time`   datetime(0)                    NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user`   varchar(255) COLLATE utf8_bin  NULL DEFAULT NULL,
    PRIMARY KEY `uniq_vid_did` (`version_id`, `datasource_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for linkis_mg_gateway_auth_token
-- ----------------------------
DROP TABLE IF EXISTS `linkis_mg_gateway_auth_token`;
CREATE TABLE `linkis_mg_gateway_auth_token` (
     `id` int(11) NOT NULL AUTO_INCREMENT,
     `token_name` varchar(128) NOT NULL,
     `legal_users` text,
     `legal_hosts` text,
     `business_owner` varchar(32),
     `create_time` DATE DEFAULT NULL,
     `update_time` DATE DEFAULT NULL,
     `elapse_day` BIGINT DEFAULT NULL,
     `update_by` varchar(32),
PRIMARY KEY (`id`),
UNIQUE KEY `uniq_token_name` (`token_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



-- ----------------------------
-- Table structure for linkis_cg_tenant_label_config
-- ----------------------------
DROP TABLE IF EXISTS `linkis_cg_tenant_label_config`;
CREATE TABLE `linkis_cg_tenant_label_config` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `user` varchar(50) COLLATE utf8_bin NOT NULL,
  `creator` varchar(50) COLLATE utf8_bin NOT NULL,
  `tenant_value` varchar(128) COLLATE utf8_bin NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `desc` varchar(100) COLLATE utf8_bin NOT NULL,
  `bussiness_user` varchar(50) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_user_creator` (`user`,`creator`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Table structure for linkis_cg_user_ip_config
-- ----------------------------
DROP TABLE IF EXISTS `linkis_cg_user_ip_config`;
CREATE TABLE `linkis_cg_user_ip_config` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `user` varchar(50) COLLATE utf8_bin NOT NULL,
  `creator` varchar(50) COLLATE utf8_bin NOT NULL,
  `ip_list` text COLLATE utf8_bin NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `desc` varchar(100) COLLATE utf8_bin NOT NULL,
  `bussiness_user` varchar(50) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_user_creator` (`user`,`creator`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
