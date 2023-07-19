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

INSERT INTO `linkis_ps_dm_datasource_type` (`name`, `description`, `option`, `classifier`, `icon`, `layers`, `description_en`, `option_en`, `classifier_en`) VALUES ('tidb', 'tidb数据库', 'tidb', '关系型数据库', '', 3, 'TiDB Database', 'TiDB', 'Relational Database');

select @data_source_type_id := id from `linkis_ps_dm_datasource_type` where `name` = 'tidb';
INSERT INTO `linkis_ps_dm_datasource_type_key`
(`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`)
VALUES (@data_source_type_id, 'address', '地址', 'Address', NULL, 'TEXT', NULL, 0, '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, 1, '主机名(Host)', 'Host', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'port', '端口号(Port)', 'Port', NULL, 'TEXT', NULL, 1, '端口号(Port)', 'Port', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'com.mysql.jdbc.Driver', 'TEXT', NULL, 1, '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'params', '连接参数(Connection params)', 'Connection params', NULL, 'TEXT', NULL, 0, '输入JSON格式(Input JSON format): {"param":"value"}', 'Input JSON format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'username', '用户名(Username)', 'Username', NULL, 'TEXT', NULL, 1, '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, 0, '密码(Password)', 'Password', '', NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'instance', '实例名(instance)', 'Instance', NULL, 'TEXT', NULL, 1, '实例名(instance)', 'Instance', NULL, NULL, NULL, NULL,  now(), now());

INSERT INTO `linkis_ps_dm_datasource_type` (`name`, `description`, `option`, `classifier`, `icon`, `layers`, `description_en`, `option_en`, `classifier_en`) VALUES ('starrocks', 'starrocks数据库', 'starrocks', 'olap', '', 4, 'StarRocks Database', 'StarRocks', 'Olap');

select @data_source_type_id := id from `linkis_ps_dm_datasource_type` where `name` = 'starrocks';
INSERT INTO `linkis_ps_dm_datasource_type_key`
(`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`)
VALUES (@data_source_type_id, 'address', '地址', 'Address', NULL, 'TEXT', NULL, 0, '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, 1, '主机名(Host)', 'Host', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'port', '端口号(Port)', 'Port', NULL, 'TEXT', NULL, 1, '端口号(Port)', 'Port', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'com.mysql.jdbc.Driver', 'TEXT', NULL, 1, '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'params', '连接参数(Connection params)', 'Connection params', NULL, 'TEXT', NULL, 0, '输入JSON格式(Input JSON format): {"param":"value"}', 'Input JSON format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'username', '用户名(Username)', 'Username', NULL, 'TEXT', NULL, 1, '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, 0, '密码(Password)', 'Password', '', NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'instance', '实例名(instance)', 'Instance', NULL, 'TEXT', NULL, 1, '实例名(instance)', 'Instance', NULL, NULL, NULL, NULL,  now(), now());

INSERT INTO `linkis_ps_dm_datasource_type` (`name`, `description`, `option`, `classifier`, `icon`, `layers`, `description_en`, `option_en`, `classifier_en`) VALUES ('gaussdb', 'gaussdb数据库', 'gaussdb', '关系型数据库', '', 3, 'GaussDB Database', 'GaussDB', 'Relational Database');

select @data_source_type_id := id from `linkis_ps_dm_datasource_type` where `name` = 'gaussdb';
INSERT INTO `linkis_ps_dm_datasource_type_key`
(`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`)
VALUES (@data_source_type_id, 'address', '地址', 'Address', NULL, 'TEXT', NULL, 0, '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, 1, '主机名(Host)', 'Host', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'port', '端口号(Port)', 'Port', NULL, 'TEXT', NULL, 1, '端口号(Port)', 'Port', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'org.postgresql.Driver', 'TEXT', NULL, 1, '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'params', '连接参数(Connection params)', 'Connection params', NULL, 'TEXT', NULL, 0, '输入JSON格式(Input JSON format): {"param":"value"}', 'Input JSON format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'username', '用户名(Username)', 'Username', NULL, 'TEXT', NULL, 1, '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, 1, '密码(Password)', 'Password', '', NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'instance', '实例名(instance)', 'Instance', NULL, 'TEXT', NULL, 1, '实例名(instance)', 'Instance', NULL, NULL, NULL, NULL,  now(), now());

INSERT INTO `linkis_ps_dm_datasource_type` (`name`, `description`, `option`, `classifier`, `icon`, `layers`, `description_en`, `option_en`, `classifier_en`) VALUES ('oceanbase', 'oceanbase数据库', 'oceanbase', 'olap', '', 4, 'oceanbase Database', 'oceanbase', 'Olap');

select @data_source_type_id := id from `linkis_ps_dm_datasource_type` where `name` = 'oceanbase';
INSERT INTO `linkis_ps_dm_datasource_type_key`
(`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`)
VALUES (@data_source_type_id, 'address', '地址', 'Address', NULL, 'TEXT', NULL, 0, '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'host', '主机名(Host)', 'Host', NULL, 'TEXT', NULL, 1, '主机名(Host)', 'Host', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'port', '端口号(Port)', 'Port', NULL, 'TEXT', NULL, 1, '端口号(Port)', 'Port', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'com.mysql.jdbc.Driver', 'TEXT', NULL, 1, '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'params', '连接参数(Connection params)', 'Connection params', NULL, 'TEXT', NULL, 0, '输入JSON格式(Input JSON format): {"param":"value"}', 'Input JSON format: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'username', '用户名(Username)', 'Username', NULL, 'TEXT', NULL, 1, '用户名(Username)', 'Username', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'password', '密码(Password)', 'Password', NULL, 'PASSWORD', NULL, 1, '密码(Password)', 'Password', '', NULL, NULL, NULL,  now(), now()),
       (@data_source_type_id, 'instance', '实例名(instance)', 'Instance', NULL, 'TEXT', NULL, 1, '实例名(instance)', 'Instance', NULL, NULL, NULL, NULL,  now(), now());

-- Change the default python version to python3
UPDATE linkis_ps_configuration_config_key SET default_value = 'python3' WHERE `key` = 'spark.python.version';