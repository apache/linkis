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

INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (1, 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, 1, '主机名(Host)', 'Host', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (1, 'port', '端口号(Port)','Port', NULL, 'TEXT', NULL, 1, '端口号(Port)','Port', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (1, 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'com.mysql.jdbc.Driver', 'TEXT', NULL, 1, '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (1, 'params', '连接参数(Connection params)', 'Connection params', NULL, 'TEXT', NULL, 0, '输入JSON格式(Input JSON format): {"param":"value"}', 'Input JSON format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (1, 'username', '用户名(Username)', 'Username', NULL, 'TEXT', NULL, 1, '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (1, 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, 1, '密码(Password)', 'Password', '', NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (1, 'databaseName', '数据库名(Database name)', 'Database name', NULL, 'TEXT', NULL, 0, '数据库名(Database name)', 'Database name', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (4, 'envId', '集群环境(Cluster env)', 'Cluster env', NULL, 'SELECT', NULL, 1, '集群环境(Cluster env)', 'Cluster env', NULL, NULL, NULL, '/data-source-manager/env-list/all/type/4', now(), now());
