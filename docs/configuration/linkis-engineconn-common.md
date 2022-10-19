## linkis-engineconn-common 配置

| 模块名(服务名) | 参数名 | 默认值 | 描述 | 是否引用|
| -------- | -------- | ----- |----- |  -----   |
|linkis-engineconn-common|wds.linkis.engine.connector.executions|org.apache.linkis.engineconn.computation.executor.execute.ComputationEngineConnExecution|connector.executions|
|linkis-engineconn-common|wds.linkis.engine.connector.hooks |org.apache.linkis.engineconn.computation.executor.hook.ComputationEngineConnHook |engine.connector.hooks|
|linkis-engineconn-common|wds.linkis.engine.launch.cmd.params.user.key|user|user.key|
|linkis-engineconn-common|wds.linkis.engine.parallelism.support.enabled| false|support.enabled |
|linkis-engineconn-common|wds.linkis.engine.push.log.enable|true |log.enable|
|linkis-engineconn-common|wds.linkis.engineconn.plugin.default.class| org.apache.linkis.engineplugin.hive.HiveEngineConnPlugin|plugin.default.class  |
|linkis-engineconn-common|wds.linkis.engine.task.expire.time|1000 *3600* 24| task.expire.time|
|linkis-engineconn-common|wds.linkis.engine.lock.refresh.time| 1000 *60* 3 |lock.refresh.time|
|linkis-engineconn-common|wds.linkis.engine.work.home.key| PWD |work.home.key |
|linkis-engineconn-common|wds.linkis.engine.logs.dir.key|LOG_DIRS|logs.dir.key|
|linkis-engineconn-common|wds.linkis.engine.connector.init.time|8m|init.time|
|linkis-engineconn-common|wds.linkis.spark.engine.yarn.app.id.parse.regex| | parse.regex |
