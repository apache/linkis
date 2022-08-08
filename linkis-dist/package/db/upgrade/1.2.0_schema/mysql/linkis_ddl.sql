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

-- ----------------------------
-- Table structure for linkis_ps_dm_datasource_type_key
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_dm_datasource_type_key`;
CREATE TABLE `linkis_ps_dm_datasource_type_key`
(
    `id`                  int(11)                       NOT NULL AUTO_INCREMENT,
    `data_source_type_id` int(11)                       NOT NULL,
    `key`                 varchar(32) COLLATE utf8_bin  NOT NULL,
    `name`                varchar(32) COLLATE utf8_bin  NOT NULL,
    `name_en`             varchar(32) COLLATE utf8_bin  NOT NULL,
    `default_value`       varchar(50) COLLATE utf8_bin  NULL     DEFAULT NULL,
    `value_type`          varchar(50) COLLATE utf8_bin  NOT NULL,
    `scope`               varchar(50) COLLATE utf8_bin  NULL     DEFAULT NULL,
    `require`             tinyint(1)                    NULL     DEFAULT 0,
    `description`         varchar(200) COLLATE utf8_bin NULL     DEFAULT NULL,
    `description_en`      varchar(200) COLLATE utf8_bin NULL     DEFAULT NULL,
    `value_regex`         varchar(200) COLLATE utf8_bin NULL     DEFAULT NULL,
    `ref_id`              bigint(20)                    NULL     DEFAULT NULL,
    `ref_value`           varchar(50) COLLATE utf8_bin  NULL     DEFAULT NULL,
    `data_source`         varchar(200) COLLATE utf8_bin NULL     DEFAULT NULL,
    `update_time`         datetime                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_time`         datetime                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
