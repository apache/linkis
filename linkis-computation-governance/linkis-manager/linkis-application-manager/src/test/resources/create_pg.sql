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


DROP TABLE IF EXISTS "linkis_cg_engine_conn_plugin_bml_resources";
CREATE TABLE linkis_cg_engine_conn_plugin_bml_resources (
	id serial NOT NULL,
	engine_conn_type varchar(100) NOT NULL,
	"version" varchar(100) NULL,
	file_name varchar(255) NULL,
	file_size int8 NOT NULL DEFAULT 0,
	last_modified int8 NULL,
	bml_resource_id varchar(100) NOT NULL,
	bml_resource_version varchar(200) NOT NULL,
	create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	last_update_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT linkis_engine_conn_plugin_bml_resources_pkey PRIMARY KEY (id)
);

DROP TABLE IF EXISTS "linkis_cg_rm_external_resource_provider";
CREATE TABLE linkis_cg_rm_external_resource_provider (
	id serial NOT NULL,
	resource_type varchar(32) NOT NULL,
	"name" varchar(32) NOT NULL,
	labels varchar(32) NULL,
	config text NOT NULL,
	CONSTRAINT linkis_external_resource_provider_pkey PRIMARY KEY (id)
);


delete from linkis_cg_rm_external_resource_provider;
insert  into "linkis_cg_rm_external_resource_provider"("resource_type","name","labels","config") values
('Yarn','default',NULL,'{"rmWebAddress":"@YARN_RESTFUL_URL","hadoopVersion":"@HADOOP_VERSION","authorEnable":@YARN_AUTH_ENABLE,"user":"@YARN_AUTH_USER","pwd":"@YARN_AUTH_PWD","kerberosEnable":@YARN_KERBEROS_ENABLE,"principalName":"@YARN_PRINCIPAL_NAME","keytabPath":"@YARN_KEYTAB_PATH","krb5Path":"@YARN_KRB5_PATH"}');

