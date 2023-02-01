## 参数变化

| 模块名(服务名)                                          | 类型  | 参数名                                                                  | 默认值   | 描述              |
|---------------------------------------------------|-----|----------------------------------------------------------------------|-------|-----------------|
| ps-linkismanager                                  | 修改  | pipeline.output.isoverwtite <br/>-><br/> pipeline.output.isoverwrite | true  | 取值范围：true或false |
| linkis-engineconn-plugins <br/> linkis-datasource | 新增  | linkis.mysql.strong.security.enable | false | 取值范围：true或false |
| linkis-common                                     | 新增  | linkis.mysql.force.params | allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false | mysql连接强制携带参数   |
| linkis-common                       | 新增  | linkis.mysql.sensitive.params | allowLoadLocalInfile,autoDeserialize,allowLocalInfile,allowUrlInLocalInfile,# | mysql连接安全校验参数   |
