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

-- ----------------------------
-- add corresponding table fields for linkis_ps_dm_datasource_type_key
-- ----------------------------

ALTER TABLE `linkis_ps_dm_datasource_type_key` ADD COLUMN `name_en` varchar(32) COLLATE utf8_bin  NOT NULL;
ALTER TABLE `linkis_ps_dm_datasource_type_key` ADD COLUMN `description_en` varchar(200) COLLATE utf8_bin NULL DEFAULT NULL;
ALTER TABLE `linkis_ps_instance_label_relation` ADD  UNIQUE KEY `label_instance` (`label_id`,`service_instance`);

