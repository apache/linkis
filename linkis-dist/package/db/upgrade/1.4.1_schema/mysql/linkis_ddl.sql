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



ALTER TABLE `linkis_ps_udf_user_load` ADD CONSTRAINT  `uniq_uid_uname` UNIQUE (`udf_id`, `user_name`);
ALTER TABLE `linkis_ps_bml_resources` ADD CONSTRAINT  `uniq_rid_eflag` UNIQUE (`resource_id`, `enable_flag`);


ALTER TABLE `linkis_ps_configuration_config_key` ADD UNIQUE `uniq_key_ectype` (`key`,`engine_conn_type`);

ALTER TABLE `linkis_ps_configuration_config_key` modify column `engine_conn_type` varchar(50) DEFAULT '' COMMENT 'engine type,such as spark,hive etc';

ALTER TABLE `linkis_ps_configuration_config_key` ADD column `template_required` tinyint(1) DEFAULT 0 COMMENT 'template required 0 none / 1 must'

ALTER TABLE  linkis_ps_configuration_config_value modify COLUMN  config_value varchar(500);