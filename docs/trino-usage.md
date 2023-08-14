---
title: Trino 引擎
sidebar_position: 12
---

本文主要介绍在 Linkis1.X 中，Trino 引擎的配置、部署和使用。

## 1. 环境准备

如果您希望在您的服务器上使用 Trino 引擎，您需要准备 Trino 服务并提供连接信息，如 Trino 集群的连接地址、用户名和密码等

## 2. 部署和配置

### 2.1 版本的选择和编译
注意: 编译 Trino 引擎之前需要进行 Linkis 项目全量编译  
发布的安装部署包中默认不包含此引擎插件，
你可以按[Linkis引擎安装指引](https://linkis.apache.org/zh-CN/blog/2022/04/15/how-to-download-engineconn-plugin)部署安装 ，或者按以下流程，手动编译部署

单独编译 Trino 引擎

```
${linkis_code_dir}/linkis-engineconn-plugins/trino/
mvn clean install
```

### 2.2 物料的部署和加载

将 2.1 步编译出来的引擎包,位于
```bash
${linkis_code_dir}/linkis-engineconn-plugins/trino/target/out/trino
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
SET @ENGINE_LABEL="trino-371";
SET @ENGINE_IDE=CONCAT('*-IDE,',@ENGINE_LABEL);
SET @ENGINE_ALL=CONCAT('*-*,',@ENGINE_LABEL);
SET @ENGINE_NAME="trino";

-- add trino engine to IDE
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType', @ENGINE_ALL, 'OPTIONAL', 2, now(), now());
insert into `linkis_cg_manager_label` (`label_key`, `label_value`, `label_feature`, `label_value_size`, `update_time`, `create_time`) VALUES ('combined_userCreator_engineType', @ENGINE_IDE, 'OPTIONAL', 2, now(), now());
select @label_id := id from `linkis_cg_manager_label` where label_value = @ENGINE_IDE;
insert into `linkis_ps_configuration_category` (`label_id`, `level`) VALUES (@label_id, 2);

-- insert configuration key
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.trino.default.limit', '查询的结果集返回条数限制', '结果集条数限制', '5000', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.trino.http.connectTimeout', '连接Trino服务器的超时时间', '连接超时时间（秒）', '60', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.trino.http.readTimeout', '等待Trino服务器返回数据的超时时间', '传输超时时间（秒）', '60', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.trino.resultSet.cache.max', 'Trino结果集缓冲区大小', '结果集缓冲区', '512k', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.trino.url', 'Trino服务器URL', 'Trino服务器URL', 'http://127.0.0.1:8080', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.trino.user', '用于连接Trino查询服务的用户名', '用户名', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.trino.password', '用于连接Trino查询服务的密码', '密码', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.trino.passwordCmd', '用于连接Trino查询服务的密码回调命令', '密码回调命令', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.trino.catalog', '连接Trino查询时使用的catalog', 'Catalog', 'system', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.trino.schema', '连接Trino查询服务的默认schema', 'Schema', '', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.trino.ssl.insecured', '是否忽略服务器的SSL证书', '验证SSL证书', 'false', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.engineconn.concurrent.limit', '引擎最大并发', '引擎最大并发', '100', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.trino.ssl.keystore', 'Trino服务器SSL keystore路径', 'keystore路径', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.trino.ssl.keystore.type', 'Trino服务器SSL keystore类型', 'keystore类型', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.trino.ssl.keystore.password', 'Trino服务器SSL keystore密码', 'keystore密码', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.trino.ssl.truststore', 'Trino服务器SSL truststore路径', 'truststore路径', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.trino.ssl.truststore.type', 'Trino服务器SSL truststore类型', 'truststore类型', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');
INSERT INTO `linkis_ps_configuration_config_key` (`key`, `description`, `name`, `default_value`, `validate_type`, `validate_range`, `engine_conn_type`, `is_hidden`, `is_advanced`, `level`, `treeName`) VALUES ('linkis.trino.ssl.truststore.password', 'Trino服务器SSL truststore密码', 'truststore密码', 'null', 'None', '', @ENGINE_NAME, 0, 0, 1, '数据源配置');


-- trino engine -*
insert into `linkis_ps_configuration_key_engine_relation` (`config_key_id`, `engine_type_label_id`)
(select config.id as config_key_id, label.id AS engine_type_label_id FROM `linkis_ps_configuration_config_key` config
INNER JOIN `linkis_cg_manager_label` label ON config.engine_conn_type = @ENGINE_NAME and label_value = @ENGINE_ALL);

-- trino engine default configuration
insert into `linkis_ps_configuration_config_value` (`config_key_id`, `config_value`, `config_label_id`)
(select relation.config_key_id AS config_key_id, '' AS config_value, relation.engine_type_label_id AS config_label_id FROM `linkis_ps_configuration_key_engine_relation` relation
INNER JOIN `linkis_cg_manager_label` label ON relation.engine_type_label_id = label.id AND label.label_value = @ENGINE_ALL);

```

### 2.4 Trino 引擎相关配置

| 配置                                    | 默认值                 |是否必须 | 说明                                     |
| ---------------------------------------| ----------------------|--------|---------------------------------------- |
| linkis.trino.default.limit             | 5000                  |   是   | 查询的结果集返回条数限制 |
| linkis.trino.http.connectTimeout       | 60                    |   是   | 连接Trino服务器的超时时间 |
| linkis.trino.http.readTimeout          | 60                    |   是   | 等待Trino服务器返回数据的超时时间                 |
| linkis.trino.resultSet.cache.max       | 512k                  |   是   | Trino结果集缓冲区大小                  |
| linkis.trino.url                       | http://127.0.0.1:8080 |   是   | Trino服务器URL                       |
| linkis.trino.user                      | null                  |   否   | 用于连接Trino查询服务的用户名                   |
| linkis.trino.password                  | null                  |   否   | 用于连接Trino查询服务的密码                                 |
| linkis.trino.passwordCmd               | null                  |   否   | 用于连接Trino查询服务的密码回调命令                 |
| linkis.trino.catalog                   | system                |   否   | 连接Trino查询时使用的catalog                  |
| linkis.trino.schema                    |                       |   否   | 连接Trino查询服务的默认schema |
| linkis.trino.ssl.insecured             | false                 |   是   | 是否忽略服务器的SSL证书 |
| linkis.engineconn.concurrent.limit     | 100                   |   否   | 引擎最大并发 |
| linkis.trino.ssl.keystore              | null                  |   否   | Trino服务器SSL keystore路径 |
| linkis.trino.ssl.keystore.type         | null                  |   否   | Trino服务器SSL keystore类型 |
| linkis.trino.ssl.keystore.password     | null                  |   否   | Trino服务器SSL keystore密码 |
| linkis.trino.ssl.truststore            | null                  |   否   | Trino服务器SSL truststore路径 |
| linkis.trino.ssl.truststore.type       | null                  |   否   | Trino服务器SSL truststore类型 |
| linkis.trino.ssl.truststore.password   | null                  |   否   | Trino服务器SSL truststore密码 |

## 3. Trino
### 3.1 准备操作
您需要配置Trino的连接信息，包括连接地址信息或用户名密码(如果启用)等信息。

![Trino](https://user-images.githubusercontent.com/12693319/195242035-d7e22f2c-f116-46a8-b3c5-4e2dea2ce37d.png)

图3-1 Trino配置信息

您也可以再提交任务接口中的params.configuration.runtime进行修改即可
```shell
linkis.trino.url
linkis.trino.user
linkis.trino.password
```

### 3.2 通过Linkis-cli进行任务提交
**使用示例**

Linkis 1.0后提供了cli的方式提交任务，我们只需要指定对应的EngineConn标签类型即可，Trino的使用如下：

```shell
 sh ./bin/linkis-cli -submitUser trino -engineType trino-371 -code 'select * from default.test limit 10' -runtimeMap linkis.es.http.method=GET -runtimeMap linkis.trino.url=127.0.0.1:8080
```

## 4. Trino引擎的用户设置

Trino的用户设置主要是设置Trino的连接信息，但是建议用户将此密码等信息进行加密管理。