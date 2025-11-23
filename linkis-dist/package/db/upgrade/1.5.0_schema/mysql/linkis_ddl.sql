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

ALTER TABLE `linkis_cg_manager_label` MODIFY COLUMN label_key varchar(50);
ALTER TABLE linkis_ps_udf_user_load ADD CONSTRAINT  uniq_uid_uname UNIQUE (`udf_id`, `user_name`);
ALTER TABLE linkis_ps_bml_resources ADD CONSTRAINT  uniq_rid_eflag UNIQUE (`resource_id`, `enable_flag`);


ALTER TABLE linkis_ps_configuration_config_key ADD UNIQUE uniq_key_ectype (`key`,`engine_conn_type`);

ALTER TABLE linkis_ps_configuration_config_key modify column engine_conn_type varchar(50) DEFAULT '' COMMENT 'engine type,such as spark,hive etc';

ALTER TABLE linkis_ps_common_lock ADD COLUMN locker VARCHAR(255) NOT NULL COMMENT 'locker';

ALTER TABLE linkis_ps_configuration_config_key ADD column template_required tinyint(1) DEFAULT 0 COMMENT 'template required 0 none / 1 must';
ALTER TABLE linkis_ps_configuration_config_key ADD column `boundary_type`   int(2) NOT NULL  COMMENT '0  none/ 1 with mix /2 with max / 3 min and max both';
ALTER TABLE linkis_ps_configuration_config_value modify COLUMN  config_value varchar(500);

DROP  TABLE IF EXISTS `linkis_ps_configuration_template_config_key`;
CREATE TABLE IF NOT EXISTS `linkis_ps_configuration_template_config_key` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `template_name` VARCHAR(200) NOT NULL COMMENT 'Configuration template name redundant storage',
    `template_uuid` VARCHAR(36) NOT NULL COMMENT 'uuid template id recorded by the third party',
    `key_id` BIGINT(20) NOT NULL COMMENT 'id of linkis_ps_configuration_config_key',
    `config_value` VARCHAR(200) NULL DEFAULT NULL COMMENT 'configuration value',
    `max_value` VARCHAR(50) NULL DEFAULT NULL COMMENT 'upper limit value',
    `min_value` VARCHAR(50) NULL DEFAULT NULL COMMENT 'Lower limit value (reserved)',
    `validate_range` VARCHAR(50) NULL DEFAULT NULL COMMENT 'Verification regularity (reserved)',
    `is_valid` VARCHAR(2) DEFAULT 'Y' COMMENT 'Is it valid? Reserved Y/N',
    `create_by` VARCHAR(50) NOT NULL COMMENT 'Creator',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `update_by` VARCHAR(50) NULL DEFAULT NULL COMMENT 'Update by',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uniq_tid_kid` (`template_uuid`, `key_id`),
    UNIQUE INDEX `uniq_tname_kid` (`template_uuid`, `key_id`)
    )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

DROP  TABLE IF EXISTS `linkis_ps_configuration_key_limit_for_user`;
CREATE TABLE IF NOT EXISTS `linkis_ps_configuration_key_limit_for_user` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `user_name` VARCHAR(50) NOT NULL COMMENT 'username',
    `combined_label_value` VARCHAR(128) NOT NULL COMMENT 'Combined label combined_userCreator_engineType such as hadoop-IDE,spark-2.4.3',
    `key_id` BIGINT(20) NOT NULL COMMENT 'id of linkis_ps_configuration_config_key',
    `config_value` VARCHAR(200) NULL DEFAULT NULL COMMENT 'configuration value',
    `max_value` VARCHAR(50) NULL DEFAULT NULL COMMENT 'upper limit value',
    `min_value` VARCHAR(50) NULL DEFAULT NULL COMMENT 'Lower limit value (reserved)',
    `latest_update_template_uuid` VARCHAR(36) NOT NULL COMMENT 'uuid template id recorded by the third party',
    `is_valid` VARCHAR(2) DEFAULT 'Y' COMMENT 'Is it valid? Reserved Y/N',
    `create_by` VARCHAR(50) NOT NULL COMMENT 'Creator',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `update_by` VARCHAR(50) NULL DEFAULT NULL COMMENT 'Update by',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uniq_com_label_kid` (`combined_label_value`, `key_id`)
    )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

DROP  TABLE IF EXISTS `linkis_ps_configutation_lm_across_cluster_rule`;
CREATE TABLE IF NOT EXISTS linkis_ps_configutation_lm_across_cluster_rule (
    id INT AUTO_INCREMENT COMMENT 'Rule ID, auto-increment primary key',
    cluster_name char(32) NOT NULL COMMENT 'Cluster name, cannot be empty',
    creator char(32) NOT NULL COMMENT 'Creator, cannot be empty',
    username char(32) NOT NULL COMMENT 'User, cannot be empty',
    create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time, cannot be empty',
    create_by char(32) NOT NULL COMMENT 'Creator, cannot be empty',
    update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Modification time, cannot be empty',
    update_by char(32) NOT NULL COMMENT 'Updater, cannot be empty',
    rules varchar(256) NOT NULL COMMENT 'Rule content, cannot be empty',
    is_valid VARCHAR(2) DEFAULT 'N' COMMENT 'Is it valid Y/N',
    PRIMARY KEY (id),
    UNIQUE KEY idx_creator_username (creator, username)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;