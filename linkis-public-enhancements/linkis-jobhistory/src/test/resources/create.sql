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

DROP TABLE IF EXISTS linkis_ps_job_history_detail CASCADE;
CREATE TABLE IF NOT EXISTS linkis_ps_job_history_detail (
  id numeric(20) NOT NULL AUTO_INCREMENT,
  job_history_id numeric(20) NOT NULL,
  result_location varchar(500),
  execution_content text,
  result_array_size integer,
  job_group_info text,
  created_time datetime(3),
  updated_time datetime(3),
  status varchar(32),
  priority integer,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS linkis_ps_job_history_group_history CASCADE;
CREATE TABLE linkis_ps_job_history_group_history (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key, auto increment',
  job_req_id varchar(64) DEFAULT NULL COMMENT 'job execId',
  submit_user varchar(50) DEFAULT NULL COMMENT 'who submitted this Job',
  execute_user varchar(50) DEFAULT NULL COMMENT 'who actually executed this Job',
  source text COMMENT 'job source',
  labels text COMMENT 'job labels',
  params text COMMENT 'job params',
  progress varchar(32) DEFAULT NULL COMMENT 'Job execution progress',
  status varchar(50) DEFAULT NULL COMMENT 'Script execution status, must be one of the following: Inited, WaitForRetry, Scheduled, Running, Succeed, Failed, Cancelled, Timeout',
  log_path varchar(200) DEFAULT NULL COMMENT 'File path of the job log',
  error_code int(11) DEFAULT NULL COMMENT 'Error code. Generated when the execution of the script fails',
  error_desc varchar(1000) DEFAULT NULL COMMENT 'Execution description. Generated when the execution of script fails',
  created_time datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Creation time',
  updated_time datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Update time',
  instances varchar(250) DEFAULT NULL COMMENT 'Entrance instances',
  metrics text COMMENT 'Job Metrics',
  engine_type varchar(32) DEFAULT NULL COMMENT 'Engine type',
  execution_code text COMMENT 'Job origin code or code path',
  result_location varchar(500) DEFAULT NULL COMMENT 'File path of the resultsets',
  observe_info varchar(500) DEFAULT NULL COMMENT 'demo',
  PRIMARY KEY (id),
  KEY created_time (created_time),
  KEY submit_user (submit_user)
) ;

INSERT INTO linkis_ps_job_history_group_history (job_req_id,submit_user,execute_user,source,labels,params,progress,status,log_path,error_code,error_desc,instances,metrics,engine_type,execution_code,result_location) VALUES
	 ('LINKISCLI_hadoop_spark_0','hadoop','hadoop','{"scriptPath":"LinkisCli","requestIP":"127.0.0.1"}','{"userCreator":"hadoop-LINKISCLI","engineType":"spark-3.0.1","codeType":"sql","executeOnce":""}','{"configuration":{"startup":{},"runtime":{"hive.resultset.use.unique.column.names":true,"wds.linkis.resultSet.store.path":"hdfs:///tmp/linkis/hadoop/linkis/20220714_185840/LINKISCLI/1","source":{"scriptPath":"LinkisCli","requestIP":"127.0.0.1"},"job":{"resultsetIndex":0,"#rt_rs_store_path":"hdfs:///tmp/linkis/hadoop/linkis/20220714_185840/LINKISCLI/1"}}},"variable":{}}','1.0','Succeed','hdfs:///tmp/linkis/log/2022-07-14/LINKISCLI/hadoop/1.log',0,'','127.0.0.1:9104','{"scheduleTime":"2022-07-14T18:58:40+0800","timeToOrchestrator":"2022-07-14T18:58:41+0800","submitTime":"2022-07-14T18:58:39+0800","yarnResource":{"application_1657595967414_0003":{"queueMemory":1073741824,"queueCores":1,"queueInstances":0,"jobStatus":"RUNNING","queue":"default"}},"completeTime":"2022-07-14T18:59:51+0800"}','spark','show databases;','hdfs:///tmp/linkis/hadoop/linkis/20220714_185840/LINKISCLI/1');
