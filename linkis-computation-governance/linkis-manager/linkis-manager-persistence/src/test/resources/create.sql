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


CREATE TABLE `linkis_cg_manager_label_resource` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `label_id` int(20) DEFAULT NULL,
  `resource_id` int(20) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);

INSERT INTO linkis_cg_manager_label_resource (label_id, resource_id, update_time, create_time) VALUES(2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

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
  PRIMARY KEY (`id`)
);
INSERT INTO linkis_cg_manager_label (id,label_key,label_value,label_feature,label_value_size,update_time,create_time) VALUES (2,'combined_userCreator_engineType','*-LINKISCLI,*-*','OPTIONAL',2,'2022-03-28 01:31:08.0','2022-03-28 01:31:08.0');