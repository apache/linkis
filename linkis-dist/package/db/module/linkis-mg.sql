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

-- ----------------------------
-- Table structure for linkis_mg_gateway_auth_token
-- ----------------------------
DROP TABLE IF EXISTS `linkis_mg_gateway_auth_token`;
CREATE TABLE `linkis_mg_gateway_auth_token` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `token_name` varchar(128) NOT NULL,
  `legal_users` text,
  `legal_hosts` text,
  `business_owner` varchar(32),
  `create_time` DATE DEFAULT NULL,
  `update_time` DATE DEFAULT NULL,
  `elapse_day` BIGINT DEFAULT NULL,
  `update_by` varchar(32),
  PRIMARY KEY (`id`),
  UNIQUE KEY `token_name` (`token_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;