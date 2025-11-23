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

DROP TABLE IF EXISTS "linkis_mg_gateway_auth_token";
CREATE TABLE linkis_mg_gateway_auth_token (
	id serial NOT NULL,
	"token_name" varchar(128) NOT NULL,
	legal_users text NULL,
	legal_hosts text NULL,
	"business_owner" varchar(32) NULL,
	create_time timestamp(6) NULL,
	update_time timestamp(6) NULL,
	elapse_day int8 NULL,
	update_by varchar(32) NULL,
	CONSTRAINT linkis_mg_gateway_auth_token_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uniq_token_name ON linkis_mg_gateway_auth_token (token_name);

delete from linkis_mg_gateway_auth_token;
-- ----------------------------
-- Default Tokens
-- ----------------------------
INSERT INTO "linkis_mg_gateway_auth_token"("token_name","legal_users","legal_hosts","business_owner","create_time","update_time","elapse_day","update_by") VALUES ('LINKIS-UNAVAILABLE-TOKEN','test','127.0.0.1','BDP',now(),now(),-1,'LINKIS');
