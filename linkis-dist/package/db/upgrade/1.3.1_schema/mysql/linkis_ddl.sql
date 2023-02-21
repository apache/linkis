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
-- Alter table structure for linkis_ps_dm_datasource_env
-- ----------------------------

ALTER TABLE `linkis_ps_dm_datasource_env` ADD CONSTRAINT `env_name` UNIQUE (`env_name`);
ALTER TABLE `linkis_ps_dm_datasource_env` MODIFY COLUMN `parameter` varchar(1024) CHARACTER SET utf8 COLLATE utf8_bin NULL;

ALTER TABLE `linkis_cg_ec_resource_info_record` ADD COLUMN `status` varchar(50) DEFAULT NULL COMMENT 'EC status: Starting,Unlock,Locked,Idle,Busy,Running,ShuttingDown,Failed,Success';
-- ----------------------------
-- Alter table structure for linkis_ps_dm_datasource
-- ----------------------------
ALTER TABLE `linkis_ps_dm_datasource` MODIFY COLUMN `parameter` varchar(1024) CHARACTER SET utf8 COLLATE utf8_bin NULL;

-- ----------------------------
-- Alter table structure for linkis_ps_dm_datasource_type_key
-- ----------------------------
ALTER TABLE `linkis_ps_dm_datasource_type_key` ADD CONSTRAINT  `data_source_type_id_key` UNIQUE (`data_source_type_id`, `key`);

-- ----------------------------
-- Rebuild linkis_cg_manager_lock and remove the unique key. Because the table data will be cleaned up, it is better to rebuild here
-- ----------------------------
DROP TABLE IF EXISTS `linkis_cg_manager_lock`;
CREATE TABLE `linkis_cg_manager_lock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lock_object` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `time_out` longtext COLLATE utf8_bin,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

