## 参数变化

| 模块名(服务名)| 类型  |     参数名                                                | 默认值             | 描述                                                    |
| ----------- | ----- | -------------------------------------------------------- | ---------------- | ------------------------------------------------------- |
|cg-engineplugin | 新增  |   linkis.trino.default.limit    | 5000 | Trino查询的结果集返回条数限制 |
|cg-engineplugin | 新增  |   linkis.trino.http.connectTimeout    | 60 | 连接Trino服务器的超时时间 |
|cg-engineplugin | 新增  |   linkis.trino.http.readTimeout    | 60 | 等待Trino服务器返回数据的超时时间 |
|cg-engineplugin | 新增  |   linkis.trino.resultSet.cache.max    | 512k | Trino结果集缓冲区大小 |
|cg-engineplugin | 新增  |   linkis.trino.url    | http://127.0.0.1:8080 | Trino服务器URL |
|cg-engineplugin | 新增  |   linkis.trino.user    | null | 用于连接Trino查询服务的用户名 |
|cg-engineplugin | 新增  |   linkis.trino.password    | null | 用于连接Trino查询服务的密码 |
|cg-engineplugin | 新增  |   linkis.trino.passwordCmd    | null | 用于连接Trino查询服务的密码回调命令 |
|cg-engineplugin | 新增  |   linkis.trino.catalog    | system | 连接Trino查询时使用的catalog |
|cg-engineplugin | 新增  |   linkis.trino.schema    |  | 连接Trino查询服务的默认schema |
|cg-engineplugin | 新增  |   linkis.trino.ssl.insecured    | false | 是否忽略服务器的SSL证书 |
|cg-engineplugin | 新增  |   linkis.trino.ssl.keystore    | null | keystore路径 |
|cg-engineplugin | 新增  |   linkis.trino.ssl.keystore.type    | null | keystore类型 |
|cg-engineplugin | 新增  |   linkis.trino.ssl.keystore.password    | null | keystore密码 |
|cg-engineplugin | 新增  |   linkis.trino.ssl.truststore    | null | truststore路径 |
|cg-engineplugin | 新增  |   linkis.trino.ssl.truststore.type    | null | truststore类型 |
|cg-engineplugin | 新增  |   linkis.trino.ssl.truststore.password    | null | truststore密码 |

## 特性说明

| 模块名(服务名)| 类型   | 特性                                                    |
| ----------- | ---------------- | ------------------------------------------------------- |
|linkis-metadata-query-service-mysql | 新增  |  基于mysql 模块融合dm，greenplum，kingbase，oracle，postgres，sqlserver ，协议和sql 区分开，metadata-query 反射多个数据源，基于mysql模块扩展，融合为一个模块。|
|linkis-engineconn-plugins-trino | 新增  |  基于trino-client实现的Trino查询引擎。|
