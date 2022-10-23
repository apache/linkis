## linkis-orchestrator-core configure


| Module Name (Service Name) | Parameter Name | Default Value | Description |Used|
| -------- | -------- | ----- |----- |  -----   |
|linkis-orchestrator-core|wds.linkis.orchestrator.builder.class | |orchestrator.builder.class|
|linkis-orchestrator-core|wds.linkis.orchestrator.version|1.0.0|orchestrator.version|
|linkis-orchestrator-core|wds.linkis.orchestrator.listener.async.queue.size.max|300|orchestrator.listener.async.queue.size.max|
|linkis-orchestrator-core|wds.linkis.orchestrator.listener.async.consumer.thread.max| 5|orchestrator.listener.async.consumer.thread.max|
|linkis-orchestrator-core|wds.linkis.orchestrator.listener.async.consumer.freetime.max|5000ms |orchestrator.listener.async.consumer.freetime.max|
|linkis-orchestrator-core|wds.linkis.orchestrator.executor.thread.max| 20|orchestrator.executor.thread.max  |
|linkis-orchestrator-core|wds.linkis.task.scheduler.clear.time|1m| task.scheduler.clear.time |
|linkis-orchestrator-core|wds.linkis.orchestrator.execution.task.max.parallelism| 5 |orchestrator.execution.task.max.parallelism|
|linkis-orchestrator-core|wds.linkis.orchestrator.execution.task.runner.max.size| 200|orchestrator.execution.task.runner.max.size|
|linkis-orchestrator-core|wds.linkis.orchestrator.exec.task.runner.factory.class| |orchestrator.exec.task.runner.factory.class|
|linkis-orchestrator-core|wds.linkis.orchestrator.task.persist.wait.max|5m|orchestrator.task.persist.wait.max|
|linkis-orchestrator-core|wds.linkis.orchestrator.task.retry.wait.time|30000| orchestrator.task.retry.wait.time |
|linkis-orchestrator-core|wds.linkis.computation.orchestrator.retry.max.age| 10| orchestrator.retry.max.age|
|linkis-orchestrator-core|wds.linkis.orchestrator.task.scheduler.retry.wait.time|100000| orchestrator.task.scheduler.retry.wait.time|
|linkis-orchestrator-core|wds.linkis.orchestrator.task.scheduler.thread.pool| 200 |orchestrator.task.scheduler.thread.pool|
|linkis-orchestrator-core|wds.linkis.orchestrator.execution.factory.class| org.apache.linkis.orchestrator.code.plans.execution.CodeExecutionFactory|orchestrator.execution.factory.class|
|linkis-orchestrator-core|wds.linkis.orchestrator.task.consumer.wait|500|orchestrator.task.consumer.wait|
|linkis-orchestrator-core|wds.linkis.task.user.max.running| 5 |task.user.max.running|
|linkis-orchestrator-core|wds.linkis.orchestrator.task.schedulis.creator| schedulis,nodeexecution|task.schedulis.creator|
|linkis-orchestrator-core|wds.linkis.orchestrator.metric.log.enable|true|orchestrator.metric.log.enable|
|linkis-orchestrator-core|wds.linkis.orchestrator.metric.log.time| 1h |orchestrator.metric.log.time|
