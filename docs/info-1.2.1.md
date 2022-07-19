## 参数变化 


| 模块名(服务名)| 类型  |     参数名                                                | 默认值             | 描述                                                    |
| ----------- | ----- | -------------------------------------------------------- | ---------------- | ------------------------------------------------------- |
|ps-metadataquery | 新增  |        | linkis-metadata-query-service-mysql| 在mysql元数据服务兼容oracle,kingbase,postgresql,sqlserver,db2,greenplum,dm,驱动外部引入 |

## 特性说明
| 模块名(服务名)| 类型   | 特性                                                    |
| ----------- | ---------------- | ------------------------------------------------------- |
|linkis-metadata-query-service-mysql | 新增  |  基于mysql 模块融合dm，greenplum，kingbase，oracle，postgres，sqlserver ，协议和sql 区分开，metadata-query 反射多个数据源，基于mysql模块扩展，融合为一个模块。|
