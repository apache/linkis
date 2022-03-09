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
 
SET FOREIGN_KEY_CHECKS = 0;
SET REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS linkis_ps_dm_datasource;
CREATE TABLE linkis_ps_dm_datasource
(
    id                   numeric(20)                     NOT NULL AUTO_INCREMENT,
    datasource_name      varchar(255) NOT NULL,
    datasource_desc      varchar(255)      DEFAULT NULL,
    datasource_type_id   numeric(20)                     NOT NULL,
    create_identify      varchar(255)      DEFAULT NULL,
    create_system        varchar(255)      DEFAULT NULL,
    parameter            varchar(255) NULL DEFAULT NULL,
    create_time          datetime(3)                      NULL DEFAULT CURRENT_TIMESTAMP,
    modify_time          datetime(3)                      NULL DEFAULT CURRENT_TIMESTAMP,
    create_user          varchar(255)      DEFAULT NULL,
    modify_user          varchar(255)      DEFAULT NULL,
    labels               varchar(255)      DEFAULT NULL,
    version_id           numeric(20)                          DEFAULT NULL COMMENT 'current version id',
    expire               numeric(20)                         DEFAULT 0,
    published_version_id numeric(20)                          DEFAULT NULL,
    PRIMARY KEY (id)
) ;

DROP TABLE IF EXISTS linkis_ps_dm_datasource_type;
CREATE TABLE linkis_ps_dm_datasource_type
(
    id          numeric(20)                      NOT NULL,
    name        varchar(32)  NOT NULL,
    description varchar(255)  DEFAULT NULL,
    option      varchar(32)   DEFAULT NULL,
    classifier  varchar(32)  NOT NULL,
    icon        varchar(255)  DEFAULT NULL,
    layers      numeric(20)                       NOT NULL,
    PRIMARY KEY (id)
) ;

DROP TABLE IF EXISTS linkis_ps_dm_datasource_env;
CREATE TABLE linkis_ps_dm_datasource_env
(
    id                 numeric(20)                       NOT NULL AUTO_INCREMENT,
    env_name           varchar(32)   NOT NULL,
    env_desc           varchar(255)           DEFAULT NULL,
    datasource_type_id numeric(20)                       NOT NULL,
    parameter          varchar(255)           DEFAULT NULL,
    create_time        datetime(3)                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user        varchar(255)  NULL     DEFAULT NULL,
    modify_time        datetime(3)                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modify_user        varchar(255)  NULL     DEFAULT NULL,
    PRIMARY KEY (id)
);
DROP TABLE IF EXISTS linkis_ps_dm_datasource_type_key;
CREATE TABLE linkis_ps_dm_datasource_type_key
(
    id                  numeric(20)                       NOT NULL,
    data_source_type_id numeric(20)                       NOT NULL,
    `key`                 varchar(32)   NOT NULL,
    `name`                varchar(32)   NOT NULL,
    default_value       varchar(50)   NULL     DEFAULT NULL,
    value_type          varchar(50)   NOT NULL,
    `scope`               varchar(50)   NULL     DEFAULT NULL,
    require             numeric(20)                    NULL     DEFAULT 0,
    description         varchar(200)  NULL     DEFAULT NULL,
    value_regex         varchar(200)  NULL     DEFAULT NULL,
    ref_id              bigint(20)                    NULL     DEFAULT NULL,
    ref_value           varchar(50)   NULL     DEFAULT NULL,
    data_source         varchar(200)  NULL     DEFAULT NULL,
    update_time         datetime(3)                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_time         datetime(3)                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
DROP TABLE IF EXISTS linkis_ps_dm_datasource_version;
CREATE TABLE linkis_ps_dm_datasource_version
(
    version_id    numeric(20)                        NOT NULL AUTO_INCREMENT,
    datasource_id numeric(20)                        NOT NULL,
    parameter     varchar(500)  NULL DEFAULT NULL,
    `comment`     varchar(255)   NULL DEFAULT NULL,
    create_time   datetime(3)                    NULL DEFAULT CURRENT_TIMESTAMP,
    create_user   varchar(255)   NULL DEFAULT NULL,
    PRIMARY KEY (version_id, datasource_id)
);
INSERT INTO linkis_ps_dm_datasource_type VALUES (1, 'mysql', 'mysql db', 'mysql db','relation database', 2000,3);
INSERT INTO linkis_ps_dm_datasource_type_key VALUES (1,1, 'host', 'Host', '127.0.0.1','TEXT', 'ENV',1,'host name','',null,null,null,'2021-04-08 03:13:36','2021-04-08 03:13:36');
INSERT INTO linkis_ps_dm_datasource_type_key VALUES (2,1, 'port', 'Port', '3306','TEXT', null,1,'port name','',null,null,null,'2021-04-08 03:13:36','2021-04-08 03:13:36');