UPDATE `linkis_ps_configuration_config_key` SET `default_value` = 'default' WHERE `key` = 'wds.linkis.rm.yarnqueue';
UPDATE `linkis_ps_configuration_config_key` SET `description` = '范围：-1-10000，单位：个', `default_value` = '-1', `validate_range` = '[-1,10000]' WHERE `key` = 'mapred.reduce.tasks';
DELETE FROM `linkis_ps_configuration_config_key` WHERE `key` IN ('dfs.block.size','hive.exec.reduce.bytes.per.reducer','wds.linkis.engineconn.max.free.time');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `is_hidden`, `is_advanced`, `level`, `treeName`, `engine_conn_type`) VALUES ('wds.linkis.jdbc.driver', '例如:org.apache.hive.jdbc.HiveDriver', 'jdbc连接驱动', '', 'None', '', '0', '0', '1', '用户配置', 'jdbc');

INSERT INTO `linkis_mg_gateway_auth_token`(`token_name`,`legal_users`,`legal_hosts`,`business_owner`,`create_time`,`update_time`,`elapse_day`,`update_by`) VALUES ('DSM-AUTH','*','*','BDP',curdate(),curdate(),-1,'LINKIS');
INSERT INTO `linkis_mg_gateway_auth_token`(`token_name`,`legal_users`,`legal_hosts`,`business_owner`,`create_time`,`update_time`,`elapse_day`,`update_by`) VALUES ('LINKIS_CLI_TEST','*','*','BDP',curdate(),curdate(),-1,'LINKIS');

INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (1, 'driverClassName', '驱动类名(Driver class name)', 'Driver class name', 'com.mysql.jdbc.Driver', 'TEXT', NULL, 1, '驱动类名(Driver class name)', 'Driver class name', NULL, NULL, NULL, NULL,  now(), now());
INSERT INTO `linkis_ps_dm_datasource_type_key` (`data_source_type_id`, `key`, `name`, `name_en`, `default_value`, `value_type`, `scope`, `require`, `description`, `description_en`, `value_regex`, `ref_id`, `ref_value`, `data_source`, `update_time`, `create_time`) VALUES (1, 'databaseName', '数据库名(Database name)', 'Database name', NULL, 'TEXT', NULL, 0, '数据库名(Database name)', 'Database name', NULL, NULL, NULL, NULL,  now(), now());

UPDATE `linkis_ps_dm_datasource_type_key` SET `name` = '主机名(Host)', `name_en` = 'Host', `description` = '主机名(Host)', `description_en` = 'Host' WHERE `key` = 'host';
UPDATE `linkis_ps_dm_datasource_type_key` SET `name` = '端口号(Port)', `name_en` = 'Port', `description` = '端口号(Port)', `description_en` = 'Port' WHERE `key` = 'port';
UPDATE `linkis_ps_dm_datasource_type_key` SET `name` = '连接参数(Connection params)', `name_en` = 'Connection params', `description` = '输入JSON格式(Input JSON format): {"param":"value"}', `description_en` = 'Input JSON format: {"param":"value"}' WHERE `key` = 'params';
UPDATE `linkis_ps_dm_datasource_type_key` SET `name` = '用户名(Username)', `name_en` = 'Username', `description` = '用户名(Username)', `description_en` = 'Username' WHERE `key` = 'username';
UPDATE `linkis_ps_dm_datasource_type_key` SET `name` = '密码(Password)', `name_en` = 'Password', `description` = '密码(Password)', `description_en` = 'Password' WHERE `key` = 'password';
UPDATE `linkis_ps_dm_datasource_type_key` SET `name` = '集群环境(Cluster env)', `name_en` = 'Cluster env', `description` = '集群环境(Cluster env)', `description_en` = 'Cluster env' WHERE `key` = 'envId';

UPDATE `linkis_ps_configuration_config_key` set `default_value` = 'com.mysql.jdbc.Driver' WHERE `key` = 'wds.linkis.jdbc.driver';

-- config es engine
-- set variable
SET @ENGINE_LABEL="elasticsearch-7.6.2";
SET @ENGINE_IDE=CONCAT('*-IDE,',@ENGINE_LABEL);
SET @ENGINE_NAME="elasticsearch";

-- add es engine to IDE
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType', @ENGINE_IDE, 'OPTIONAL', 2, now(), now());
select @label_id := id from `linkis_cg_manager_label` where label_value = @ENGINE_IDE;
insert into `linkis_ps_configuration_category` (`label_id`, `level`) VALUES (@label_id, 2);

-- insert configuration key
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.es.cluster', 'eg:127.0.0.1:9200', 'connection address', '127.0.0.1:9200', 'None', '', @ENGINE_NAME, 0, 0, 1, 'datasource configuration');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.es.datasource', 'datasource', 'datasource', 'hadoop', 'None', '', @ENGINE_NAME, 0, 0, 1, 'datasource configuration');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.es.username', 'username', 'ES cluster username', 'None', 'None', '', @ENGINE_NAME, 0, 0, 1, 'datasource configuration');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.es.password', 'password', 'ES cluster password', 'None', 'None', '', @ENGINE_NAME, 0, 0, 1, 'datasource configuration');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.es.auth.cache', 'Whether the client is cache authenticated', 'Whether the client is cache authenticated', 'false', 'None', '', @ENGINE_NAME, 0, 0, 1, 'datasource configuration');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.es.sniffer.enable', 'Whether Sniffer is enabled on the client', 'Whether Sniffer is enabled on the client', 'false', 'None', '', @ENGINE_NAME, 0, 0, 1, 'datasource configuration');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.es.http.method', 'Request Method', 'HTTP Request Method', 'GET', 'None', '', @ENGINE_NAME, 0, 0, 1, 'datasource configuration');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.es.http.endpoint', '/_search', 'The Endpoint of th JSON script', '/_search', 'None', '', @ENGINE_NAME, 0, 0, 1, 'datasource configuration');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.es.sql.endpoint', '/_sql', 'The Endpoint of th SQL script', '/_sql', 'None', '', @ENGINE_NAME, 0, 0, 1, 'datasource configuration');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.es.sql.format', 'SQL script call template, %s replaced with SQL as the body of the request request Es cluster', 'request body', '{"query":"%s"}', 'None', '', @ENGINE_NAME, 0, 0, 1, 'datasource configuration');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.es.headers.*', 'client headers configuration', 'client headers configuration', 'None', 'None', '', @ENGINE_NAME, 0, 0, 1, 'datasource configuration');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.engineconn.concurrent.limit', 'the maximum engine concurrency', 'the maximum engine concurrency', '100', 'None', '', @ENGINE_NAME, 0, 0, 1, 'datasource configuration');

-- elasticsearch engine -*
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(select config.id as config_key_id, label.id AS engine_type_label_id FROM `linkis_ps_configuration_config_key` config
INNER JOIN `linkis_cg_manager_label` label ON config.engine_conn_type = @ENGINE_NAME and label_value = @ENGINE_IDE);

-- elasticsearch engine default configuration
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(select relation.config_key_id AS config_key_id, '' AS config_value, relation.engine_type_label_id AS config_label_id FROM `linkis_ps_configuration_key_engine_relation` relation
INNER JOIN `linkis_cg_manager_label` label ON relation.engine_type_label_id = label.id AND label.label_value = @ENGINE_IDE);


-- config presto engine
-- set variable
SET @PRESTO_LABEL="presto-0.234";
SET @PRESTO_ALL=CONCAT('*-*,',@PRESTO_LABEL);
SET @PRESTO_IDE=CONCAT('*-IDE,',@PRESTO_LABEL);

SET @PRESTO_NAME="presto";

insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@PRESTO_ALL, 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType',@PRESTO_IDE, 'OPTIONAL', 2, now(), now());

select @label_id := id from `linkis_cg_manager_label` where `label_value` = @PRESTO_IDE;
insert into `linkis_ps_configuration_category` (`label_id`, `level`) VALUES (@label_id, 2);

INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('wds.linkis.presto.url','Presto cluster connection','presto connection address','http://127.0.0.1:8080','None',null,@PRESTO_NAME,0,0, 1,'Data source configuration');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('wds.linkis.presto.catalog','Querys Catalog','presto-connected catalog','hive','None',null,@PRESTO_NAME,0,0,1,'Data source configuration');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('wds.linkis.presto.schema','Query Schema','Database connection schema','','None',null,@PRESTO_NAME,0,0,1,'Data source configuration');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('wds.linkis.presto.source','source used for query','database connection source','','None',null,@PRESTO_NAME,0,0,1,'data source configuration') ;

-- engine -*
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(select config.id as `config_key_id`, label.id AS `engine_type_label_id` FROM linkis_ps_configuration_config_key config
INNER JOIN linkis_cg_manager_label label ON config.engine_conn_type = @PRESTO_NAME and label_value = @PRESTO_IDE);

-- engine default configuration
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(select `relation`.`config_key_id` AS `config_key_id`, '' AS `config_value`, `relation`.`engine_type_label_id` AS `config_label_id` FROM linkis_ps_configuration_key_engine_relation relation
INNER JOIN linkis_cg_manager_label label ON relation.engine_type_label_id = label.id AND label.label_value = @PRESTO_IDE);