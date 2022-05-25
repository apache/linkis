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

DROP TABLE IF EXISTS `linkis_cg_manager_label`;
CREATE TABLE `linkis_cg_manager_label`
(
    `id`               int(20)      NOT NULL AUTO_INCREMENT,
    `label_key`        varchar(32)  NOT NULL,
    `label_value`      varchar(255) NOT NULL,
    `label_feature`    varchar(16)  NOT NULL,
    `label_value_size` int(20)      NOT NULL,
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `label_key_value` (`label_key`,`label_value`)
);

DROP TABLE IF EXISTS `linkis_ps_configuration_config_key`;
CREATE TABLE `linkis_ps_configuration_config_key`
(
    `id`               bigint(20) NOT NULL AUTO_INCREMENT,
    `key`              varchar(50)  DEFAULT NULL COMMENT 'Set key, e.g. spark.executor.instances',
    `description`      varchar(200) DEFAULT NULL,
    `name`             varchar(50)  DEFAULT NULL,
    `default_value`    varchar(200) DEFAULT NULL COMMENT 'Adopted when user does not set key',
    `validate_type`    varchar(50)  DEFAULT NULL COMMENT 'Validate type, one of the following: None, NumInterval, FloatInterval, Include, Regex, OPF, Custom Rules',
    `validate_range`   varchar(50)  DEFAULT NULL COMMENT 'Validate range',
    `engine_conn_type` varchar(50)  DEFAULT NULL COMMENT 'engine type,such as spark,hive etc',
    `is_hidden`        tinyint(1)   DEFAULT NULL COMMENT 'Whether it is hidden from user. If set to 1(true), then user cannot modify, however, it could still be used in back-end',
    `is_advanced`      tinyint(1)   DEFAULT NULL COMMENT 'Whether it is an advanced parameter. If set to 1(true), parameters would be displayed only when user choose to do so',
    `level`            tinyint(1)   DEFAULT NULL COMMENT 'Basis for displaying sorting in the front-end. Higher the level is, higher the rank the parameter gets',
    `treeName`         varchar(20)  DEFAULT NULL COMMENT 'Reserved field, representing the subdirectory of engineType',
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `linkis_ps_configuration_config_value`;
CREATE TABLE linkis_ps_configuration_config_value
(
    `id`              bigint(20) NOT NULL AUTO_INCREMENT,
    `config_key_id`   bigint(20),
    `config_value`    varchar(50),
    `config_label_id` int(20),
    `update_time`     datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_time`     datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX (`config_key_id`, `config_label_id`)
);

DROP TABLE IF EXISTS `linkis_ps_configuration_key_engine_relation`;
CREATE TABLE `linkis_ps_configuration_key_engine_relation`
(
    `id`                   bigint(20) NOT NULL AUTO_INCREMENT,
    `config_key_id`        bigint(20) NOT NULL COMMENT 'config key id',
    `engine_type_label_id` bigint(20) NOT NULL COMMENT 'engine label id',
    PRIMARY KEY (`id`),
    UNIQUE INDEX (`config_key_id`, `engine_type_label_id`)
);

DROP TABLE IF EXISTS `linkis_ps_configuration_category`;
CREATE TABLE `linkis_ps_configuration_category`
(
    `id`          int(20)  NOT NULL AUTO_INCREMENT,
    `label_id`    int(20)  NOT NULL,
    `level`       int(20)  NOT NULL,
    `description` varchar(200),
    `tag`         varchar(200),
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX (`label_id`)
);