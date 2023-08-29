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
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for linkis_ps_variable_key_user
-- ----------------------------
DROP TABLE IF EXISTS `linkis_ps_variable_key_user`;
CREATE TABLE `linkis_ps_variable_key_user`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT,
    `application_id` bigint(20)   DEFAULT NULL COMMENT 'Reserved word',
    `key_id`         bigint(20)   DEFAULT NULL,
    `user_name`      varchar(50)  DEFAULT NULL,
    `value`          varchar(200) DEFAULT NULL COMMENT 'Value of the global variable',
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `linkis_ps_variable_key`;
CREATE TABLE `linkis_ps_variable_key`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT,
    `key`            varchar(50)  DEFAULT NULL COMMENT 'Key of the global variable',
    `description`    varchar(200) DEFAULT NULL COMMENT 'Reserved word',
    `name`           varchar(50)  DEFAULT NULL COMMENT 'Reserved word',
    `application_id` bigint(20)   DEFAULT NULL COMMENT 'Reserved word',
    `default_value`  varchar(200) DEFAULT NULL COMMENT 'Reserved word',
    `value_type`     varchar(50)  DEFAULT NULL COMMENT 'Reserved word',
    `value_regex`    varchar(100) DEFAULT NULL COMMENT 'Reserved word',
    PRIMARY KEY (`id`)
);

DELETE FROM linkis_ps_variable_key;

insert into linkis_ps_variable_key(`id`,`key`,`application_id`) values (1,'mywork1',-1);
insert into linkis_ps_variable_key(`id`,`key`,`application_id`) values (2,'mywork2',-1);
insert into linkis_ps_variable_key(`id`,`key`,`application_id`) values (3,'mywork3',-1);
insert into linkis_ps_variable_key(`id`,`key`,`application_id`) values (4,'mywork4',-1);
insert into linkis_ps_variable_key(`id`,`key`,`application_id`) values (5,'mywork5',-1);

DELETE FROM linkis_ps_variable_key_user;

insert into linkis_ps_variable_key_user values (1,-1,1,'tom1','stu');
insert into linkis_ps_variable_key_user values (2,-1,2,'tom2','tea');
insert into linkis_ps_variable_key_user values (3,-1,3,'tom3','bob');
insert into linkis_ps_variable_key_user values (4,-1,4,'tom4','swim');
insert into linkis_ps_variable_key_user values (5,-1,5,'tom5','smile');