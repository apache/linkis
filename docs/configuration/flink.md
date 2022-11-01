## flink configure

| Module Name (Service Name) | Parameter Name | Default Value | Description |Used|
| -------- | -------- | ----- |----- |  -----   |
|flink|flink.client.memory |1024|client.memory |
|flink|flink.taskmanager.memory|4096 |taskmanager.memory|
|flink|flink.jobmanager.memory|1024|jobmanager.memory|
|flink|flink.taskmanager.numberOfTaskSlots| 2|taskmanager.numberOfTaskSlots|
|flink|flink.taskmanager.cpu.cores|2 |taskmanager.cpu.cores|
|flink|flink.container.num| 2|container.num  |
|flink|wds.linkis.rm.yarnqueue|default| rm.yarnqueue |
|flink|wds.linkis.engineconn.flink.app.parallelism| 4 |flink.app.parallelism|
|flink|spark.lib.path|  |spark.lib.path |
|flink|flink.version| 1.12.2 |flink.version|
|flink|flink.home|/appcom/Install/flink|flink.home|
|flink|flink.conf.dir|  | flink.conf.dir |
|flink|flink.user.lib.path| The hdfs lib path of each user in Flink EngineConn.| user.lib.path|
|flink|flink.local.lib.path|/appcom/Install/flink/lib| local.lib.path |
|flink|flink.user.local.lib.path| /appcom/Install/flink/lib | user.local.lib.path|
|flink|flink.yarn.ship-directories|  |yarn.ship-directorie|
|flink|flink.yarn.remote.ship-directories| |yarn.remote.ship-directories|
|flink|flink.app.checkpoint.enable| false |checkpoint.enable|
|flink|flink.app.checkpoint.interval| 3000|checkpoint.interval|
|flink|flink.app.checkpoint.mode| EXACTLY_ONCE |checkpoint.mode|
|flink|flink.app.checkpoint.timeout| 60000 |checkpoint.timeout |
|flink|flink.app.checkpoint.minPause|   |checkpoint.minPause|
|flink|flink.app.savePointPath|savePointPath|savePointPath|
|flink|flink.app.allowNonRestoredStatus|  false | allowNonRestoredStatus |
|flink|flink.sql.planner| blink | sql.planner|
|flink|flink.sql.executionType|streaming|executionType|
|flink|flink.dev.sql.select.lines.max| 500 | lines.max|
|flink|flink.dev.sql.result.wait.time.max|1m  |wait.time.maxe|
|flink|flink.app.args|  |app.args|
|flink|flink.app.main.class|   |app.main.class|
|flink|flink.app.main.class.jar|  |app.main.class.jar|
|flink|flink.app.user.class.path|  |user.class.path|
|flink|flink.client.request.timeout| 30s |fetch.status.interval|
|flink|flink.app.fetch.status.interval| 5s  |checkpoint.minPause|
|flink|flink.app.fetch.status.failed.num|3|fetch.status.failed.num|
|flink|linkis.flink.reporter.enable|  false | reporter.enable|
|flink|linkis.flink.reporter.class|   | reporter.class|
|flink|linkis.flink.reporter.interval|60s|reporter.interval|
|flink|linkis.flink.execution.attached| true | execution.attached|
|flink|linkis.flink.kerberos.enable|false  |kerberos.enable|
|flink|linkis.flink.kerberos.login.contexts|Client,KafkaClient  |kerberos.login.contexts|
|flink|linkis.flink.kerberos.login.keytab|   |kerberos.login.keytab|
|flink|linkis.flink.kerberos.login.principal|  |kerberos.login.principal|
|flink|linkis.flink.kerberos.krb5-conf.path|   |kerberos.krb5-conf.path|
|flink|linkis.flink.params.placeholder.blank|\\0x001  |params.placeholder.blank|
|flink|linkis.flink.hudi.enable|false  | hudi.enable|
|flink|linkis.flink.hudi.extra.yarn.classpath|   |hudi.extra.yarn.classpath|
 