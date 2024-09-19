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

ALTER TABLE linkis_ps_bml_resources_task ADD CONSTRAINT  uniq_rid_version UNIQUE (resource_id, version);
ALTER TABLE linkis_cg_ec_resource_info_record ADD UNIQUE INDEX uniq_sinstance_status_cuser_ctime (service_instance, status, create_user, create_time);
ALTER TABLE linkis_cg_manager_service_instance_metrics ADD COLUMN description varchar(256) CHARSET utf8mb4 COLLATE utf8mb4_bin DEFAULT '';
ALTER TABLE  linkis_ps_configuration_config_value modify COLUMN  config_value varchar(500) CHARSET utf8mb4 COLLATE utf8mb4_bin;
ALTER TABLE `linkis_ps_configuration_config_key` ADD UNIQUE `uniq_key_ectype` (`key`,`engine_conn_type`);
ALTER TABLE `linkis_ps_configuration_config_key` modify column `engine_conn_type` varchar(50) DEFAULT '' COMMENT 'engine type,such as spark,hive etc';
ALTER TABLE `linkis_ps_udf_version`  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update_time';
ALTER TABLE `linkis_ps_udf_tree` ADD CONSTRAINT  `uniq_parent_name_uname_category` UNIQUE (`parent`,`name`, `user_name`, `category`);
ALTER TABLE `linkis_cg_ec_resource_info_record` MODIFY COLUMN `metrics` text CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT 'ec metrics';
ALTER TABLE `linkis_ps_configuration_config_key`
    CHANGE COLUMN `validate_range` `validate_range` VARCHAR(150) NULL DEFAULT NULL COMMENT 'Validate range' COLLATE 'utf8_bin' AFTER `validate_type`;
ALTER TABLE linkis_cg_tenant_label_config ADD COLUMN is_valid varchar(1) CHARSET utf8mb4 COLLATE utf8mb4_bin DEFAULT 'Y' COMMENT '是否有效';
ALTER TABLE linkis_ps_configuration_across_cluster_rule modify COLUMN rules varchar(512) CHARSET utf8mb4 COLLATE utf8mb4_bin;
ALTER TABLE linkis_cg_manager_label_value_relation ADD CONSTRAINT unlid_lvk_lvc UNIQUE (label_id,label_value_key,label_value_content);

-- ----------------------------
-- Table structure for linkis_org_user
-- ----------------------------
DROP TABLE IF EXISTS `linkis_org_user`;
CREATE TABLE `linkis_org_user` (
    `cluster_code` varchar(16) COMMENT 'cluster code',
    `user_type` varchar(64) COMMENT 'user type',
    `user_name` varchar(128) COMMENT 'username',
    `org_id` varchar(16) COMMENT 'org id',
    `org_name` varchar(64) COMMENT 'org name',
    `queue_name` varchar(64) COMMENT 'yarn queue name',
    `db_name` varchar(64) COMMENT 'default db name',
    `interface_user` varchar(64) COMMENT 'interface user',
    `is_union_analyse` varchar(64) COMMENT 'is union analyse',
    `create_time` varchar(64) COMMENT 'create time',
    `user_itsm_no` varchar(64) COMMENT 'user itsm no',
    PRIMARY KEY (`user_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_bin COMMENT ='user org info';

DROP TABLE IF EXISTS `linkis_ps_job_history_detail`;
DROP TABLE IF EXISTS `linkis_mg_gateway_whitelist_config`;

-- ----------------------------
-- Table structure for linkis_cg_tenant_department_config
-- ----------------------------
DROP TABLE IF EXISTS `linkis_cg_tenant_department_config`;
CREATE TABLE `linkis_cg_tenant_department_config` (
  `id` int(20) NOT NULL AUTO_INCREMENT  COMMENT 'ID',
  `creator` varchar(50) COLLATE utf8_bin NOT NULL  COMMENT '应用',
  `department` varchar(64) COLLATE utf8_bin NOT NULL  COMMENT '部门名称',
  `department_id` varchar(16) COLLATE utf8_bin NOT NULL COMMENT '部门ID',
  `tenant_value` varchar(128) COLLATE utf8_bin NOT NULL  COMMENT '部门租户标签',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '更新时间',
  `create_by` varchar(50) COLLATE utf8_bin NOT NULL  COMMENT '创建用户',
  `is_valid` varchar(1) COLLATE utf8_bin NOT NULL DEFAULT 'Y' COMMENT '是否有效',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_creator_department` (`creator`,`department`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

DROP TABLE IF EXISTS `linkis_org_user_sync`;
CREATE TABLE `linkis_org_user_sync` (
    `cluster_code` varchar(16) COMMENT '集群',
    `user_type` varchar(64) COMMENT '用户类型',
    `user_name` varchar(128) COMMENT '授权用户',
    `org_id` varchar(16) COMMENT '部门ID',
    `org_name` varchar(64) COMMENT '部门名字',
    `queue_name` varchar(64) COMMENT '默认资源队列',
    `db_name` varchar(64) COMMENT '默认操作数据库',
    `interface_user` varchar(64) COMMENT '接口人',
    `is_union_analyse` varchar(64) COMMENT '是否联合分析人',
    `create_time` varchar(64) COMMENT '用户创建时间',
    `user_itsm_no` varchar(64) COMMENT '用户创建单号',
    PRIMARY KEY (`user_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_bin COMMENT ='用户部门统计INC表';

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
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(255) NOT NULL COMMENT 'python模块名称',
  `description` text COMMENT 'python模块描述',
  `path` varchar(255) NOT NULL COMMENT 'hdfs路径',
  `engine_type` varchar(50) NOT NULL COMMENT '引擎类型，python/spark/all',
  `create_user` varchar(50) NOT NULL COMMENT '创建用户',
  `update_user` varchar(50) NOT NULL COMMENT '修改用户',
  `is_load` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否加载，0-未加载，1-已加载',
  `is_expire` tinyint(1) DEFAULT NULL COMMENT '是否过期，0-未过期，1-已过期）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;  COMMENT='Python模块包信息表';

ALTER TABLE linkis_cg_manager_service_instance ADD COLUMN params text COLLATE utf8_bin DEFAULT NULL;




