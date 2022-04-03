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

INSERT INTO `linkis_ps_dm_datasource_type` (`name`, `description`, `option`, `classifier`, `icon`, `layers`) VALUES ('mysql', 'mysql数据库', 'mysql数据库', '关系型数据库', '', 3);
INSERT INTO `linkis_ps_dm_datasource_type` (`name`, `description`, `option`, `classifier`, `icon`, `layers`) VALUES ('kafka', 'kafka', 'kafka', '消息队列', '', 2);
INSERT INTO `linkis_ps_dm_datasource_type` (`name`, `description`, `option`, `classifier`, `icon`, `layers`) VALUES ('presto', 'presto SQL', 'presto', '大数据存储', '', 3);
INSERT INTO `linkis_ps_dm_datasource_type` (`name`, `description`, `option`, `classifier`, `icon`, `layers`) VALUES ('hive', 'hive数据库', 'hive', '大数据存储', '', 3);
INSERT INTO `linkis_ps_dm_datasource_type` (`name`, `description`, `option`, `classifier`, `icon`, `layers`) VALUES ('mongodb', 'default', 'default', 'DEFAULT', NULL, 3);


INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `default_value`, `value_type`, `scope`, `require`, `description`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (1, 'host', 'Host', NULL, 'TEXT', NULL, 1, 'mysql Host ', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `default_value`, `value_type`, `scope`, `require`, `description`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (1, 'port', '端口', NULL, 'TEXT', NULL, 1, '端口', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `default_value`, `value_type`, `scope`, `require`, `description`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (1, 'params', '连接参数', NULL, 'TEXT', NULL, 0, '输入JSON格式: {"param":"value"}', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `default_value`, `value_type`, `scope`, `require`, `description`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (1, 'username', '用户名', NULL, 'TEXT', NULL, 1, '用户名', '^[0-9A-Za-z_-]+$', NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `default_value`, `value_type`, `scope`, `require`, `description`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (1, 'password', '密码', NULL, 'PASSWORD', NULL, 1, '密码', '', NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `default_value`, `value_type`, `scope`, `require`, `description`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (4, 'envId', '集群环境', NULL, 'SELECT', NULL, 1, '集群环境', NULL, NULL, NULL, '/data-source-manager/env-list/all/type/4', now(), now());


INSERT INTO `linkis_ps_dm_datasource_env` (`env_name`, `env_desc`, `datasource_type_id`, `parameter`, `create_time`, `create_user`, `modify_time`, `modify_user`) VALUES ('测试环境SIT', '测试环境SIT', 4, '{"uris":"thrift://localhost:9083", "hadoopConf":{"hive.metastore.execute.setugi":"true"}}',  now(), NULL,  now(), NULL);
INSERT INTO `linkis_ps_dm_datasource_env` (`env_name`, `env_desc`, `datasource_type_id`, `parameter`, `create_time`, `create_user`, `modify_time`, `modify_user`) VALUES ('测试环境UAT', '测试环境UAT', 4, '{"uris":"thrift://localhost:9083", "hadoopConf":{"hive.metastore.execute.setugi":"true"}}',  now(), NULL,  now(), NULL);

