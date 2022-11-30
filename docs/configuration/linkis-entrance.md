## linkis-entrance configure


| Module Name (Service Name) | Parameter Name | Default Value | Description |Used|
| -------- | -------- | ----- |----- |  -----   |
|linkis-entrance|wds.linkis.entrance.scheduler.maxParallelismUsers |1000| scheduler.maxParallelismUsers|
|linkis-entrance|wds.linkis.entrance.listenerBus.queue.capacity|5000 |queue.capacity|
|linkis-entrance|wds.linkis.entrance.job.persist.wait.max|5m|persist.wait.max|
|linkis-entrance|wds.linkis.entrance.multi.entrance.flag| true |entrance.flag|
|linkis-entrance|wds.linkis.query.application.name|linkis-ps-jobhistory |application.name|
|linkis-entrance|wds.linkis.entrance.config.log.path|  |config.log.path |
|linkis-entrance|wds.linkis.entrance.log.cacheMax|500| log.cacheMax |
|linkis-entrance|wds.linkis.entrance.log.defaultCharSet| utf-8 |log.defaultCharSet|
|linkis-entrance|wds.linkis.console.config.logPath| wds.linkis.config.logPath|config.logPath|
|linkis-entrance|wds.linkis.default.requestApplication.name|IDE|requestApplication.name|
|linkis-entrance|wds.linkis.sql.limit.creator|IDE|limit.creator|
|linkis-entrance|wds.linkis.default.runType|sql| default.runType|
|linkis-entrance|wds.linkis.default.create.service| default_create_service| create.service|
|linkis-entrance|wds.linkis.log.exclude|com.netflix| log.exclude|
|linkis-entrance|wds.linkis.log.clear| false |log.clear|
|linkis-entrance|wds.linkis.rm.instance|10|rm.instance|
|linkis-entrance|wds.linkis.log.exclude.all| com.netflix |log.exclude.all|
|linkis-entrance|wds.linkis.max.ask.executor.time| 5m|executor.time|
|linkis-entrance|wds.linkis.errorcode.file.dir|  | errorcode.file.dir|
|linkis-entrance|wds.linkis.entrance.user| | entrance.user|
|linkis-entrance|wds.linkis.errorcode.file|   |errorcode.file|
|linkis-entrance|wds.linkis.hive.thread.name| |hive.thread.name|
|linkis-entrance|wds.linkis.hive.stage.name| Stage-|hive.stage.name|
|linkis-entrance|wds.linkis.spark.special.log.include| org.apache.linkis.engine.spark.utils.JobProgressUtil|spark.special.log.include|
|linkis-entrance|wds.linkis.spark.progress.name| org.apache.linkis.engine.spark.utils.JobProgressUtil$ |spark.progress.name|
|linkis-entrance|bdp.dataworkcloud.entrance.end.flag|info -|entrance.end.flag|
|linkis-entrance|wds.linkis.hive.create.table.log| numFiles |hive.create.table.log|
|linkis-entrance|wds.linkis.entrance.bdp.env| printInfo -|hive.printinfo.log|
|linkis-entrance|wds.linkis.errorcode.file.dir| true | entrance.bdp.env|
|linkis-entrance|wds.linkis.entrance.shell.danger.check.enabled|true | check.enabled|
|linkis-entrance|wds.linkis.shell.danger.usage|rm,sh,find,kill,python,for,source,hdfs,hadoop,spark-sql,spark-submit,pyspark,spark-shell,hive,yarn |danger.usage|
|linkis-entrance|wds.linkis.shell.white.usage|sqoop,cd,ll,ls,echo,cat,tree,diff,who,grep,whoami,set,pwd,cut,file,head,less,if,while |.white.usage|
|linkis-entrance|wds.linkis.entrance.flow.creator| nodeexecution|flow.creator|
|linkis-entrance|wds.linkis.entrance.scheduler.creator| Schedulis|scheduler.creator|
|linkis-entrance|wds.linkis.entrance.skip.auth|false|skip.auth|
|linkis-entrance|wds.linkis.entrance.push.progress| false |push.progress|
|linkis-entrance|wds.linkis.concurrent.group.factory.capacity| 1000|factory.capacity|
|linkis-entrance|wds.linkis.concurrent.group.factory.running.jobs| 30 | running.jobs|
|linkis-entrance|wds.linkis.concurrent.group.factory.executor.time| 5 * 60 * 1000 | factory.executor.time|
|linkis-entrance|wds.linkis.entrance.engine.lastupdate.timeout| 5s |lastupdate.timeout|
|linkis-entrance|wds.linkis.entrance.engine.timeout| 10s|engine.timeout|
|linkis-entrance|wds.linkis.entrance.engine.activity_monitor.interval| 3s|activity_monitor.interval|
|linkis-entrance|wds.linkis.enable.job.timeout.check|true|timeout.check|
|linkis-entrance|wds.linkis.timeout.thread.scan.interval| 120 |thread.scan.interval|
|linkis-entrance|wds.linkis.user.parallel.reflesh.time| 30|user.parallel.reflesh.time|
|linkis-entrance|wds.linkis.entrance.jobinfo.update.retry| true | jobinfo.update.retry|
|linkis-entrance|wds.linkis.entrance.jobinfo.update.retry.max.times| 3 | update.retry.max.times|
|linkis-entrance|wds.linkis.entrance.jobinfo.update.retry.interval| 2 * 60 * 1000| update.retry.interval|
|linkis-entrance|wds.linkis.entrance.code.parser.selective.ignored| true|parser.selective.ignored|
|linkis-entrance|wds.linkis.entrance.code.parser.enable| false|parser.enable|
|linkis-entrance|wds.linkis.entrance.yarn.queue.core.max| 300|yarn.queue.core.max|
|linkis-entrance|wds.linkis.entrance.yarn.queue.memory.max.g| 1000|yarn.queue.memory.max.g|
|linkis-entrance|linkis.entrance.enable.hdfs.log.cache|true|hdfs.log.cache|
|linkis-entrance|linkis.entrance.cli.heartbeat.threshold.sec| 30L |heartbeat.threshold.sec|
|linkis-entrance|wds.linkis.entrance.log.push.interval.time| 5 * 60 * 1000|push.interval.time|
|linkis-entrance|wds.linkis.consumer.group.cache.capacity| 5000 | group.cache.capacity|
|linkis-entrance|wds.linkis.consumer.group.expire.time.hour| 50 | expire.time.hour|
|linkis-entrance|wds.linkis.entrance.client.monitor.creator|LINKISCLI| client.monitor.creator|
 


