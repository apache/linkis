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
    `id`             bigint(20) NOT NULL AUTO_INCREMENT,
    `table_id`       bigint(20) NOT NULL,
    `visitor`        varchar(16) NOT NULL,
    `fields`         varchar(255) DEFAULT NULL,
    `application_id` int(4) NOT NULL,
    `access_time`    datetime    NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `linkis_ps_dm_datasource_env`;
CREATE TABLE `linkis_ps_dm_datasource_env`
(
    `id`                 int(11) NOT NULL AUTO_INCREMENT,
    `env_name`           varchar(32) NOT NULL,
    `env_desc`           varchar(255)         DEFAULT NULL,
    `datasource_type_id` int(11) NOT NULL,
    `parameter`          varchar(1024)        DEFAULT NULL,
    `create_time`        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user`        varchar(255) NULL DEFAULT NULL,
    `modify_time`        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modify_user`        varchar(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_env_name` (`env_name`),
    UNIQUE INDEX `uniq_name_dtid` (`env_name`, `datasource_type_id`)
);

DROP TABLE IF EXISTS `linkis_ps_dm_datasource_type_key`;
CREATE TABLE `linkis_ps_dm_datasource_type_key`
(
    `id`                  int(11) NOT NULL AUTO_INCREMENT,
    `data_source_type_id` int(11) NOT NULL,
    `key`                 varchar(32) NOT NULL,
    `name`                varchar(32) NOT NULL,
    `name_en`             varchar(32) NOT NULL,
    `default_value`       varchar(50) NULL DEFAULT NULL,
    `value_type`          varchar(50) NOT NULL,
    `scope`               varchar(50) NULL DEFAULT NULL,
    `require`             tinyint(1) NULL DEFAULT 0,
    `description`         varchar(200) NULL DEFAULT NULL,
    `description_en`      varchar(200) NULL DEFAULT NULL,
    `value_regex`         varchar(200) NULL DEFAULT NULL,
    `ref_id`              bigint(20) NULL DEFAULT NULL,
    `ref_value`           varchar(50) NULL DEFAULT NULL,
    `data_source`         varchar(200) NULL DEFAULT NULL,
    `update_time`         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_time`         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_dstid_key` (`data_source_type_id`, `key`)
);

DROP TABLE IF EXISTS `linkis_ps_dm_datasource_type`;
CREATE TABLE `linkis_ps_dm_datasource_type`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT,
    `name`        varchar(32) NOT NULL,
    `description` varchar(255) DEFAULT NULL,
    `option`      varchar(32)  DEFAULT NULL,
    `classifier`  varchar(32) NOT NULL,
    `icon`        varchar(255) DEFAULT NULL,
    `layers`      int(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uniq_name` (`name`)
);

DROP TABLE IF EXISTS `linkis_ps_error_code`;
CREATE TABLE `linkis_ps_error_code`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT,
    `error_code`  varchar(50)   NOT NULL,
    `error_desc`  varchar(1024) NOT NULL,
    `error_regex` varchar(1024) DEFAULT NULL,
    `error_type`  int(3) DEFAULT 0,
    PRIMARY KEY (`id`)
);


DROP TABLE IF EXISTS `linkis_mg_gateway_auth_token`;
CREATE TABLE `linkis_mg_gateway_auth_token`
(
    `id`             int(11) NOT NULL AUTO_INCREMENT,
    `token_name`     varchar(128) NOT NULL,
    `legal_users`    text,
    `legal_hosts`    text,
    `business_owner` varchar(32),
    `create_time`    DATE   DEFAULT NULL,
    `update_time`    DATE   DEFAULT NULL,
    `elapse_day`     BIGINT DEFAULT NULL,
    `update_by`      varchar(32),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_token_name` (`token_name`)
);


DROP TABLE IF EXISTS `linkis_cg_rm_external_resource_provider`;
CREATE TABLE `linkis_cg_rm_external_resource_provider`
(
    `id`            int(10) NOT NULL AUTO_INCREMENT,
    `resource_type` varchar(32) NOT NULL,
    `name`          varchar(32) NOT NULL,
    `labels`        varchar(32) DEFAULT NULL,
    `config`        text        NOT NULL,
    PRIMARY KEY (`id`)
);


DROP TABLE IF EXISTS `linkis_ps_udf_manager`;
CREATE TABLE `linkis_ps_udf_manager`
(
    `id`        bigint(20) NOT NULL AUTO_INCREMENT,
    `user_name` varchar(20) DEFAULT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `linkis_ps_udf_tree`;
CREATE TABLE `linkis_ps_udf_tree`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT,
    `parent`      bigint(20) NOT NULL,
    `name`        varchar(100) DEFAULT NULL COMMENT 'Category name of the function. It would be displayed in the front-end',
    `user_name`   varchar(50) NOT NULL,
    `description` varchar(255) DEFAULT NULL,
    `create_time` timestamp   NOT NULL,
    `update_time` timestamp   NOT NULL,
    `category`    varchar(50)  DEFAULT NULL COMMENT 'Used to distinguish between udf and function',
    PRIMARY KEY (`id`)
);



DROP TABLE IF EXISTS `linkis_cg_manager_label`;
CREATE TABLE `linkis_cg_manager_label`
(
    `id`               int(20) NOT NULL AUTO_INCREMENT,
    `label_key`        varchar(32)  NOT NULL,
    `label_value`      varchar(255) NOT NULL,
    `label_feature`    varchar(16)  NOT NULL,
    `label_value_size` int(20) NOT NULL,
    `update_time`      datetime     NOT NULL,
    `create_time`      datetime     NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `linkis_cg_manager_label_value_relation`;
CREATE TABLE `linkis_cg_manager_label_value_relation`
(
    `id`                  int(20) NOT NULL AUTO_INCREMENT,
    `label_value_key`     varchar(255) NOT NULL,
    `label_value_content` varchar(255) DEFAULT NULL,
    `label_id`            int(20) DEFAULT NULL,
    `update_time`         datetime,
    `create_time`         datetime,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `linkis_ps_configuration_config_key`;
CREATE TABLE `linkis_ps_configuration_config_key`
(
    `id`               bigint(20) NOT NULL AUTO_INCREMENT,
    `key`              varchar(50)   DEFAULT NULL COMMENT 'Set key, e.g. spark.executor.instances',
    `description`      varchar(200)  DEFAULT NULL,
    `name`             varchar(50)   DEFAULT NULL,
    `default_value`    varchar(200)  DEFAULT NULL COMMENT 'Adopted when user does not set key',
    `validate_type`    varchar(50)   DEFAULT NULL COMMENT 'Validate type, one of the following: None, NumInterval, FloatInterval, Include, Regex, OPF, Custom Rules',
    `validate_range`   varchar(1000) DEFAULT NULL COMMENT 'Validate range',
    `engine_conn_type` varchar(50)   DEFAULT NULL COMMENT 'engine type,such as spark,hive etc',
    `is_hidden`        tinyint(1) DEFAULT NULL COMMENT 'Whether it is hidden from user. If set to 1(true), then user cannot modify, however, it could still be used in back-end',
    `is_advanced`      tinyint(1) DEFAULT NULL COMMENT 'Whether it is an advanced parameter. If set to 1(true), parameters would be displayed only when user choose to do so',
    `level`            tinyint(1) DEFAULT NULL COMMENT 'Basis for displaying sorting in the front-end. Higher the level is, higher the rank the parameter gets',
    `treeName`         varchar(20)   DEFAULT NULL COMMENT 'Reserved field, representing the subdirectory of engineType',
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `linkis_ps_configuration_config_value`;
CREATE TABLE `linkis_ps_configuration_config_value`
(
    `id`              bigint(20) NOT NULL AUTO_INCREMENT,
    `config_key_id`   bigint(20),
    `config_value`    varchar(200),
    `config_label_id` int(20),
    `update_time`     datetime NOT NULL,
    `create_time`     datetime NOT NULL,
    PRIMARY KEY (`id`)
);



DROP TABLE IF EXISTS `linkis_ps_configuration_key_engine_relation`;
CREATE TABLE `linkis_ps_configuration_key_engine_relation`
(
    `id`                   bigint(20) NOT NULL AUTO_INCREMENT,
    `config_key_id`        bigint(20) NOT NULL COMMENT 'config key id',
    `engine_type_label_id` bigint(20) NOT NULL COMMENT 'engine label id',
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `linkis_cg_engine_conn_plugin_bml_resources`;
CREATE TABLE `linkis_cg_engine_conn_plugin_bml_resources`
(
    `id`                   bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `engine_conn_type`     varchar(100) NOT NULL COMMENT 'Engine type',
    `version`              varchar(100) COMMENT 'version',
    `file_name`            varchar(255) COMMENT 'file name',
    `file_size`            bigint(20) DEFAULT 0 NOT NULL COMMENT 'file size',
    `last_modified`        bigint(20) COMMENT 'File update time',
    `bml_resource_id`      varchar(100) NOT NULL COMMENT 'Owning system',
    `bml_resource_version` varchar(200) NOT NULL COMMENT 'Resource owner',
    `create_time`          datetime     NOT NULL COMMENT 'created time',
    `last_update_time`     datetime     NOT NULL COMMENT 'updated time',
    PRIMARY KEY (`id`)
);