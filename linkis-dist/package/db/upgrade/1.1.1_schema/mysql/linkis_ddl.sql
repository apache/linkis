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

DROP  TABLE IF EXISTS `linkis_ps_common_lock`;
CREATE TABLE `linkis_ps_common_lock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lock_object` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `time_out` longtext COLLATE utf8_bin,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `lock_object` (`lock_object`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


DROP TABLE IF EXISTS `linkis_ps_udf_shared_info`;
CREATE TABLE `linkis_ps_udf_shared_info`
(
   `id` bigint(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
   `udf_id` bigint(20) NOT NULL,
   `user_name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



-- linkis_ps_udf_version definition
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


DROP TABLE IF EXISTS `linkis_cg_rm_resource_action_record`;
CREATE TABLE linkis_cg_rm_resource_action_record (
  `id` INT(20) NOT NULL AUTO_INCREMENT,
  `label_value` VARCHAR(100) NOT NULL,
  `ticket_id` VARCHAR(100) NOT NULL,
  `request_times` INT(8),
  `request_resource_all` VARCHAR(100),
  `used_times` INT(8),
  `used_resource_all` VARCHAR(100),
  `release_times` INT(8),
  `release_resource_all` VARCHAR(100),
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `label_value_ticket_id` (`label_value`, `ticket_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
UNIQUE KEY `token_name` (`token_name`)
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

ALTER TABLE `linkis_ps_job_history_group_history` CHANGE COLUMN `params` `params` TEXT NULL COMMENT 'job params' AFTER `params`;
ALTER TABLE `linkis_ps_job_history_group_history` ADD COLUMN `result_location` VARCHAR(500) NULL DEFAULT NULL COMMENT 'File path of the resultsets' AFTER `result_location`;
ALTER TABLE `linkis_ps_udf_shared_group` CHANGE COLUMN `user_name` `shared_group` VARCHAR(50) NOT NULL AFTER `udf_id`;
RENAME TABLE `linkis_ps_udf_user_load_info` TO `linkis_ps_udf_user_load`;
RENAME TABLE `linkis_ps_udf_shared_user` TO `linkis_ps_udf_shared_info`;

ALTER TABLE `linkis_cg_manager_label_resource`         ADD UNIQUE INDEX  `label_id` (`label_id`);
ALTER TABLE `linkis_cg_manager_label_service_instance` ADD KEY           `label_serviceinstance` (`label_id`,`service_instance`);
ALTER TABLE `linkis_cg_manager_label_service_instance` ADD INDEX         `label_serviceinstance` (`label_id`, `service_instance`);