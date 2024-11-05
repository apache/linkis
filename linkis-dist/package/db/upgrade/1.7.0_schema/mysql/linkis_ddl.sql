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

ALTER TABLE linkis_ps_configuration_across_cluster_rule modify COLUMN rules varchar(512) CHARSET utf8mb4 COLLATE utf8mb4_bin;
ALTER TABLE linkis_cg_manager_label_value_relation ADD CONSTRAINT unlid_lvk_lvc UNIQUE (label_id,label_value_key,label_value_content);
ALTER TABLE linkis_cg_manager_service_instance ADD COLUMN params text COLLATE utf8_bin DEFAULT NULL;

DROP TABLE IF EXISTS `linkis_ps_job_history_detail`;
DROP TABLE IF EXISTS `linkis_mg_gateway_whitelist_config`;

-- ----------------------------
-- Table structure for linkis_cg_tenant_department_config
-- ----------------------------
DROP TABLE IF EXISTS `linkis_cg_tenant_department_config`;
CREATE TABLE `linkis_cg_tenant_department_config` (
      `id` int(20) NOT NULL AUTO_INCREMENT  COMMENT 'ID',
      `creator` varchar(50) COLLATE utf8_bin NOT NULL  COMMENT 'app',
      `department` varchar(64) COLLATE utf8_bin NOT NULL  COMMENT 'department name',
      `department_id` varchar(16) COLLATE utf8_bin NOT NULL COMMENT 'department id',
      `tenant_value` varchar(128) COLLATE utf8_bin NOT NULL  COMMENT 'department tenant',
      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT 'create time',
      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT 'update time',
      `create_by` varchar(50) COLLATE utf8_bin NOT NULL  COMMENT 'create user',
      `is_valid` varchar(1) COLLATE utf8_bin NOT NULL DEFAULT 'Y' COMMENT 'is valid',
      PRIMARY KEY (`id`),
      UNIQUE KEY `uniq_creator_department` (`creator`,`department`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

DROP TABLE IF EXISTS `linkis_org_user_sync`;
CREATE TABLE `linkis_org_user_sync` (
     `cluster_code` varchar(16) COMMENT 'cluster code',
     `user_type` varchar(64) COMMENT 'user type',
     `user_name` varchar(128) COMMENT 'user name',
     `org_id` varchar(16) COMMENT 'department id',
     `org_name` varchar(64) COMMENT 'org name',
     `queue_name` varchar(64) COMMENT 'queue name',
     `db_name` varchar(64) COMMENT 'db name',
     `interface_user` varchar(64) COMMENT 'interface user',
     `is_union_analyse` varchar(64) COMMENT 'union analyse',
     `create_time` varchar(64) COMMENT 'create time',
     `user_itsm_no` varchar(64) COMMENT 'user itsm no',
     PRIMARY KEY (`user_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_bin COMMENT ='usr org info sync';

DROP TABLE IF EXISTS `linkis_mg_gateway_whitelist_config`;
CREATE TABLE `linkis_mg_gateway_whitelist_config` (
           `id` int(11) NOT NULL AUTO_INCREMENT,
           `allowed_user` varchar(128) COLLATE utf8_bin NOT NULL,
           `client_address` varchar(128) COLLATE utf8_bin NOT NULL,
           `create_time` datetime DEFAULT NULL,
           `update_time` datetime DEFAULT NULL,
           PRIMARY KEY (`id`),
           UNIQUE KEY `address_uniq` (`allowed_user`, `client_address`),
           KEY `linkis_mg_gateway_whitelist_config_allowed_user` (`allowed_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

DROP TABLE IF EXISTS `linkis_mg_gateway_whitelist_sensitive_user`;
CREATE TABLE `linkis_mg_gateway_whitelist_sensitive_user` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `sensitive_username` varchar(128) COLLATE utf8_bin NOT NULL,
      `create_time` datetime DEFAULT NULL,
      `update_time` datetime DEFAULT NULL,
      PRIMARY KEY (`id`),
      UNIQUE KEY `sensitive_username` (`sensitive_username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

DROP TABLE IF EXISTS `linkis_ps_python_module_info`;
CREATE TABLE `linkis_ps_python_module_info` (
           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
           `name` varchar(255) NOT NULL COMMENT 'python module name',
           `description` text COMMENT 'python module description',
           `path` varchar(255) NOT NULL COMMENT 'hdfs path',
           `engine_type` varchar(50) NOT NULL COMMENT 'Engine type，python/spark/all',
           `create_user` varchar(50) NOT NULL COMMENT 'create user',
           `update_user` varchar(50) NOT NULL COMMENT 'update user',
           `is_load` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'is load',
           `is_expire` tinyint(1) DEFAULT NULL COMMENT 'is expire',
           `create_time` datetime NOT NULL COMMENT 'create time',
           `update_time` datetime NOT NULL COMMENT 'update time',
           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;









