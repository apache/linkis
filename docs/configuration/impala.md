## impala 配置

| 模块名(服务名) | 参数名                                  | 默认值                 |描述                                   |是否引用|
| ------------ | ---------------------------------------| ----------------------|---------------------------------------- | -----   |
| impala        | linkis.impala.default.limit             | 5000                  | 查询的结果集返回条数限制      |
| impala        | linkis.impala.engine.user               | ${HDFS_ROOT_USER}     | 默认引擎启动用户            |
| impala        | linkis.impala.user.isolation.mode       | false                 | 以多用户模式启动引擎        |
| impala        | linkis.impala.servers                   | 127.0.0.1:21050       | Impala服务器地址，','分隔    |
| impala        | linkis.impala.maxConnections            | 10                    | 对每台Impala服务器的连接数上限 |
| impala        | linkis.impala.ssl.enable                | false                 | 是否启用SSL连接               |
| impala        | linkis.impala.ssl.keystore.type         | ${keystore.type}      | SSL Keystore类型           |
| impala        | linkis.impala.ssl.keystore              | null                  | SSL Keystore路径           |
| impala        | linkis.impala.ssl.keystore.password     | null                  | SSL Keystore密码           |
| impala        | linkis.impala.ssl.truststore.type       | ${keystore.type}      | SSL Truststore类型          |
| impala        | linkis.impala.ssl.truststore            | null                  | SSL Truststore路径          |
| impala        | linkis.impala.ssl.truststore.password   | null                  | SSL Truststore密码           |
| impala        | linkis.impala.sasl.enable               | false                 | 是否启用SASL认证             |
| impala        | linkis.impala.sasl.mechanism            | PLAIN                 | SASL Mechanism            |
| impala        | linkis.impala.sasl.authorizationId      | null                  | SASL AuthorizationId           |
| impala        | linkis.impala.sasl.protocol             | LDAP                  | SASL Protocol                  |
| impala        | linkis.impala.sasl.properties           | null                  | SASL Properties: key1=value1,key2=value2 |
| impala        | linkis.impala.sasl.username             | ${impala.engine.user} | SASL Username               |
| impala        | linkis.impala.sasl.password             | null                  | SASL Password                 |
| impala        | linkis.impala.sasl.password.cmd         | null                  | SASL Password获取命令            |
| impala        | linkis.impala.heartbeat.seconds         | 1                     | 任务状态更新间隔                 |
| impala        | linkis.impala.query.timeout.seconds     | 0                     | 任务执行超时时间               |
| impala        | linkis.impala.query.batchSize           | 1000                  | 结果集获取批次大小               |
| impala        | linkis.impala.query.options             | null                  | 查询提交参数: key1=value1,key2=value2 |
