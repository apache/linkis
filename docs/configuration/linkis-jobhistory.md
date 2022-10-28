## linkis-jobhistory configure


| Module Name (Service Name) | Parameter Name | Default Value | Description |Used|
| -------- | -------- | ----- |----- |  -----   |
|linkis-jobhistory|wds.linkis.jobhistory.safe.trigger |true|jobhistory.safe.trigger|
|linkis-jobhistory|wds.linkis.entrance.spring.name| linkis-cg-entrance |entrance.spring.name|
|linkis-jobhistory|wds.linkis.jobhistory.instance.delemiter|;|jobhistory.instance.delemiter|
|linkis-jobhistory|wds.linkis.jobhistory.update.retry.times| 3|jobhistory.update.retry.times|
|linkis-jobhistory|wds.linkis.jobhistory.update.retry.interval|3 * 1000 |jobhistory.update.retry.interval|
|linkis-jobhistory|wds.linkis.jobhistory.undone.job.minimum.id| 0L|jobhistory.undone.job.minimum.id  |
|linkis-jobhistory|wds.linkis.jobhistory.undone.job.refreshtime.daily|00:15| jobhistory.undone.job.refreshtime.daily|
|linkis-jobhistory|wds.linkis.query.store.prefix| hdfs:///apps-data/bdp-ide/ |query.store.prefix|
|linkis-jobhistory|wds.linkis.query.store.prefix.viewfs| hdfs:///apps-data/ |query.store.prefix.viewfs |
|linkis-jobhistory|wds.linkis.env.is.viewfs| true|env.is.viewfs|
|linkis-jobhistory|wds.linkis.query.store.suffix| |linkis.query.store.suffix|
|linkis-jobhistory|wds.linkis.query.code.store.length|50000| query.code.store.length|
