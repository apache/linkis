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

ALTER TABLE `linkis_ps_configuration_config_key`
	CHANGE COLUMN `validate_range` `validate_range` VARCHAR(150) NULL DEFAULT NULL COMMENT 'Validate range' COLLATE 'utf8_bin' AFTER `validate_type`;

ALTER TABLE linkis_cg_ec_resource_info_record MODIFY COLUMN metrics text CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT 'ec metrics';


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

ALTER TABLE linkis_ps_python_module_info ADD COLUMN python_module varchar(200) COLLATE utf8_bin DEFAULT NULL COMMENT '依赖python模块';
ALTER TABLE linkis_ps_bml_project_resource ADD INDEX idx_resource_id (resource_id);

DROP TABLE IF EXISTS `linkis_ps_job_history_diagnosis`;
CREATE TABLE `linkis_ps_job_history_diagnosis` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key, auto increment',
  `job_history_id` bigint(20) NOT NULL COMMENT 'ID of JobHistory',
  `diagnosis_content` text COLLATE utf8mb4_bin COMMENT 'Diagnosis failed task information',
  `created_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Creation time',
  `updated_time` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Update time',
  `only_read` varchar(5) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '1 just read,can not update',
  PRIMARY KEY (`id`),
  UNIQUE KEY `job_history_id` (`job_history_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT ='任务诊断分析表';

CREATE TABLE `linkis_mg_gateway_ecc_userinfo` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID，自增',
	`om_tool` VARCHAR(255) NOT NULL COMMENT '工具系统',
	`user_id` VARCHAR(255) NOT NULL COMMENT '申请授权用户',
	`op_user_id` VARCHAR(255) NOT NULL COMMENT '协助运维账号',
	`roles` VARCHAR(255) NOT NULL COMMENT '角色列表，多个逗号,分隔',
	`auth_system_id` VARCHAR(500) NOT NULL COMMENT '授权子系统名称ID，多个逗号,分隔',
	`apply_itsm_id` VARCHAR(255) NOT NULL COMMENT 'ITSM申请单号，唯一，重复推送时根据这个字段做更新',
	`effective_datetime` DATETIME NOT NULL COMMENT '生效时间，允许登录的最早时间',
	`expire_datetime` DATETIME NOT NULL COMMENT '失效时间，根据这个时间计算cookie的有效期',
	`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，默认当前时间',
	`updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，默认当前时间，更新时修改',
	PRIMARY KEY (`id`),
	UNIQUE INDEX `apply_itsm_id` (`apply_itsm_id`,`user_id`)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='转协查用户授权表';

-- AI 作业历史记录表
CREATE TABLE IF NOT EXISTS `linkis_ps_ai_job_history` (
    `id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `job_history_id` VARCHAR(64) NOT NULL COMMENT '作业历史ID',
    `submit_user` VARCHAR(50) NOT NULL COMMENT '提交用户',
    `execute_user` VARCHAR(50) NOT NULL COMMENT '执行用户',
    `submit_code` TEXT COMMENT '用户提交代码',
    `execution_code` TEXT COMMENT '执行代码',
    `metrics` text COMMENT 'metrics 信息',
    `params` text COMMENT '任务参数',
    `labels` text COMMENT '任务标签',
    `error_code` int DEFAULT NULL COMMENT '错误码',
    `error_desc` TEXT COMMENT '错误信息',
    `engine_instances` VARCHAR(250) COMMENT '引擎实例',
    `engine_type` VARCHAR(50) COMMENT '引擎类型',
    `change_time` DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '切换时间',
    `created_time` DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_time` DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='AI作业历史记录表';


ALTER TABLE linkis_cg_manager_service_instance
ADD INDEX idx_instance_name (instance, name);

-- 添加 token 密文存储字段
ALTER TABLE `linkis_mg_gateway_auth_token` ADD COLUMN `token_sign` TEXT DEFAULT NULL COMMENT '存储token密文' AFTER `token_name`;
