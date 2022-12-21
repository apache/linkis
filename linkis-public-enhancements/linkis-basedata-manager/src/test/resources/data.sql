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

