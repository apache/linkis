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
 
CREATE TABLE `linkis_cg_manager_linkis_resources` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `max_resource` varchar(1020)  DEFAULT NULL,
  `min_resource` varchar(1020)  DEFAULT NULL,
  `used_resource` varchar(1020)  DEFAULT NULL,
  `left_resource` varchar(1020)  DEFAULT NULL,
  `expected_resource` varchar(1020)  DEFAULT NULL,
  `locked_resource` varchar(1020)  DEFAULT NULL,
  `resourceType` varchar(255)  DEFAULT NULL,
  `ticketId` varchar(255)  DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updator` varchar(255)  DEFAULT NULL,
  `creator` varchar(255)  DEFAULT NULL,
  PRIMARY KEY (`id`)
);
INSERT INTO linkis_cg_manager_linkis_resources
(`id`, `max_resource`,`min_resource`,`used_resource`,`left_resource`,`expected_resource`,`locked_resource`,`resourceType`,`ticketId`,`update_time`,`create_time`,`updator`,`creator` )
VALUES (1,'testmax','mintest','user','left',null,null,'testtype',null,now(),now(),null,null);
INSERT INTO linkis_cg_manager_linkis_resources
(`id`, `max_resource`,`min_resource`,`used_resource`,`left_resource`,`expected_resource`,`locked_resource`,`resourceType`,`ticketId`,`update_time`,`create_time`,`updator`,`creator` )
VALUES (2,'testmax2','mintest2','user2','left2',null,null,'testtype',null,now(),now(),null,null);
INSERT INTO linkis_cg_manager_linkis_resources
(`id`, `max_resource`,`min_resource`,`used_resource`,`left_resource`,`expected_resource`,`locked_resource`,`resourceType`,`ticketId`,`update_time`,`create_time`,`updator`,`creator` )
VALUES (3,'testmax3','mintest3','user3','left3',null,null,'testtype','1',now(),now(),null,null);

CREATE TABLE `linkis_cg_manager_label_resource` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_id` int(20) DEFAULT NULL,
  `resource_id` int(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);

INSERT INTO linkis_cg_manager_label_resource (label_id, resource_id, update_time, create_time) VALUES(2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO linkis_cg_manager_label_resource (label_id, resource_id, update_time, create_time) VALUES(1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO linkis_cg_manager_label_resource (label_id, resource_id, update_time, create_time) VALUES(3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

CREATE TABLE `linkis_cg_manager_label_service_instance` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_id` int(20) DEFAULT NULL,
  `service_instance` varchar(128)  DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);

INSERT INTO linkis_cg_manager_label_service_instance(label_id, service_instance, update_time, create_time) VALUES (2, 'instance1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

CREATE TABLE `linkis_cg_manager_label_user` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(255)   DEFAULT NULL,
  `label_id` int(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ;

INSERT INTO linkis_cg_manager_label_user(username, label_id, update_time, create_time)VALUES('testname', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

CREATE TABLE `linkis_cg_manager_label` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_key` varchar(32)   NOT NULL,
  `label_value` varchar(255)   NOT NULL,
  `label_feature` varchar(16)   NOT NULL,
  `label_value_size` int(20) NOT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  unique key label_key_value(`label_key`, `label_value`),
  PRIMARY KEY (`id`)
);
INSERT INTO linkis_cg_manager_label (id,label_key,label_value,label_feature,label_value_size,update_time,create_time) VALUES (1,'combined_userCreator_engineType','*-LINKISCLI,*-*','OPTIONAL',2,'2022-03-28 01:31:08.0','2022-03-28 01:31:08.0');

CREATE TABLE `linkis_cg_manager_service_instance_metrics` (
  `instance` varchar(128)   NOT NULL,
  `instance_status` int(11) DEFAULT NULL,
  `overload` varchar(255)   DEFAULT NULL,
  `heartbeat_msg` text  ,
  `healthy_status` varchar(255)   DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`instance`)
) ;

CREATE TABLE `linkis_cg_manager_service_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `instance` varchar(128)    DEFAULT NULL,
  `name` varchar(32)    DEFAULT NULL,
  `owner` varchar(32)    DEFAULT NULL,
  `mark` varchar(32)    DEFAULT NULL,
  `identifier` varchar(32) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updator` varchar(32)    DEFAULT NULL,
  `creator` varchar(32)    DEFAULT NULL,
  PRIMARY KEY (`id`)
);
INSERT INTO  linkis_cg_manager_service_instance(`instance`, name, owner, mark, update_time, create_time, updator, creator)VALUES('instance1', 'testname', 'testowner', 'testmark', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'testupdator', 'testcreator');

CREATE TABLE `linkis_cg_manager_engine_em` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `engine_instance` varchar(128)     DEFAULT NULL,
  `em_instance` varchar(128)     DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ;

INSERT INTO  linkis_cg_manager_engine_em (engine_instance,em_instance,update_time,create_time) VALUES ('instance1','instance1','2022-02-26 14:54:05.0','2022-02-26 14:54:05.0');


CREATE TABLE `linkis_cg_manager_lock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lock_object` varchar(255)   DEFAULT NULL,
  `time_out` longtext  ,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);
INSERT INTO linkis_cg_manager_lock(lock_object, time_out, update_time, create_time)VALUES('testjson', 1l, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

CREATE TABLE `linkis_cg_ec_resource_info_record` (
    `id` INT(20) NOT NULL AUTO_INCREMENT,
    `label_value` VARCHAR(255) NOT NULL COMMENT 'ec labels stringValue',
    `create_user` VARCHAR(128) NOT NULL COMMENT 'ec create user',
    `service_instance` varchar(128)  DEFAULT NULL COMMENT 'ec instance info',
    `ecm_instance` varchar(128)  DEFAULT NULL COMMENT 'ecm instance info ',
    `ticket_id` VARCHAR(100) NOT NULL COMMENT 'ec ticket id',
    `status` varchar(50) DEFAULT NULL COMMENT 'EC status: Starting,Unlock,Locked,Idle,Busy,Running,ShuttingDown,Failed,Success',
    `log_dir_suffix` varchar(128)  DEFAULT NULL COMMENT 'log path',
    `request_times` INT(8) COMMENT 'resource request times',
    `request_resource` VARCHAR(1020) COMMENT 'request resource',
    `used_times` INT(8) COMMENT 'resource used times',
    `used_resource` VARCHAR(1020) COMMENT 'used resource',
    `metrics` TEXT DEFAULT NULL COMMENT 'ec metrics',
    `release_times` INT(8) COMMENT 'resource released times',
    `released_resource` VARCHAR(1020)  COMMENT 'released resource',
    `release_time` datetime DEFAULT NULL COMMENT 'released time',
    `used_time` datetime DEFAULT NULL COMMENT 'used time',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    PRIMARY KEY (`id`),
    KEY `idx_ticket_id` (`ticket_id`),
    UNIQUE KEY `uniq_tid_lv` (`ticket_id`,`label_value`)
);

CREATE TABLE `linkis_cg_manager_label_value_relation` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_value_key` varchar(255) NOT NULL,
  `label_value_content` varchar(255) DEFAULT NULL,
  `label_id` int(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_lvk_lid` (`label_value_key`,`label_id`)
) ;