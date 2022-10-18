## trino 配置

| 模块名(服务名) | 参数名                                  | 默认值                 |描述                                   |是否引用|
| ------------ | ---------------------------------------| ----------------------|---------------------------------------- | -----   |
| trino        | linkis.trino.default.limit             | 5000                  | 查询的结果集返回条数限制 |
| trino        | linkis.trino.http.connectTimeout       | 60                    | 连接Trino服务器的超时时间（秒） |
| trino        | linkis.trino.http.readTimeout          | 60                    | 等待Trino服务器返回数据的超时时间（秒） |
| trino        | linkis.trino.resultSet.cache.max       | 512k                  | Trino结果集缓冲区大小 |
| trino        | linkis.trino.url                       | http://127.0.0.1:8080 | Trino服务器URL |
| trino        | linkis.trino.user                      | null                  | 用于连接Trino查询服务的用户名 |
| trino        | linkis.trino.password                  | null                  | 用于连接Trino查询服务的密码 |
| trino        | linkis.trino.passwordCmd               | null                  | 用于连接Trino查询服务的密码回调命令 |
| trino        | linkis.trino.catalog                   | system                | 连接Trino查询时使用的catalog |
| trino        | linkis.trino.schema                    |                       | 连接Trino查询服务的默认schema |
| trino        | linkis.trino.ssl.insecured             | false                 | 是否忽略服务器的SSL证书 |
| trino        | linkis.engineconn.concurrent.limit     | 100                   | 引擎最大并发 |
| trino        | linkis.trino.ssl.keystore              | null                  | Trino服务器SSL keystore路径 |
| trino        | linkis.trino.ssl.keystore.type         | null                  | Trino服务器SSL keystore类型 |
| trino        | linkis.trino.ssl.keystore.password     | null                  | Trino服务器SSL keystore密码 |
| trino        | linkis.trino.ssl.truststore            | null                  | Trino服务器SSL truststore路径 |
| trino        | linkis.trino.ssl.truststore.type       | null                  | Trino服务器SSL truststore类型 |
| trino        | linkis.trino.ssl.truststore.password   | null                  | Trino服务器SSL truststore密码 |
