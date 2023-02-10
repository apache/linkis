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
SET
    FOREIGN_KEY_CHECKS = 0;
SET
    REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS `linkis_ps_datasource_access`;
CREATE TABLE `linkis_ps_datasource_access`
(
    `id`             bigint(20)  NOT NULL AUTO_INCREMENT,
    `table_id`       bigint(20)  NOT NULL,
    `visitor`        varchar(16) NOT NULL,
    `fields`         varchar(255) DEFAULT NULL,
    `application_id` int(4)      NOT NULL,
    `access_time`    datetime    NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `linkis_ps_dm_datasource_env`;
CREATE TABLE `linkis_ps_dm_datasource_env`
(
    `id`                 int(11)                       NOT NULL AUTO_INCREMENT,
    `env_name`           varchar(32)   NOT NULL,
    `env_desc`           varchar(255)           DEFAULT NULL,
    `datasource_type_id` int(11)                       NOT NULL,
    `parameter`          varchar(1024)           DEFAULT NULL,
    `create_time`        datetime                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user`        varchar(255)  NULL     DEFAULT NULL,
    `modify_time`        datetime                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modify_user`        varchar(255)  NULL     DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_env_name` (`env_name`),
    UNIQUE INDEX `uniq_name_dtid` (`env_name`, `datasource_type_id`)
);

DROP TABLE IF EXISTS `linkis_ps_dm_datasource_type_key`;
CREATE TABLE `linkis_ps_dm_datasource_type_key`
(
    `id`                  int(11)                       NOT NULL AUTO_INCREMENT,
    `data_source_type_id` int(11)                       NOT NULL,
    `key`                 varchar(32)   NOT NULL,
    `name`                varchar(32)   NOT NULL,
    `name_en`             varchar(32)   NOT NULL,
    `default_value`       varchar(50)   NULL     DEFAULT NULL,
    `value_type`          varchar(50)   NOT NULL,
    `scope`               varchar(50)   NULL     DEFAULT NULL,
    `require`             tinyint(1)                    NULL     DEFAULT 0,
    `description`         varchar(200)  NULL     DEFAULT NULL,
    `description_en`      varchar(200)  NULL     DEFAULT NULL,
    `value_regex`         varchar(200)  NULL     DEFAULT NULL,
    `ref_id`              bigint(20)                    NULL     DEFAULT NULL,
    `ref_value`           varchar(50)   NULL     DEFAULT NULL,
    `data_source`         varchar(200)  NULL     DEFAULT NULL,
    `update_time`         datetime                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_time`         datetime                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_dstid_key` (`data_source_type_id`, `key`)
);

DROP TABLE IF EXISTS `linkis_ps_dm_datasource_type`;
CREATE TABLE `linkis_ps_dm_datasource_type`
(
    `id`          int(11)                      NOT NULL AUTO_INCREMENT,
    `name`        varchar(32)  NOT NULL,
    `description` varchar(255)  DEFAULT NULL,
    `option`      varchar(32)   DEFAULT NULL,
    `classifier`  varchar(32)  NOT NULL,
    `icon`        varchar(255)  DEFAULT NULL,
    `layers`      int(3)                       NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uniq_name` (`name`)
);

DROP TABLE IF EXISTS `linkis_ps_error_code`;
CREATE TABLE `linkis_ps_error_code` (
                                        `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                        `error_code` varchar(50) NOT NULL,
                                        `error_desc` varchar(1024) NOT NULL,
                                        `error_regex` varchar(1024) DEFAULT NULL,
                                        `error_type` int(3) DEFAULT 0,
                                        PRIMARY KEY (`id`)
);


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
);


DROP TABLE IF EXISTS `linkis_cg_rm_external_resource_provider`;
CREATE TABLE `linkis_cg_rm_external_resource_provider` (
                                                           `id` int(10) NOT NULL AUTO_INCREMENT,
                                                           `resource_type` varchar(32) NOT NULL,
                                                           `name` varchar(32) NOT NULL,
                                                           `labels` varchar(32) DEFAULT NULL,
                                                           `config` text NOT NULL,
                                                           PRIMARY KEY (`id`)
);


DROP TABLE IF EXISTS `linkis_ps_udf_manager`;
CREATE TABLE `linkis_ps_udf_manager` (
                                         `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                         `user_name` varchar(20) DEFAULT NULL,
                                         PRIMARY KEY (`id`)
);

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
);