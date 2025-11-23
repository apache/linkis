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

SET FOREIGN_KEY_CHECKS=0;
SET REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS linkis_mg_gateway_auth_token CASCADE;
CREATE TABLE IF NOT EXISTS linkis_mg_gateway_auth_token (
  id int(11) NOT NULL AUTO_INCREMENT,
  token_name varchar(128) NOT NULL,
  legal_users varchar(512) NOT NULL,
  legal_hosts varchar(512) NOT NULL,
  business_owner varchar(32),
  create_time datetime DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  elapse_day  bigint(20) DEFAULT  NULL,
  update_by   varchar(32),
  PRIMARY KEY (id),
  UNIQUE KEY token_name (token_name)
);

DELETE FROM linkis_mg_gateway_auth_token;
-- ----------------------------
-- Default Tokens
-- ----------------------------
INSERT INTO `linkis_mg_gateway_auth_token`(`token_name`,`legal_users`,`legal_hosts`,`business_owner`,`create_time`,`update_time`,`elapse_day`,`update_by`) VALUES ('LINKIS-UNAVAILABLE-TOKEN','test','127.0.0.1','BDP',curdate(),curdate(),-1,'LINKIS');
