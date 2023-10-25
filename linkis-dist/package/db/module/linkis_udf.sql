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
 

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for linkis_ps_udf_manager
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_udf_manager`;
CREATE TABLE `linkis_ps_udf_manager` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
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
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_uid_uname` (`udf_id`, `user_name`)
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