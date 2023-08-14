## linkis-computation-engineconn configure


| Module Name (Service Name) | Parameter Name | Default Value | Description |Used|
| -------- | -------- | ----- |----- |  -----   |
|linkis-computation-engineconn|wds.linkis.engine.resultSet.cache.max |0k|engine.resultSet.cache.max|
|linkis-computation-engineconn|wds.linkis.engine.lock.expire.time|2 * 60 * 1000 |lock.expire.time|
|linkis-computation-engineconn|wds.linkis.engineconn.max.task.execute.num|0|task.execute.num|
|linkis-computation-engineconn|wds.linkis.engineconn.progresss.fetch.interval-in-seconds| 5|interval-in-seconds|
|linkis-computation-engineconn|wds.linkis.engineconn.udf.load.ignore|true |load.ignore|
|linkis-computation-engineconn|wds.linkis.engineconn.function.load.ignore| true|function.load.ignore  |
|linkis-computation-engineconn|wds.linkis.engineconn.concurrent.thread.num|20| thread.num |
|linkis-computation-engineconn|wds.linkis.engineconn.max.parallelism| 300 |max.parallelism|
|linkis-computation-engineconn|wds.linkis.engineconn.async.group.max.running| 10|max.running |
|linkis-computation-engineconn|wds.linkis.default.computation.executormanager.clazz|org.apache.linkis.engineconn.computation.executor.creation.ComputationExecutorManagerImpl|executormanager.clazz|
|linkis-computation-engineconn|linkis.upstream.monitor.ectask.should.start|true|should.start|
|linkis-computation-engineconn|linkis.upstream.monitor.wrapper.entries.survive.time.sec|86400| survive.time.sec |
|linkis-computation-engineconn|linkis.upstream.monitor.ectask.entrance.threshold.sec| 15| entrance.threshold.sec|
|linkis-computation-engineconn|hive.resultset.use.unique.column.names|false| column.names|
|linkis-computation-engineconn|wds.linkis.ec.job.id.env.key| LINKIS_JOB_ID |job.id.env.key|
|linkis-computation-engineconn|linkis.ec.task.execution.async.thread.size| 50|thread.size|
|linkis-computation-engineconn|linkis.ec.task.submit.wait.time.ms|22|wait.time.ms|
|linkis-computation-engineconn|wds.linkis.bdp.hive.init.sql.enable| false |sql.enable|
|linkis-computation-engineconn|linkis.bdp.use.default.db.enable| true|db.enable|




