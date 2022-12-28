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
insert into linkis_ps_udf_manager(`user_name`) values('udf_admin')