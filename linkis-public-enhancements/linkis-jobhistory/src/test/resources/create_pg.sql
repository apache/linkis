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

DROP TABLE IF EXISTS "linkis_ps_job_history_detail";
CREATE TABLE linkis_ps_job_history_detail (
	id serial NOT NULL,
	job_history_id int8 NOT NULL,
	result_location varchar(500) NULL,
	execution_content text NULL,
	result_array_size int4 NULL DEFAULT 0,
	job_group_info text NULL,
	created_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	updated_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	status varchar(32) NULL,
	priority int4 NULL DEFAULT 0,
	CONSTRAINT linkis_ps_job_history_detail_pkey PRIMARY KEY (id)
);

DROP TABLE IF EXISTS "linkis_ps_job_history_group_history";
CREATE TABLE linkis_ps_job_history_group_history (
	id serial NOT NULL,
	job_req_id varchar(64) NULL,
	submit_user varchar(50) NULL,
	execute_user varchar(50) NULL,
	"source" text NULL,
	labels text NULL,
	params text NULL,
	progress varchar(32) NULL,
	status varchar(50) NULL,
	log_path varchar(200) NULL,
	error_code int4 NULL,
	error_desc varchar(1000) NULL,
	created_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	updated_time timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP,
	instances varchar(250) NULL,
	metrics text NULL,
	engine_type varchar(32) NULL,
	execution_code text NULL,
	result_location varchar(500) NULL,
	observe_info varchar(500) NULL,
	CONSTRAINT linkis_ps_job_history_group_history_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_created_time ON linkis_ps_job_history_group_history (created_time);
CREATE INDEX idx_submit_user ON linkis_ps_job_history_group_history (submit_user);


INSERT INTO linkis_ps_job_history_group_history (job_req_id,submit_user,execute_user,source,labels,params,progress,status,log_path,error_code,error_desc,created_time,updated_time,instances,metrics,engine_type,execution_code,result_location) VALUES
	 ('LINKISCLI_hadoop_spark_0','hadoop','hadoop','{"scriptPath":"LinkisCli","requestIP":"127.0.0.1"}','{"userCreator":"hadoop-LINKISCLI","engineType":"spark-3.0.1","codeType":"sql","executeOnce":""}','{"configuration":{"startup":{},"runtime":{"hive.resultset.use.unique.column.names":true,"wds.linkis.resultSet.store.path":"hdfs:///tmp/linkis/hadoop/linkis/20220714_185840/LINKISCLI/1","source":{"scriptPath":"LinkisCli","requestIP":"127.0.0.1"},"job":{"resultsetIndex":0,"#rt_rs_store_path":"hdfs:///tmp/linkis/hadoop/linkis/20220714_185840/LINKISCLI/1"}}},"variable":{}}','1.0','Succeed','hdfs:///tmp/linkis/log/2022-07-14/LINKISCLI/hadoop/1.log',0,'','2022-07-14 18:58:39.019000000','2022-07-14 18:59:51.589000000','127.0.0.1:9104','{"scheduleTime":"2022-07-14T18:58:40+0800","timeToOrchestrator":"2022-07-14T18:58:41+0800","submitTime":"2022-07-14T18:58:39+0800","yarnResource":{"application_1657595967414_0003":{"queueMemory":1073741824,"queueCores":1,"queueInstances":0,"jobStatus":"RUNNING","queue":"default"}},"completeTime":"2022-07-14T18:59:51+0800"}','spark','show databases;','hdfs:///tmp/linkis/hadoop/linkis/20220714_185840/LINKISCLI/1');
