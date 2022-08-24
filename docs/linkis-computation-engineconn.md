## linkis-hadoop-common 配置


| 模块名(服务名) | 参数名 | 默认值 | 描述 | 是否引用|
| -------- | -------- | ----- |----- |  -----   |
|linkis-hadoop-common|wds.linkis.engine.resultSet.cache.max |0k|engine.resultSet.cache.max|
|linkis-hadoop-common|wds.linkis.engine.lock.expire.time|2 * 60 * 1000 |lock.expire.time|
|linkis-hadoop-common|wds.linkis.engineconn.max.task.execute.num|0|task.execute.num|
|linkis-hadoop-common|wds.linkis.engineconn.progresss.fetch.interval-in-seconds| 5|interval-in-seconds|
|linkis-hadoop-common|wds.linkis.engineconn.udf.load.ignore|true |load.ignore|
|linkis-hadoop-common|wds.linkis.engineconn.function.load.ignore| true|function.load.ignore  |
|linkis-hadoop-common|wds.linkis.engineconn.concurrent.thread.num|20| thread.num |
|linkis-hadoop-common|wds.linkis.engineconn.max.parallelism| 300 |max.parallelism|
|linkis-hadoop-common|wds.linkis.engineconn.async.group.max.running| 10|max.running |
|linkis-hadoop-common|wds.linkis.default.computation.executormanager.clazz|org.apache.linkis.engineconn.computation.executor.creation.ComputationExecutorManagerImpl|executormanager.clazz|
|linkis-hadoop-common|linkis.upstream.monitor.ectask.should.start|true|should.start|
|linkis-hadoop-common|linkis.upstream.monitor.wrapper.entries.survive.time.sec|86400| survive.time.sec |
|linkis-hadoop-common|linkis.upstream.monitor.ectask.entrance.threshold.sec| 15| entrance.threshold.sec|
|linkis-hadoop-common|hive.resultset.use.unique.column.names|false| column.names|
|linkis-hadoop-common|wds.linkis.ec.job.id.env.key| LINKIS_JOB_ID |job.id.env.key|
|linkis-hadoop-common|linkis.ec.task.execution.async.thread.size| 50|thread.size|
|linkis-hadoop-common|linkis.ec.task.submit.wait.time.ms|22|wait.time.ms|
|linkis-hadoop-common|wds.linkis.bdp.hive.init.sql.enable| false |sql.enable|
|linkis-hadoop-common|wds.linkis.bdp.use.default.db.enable| true|db.enable|




