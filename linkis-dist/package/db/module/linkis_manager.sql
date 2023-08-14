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
 
DROP TABLE IF EXISTS `linkis_cg_manager_service_instance`;

CREATE TABLE `linkis_cg_manager_service_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `instance` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `name` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `owner` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `mark` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `identifier` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updator` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `creator` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `instance` (`instance`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_linkis_resources`;

CREATE TABLE `linkis_cg_manager_linkis_resources` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `max_resource` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `min_resource` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `used_resource` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `left_resource` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `expected_resource` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `locked_resource` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `resourceType` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ticketId` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updator` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `creator` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_lock`;

CREATE TABLE `linkis_cg_manager_lock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lock_object` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `time_out` longtext COLLATE utf8_bin,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_rm_external_resource_provider`;
CREATE TABLE `linkis_cg_rm_external_resource_provider` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `resource_type` varchar(32) NOT NULL,
  `name` varchar(32) NOT NULL,
  `labels` varchar(32) DEFAULT NULL,
  `config` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `linkis_cg_manager_engine_em`;
CREATE TABLE `linkis_cg_manager_engine_em` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `engine_instance` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `em_instance` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_label`;

CREATE TABLE `linkis_cg_manager_label` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_key` varchar(50) COLLATE utf8_bin NOT NULL,
  `label_value` varchar(255) COLLATE utf8_bin NOT NULL,
  `label_feature` varchar(16) COLLATE utf8_bin NOT NULL,
  `label_value_size` int(20) NOT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `label_key_value` (`label_key`,`label_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_label_value_relation`;

CREATE TABLE `linkis_cg_manager_label_value_relation` (
  `label_value_key` varchar(255) COLLATE utf8_bin NOT NULL,
  `label_value_content` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `label_id` int(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `label_value_key_label_id` (`label_value_key`,`label_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_label_resource`;
CREATE TABLE `linkis_cg_manager_label_resource` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_id` int(20) DEFAULT NULL,
  `resource_id` int(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


DROP TABLE IF EXISTS `linkis_cg_manager_label_service_instance`;
CREATE TABLE `linkis_cg_manager_label_service_instance` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_id` int(20) DEFAULT NULL,
  `service_instance` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


DROP TABLE IF EXISTS `linkis_cg_manager_label_user`;
CREATE TABLE `linkis_cg_manager_label_user` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `label_id` int(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


DROP TABLE IF EXISTS `linkis_cg_manager_metrics_history`;

CREATE TABLE `linkis_cg_manager_metrics_history` (
  `instance_status` int(20) DEFAULT NULL,
  `overload` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `heartbeat_msg` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `healthy_status` int(20) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `creator` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `ticketID` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `serviceName` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `instance` varchar(255) COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `linkis_cg_manager_service_instance_metrics`;

CREATE TABLE `linkis_cg_manager_service_instance_metrics` (
  `instance` varchar(32) COLLATE utf8_bin NOT NULL,
  `instance_status` int(11) DEFAULT NULL,
  `overload` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `heartbeat_msg` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `healthy_status` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`instance`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
