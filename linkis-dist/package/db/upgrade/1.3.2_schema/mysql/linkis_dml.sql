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


ALTER TABLE `linkis_ps_job_history_group_history` ADD COLUMN `observe_info` VARCHAR(500) NULL DEFAULT NULL ;

update linkis_cg_manager_label set label_value ="*-GlobalSettings,*-*"  where label_key ="combined_userCreator_engineType" and id = 1;
update linkis_ps_configuration_config_key set en_treeName ="QueueResources" where treeName  = '队列资源';
update linkis_ps_configuration_config_key set en_treeName ="SparkResourceSettings" where treeName  = 'spark资源设置';
update linkis_ps_configuration_config_key set en_treeName ="Spark Engine Settings" where treeName  = 'spark引擎设置';
update linkis_ps_configuration_config_key set en_treeName ="Tidb Settings" where treeName  = 'tidb设置';
update linkis_ps_configuration_config_key set en_treeName ="Hive Engine Settings" where treeName  = 'hive引擎设置';
update linkis_ps_configuration_config_key set en_treeName ="Python Engine Settings" where treeName  = 'python引擎设置';
update linkis_ps_configuration_config_key set en_treeName ="Pipeline Engine Settings" where treeName  = 'pipeline引擎设置';
update linkis_ps_configuration_config_key set en_treeName ="Pipeline Resource Settings" where treeName  = 'pipeline资源设置';
update linkis_ps_configuration_config_key set en_treeName ="DataSource Configuration" where treeName  = '数据源配置';
update linkis_ps_configuration_config_key set en_treeName ="User Configuration" where treeName  = '用户配置';
update linkis_ps_configuration_config_key set en_treeName ="Io_File EngineResource Upper Limit" where treeName  = 'io_file引擎资源上限';
update linkis_ps_configuration_config_key set en_treeName ="Hive Resource Settings" where treeName  = 'hive资源设置';
update linkis_ps_configuration_config_key set en_treeName ="Spark Engine Resource Upper Limit" where treeName  = 'spark引擎资源上限';
update linkis_ps_configuration_config_key set en_treeName ="Worker Resource Settings" where treeName  = 'worker资源设置';
update linkis_ps_configuration_config_key set en_treeName ="Spark Engine Resource Settings" where treeName  = 'spark引擎资源设置';
update linkis_ps_configuration_config_key set en_treeName ="Hive Engine Resource Upper Limit" where treeName  = 'hive引擎资源上限';
update linkis_ps_configuration_config_key set en_treeName ="Hive Engine Resource Settings" where treeName  = 'hive引擎资源设置';
update linkis_ps_configuration_config_key set en_treeName ="MapReduce Settings" where treeName  = 'MapReduce设置';
update linkis_ps_configuration_config_key set en_treeName ="Python Engine Resource Upper Limit" where treeName  = 'python引擎资源上限';
update linkis_ps_configuration_config_key set en_treeName ="Pipeline Engine Resource Upper Limit" where treeName  = 'pipeline引擎资源上限';
update linkis_ps_configuration_config_key set en_treeName ="Python Engine Resource Settings" where treeName  = 'python引擎资源设置';

update linkis_ps_configuration_config_key set en_description ="Yarn Queue" where description ="yarn队列名";
update linkis_ps_configuration_config_key set en_description ="Value Range: 1-128, Unit: Piece" where description ="取值范围：1-128，单位：个";
update linkis_ps_configuration_config_key set en_description ="Value Range: 1-500, Unit: Piece" where description ="取值范围：1-500，单位：个";
update linkis_ps_configuration_config_key set en_description ="Value Range: 1-1000, Unit: G" where description ="取值范围：1-1000，单位：G";
update linkis_ps_configuration_config_key set en_description ="Value Range: 1-100, Unit: G" where description ="取值范围：1-100，单位：G";
update linkis_ps_configuration_config_key set en_description ="Range: 1-20, Unit: Piece" where description ="范围：1-20，单位：个";
update linkis_ps_configuration_config_key set en_description ="Value Range: 1-40, Unit: Piece" where description ="取值范围：1-40，单位：个";
update linkis_ps_configuration_config_key set en_description ="Value Range: 1-8, Unit: Piece" where description ="取值范围：1-8，单位：个";
update linkis_ps_configuration_config_key set en_description ="Value Range: 1-15, Unit: G" where description ="取值范围：1-15，单位：G";
update linkis_ps_configuration_config_key set en_description ="Value Range: only 1, Unit: Piece" where description ="取值范围：只能取1，单位：个";
update linkis_ps_configuration_config_key set en_description ="Value Range: 3m, 15m, 30m, 1h, 2h" where description ="取值范围：3m,15m,30m,1h,2h";
update linkis_ps_configuration_config_key set en_description ="Value Range: python2, python3" where description ="取值范围：python2,python3";
update linkis_ps_configuration_config_key set en_description ="Value Range: 1-10, Unit: G" where description ="取值范围：1-10，单位：G";
update linkis_ps_configuration_config_key set en_description ="Hive Client Process Parameters" where description ="hive客户端进程参数";
update linkis_ps_configuration_config_key set en_description ="Value Range: 2-10, Unit: G" where description ="取值范围：2-10，单位：G";
update linkis_ps_configuration_config_key set en_description ="Value Range: 1-2, Unit: G" where description ="取值范围：1-2，单位：G";
update linkis_ps_configuration_config_key set en_description ="Value Range: csv or excel" where description ="取值范围：csv或excel";
update linkis_ps_configuration_config_key set en_description ="Value Range:, or  t" where description ="取值范围：，或\t";
update linkis_ps_configuration_config_key set en_description ="Value Range: utf-8 or gbk" where description ="取值范围：utf-8或gbk";
update linkis_ps_configuration_config_key set en_description ="Value Range: true or false" where description ="取值范围：true或false";
update linkis_ps_configuration_config_key set en_description ="Range: 1-3, Unit: Piece" where description ="范围：1-3，单位：个";
update linkis_ps_configuration_config_key set en_description ="Value Range: NULL Or BLANK" where description ="取值范围：NULL或者BLANK";
update linkis_ps_configuration_config_key set en_description ="For Example: jdbc: hive2://127.0.0.1:10000" where description ="例如:jdbc:hive2://127.0.0.1:10000";
update linkis_ps_configuration_config_key set en_description ="For Example: org.apache.hive.jdbc.HiveDriver" where description ="例如:org.apache.hive.jdbc.HiveDriver";
update linkis_ps_configuration_config_key set en_description ="Value Range: jdbc3, jdbc4" where description ="取值范围：jdbc3,jdbc4";
update linkis_ps_configuration_config_key set en_description ="Username" where description ="username";
update linkis_ps_configuration_config_key set en_description ="Password" where description ="password";
update linkis_ps_configuration_config_key set en_description ="Value Range: 1-50, Unit: G" where description ="取值范围：1-50，单位：G";
update linkis_ps_configuration_config_key set en_description ="Value Range: 1-100, Unit: Piece" where description ="取值范围：1-100，单位：个";
update linkis_ps_configuration_config_key set en_description ="For Example: http://127.0.0.1:8080" where description ="例如:http://127.0.0.1:8080";
update linkis_ps_configuration_config_key set en_description ="Catalog" where description ="catalog";
update linkis_ps_configuration_config_key set en_description ="Source" where description ="source";
update linkis_ps_configuration_config_key set en_description ="For Example: http://127.0.0.1:9200" where description ="例如:http://127.0.0.1:9200";
update linkis_ps_configuration_config_key set en_description ="Whether The Client Caches Authentication" where description ="客户端是否缓存认证";
update linkis_ps_configuration_config_key set en_description ="Whether The Client Enables Sniffer" where description ="客户端是否开启 sniffer";
update linkis_ps_configuration_config_key set en_description ="Call Mode" where description ="调用方式";
update linkis_ps_configuration_config_key set en_description ="/_ search" where description ="/_search";
update linkis_ps_configuration_config_key set en_description ="/_ sql" where description ="/_sql";
update linkis_ps_configuration_config_key set en_description ="Template Called By SQL Script. Replace% s With SQL As The Requester To Request Es Cluster" where description ="SQL 脚本调用的模板，%s 替换成 SQL 作为请求体请求Es 集群";
update linkis_ps_configuration_config_key set en_description ="Client Headers Configuration" where description ="客户端 Headers 配置";
update linkis_ps_configuration_config_key set en_description ="Maximum Engine concurrency" where description ="引擎最大并发";
update linkis_ps_configuration_config_key set en_description ="Connect Data Source" where description ="连接数据源";
update linkis_ps_configuration_config_key set en_description ="Presto Cluster Connection" where description ="Presto 集群连接";
update linkis_ps_configuration_config_key set en_description ="Catalog Queried" where description ="查询的 Catalog";
update linkis_ps_configuration_config_key set en_description ="Query Schema" where description ="查询的 Schema";
update linkis_ps_configuration_config_key set en_description ="Source Used For Query" where description ="查询使用的 source";
update linkis_ps_configuration_config_key set en_description ="Value Range: 1-1500, Unit: Piece" where description ="取值范围：1-1500，单位：个";
update linkis_ps_configuration_config_key set en_description ="Value Range: 1-5000, Unit: Piece" where description ="取值范围：1-5000，单位：G";
update linkis_ps_configuration_config_key set en_description ="Value Range: 1-150, Unit: Piece" where description ="取值范围：1-150，单位：G";
update linkis_ps_configuration_config_key set en_description ="Value Range: 3-15, Unit: Piece" where description ="取值范围：3-15，单位：G";
update linkis_ps_configuration_config_key set en_description ="Schema" where description ="schema";
update linkis_ps_configuration_config_key set en_description ="For Example:hive、mysql、kudu" where description ="例如hive、mysql、kudu";
update linkis_ps_configuration_config_key set en_description ="Value Range: Automatic Release, One Engine From Monday To Friday Working Hours, And One Engine From Monday To Friday" where description ="取值范围：自动释放,周一到周五工作时间保持一个引擎,周一到周五保持一个引擎";

update linkis_ps_configuration_config_key set en_name ="Yarn Queue" where name ="yarn队列名";
update linkis_ps_configuration_config_key set en_name ="Maximum Number Of Queue Instances" where name ="队列实例最大个数";
update linkis_ps_configuration_config_key set en_name ="Maximum Queue CPU Usage" where name ="队列CPU使用上限";
update linkis_ps_configuration_config_key set en_name ="Maximum Queue Memory Usage" where name ="队列内存使用上限";
update linkis_ps_configuration_config_key set en_name ="Global Upper Limit Of Each Engine's Memory Usage" where name ="全局各个引擎内存使用上限";
update linkis_ps_configuration_config_key set en_name ="Maximum Number Of Global Engine Cores" where name ="全局各个引擎核心个数上限";
update linkis_ps_configuration_config_key set en_name ="Maximum Concurrent Number Of Global Engines" where name ="全局各个引擎最大并发数";
update linkis_ps_configuration_config_key set en_name ="Maximum Concurrent Number Of Spark Engine" where name ="spark引擎最大并发数";
update linkis_ps_configuration_config_key set en_name ="Maximum Concurrent Number Of spark executor Instances" where name ="spark执行器实例最大并发数";
update linkis_ps_configuration_config_key set en_name ="Number Of Spark Actuator Cores" where name ="spark执行器核心个数";
update linkis_ps_configuration_config_key set en_name ="Spark Actuator Memory Size" where name ="spark执行器内存大小";
update linkis_ps_configuration_config_key set en_name ="Number Of Spark Drive Cores" where name ="spark驱动器核心个数";
update linkis_ps_configuration_config_key set en_name ="Spark Drive Memory Size" where name ="spark驱动器内存大小";
update linkis_ps_configuration_config_key set en_name ="Engine Idle Exit Time" where name ="引擎空闲退出时间";
update linkis_ps_configuration_config_key set en_name ="Python Version" where name ="python版本";
update linkis_ps_configuration_config_key set en_name ="Maximum Concurrent Number Of Hive Engine" where name ="hive引擎最大并发数";
update linkis_ps_configuration_config_key set en_name ="The Initialization Memory Size Of The Hive Engine" where name ="hive引擎初始化内存大小";
update linkis_ps_configuration_config_key set en_name ="Jvm Parameters When The Hive Engine Starts" where name ="hive引擎启动时jvm参数";
update linkis_ps_configuration_config_key set en_name ="Reduce Number" where name ="reduce数";
update linkis_ps_configuration_config_key set en_name ="Map Data Block Size" where name ="map数据块大小";
update linkis_ps_configuration_config_key set en_name ="Reduce The Amount Of Data Processed" where name ="reduce处理的数据量";
update linkis_ps_configuration_config_key set en_name ="Maximum Memory Usage Of Python Drive" where name ="python驱动器内存使用上限";
update linkis_ps_configuration_config_key set en_name ="Maximum Number Of Python Drive Cores" where name ="python驱动器核心个数上限";
update linkis_ps_configuration_config_key set en_name ="Maximum Number Of Concurrent Python Engines" where name ="python引擎最大并发数";
update linkis_ps_configuration_config_key set en_name ="Python Engine Initialization Memory Size" where name ="python引擎初始化内存大小";
update linkis_ps_configuration_config_key set en_name ="Result Set Export Type" where name ="结果集导出类型";
update linkis_ps_configuration_config_key set en_name ="Csv Separator" where name ="csv分隔符";
update linkis_ps_configuration_config_key set en_name ="Result Set Export Character Set" where name ="结果集导出字符集";
update linkis_ps_configuration_config_key set en_name ="WheTher To Overwrite" where name ="是否覆写";
update linkis_ps_configuration_config_key set en_name ="Maximum Concurrent Number Of pipeline Engine" where name ="pipeline引擎最大并发数";
update linkis_ps_configuration_config_key set en_name ="Pipeline Engine Initialization Memory Size" where name ="pipeline引擎初始化内存大小";
update linkis_ps_configuration_config_key set en_name ="Null Value Replacement" where name ="空值替换";
update linkis_ps_configuration_config_key set en_name ="Jdbc Connection Address" where name ="jdbc连接地址";
update linkis_ps_configuration_config_key set en_name ="Jdbc Connection Driver" where name ="jdbc连接驱动";
update linkis_ps_configuration_config_key set en_name ="Jdbc Bersion" where name ="jdbc版本";
update linkis_ps_configuration_config_key set en_name ="Database Connection User Name" where name ="数据库连接用户名";
update linkis_ps_configuration_config_key set en_name ="Database Connection Password" where name ="数据库连接密码";
update linkis_ps_configuration_config_key set en_name ="Maximum connections Of Jdbc Engine" where name ="jdbc引擎最大连接数";
update linkis_ps_configuration_config_key set en_name ="Io_Maximum Concurrent Number Of file Engine" where name ="io_file引擎最大并发数";
update linkis_ps_configuration_config_key set en_name ="Io_File Engine Maximum Memory" where name ="io_file引擎最大内存";
update linkis_ps_configuration_config_key set en_name ="Io_Maximum Number Of Cores Of file Engine" where name ="io_file引擎最大核心数";
update linkis_ps_configuration_config_key set en_name ="Connection Address" where name ="连接地址";
update linkis_ps_configuration_config_key set en_name ="Catalog" where name ="catalog";
update linkis_ps_configuration_config_key set en_name ="Source" where name ="source";
update linkis_ps_configuration_config_key set en_name ="ES Cluster User Name" where name ="ES集群用户名";
update linkis_ps_configuration_config_key set en_name ="ES Cluster Password" where name ="ES集群密码";
update linkis_ps_configuration_config_key set en_name ="WheTher The Client Caches AuThentication" where name ="客户端是否缓存认证";
update linkis_ps_configuration_config_key set en_name ="WheTher The Client Enables Sniffer" where name ="客户端是否开启 sniffer";
update linkis_ps_configuration_config_key set en_name ="HTTP Request Mode" where name ="HTTP请求方式";
update linkis_ps_configuration_config_key set en_name ="Endpoint Called By JSON script" where name ="JSON 脚本调用的 Endpoint";
update linkis_ps_configuration_config_key set en_name ="Endpoint Called By SQL script" where name ="SQL 脚本调用的 Endpoint";
update linkis_ps_configuration_config_key set en_name ="Requestor" where name ="请求体";
update linkis_ps_configuration_config_key set en_name ="Client Headers Configuration" where name ="客户端 Headers 配置";
update linkis_ps_configuration_config_key set en_name ="Maximum Engine concurrency" where name ="引擎最大并发";
update linkis_ps_configuration_config_key set en_name ="Connect Data Source" where name ="连接数据源";
update linkis_ps_configuration_config_key set en_name ="Presto Connection Address" where name ="presto连接地址";
update linkis_ps_configuration_config_key set en_name ="Presto Connection Catalog" where name ="presto连接的catalog";
update linkis_ps_configuration_config_key set en_name ="Database Connection Schema" where name ="数据库连接schema";
update linkis_ps_configuration_config_key set en_name ="Database Connection Source" where name ="数据库连接source";

update linkis_ps_configuration_config_key set en_name ="Maximum Number Of yarn Queue Instances" where name ="yarn队列实例最大个数";
update linkis_ps_configuration_config_key set en_name ="Number Of Concurrent Workers" where name ="worker并发数";
update linkis_ps_configuration_config_key set en_name ="Worker Memory Size" where name ="worker内存大小";
update linkis_ps_configuration_config_key set en_name ="Number Of Spark Engine Cores" where name ="spark引擎核心个数";
update linkis_ps_configuration_config_key set en_name ="Spark Engine Memory" where name ="spark引擎内存";
update linkis_ps_configuration_config_key set en_name ="Hive Engine Memory" where name ="hive引擎内存";
update linkis_ps_configuration_config_key set en_name ="Python Engine Maximum Memory" where name ="python引擎最大内存";
update linkis_ps_configuration_config_key set en_name ="Maximum Number Of Python Engine Cores" where name ="python引擎最大核心数";
update linkis_ps_configuration_config_key set en_name ="Python Engine Memory" where name ="python引擎内存";
update linkis_ps_configuration_config_key set en_name ="Trino Connection Address" where name ="trino连接地址";
update linkis_ps_configuration_config_key set en_name ="Catalog Connected By Trino" where name ="trino连接的catalog";
update linkis_ps_configuration_config_key set en_name ="Number Of Maps" where name ="map数";

update linkis_ps_dm_datasource_type set description_en ="Number Of maps" where description ="mysql数据库";
update linkis_ps_dm_datasource_type set description_en ="Kafka" where description ="kafka";
update linkis_ps_dm_datasource_type set description_en ="Presto SQL" where description ="presto SQL";
update linkis_ps_dm_datasource_type set description_en ="Hive Database" where description ="hive数据库";
update linkis_ps_dm_datasource_type set description_en ="Default" where description ="default";
update linkis_ps_dm_datasource_type set description_en ="Tdsql Database, Supporting Password Steward" where description ="tdsql数据库,支持密码管家 ";
update linkis_ps_dm_datasource_type set description_en ="Mongodb Data Source" where description ="mongodb 数据源";
update linkis_ps_dm_datasource_type set description_en ="ES Description" where description ="ES description";
update linkis_ps_dm_datasource_type set description_en ="This Is Oracle Datasource" where description ="This is oracle datasource";
update linkis_ps_dm_datasource_type set description_en ="Oracle Database" where description ="oracle数据库";
update linkis_ps_dm_datasource_type set description_en ="Dameng Database" where description ="达梦数据库";
update linkis_ps_dm_datasource_type set description_en ="Renmin Jincang Database" where description ="人大金仓数据库";
update linkis_ps_dm_datasource_type set description_en ="Postgresql Database" where description ="postgresql数据库";
update linkis_ps_dm_datasource_type set description_en ="Sqlserver Database" where description ="sqlserver数据库";
update linkis_ps_dm_datasource_type set description_en ="Db2 Database" where description ="db2数据库";
update linkis_ps_dm_datasource_type set description_en ="Greenplum Database" where description ="greenplum数据库";
update linkis_ps_dm_datasource_type set description_en ="Doris Database" where description ="doris数据库";
update linkis_ps_dm_datasource_type set description_en ="Clickhouse Database" where description ="clickhouse数据库";
update linkis_ps_dm_datasource_type set description_en ="Elasticsearch Datasource" where description ="elasticsearch数据源";

update linkis_ps_dm_datasource_type set option_en ="Mysql Database" where option ="mysql数据库";
update linkis_ps_dm_datasource_type set option_en ="Kafka" where option ="kafka";
update linkis_ps_dm_datasource_type set option_en ="Presto" where option ="presto";
update linkis_ps_dm_datasource_type set option_en ="Hive" where option ="hive";
update linkis_ps_dm_datasource_type set option_en ="Default" where option ="default";
update linkis_ps_dm_datasource_type set option_en ="Tdsql Database" where option ="tdsql数据库";
update linkis_ps_dm_datasource_type set option_en ="Mongodb" where option ="mongodb";
update linkis_ps_dm_datasource_type set option_en ="Es Unstructured storage" where option ="es无结构存储";
update linkis_ps_dm_datasource_type set option_en ="Oracle" where option ="oracle关系型数据库";
update linkis_ps_dm_datasource_type set option_en ="Oracle Relational Database" where option ="oracle";
update linkis_ps_dm_datasource_type set option_en ="Dm" where option ="dm";
update linkis_ps_dm_datasource_type set option_en ="Kingbase" where option ="kingbase";
update linkis_ps_dm_datasource_type set option_en ="Sqlserver" where option ="sqlserver";
update linkis_ps_dm_datasource_type set option_en ="Db2" where option ="db2";
update linkis_ps_dm_datasource_type set option_en ="Greenplum" where option ="greenplum";
update linkis_ps_dm_datasource_type set option_en ="Doris" where option ="doris";
update linkis_ps_dm_datasource_type set option_en ="Clickhouse" where option ="clickhouse";
update linkis_ps_dm_datasource_type set option_en ="Es No Structured Storage" where option ="es无结构化存储";
update linkis_ps_dm_datasource_type set option_en ="Postgresql" where option ="postgresql";
update linkis_ps_dm_datasource_type set classifier_en ="Relational Database" where classifier ="关系型数据库";
update linkis_ps_dm_datasource_type set classifier_en ="Message Queue" where classifier ="消息队列";
update linkis_ps_dm_datasource_type set classifier_en ="Big Data storage" where classifier ="大数据存储";
update linkis_ps_dm_datasource_type set classifier_en ="Default" where classifier ="DEFAULT";
update linkis_ps_dm_datasource_type set classifier_en ="Semi Structured Database" where classifier ="半结构化数据库";
update linkis_ps_dm_datasource_type set classifier_en ="Distributed Full-Text Indexing" where classifier ="分布式全文索引";
update linkis_ps_dm_datasource_type set classifier_en ="Olap" where classifier ="olap";

update linkis_ps_dm_datasource_type_key set name_en="Host" where name = "主机名(Host)" ;
update linkis_ps_dm_datasource_type_key set name_en="Port" where name = "端口号(Port)" ;
update linkis_ps_dm_datasource_type_key set name_en="Driver Class Name" where name = "驱动类名(Driver class name)" ;
update linkis_ps_dm_datasource_type_key set name_en="Connection Params" where name = "连接参数(Connection params)" ;
update linkis_ps_dm_datasource_type_key set name_en="Username" where name = "用户名(Username)" ;
update linkis_ps_dm_datasource_type_key set name_en="Password" where name = "密码(Password)" ;
update linkis_ps_dm_datasource_type_key set name_en="Database Name" where name = "数据库名(Database name)" ;
update linkis_ps_dm_datasource_type_key set name_en="Cluster Env" where name = "集群环境(Cluster env)" ;
update linkis_ps_dm_datasource_type_key set name_en="Instance" where name = "实例名(instance)" ;
update linkis_ps_dm_datasource_type_key set name_en="Elastic Url" where name = "ES连接URL(Elastic Url)" ;
update linkis_ps_dm_datasource_type_key set name_en="Username" where name = "用户名" ;
update linkis_ps_dm_datasource_type_key set name_en="Password" where name = "密码" ;
update linkis_ps_dm_datasource_type_key set name_en="Default Library" where name = "默认库" ;
update linkis_ps_dm_datasource_type_key set name_en="Host" where name = "Host" ;
update linkis_ps_dm_datasource_type_key set name_en="Port" where name = "端口" ;
update linkis_ps_dm_datasource_type_key set name_en="Connection Params" where name = "连接参数" ;


update linkis_ps_dm_datasource_type_key set description_en="Host" where description ="主机名(Host)";
update linkis_ps_dm_datasource_type_key set description_en="Port" where description ="端口号(Port)";
update linkis_ps_dm_datasource_type_key set description_en="Driver Class Name" where description ="驱动类名(Driver class name)";
update linkis_ps_dm_datasource_type_key set description_en="Input JSON Format: {\"param\":\"value\"}" where description ="输入JSON格式(Input JSON format): {\"param\":\"value\"}";
update linkis_ps_dm_datasource_type_key set description_en="Username" where description ="用户名(Username)";
update linkis_ps_dm_datasource_type_key set description_en="Password" where description ="密码(Password)";
update linkis_ps_dm_datasource_type_key set description_en="Database Name" where description ="数据库名(Database name)";
update linkis_ps_dm_datasource_type_key set description_en="Cluster Env" where description ="集群环境(Cluster env)";
update linkis_ps_dm_datasource_type_key set description_en="Instance" where description ="实例名(instance)";
update linkis_ps_dm_datasource_type_key set description_en="Elastic Url" where description ="ES连接URL(Elastic Url)";
update linkis_ps_dm_datasource_type_key set description_en="Username" where description ="用户名";
update linkis_ps_dm_datasource_type_key set description_en="Password" where description ="密码";
update linkis_ps_dm_datasource_type_key set description_en="Default Library" where description ="默认库";
update linkis_ps_dm_datasource_type_key set description_en="Mongodb Host" where description ="mongodb Host";
update linkis_ps_dm_datasource_type_key set description_en="Port" where description ="端口";
update linkis_ps_dm_datasource_type_key set description_en="Input JSON Format: {\"param\":\"value\"}" where description ="输入JSON格式: {\"param\":\"value\"}";

select @data_source_type_id := id from `linkis_ps_dm_datasource_type` where `name` = 'mysql';
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (@data_source_type_id, 'address', '地址', 'Address', NULL, 'TEXT', NULL, 0, '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now());

select @data_source_type_id := id from `linkis_ps_dm_datasource_type` where `name` = 'oracle';
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (@data_source_type_id, 'address', '地址', 'Address', NULL, 'TEXT', NULL, 0, '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (@data_source_type_id, 'sid', 'SID', 'SID', NULL, 'TEXT', NULL, 0, 'SID', 'SID', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (@data_source_type_id, 'serviceName', 'service_name', 'service_name', NULL, 'TEXT', NULL, 0, 'service_name', 'service_name', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (@data_source_type_id, 'server', 'server', 'server', NULL, 'TEXT', NULL, 0, 'server', 'server', NULL, NULL, NULL, NULL,  now(), now());

select @data_source_type_id := id from `linkis_ps_dm_datasource_type` where `name` = 'dm';
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (@data_source_type_id, 'address', '地址', 'Address', NULL, 'TEXT', NULL, 0, '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now());

select @data_source_type_id := id from `linkis_ps_dm_datasource_type` where `name` = 'kingbase';
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (@data_source_type_id, 'address', '地址', 'Address', NULL, 'TEXT', NULL, 0, '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now());

select @data_source_type_id := id from `linkis_ps_dm_datasource_type` where `name` = 'postgresql';
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (@data_source_type_id, 'address', '地址', 'Address', NULL, 'TEXT', NULL, 0, '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (@data_source_type_id, 'databaseName', '数据库名(Database name)', 'Database name', NULL, 'TEXT', NULL, 0, '数据库名(Database name)', 'Database name', NULL, NULL, NULL, NULL,  now(), now());

select @data_source_type_id := id from `linkis_ps_dm_datasource_type` where `name` = 'sqlserver';
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (@data_source_type_id, 'address', '地址', 'Address', NULL, 'TEXT', NULL, 0, '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (@data_source_type_id, 'databaseName', '数据库名(Database name)', 'Database name', NULL, 'TEXT', NULL, 0, '数据库名(Database name)', 'Database name', NULL, NULL, NULL, NULL,  now(), now());

select @data_source_type_id := id from `linkis_ps_dm_datasource_type` where `name` = 'db2';
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (@data_source_type_id, 'address', '地址', 'Address', NULL, 'TEXT', NULL, 0, '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (@data_source_type_id, 'databaseName', '数据库名(Database name)', 'Database name', NULL, 'TEXT', NULL, 0, '数据库名(Database name)', 'Database name', NULL, NULL, NULL, NULL,  now(), now());

select @data_source_type_id := id from `linkis_ps_dm_datasource_type` where `name` = 'greenplum';
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (@data_source_type_id, 'address', '地址', 'Address', NULL, 'TEXT', NULL, 0, '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (@data_source_type_id, 'databaseName', '数据库名(Database name)', 'Database name', NULL, 'TEXT', NULL, 0, '数据库名(Database name)', 'Database name', NULL, NULL, NULL, NULL,  now(), now());

select @data_source_type_id := id from `linkis_ps_dm_datasource_type` where `name` = 'doris';
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (@data_source_type_id, 'address', '地址', 'Address', NULL, 'TEXT', NULL, 0, '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (@data_source_type_id, 'databaseName', '数据库名(Database name)', 'Database name', NULL, 'TEXT', NULL, 0, '数据库名(Database name)', 'Database name', NULL, NULL, NULL, NULL,  now(), now());

select @data_source_type_id := id from `linkis_ps_dm_datasource_type` where `name` = 'clickhouse';
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (@data_source_type_id, 'address', '地址', 'Address', NULL, 'TEXT', NULL, 0, '地址(host1:port1,host2:port2...)', 'Address(host1:port1,host2:port2...)', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (@data_source_type_id, 'databaseName', '数据库名(Database name)', 'Database name', NULL, 'TEXT', NULL, 0, '数据库名(Database name)', 'Database name', NULL, NULL, NULL, NULL,  now(), now());


