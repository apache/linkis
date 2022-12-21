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