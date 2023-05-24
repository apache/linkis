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
    `id`             bigint(20) NOT NULL AUTO_INCREMENT,
    `table_id`       bigint(20) NOT NULL,
    `visitor`        varchar(16) NOT NULL,
    `fields`         varchar(255) DEFAULT NULL,
    `application_id` int(4) NOT NULL,
    `access_time`    datetime    NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `linkis_ps_dm_datasource_env`;
CREATE TABLE `linkis_ps_dm_datasource_env`
(
    `id`                 int(11) NOT NULL AUTO_INCREMENT,
    `env_name`           varchar(32) NOT NULL,
    `env_desc`           varchar(255)         DEFAULT NULL,
    `datasource_type_id` int(11) NOT NULL,
    `parameter`          varchar(1024)        DEFAULT NULL,
    `create_time`        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_user`        varchar(255) NULL DEFAULT NULL,
    `modify_time`        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modify_user`        varchar(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_env_name` (`env_name`),
    UNIQUE INDEX `uniq_name_dtid` (`env_name`, `datasource_type_id`)
);

DROP TABLE IF EXISTS `linkis_ps_dm_datasource_type_key`;
CREATE TABLE `linkis_ps_dm_datasource_type_key`
(
    `id`                  int(11) NOT NULL AUTO_INCREMENT,
    `data_source_type_id` int(11) NOT NULL,
    `key`                 varchar(32) NOT NULL,
    `name`                varchar(32) NOT NULL,
    `name_en`             varchar(32) NOT NULL,
    `default_value`       varchar(50) NULL DEFAULT NULL,
    `value_type`          varchar(50) NOT NULL,
    `scope`               varchar(50) NULL DEFAULT NULL,
    `require`             tinyint(1) NULL DEFAULT 0,
    `description`         varchar(200) NULL DEFAULT NULL,
    `description_en`      varchar(200) NULL DEFAULT NULL,
    `value_regex`         varchar(200) NULL DEFAULT NULL,
    `ref_id`              bigint(20) NULL DEFAULT NULL,
    `ref_value`           varchar(50) NULL DEFAULT NULL,
    `data_source`         varchar(200) NULL DEFAULT NULL,
    `update_time`         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_time`         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_dstid_key` (`data_source_type_id`, `key`)
);

DROP TABLE IF EXISTS `linkis_ps_dm_datasource_type`;
CREATE TABLE `linkis_ps_dm_datasource_type`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT,
    `name`        varchar(32) NOT NULL,
    `description` varchar(255) DEFAULT NULL,
    `option`      varchar(32)  DEFAULT NULL,
    `classifier`  varchar(32) NOT NULL,
    `icon`        varchar(255) DEFAULT NULL,
    `layers`      int(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uniq_name` (`name`)
);

DROP TABLE IF EXISTS `linkis_ps_error_code`;
CREATE TABLE `linkis_ps_error_code`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT,
    `error_code`  varchar(50)   NOT NULL,
    `error_desc`  varchar(1024) NOT NULL,
    `error_regex` varchar(1024) DEFAULT NULL,
    `error_type`  int(3) DEFAULT 0,
    PRIMARY KEY (`id`)
);


DROP TABLE IF EXISTS `linkis_mg_gateway_auth_token`;
CREATE TABLE `linkis_mg_gateway_auth_token`
(
    `id`             int(11) NOT NULL AUTO_INCREMENT,
    `token_name`     varchar(128) NOT NULL,
    `legal_users`    text,
    `legal_hosts`    text,
    `business_owner` varchar(32),
    `create_time`    DATE   DEFAULT NULL,
    `update_time`    DATE   DEFAULT NULL,
    `elapse_day`     BIGINT DEFAULT NULL,
    `update_by`      varchar(32),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_token_name` (`token_name`)
);


DROP TABLE IF EXISTS `linkis_cg_rm_external_resource_provider`;
CREATE TABLE `linkis_cg_rm_external_resource_provider`
(
    `id`            int(10) NOT NULL AUTO_INCREMENT,
    `resource_type` varchar(32) NOT NULL,
    `name`          varchar(32) NOT NULL,
    `labels`        varchar(32) DEFAULT NULL,
    `config`        text        NOT NULL,
    PRIMARY KEY (`id`)
);


DROP TABLE IF EXISTS `linkis_ps_udf_manager`;
CREATE TABLE `linkis_ps_udf_manager`
(
    `id`        bigint(20) NOT NULL AUTO_INCREMENT,
    `user_name` varchar(20) DEFAULT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `linkis_ps_udf_tree`;
CREATE TABLE `linkis_ps_udf_tree`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT,
    `parent`      bigint(20) NOT NULL,
    `name`        varchar(100) DEFAULT NULL COMMENT 'Category name of the function. It would be displayed in the front-end',
    `user_name`   varchar(50) NOT NULL,
    `description` varchar(255) DEFAULT NULL,
    `create_time` timestamp   NOT NULL,
    `update_time` timestamp   NOT NULL,
    `category`    varchar(50)  DEFAULT NULL COMMENT 'Used to distinguish between udf and function',
    PRIMARY KEY (`id`)
);



DROP TABLE IF EXISTS `linkis_cg_manager_label`;
CREATE TABLE `linkis_cg_manager_label`
(
    `id`               int(20) NOT NULL AUTO_INCREMENT,
    `label_key`        varchar(32)  NOT NULL,
    `label_value`      varchar(255) NOT NULL,
    `label_feature`    varchar(16)  NOT NULL,
    `label_value_size` int(20) NOT NULL,
    `update_time`      datetime     NOT NULL,
    `create_time`      datetime     NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `linkis_cg_manager_label_value_relation`;
CREATE TABLE `linkis_cg_manager_label_value_relation`
(
    `id`                  int(20) NOT NULL AUTO_INCREMENT,
    `label_value_key`     varchar(255) NOT NULL,
    `label_value_content` varchar(255) DEFAULT NULL,
    `label_id`            int(20) DEFAULT NULL,
    `update_time`         datetime,
    `create_time`         datetime,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `linkis_ps_configuration_config_key`;
CREATE TABLE `linkis_ps_configuration_config_key`
(
    `id`               bigint(20) NOT NULL AUTO_INCREMENT,
    `key`              varchar(50)   DEFAULT NULL COMMENT 'Set key, e.g. spark.executor.instances',
    `description`      varchar(200)  DEFAULT NULL,
    `name`             varchar(50)   DEFAULT NULL,
    `default_value`    varchar(200)  DEFAULT NULL COMMENT 'Adopted when user does not set key',
    `validate_type`    varchar(50)   DEFAULT NULL COMMENT 'Validate type, one of the following: None, NumInterval, FloatInterval, Include, Regex, OPF, Custom Rules',
    `validate_range`   varchar(1000) DEFAULT NULL COMMENT 'Validate range',
    `engine_conn_type` varchar(50)   DEFAULT NULL COMMENT 'engine type,such as spark,hive etc',
    `is_hidden`        tinyint(1) DEFAULT NULL COMMENT 'Whether it is hidden from user. If set to 1(true), then user cannot modify, however, it could still be used in back-end',
    `is_advanced`      tinyint(1) DEFAULT NULL COMMENT 'Whether it is an advanced parameter. If set to 1(true), parameters would be displayed only when user choose to do so',
    `level`            tinyint(1) DEFAULT NULL COMMENT 'Basis for displaying sorting in the front-end. Higher the level is, higher the rank the parameter gets',
    `treeName`         varchar(20)   DEFAULT NULL COMMENT 'Reserved field, representing the subdirectory of engineType',
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `linkis_ps_configuration_config_value`;
CREATE TABLE `linkis_ps_configuration_config_value`
(
    `id`              bigint(20) NOT NULL AUTO_INCREMENT,
    `config_key_id`   bigint(20),
    `config_value`    varchar(200),
    `config_label_id` int(20),
    `update_time`     datetime NOT NULL,
    `create_time`     datetime NOT NULL,
    PRIMARY KEY (`id`)
);



DROP TABLE IF EXISTS `linkis_ps_configuration_key_engine_relation`;
CREATE TABLE `linkis_ps_configuration_key_engine_relation`
(
    `id`                   bigint(20) NOT NULL AUTO_INCREMENT,
    `config_key_id`        bigint(20) NOT NULL COMMENT 'config key id',
    `engine_type_label_id` bigint(20) NOT NULL COMMENT 'engine label id',
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `linkis_cg_engine_conn_plugin_bml_resources`;
CREATE TABLE `linkis_cg_engine_conn_plugin_bml_resources`
(
    `id`                   bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `engine_conn_type`     varchar(100) NOT NULL COMMENT 'Engine type',
    `version`              varchar(100) COMMENT 'version',
    `file_name`            varchar(255) COMMENT 'file name',
    `file_size`            bigint(20) DEFAULT 0 NOT NULL COMMENT 'file size',
    `last_modified`        bigint(20) COMMENT 'File update time',
    `bml_resource_id`      varchar(100) NOT NULL COMMENT 'Owning system',
    `bml_resource_version` varchar(200) NOT NULL COMMENT 'Resource owner',
    `create_time`          datetime     NOT NULL COMMENT 'created time',
    `last_update_time`     datetime     NOT NULL COMMENT 'updated time',
    PRIMARY KEY (`id`)
);

DELETE FROM linkis_ps_datasource_access;
INSERT INTO `linkis_ps_datasource_access` (`id`, `table_id`, `visitor`, `fields`, `application_id`, `access_time`) VALUES (1, 1, 'test', 'test', 1, '2022-12-20 22:54:36');


DELETE FROM linkis_ps_dm_datasource_env;
INSERT INTO `linkis_ps_dm_datasource_env` (`id`, `env_name`, `env_desc`, `datasource_type_id`, `parameter`, `create_time`, `create_user`, `modify_time`, `modify_user`) VALUES (1, '测试环境SIT', '测试环境SIT', 4, '{\"uris\":\"thrift://localhost:9083\", \"hadoopConf\":{\"hive.metastore.execute.setugi\":\"true\"}}', '2022-11-24 20:46:21', NULL, '2022-11-24 20:46:21', NULL);

DELETE FROM linkis_ps_dm_datasource_type_key;
INSERT INTO `linkis_ps_dm_datasource_type_key` (`id`, `data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (1, 1, 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, 0, '主机名(Host)', 'Host1', NULL, NULL, NULL, NULL, '2022-11-24 20:46:21', '2022-11-24 20:46:21');

DELETE FROM linkis_ps_dm_datasource_type;
INSERT INTO `linkis_ps_dm_datasource_type` (`name`, `description`, `option`, `classifier`, `icon`, `layers`) VALUES ('kafka', 'kafka', 'kafka', '消息队列', '', 2);
INSERT INTO `linkis_ps_dm_datasource_type` (`name`, `description`, `option`, `classifier`, `icon`, `layers`) VALUES ('hive', 'hive数据库', 'hive', '大数据存储', '', 3);
INSERT INTO `linkis_ps_dm_datasource_type` (`name`, `description`, `option`, `classifier`, `icon`, `layers`) VALUES ('elasticsearch','elasticsearch数据源','es无结构化存储','分布式全文索引','',3);

DELETE FROM linkis_ps_error_code;
INSERT INTO linkis_ps_error_code (error_code,error_desc,error_regex,error_type) VALUES ('01001','您的任务没有路由到后台ECM，请联系管理员','The em of labels',0);

DELETE FROM linkis_mg_gateway_auth_token;
INSERT INTO `linkis_mg_gateway_auth_token`(`token_name`,`legal_users`,`legal_hosts`,`business_owner`,`create_time`,`update_time`,`elapse_day`,`update_by`) VALUES ('QML-AUTH','*','*','BDP',curdate(),curdate(),-1,'LINKIS');

DELETE FROM linkis_cg_rm_external_resource_provider;
insert  into `linkis_cg_rm_external_resource_provider`(`id`,`resource_type`,`name`,`labels`,`config`) values
    (1,'Yarn','default',NULL,'{"rmWebAddress":"@YARN_RESTFUL_URL","hadoopVersion":"@HADOOP_VERSION","authorEnable":@YARN_AUTH_ENABLE,"user":"@YARN_AUTH_USER","pwd":"@YARN_AUTH_PWD","kerberosEnable":@YARN_KERBEROS_ENABLE,"principalName":"@YARN_PRINCIPAL_NAME","keytabPath":"@YARN_KEYTAB_PATH","krb5Path":"@YARN_KRB5_PATH"}');


DELETE FROM linkis_ps_udf_manager;
insert into linkis_ps_udf_manager(`user_name`) values('udf_admin');

INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (1, 'combined_userCreator_engineType', '*-全局设置,*-*', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (2, 'combined_userCreator_engineType', '*-IDE,*-*', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (3, 'combined_userCreator_engineType', '*-Visualis,*-*', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (4, 'combined_userCreator_engineType', '*-nodeexecution,*-*', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (5, 'combined_userCreator_engineType', '*-*,*-*', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (6, 'combined_userCreator_engineType', '*-*,spark-3.2.1', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (7, 'combined_userCreator_engineType', '*-*,hive-3.1.3', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (8, 'combined_userCreator_engineType', '*-*,python-python2', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (9, 'combined_userCreator_engineType', '*-*,pipeline-1', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (10, 'combined_userCreator_engineType', '*-*,jdbc-4', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (11, 'combined_userCreator_engineType', '*-*,openlookeng-1.5.0', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (12, 'combined_userCreator_engineType', '*-IDE,spark-3.2.1', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (13, 'combined_userCreator_engineType', '*-IDE,hive-3.1.3', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (14, 'combined_userCreator_engineType', '*-IDE,python-python2', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (15, 'combined_userCreator_engineType', '*-IDE,pipeline-1', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (16, 'combined_userCreator_engineType', '*-IDE,jdbc-4', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (17, 'combined_userCreator_engineType', '*-IDE,openlookeng-1.5.0', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (18, 'combined_userCreator_engineType', '*-Visualis,spark-3.2.1', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (19, 'combined_userCreator_engineType', '*-nodeexecution,spark-3.2.1', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (20, 'combined_userCreator_engineType', '*-nodeexecution,hive-3.1.3', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_cg_manager_label` (`id`, `label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES (21, 'combined_userCreator_engineType', '*-nodeexecution,python-python2', 'OPTIONAL', 2, '2022-11-24 20:46:21', '2022-11-24 20:46:21');


INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (1, 'wds.linkis.rm.yarnqueue', 'yarn队列名', 'yarn队列名', 'default', 'None', NULL, NULL, 0, 0, 1, '队列资源');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (2, 'wds.linkis.rm.yarnqueue.instance.max', '取值范围：1-128，单位：个', '队列实例最大个数', '30', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|128)$', NULL, 0, 0, 1, '队列资源');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (3, 'wds.linkis.rm.yarnqueue.cores.max', '取值范围：1-500，单位：个', '队列CPU使用上限', '150', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|500)$', NULL, 0, 0, 1, '队列资源');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (4, 'wds.linkis.rm.yarnqueue.memory.max', '取值范围：1-1000，单位：G', '队列内存使用上限', '300G', 'Regex', '^([1-9]\\d{0,2}|1000)(G|g)$', NULL, 0, 0, 1, '队列资源');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (5, 'wds.linkis.rm.client.memory.max', '取值范围：1-100，单位：G', '全局各个引擎内存使用上限', '20G', 'Regex', '^([1-9]\\d{0,1}|100)(G|g)$', NULL, 0, 0, 1, '队列资源');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (6, 'wds.linkis.rm.client.core.max', '取值范围：1-128，单位：个', '全局各个引擎核心个数上限', '10', 'Regex', '^(?:[1-9]\\d?|[1][0-2][0-8])$', NULL, 0, 0, 1, '队列资源');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (7, 'wds.linkis.rm.instance', '范围：1-20，单位：个', '全局各个引擎最大并发数', '10', 'NumInterval', '[1,20]', NULL, 0, 0, 1, '队列资源');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (8, 'wds.linkis.rm.instance', '范围：1-20，单位：个', 'spark引擎最大并发数', '10', 'NumInterval', '[1,20]', 'spark', 0, 0, 1, '队列资源');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (9, 'spark.executor.instances', '取值范围：1-40，单位：个', 'spark执行器实例最大并发数', '1', 'NumInterval', '[1,40]', 'spark', 0, 0, 2, 'spark资源设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (10, 'spark.executor.cores', '取值范围：1-8，单位：个', 'spark执行器核心个数', '1', 'NumInterval', '[1,8]', 'spark', 0, 0, 1, 'spark资源设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (11, 'spark.executor.memory', '取值范围：1-15，单位：G', 'spark执行器内存大小', '1g', 'Regex', '^([1-9]|1[0-5])(G|g)$', 'spark', 0, 0, 3, 'spark资源设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (12, 'spark.driver.cores', '取值范围：只能取1，单位：个', 'spark驱动器核心个数', '1', 'NumInterval', '[1,1]', 'spark', 0, 1, 1, 'spark资源设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (13, 'spark.driver.memory', '取值范围：1-15，单位：G', 'spark驱动器内存大小', '1g', 'Regex', '^([1-9]|1[0-5])(G|g)$', 'spark', 0, 0, 1, 'spark资源设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (14, 'wds.linkis.engineconn.max.free.time', '取值范围：3m,15m,30m,1h,2h', '引擎空闲退出时间', '1h', 'OFT', '[\"1h\",\"2h\",\"30m\",\"15m\",\"3m\"]', 'spark', 0, 0, 1, 'spark引擎设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (15, 'spark.tispark.pd.addresses', NULL, NULL, 'pd0:2379', 'None', NULL, 'spark', 0, 0, 1, 'tidb设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (16, 'spark.tispark.tidb.addr', NULL, NULL, 'tidb', 'None', NULL, 'spark', 0, 0, 1, 'tidb设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (17, 'spark.tispark.tidb.password', NULL, NULL, NULL, 'None', NULL, 'spark', 0, 0, 1, 'tidb设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (18, 'spark.tispark.tidb.port', NULL, NULL, '4000', 'None', NULL, 'spark', 0, 0, 1, 'tidb设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (19, 'spark.tispark.tidb.user', NULL, NULL, 'root', 'None', NULL, 'spark', 0, 0, 1, 'tidb设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (20, 'spark.python.version', '取值范围：python2,python3', 'python版本', 'python2', 'OFT', '[\"python3\",\"python2\"]', 'spark', 0, 0, 1, 'spark引擎设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (21, 'wds.linkis.rm.instance', '范围：1-20，单位：个', 'hive引擎最大并发数', '10', 'NumInterval', '[1,20]', 'hive', 0, 0, 1, '队列资源');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (22, 'wds.linkis.engineconn.java.driver.memory', '取值范围：1-10，单位：G', 'hive引擎初始化内存大小', '1g', 'Regex', '^([1-9]|10)(G|g)$', 'hive', 0, 0, 1, 'hive引擎设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (23, 'hive.client.java.opts', 'hive客户端进程参数', 'hive引擎启动时jvm参数', '', 'None', NULL, 'hive', 1, 1, 1, 'hive引擎设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (24, 'mapred.reduce.tasks', '范围：-1-10000，单位：个', 'reduce数', '-1', 'NumInterval', '[-1,10000]', 'hive', 0, 1, 1, 'hive资源设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (25, 'wds.linkis.engineconn.max.free.time', '取值范围：3m,15m,30m,1h,2h', '引擎空闲退出时间', '1h', 'OFT', '[\"1h\",\"2h\",\"30m\",\"15m\",\"3m\"]', 'hive', 0, 0, 1, 'hive引擎设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (26, 'wds.linkis.rm.client.memory.max', '取值范围：1-100，单位：G', 'python驱动器内存使用上限', '20G', 'Regex', '^([1-9]\\d{0,1}|100)(G|g)$', 'python', 0, 0, 1, '队列资源');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (27, 'wds.linkis.rm.client.core.max', '取值范围：1-128，单位：个', 'python驱动器核心个数上限', '10', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|128)$', 'python', 0, 0, 1, '队列资源');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (28, 'wds.linkis.rm.instance', '范围：1-20，单位：个', 'python引擎最大并发数', '10', 'NumInterval', '[1,20]', 'python', 0, 0, 1, '队列资源');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (29, 'wds.linkis.engineconn.java.driver.memory', '取值范围：1-2，单位：G', 'python引擎初始化内存大小', '1g', 'Regex', '^([1-2])(G|g)$', 'python', 0, 0, 1, 'python引擎设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (30, 'python.version', '取值范围：python2,python3', 'python版本', 'python2', 'OFT', '[\"python3\",\"python2\"]', 'python', 0, 0, 1, 'python引擎设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (31, 'wds.linkis.engineconn.max.free.time', '取值范围：3m,15m,30m,1h,2h', '引擎空闲退出时间', '1h', 'OFT', '[\"1h\",\"2h\",\"30m\",\"15m\",\"3m\"]', 'python', 0, 0, 1, 'python引擎设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (32, 'pipeline.output.mold', '取值范围：csv或excel', '结果集导出类型', 'csv', 'OFT', '[\"csv\",\"excel\"]', 'pipeline', 0, 0, 1, 'pipeline引擎设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (33, 'pipeline.field.split', '取值范围：，或\\t', 'csv分隔符', ',', 'OFT', '[\",\",\"\\\\t\"]', 'pipeline', 0, 0, 1, 'pipeline引擎设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (34, 'pipeline.output.charset', '取值范围：utf-8或gbk', '结果集导出字符集', 'gbk', 'OFT', '[\"utf-8\",\"gbk\"]', 'pipeline', 0, 0, 1, 'pipeline引擎设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (35, 'pipeline.output.isoverwrite', '取值范围：true或false', '是否覆写', 'true', 'OFT', '[\"true\",\"false\"]', 'pipeline', 0, 0, 1, 'pipeline引擎设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (36, 'wds.linkis.rm.instance', '范围：1-3，单位：个', 'pipeline引擎最大并发数', '3', 'NumInterval', '[1,3]', 'pipeline', 0, 0, 1, 'pipeline引擎设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (37, 'wds.linkis.engineconn.java.driver.memory', '取值范围：1-10，单位：G', 'pipeline引擎初始化内存大小', '2g', 'Regex', '^([1-9]|10)(G|g)$', 'pipeline', 0, 0, 1, 'pipeline资源设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (38, 'pipeline.output.shuffle.null.type', '取值范围：NULL或者BLANK', '空值替换', 'NULL', 'OFT', '[\"NULL\",\"BLANK\"]', 'pipeline', 0, 0, 1, 'pipeline引擎设置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (39, 'wds.linkis.jdbc.connect.url', '例如:jdbc:mysql://127.0.0.1:10000', 'jdbc连接地址', 'jdbc:mysql://127.0.0.1:10000', 'Regex', '^\\s*jdbc:\\w+://([^:]+)(:\\d+)(/[^\\?]+)?(\\?\\S*)?$', 'jdbc', 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (40, 'wds.linkis.jdbc.driver', '例如:com.mysql.jdbc.Driver', 'jdbc连接驱动', '', 'None', '', 'jdbc', 0, 0, 1, '用户配置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (41, 'wds.linkis.jdbc.version', '取值范围：jdbc3,jdbc4', 'jdbc版本', 'jdbc4', 'OFT', '[\"jdbc3\",\"jdbc4\"]', 'jdbc', 0, 0, 1, '用户配置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (42, 'wds.linkis.jdbc.username', 'username', '数据库连接用户名', '', 'None', '', 'jdbc', 0, 0, 1, '用户配置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (43, 'wds.linkis.jdbc.password', 'password', '数据库连接密码', '', 'None', '', 'jdbc', 0, 0, 1, '用户配置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (44, 'wds.linkis.jdbc.connect.max', '范围：1-20，单位：个', 'jdbc引擎最大连接数', '10', 'NumInterval', '[1,20]', 'jdbc', 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (45, 'wds.linkis.rm.instance', '范围：1-20，单位：个', 'io_file引擎最大并发数', '10', 'NumInterval', '[1,20]', 'io_file', 0, 0, 1, 'io_file引擎资源上限');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (46, 'wds.linkis.rm.client.memory.max', '取值范围：1-50，单位：G', 'io_file引擎最大内存', '20G', 'Regex', '^([1-9]\\d{0,1}|100)(G|g)$', 'io_file', 0, 0, 1, 'io_file引擎资源上限');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (47, 'wds.linkis.rm.client.core.max', '取值范围：1-100，单位：个', 'io_file引擎最大核心数', '40', 'Regex', '^(?:[1-9]\\d?|[1234]\\d{2}|128)$', 'io_file', 0, 0, 1, 'io_file引擎资源上限');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (48, 'linkis.openlookeng.url', '例如:http://127.0.0.1:8080', '连接地址', 'http://127.0.0.1:8080', 'Regex', '^\\s*http://([^:]+)(:\\d+)(/[^\\?]+)?(\\?\\S*)?$', 'openlookeng', 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (49, 'linkis.openlookeng.catalog', 'catalog', 'catalog', 'system', 'None', '', 'openlookeng', 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`id`, `key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES (50, 'linkis.openlookeng.source', 'source', 'source', 'global', 'None', '', 'openlookeng', 0, 0, 1, '数据源配置');

INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (1, 1, '', 5, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (2, 2, '', 5, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (3, 3, '', 5, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (4, 4, '', 5, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (5, 5, '', 5, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (6, 6, '', 5, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (7, 7, '', 5, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (8, 8, '', 6, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (9, 9, '', 6, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (10, 10, '', 6, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (11, 11, '', 6, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (12, 12, '', 6, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (13, 13, '', 6, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (14, 14, '', 6, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (15, 15, '', 6, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (16, 16, '', 6, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (17, 17, '', 6, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (18, 18, '', 6, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (19, 19, '', 6, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (20, 20, '', 6, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (23, 21, '', 7, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (24, 22, '', 7, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (25, 23, '', 7, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (26, 24, '', 7, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (27, 25, '', 7, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (30, 26, '', 8, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (31, 27, '', 8, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (32, 28, '', 8, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (33, 29, '', 8, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (34, 30, '', 8, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (35, 31, '', 8, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (37, 32, '', 9, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (38, 33, '', 9, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (39, 34, '', 9, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (40, 35, '', 9, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (41, 36, '', 9, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (42, 37, '', 9, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (43, 38, '', 9, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (44, 39, '', 10, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (45, 40, '', 10, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (46, 41, '', 10, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (47, 42, '', 10, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (48, 43, '', 10, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (49, 44, '', 10, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (51, 48, '', 11, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (52, 49, '', 11, '2022-11-24 20:46:21', '2022-11-24 20:46:21');
INSERT INTO `linkis_ps_configuration_config_value` (`id`, `config_key_id`, `config_value`, `config_label_id`, `update_time`, `create_time`) VALUES (53, 50, '', 11, '2022-11-24 20:46:21', '2022-11-24 20:46:21');


INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (1, 1, 5);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (2, 2, 5);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (3, 3, 5);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (4, 4, 5);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (5, 5, 5);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (6, 6, 5);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (7, 7, 5);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (8, 8, 6);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (9, 9, 6);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (10, 10, 6);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (11, 11, 6);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (12, 12, 6);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (13, 13, 6);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (14, 14, 6);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (15, 15, 6);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (16, 16, 6);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (17, 17, 6);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (18, 18, 6);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (19, 19, 6);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (20, 20, 6);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (23, 21, 7);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (24, 22, 7);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (25, 23, 7);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (26, 24, 7);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (27, 25, 7);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (30, 26, 8);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (31, 27, 8);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (32, 28, 8);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (33, 29, 8);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (34, 30, 8);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (35, 31, 8);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (37, 32, 9);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (38, 33, 9);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (39, 34, 9);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (40, 35, 9);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (41, 36, 9);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (42, 37, 9);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (43, 38, 9);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (44, 39, 10);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (45, 40, 10);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (46, 41, 10);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (47, 42, 10);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (48, 43, 10);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (49, 44, 10);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (51, 48, 11);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (52, 49, 11);
INSERT INTO `linkis_ps_configuration_key_engine_relation` (`id`, `config_key_id`, `engine_type_label_id`) VALUES (53, 50, 11);
