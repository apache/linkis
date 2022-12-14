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
-- CREATE TABLE linkis_cg_tenant_label_config
-- ----------------------------
CREATE TABLE `linkis_cg_tenant_label_config` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `user` varchar(50) COLLATE utf8_bin NOT NULL,
  `creator` varchar(50) COLLATE utf8_bin NOT NULL,
  `tenant_value` varchar(128) COLLATE utf8_bin NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `desc` varchar(100) COLLATE utf8_bin NOT NULL,
  `bussiness_user` varchar(50) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_creator` (`user`,`creator`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


-- ----------------------------
-- CREATE TABLE linkis_cg_user_ip_config
-- ----------------------------
CREATE TABLE `linkis_cg_user_ip_config` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `user` varchar(50) COLLATE utf8_bin NOT NULL,
  `creator` varchar(50) COLLATE utf8_bin NOT NULL,
  `ip_list` text COLLATE utf8_bin NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `desc` varchar(100) COLLATE utf8_bin NOT NULL,
  `bussiness_user` varchar(50) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_creator` (`user`,`creator`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



ALTER TABLE `linkis_ps_configuration_config_key` ADD COLUMN `en_description` varchar(200) DEFAULT NULL COMMENT 'english description';

ALTER TABLE `linkis_ps_configuration_config_key` ADD COLUMN `en_name` varchar(100) DEFAULT NULL COMMENT 'english name';

ALTER TABLE `linkis_ps_configuration_config_key` ADD COLUMN `en_treeName` varchar(100) DEFAULT NULL COMMENT 'english treeName';


ALTER TABLE `linkis_ps_dm_datasource_type` ADD COLUMN `description_en` varchar(255) DEFAULT NULL COMMENT 'english description';

ALTER TABLE `linkis_ps_dm_datasource_type` ADD COLUMN `option_en` varchar(32) DEFAULT NULL COMMENT 'english option';

ALTER TABLE `linkis_ps_dm_datasource_type` ADD COLUMN `classifier_en` varchar(32) DEFAULT NULL COMMENT 'english classifier';

ALTER TABLE `linkis_ps_dm_datasource_type_key` ADD COLUMN `name_en` varchar(32) DEFAULT NULL COMMENT 'english name';

ALTER TABLE `linkis_ps_dm_datasource_type_key` ADD COLUMN `description_en` varchar(200) DEFAULT NULL COMMENT 'english description';

