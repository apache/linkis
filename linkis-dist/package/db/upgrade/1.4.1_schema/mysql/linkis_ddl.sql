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



ALTER TABLE linkis_ps_udf_user_load ADD CONSTRAINT  uniq_uid_uname UNIQUE (`udf_id`, `user_name`);
ALTER TABLE linkis_ps_bml_resources ADD CONSTRAINT  uniq_rid_eflag UNIQUE (`resource_id`, `enable_flag`);


ALTER TABLE linkis_ps_configuration_config_key ADD UNIQUE uniq_key_ectype (`key`,`engine_conn_type`);

ALTER TABLE linkis_ps_configuration_config_key modify column engine_conn_type varchar(50) DEFAULT '' COMMENT 'engine type,such as spark,hive etc';

ALTER TABLE linkis_ps_common_lock ADD COLUMN locker VARCHAR(255) NOT NULL COMMENT 'locker';

ALTER TABLE linkis_ps_configuration_config_key ADD column template_required tinyint(1) DEFAULT 0 COMMENT 'template required 0 none / 1 must'

ALTER TABLE linkis_ps_configuration_config_value modify COLUMN  config_value varchar(500);


-- ----------------------------
-- Table structure for linkis_cg_tenant_department_config
-- ----------------------------
DROP TABLE IF EXISTS `linkis_cg_tenant_department_config`;
CREATE TABLE `linkis_cg_tenant_department_config` (
  `id` int(20) NOT NULL AUTO_INCREMENT  COMMENT 'ID',
  `creator` varchar(50) COLLATE utf8_bin NOT NULL  COMMENT '应用',
  `department` varchar(50) COLLATE utf8_bin NOT NULL  COMMENT '部门名称',
  `department_id` varchar(128) COLLATE utf8_bin NOT NULL COMMENT '部门ID',
  `tenant_value` varchar(128) COLLATE utf8_bin NOT NULL  COMMENT '部门租户标签',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '更新时间',
  `bussiness_user` varchar(50) COLLATE utf8_bin NOT NULL  COMMENT '创建用户',
  `is_valid` varchar(50) COLLATE utf8_bin NOT NULL DEFAULT 'Y' COMMENT '是否有效',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_user_creator` (`creator`,`department`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

ALTER TABLE linkis_cg_tenant_label_config ADD column is_valid varchar(50) DEFAULT 'Y' COMMENT '是否有效'