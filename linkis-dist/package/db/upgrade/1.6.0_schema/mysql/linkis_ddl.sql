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



