---
title: Impala 引擎
sidebar_position: 13
---

本文主要介绍在 Linkis1.X 中，Impala 引擎的配置、部署和使用。

## 1. 环境准备

如果您希望在您的服务器上使用 Impala 引擎，您需要准备 Impala 服务并提供连接信息，如 Impala 集群的连接地址、SASL用户名和密码等

## 2. 部署和配置

### 2.1 版本的选择和编译
注意: 编译 Impala 引擎之前需要进行 Linkis 项目全量编译  
发布的安装部署包中默认不包含此引擎插件，
你可以按[Linkis引擎安装指引](https://linkis.apache.org/zh-CN/blog/2022/04/15/how-to-download-engineconn-plugin)部署安装 ，或者按以下流程，手动编译部署

单独编译 Impala 引擎

```
${linkis_code_dir}/linkis-engineconn-plugins/impala/
mvn clean install
```

### 2.2 物料的部署和加载

将 2.1 步编译出来的引擎包,位于
```bash
${linkis_code_dir}/linkis-engineconn-plugins/impala/target/out/impala
```
上传到服务器的引擎目录下
```bash 
${LINKIS_HOME}/lib/linkis-engineplugins
```
并重启linkis-engineplugin（或者通过引擎接口进行刷新）
```bash
cd ${LINKIS_HOME}/sbin
sh linkis-daemon.sh restart cg-engineplugin
```
### 2.3 引擎的标签

Linkis1.X是通过标签来进行的，所以需要在我们数据库中插入数据，插入的方式如下文所示。

管理台的配置是按照引擎标签来进行管理的，如果新增的引擎，有配置参数需要配置的话，需要修改对应的表的元数据

```
linkis_ps_configuration_config_key:  插入引擎的配置参数的key和默认values
linkis_cg_manager_label：插入引擎label如：hive-3.1.3
linkis_ps_configuration_category： 插入引擎的目录关联关系
linkis_ps_configuration_config_value： 插入引擎需要展示的配置
linkis_ps_configuration_key_engine_relation:配置项和引擎的关联关系
```

```sql
-- set variable
SET @ENGINE_LABEL="impala-340";
SET @ENGINE_IDE=CONCAT('*-IDE,',@ENGINE_LABEL);
SET @ENGINE_ALL=CONCAT('*-*,',@ENGINE_LABEL);
SET @ENGINE_NAME="impala";

-- add impala engine to IDE
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType', @ENGINE_ALL, 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType', @ENGINE_IDE, 'OPTIONAL', 2, now(), now());
select @label_id := id from `linkis_cg_manager_label` where label_value = @ENGINE_IDE;
insert into `linkis_ps_configuration_category` (`label_id`, `level`) VALUES (@label_id, 2);

-- insert configuration key
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.default.limit', '查询的结果集返回条数限制', '结果集条数限制', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.engine.user', '默认引擎启动用户', '默认启动用户', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.user.isolation.mode', '以多用户模式启动引擎', '多用户模式', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.servers', 'Impala服务器地址, '服务地址', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.maxConnections ', '对每台Impala服务器的连接数上限', '最大连接数', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.ssl.enable', '是否启用SSL连接', '启用SSL', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.ssl.keystore.type', 'SSL Keystore类型', 'SSL Keystore类型', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.ssl.keystore', 'SSL Keystore路径', 'SSL Keystore路径', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.ssl.keystore.password', 'SSL Keystore密码', 'SSL Keystore密码', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.ssl.truststore.type', 'SSL Truststore类型', 'SSL Truststore类型', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.ssl.truststore', 'SSL Truststore路径', 'SSL Truststore路径', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.ssl.truststore.password', 'SSL Truststore密码', 'SSL Truststore密码', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.sasl.enable', '是否启用SASL认证', '启用SASL', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.sasl.mechanism', 'SASL Mechanism', 'SASL Mechanism', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.sasl.authorizationId', 'SASL AuthorizationId', 'SASL AuthorizationId', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.sasl.protocol', 'SASL Protocol', 'SASL Protocol', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.sasl.properties', 'SASL Properties: key1=value1,key2=value2', 'SASL Properties', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.sasl.username', 'SASL Username', 'SASL Username', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.sasl.password', 'SASL Password', 'SASL Password', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.sasl.password.cmd', 'SASL Password获取命令', 'SASL Password获取命令', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.heartbeat.seconds', '任务状态更新间隔', '任务状态更新间隔', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.query.timeout.seconds', '任务执行超时时间', '任务执行超时时间', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.query.batchSize', '结果集获取批次大小', '结果集获取批次大小', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.impala.query.options', '查询提交参数: key1=value1,key2=value2', '查询提交参数', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');


-- impala engine -*
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(select config.id as config_key_id, label.id AS engine_type_label_id FROM `linkis_ps_configuration_config_key` config
INNER JOIN `linkis_cg_manager_label` label ON config.engine_conn_type = @ENGINE_NAME and label_value = @ENGINE_ALL);

-- impala engine default configuration
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(select relation.config_key_id AS config_key_id, '' AS config_value, relation.engine_type_label_id AS config_label_id FROM `linkis_ps_configuration_key_engine_relation` relation
INNER JOIN `linkis_cg_manager_label` label ON relation.engine_type_label_id = label.id AND label.label_value = @ENGINE_ALL);

```

### 2.4 Impala 引擎相关配置

| 配置                                    | 默认值                 |是否必须 | 说明                                     |
| ---------------------------------------| ----------------------|--------|---------------------------------------- |
| linkis.impala.default.limit             | 5000                 |   是   | 查询的结果集返回条数限制                    |
| linkis.impala.engine.user               | ${HDFS_ROOT_USER}    |   是   | 默认引擎启动用户                           |
| linkis.impala.user.isolation.mode       | false                |   是   | 以多用户模式启动引擎                        |
| linkis.impala.servers                   | 127.0.0.1:21050      |   是   | Impala服务器地址，','分隔                  |
| linkis.impala.maxConnections            | 10                   |   是   | 对每台Impala服务器的连接数上限               |
| linkis.impala.ssl.enable                | false                |   是   | 是否启用SSL连接                            |
| linkis.impala.ssl.keystore.type         | JKS                  |   否   | SSL Keystore类型                          |
| linkis.impala.ssl.keystore              | null                 |   否   | SSL Keystore路径                          |
| linkis.impala.ssl.keystore.password     | null                 |   否   | SSL Keystore密码                          |
| linkis.impala.ssl.truststore.type       | JKS                  |   否   | SSL Truststore类型                        |
| linkis.impala.ssl.truststore            | null                 |   否   | SSL Truststore路径                        |
| linkis.impala.ssl.truststore.password   | null                 |   否   | SSL Truststore密码                        |
| linkis.impala.sasl.enable               | false                |   是   | 是否启用SASL认证                           |
| linkis.impala.sasl.mechanism            | PLAIN                |   否   | SASL Mechanism                           |
| linkis.impala.sasl.authorizationId      | null                 |   否   | SASL AuthorizationId                     |
| linkis.impala.sasl.protocol             | LDAP                 |   否   | SASL Protocol                            |
| linkis.impala.sasl.properties           | null                 |   否   | SASL Properties: key1=value1,key2=value2 |
| linkis.impala.sasl.username             | ${impala.engine.user}|   否   | SASL Username                            |
| linkis.impala.sasl.password             | null                 |   否   | SASL Password                            |
| linkis.impala.sasl.password.cmd         | null                 |   否   | SASL Password获取命令                     |
| linkis.impala.heartbeat.seconds         | 1                    |   是   | 任务状态更新间隔                            |
| linkis.impala.query.timeout.seconds     | 0                    |   否   | 任务执行超时时间                           |
| linkis.impala.query.batchSize           | 1000                 |   是   | 结果集获取批次大小                          |
| linkis.impala.query.options             | null                 |   否   | 查询提交参数: key1=value1,key2=value2      |

## 3. Impala
### 3.1 准备操作
您需要在管理后台配置Impala的连接信息，包括连接地址信息或用户名密码(如果启用)等信息。
您也可以在提交任务接口中的params.configuration.runtime进行修改
```shell
linkis.impala.servers
```

### 3.2 通过Linkis-cli进行任务提交
**使用示例**

Linkis 1.0后提供了cli的方式提交任务，我们只需要指定对应的EngineConn标签类型即可，Impala的使用如下：

```shell
 sh ./bin/linkis-cli -submitUser impala -engineType impala-340 -code 'select * from default.test limit 10' -runtimeMap linkis.es.http.method=GET -runtimeMap linkis.impala.servers=127.0.0.1:21050
```

## 4. Impala引擎的用户设置

Impala的用户设置主要是设置Impala的连接信息，但是建议用户将此密码等信息进行加密管理。