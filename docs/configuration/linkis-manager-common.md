## linkis-manager-common configure


| Module Name (Service Name) | Parameter Name | Default Value | Description |Used|
| -------- | -------- | ----- |----- |  -----   |
|linkis-manager-common|wds.linkis.default.engine.type |spark|engine.type|
|linkis-manager-common|wds.linkis.default.engine.version |3.2.1|engine.version|
|linkis-manager-common|wds.linkis.manager.admin|hadoop|manager.admin|
|linkis-manager-common|wds.linkis.rm.application.name|ResourceManager|rm.application.name|
|linkis-manager-common|wds.linkis.rm.wait.event.time.out| 1000 * 60 * 12L |event.time.out|
|linkis-manager-common|wds.linkis.rm.register.interval.time|1000 * 60 * 2L |interval.time|
|linkis-manager-common|wds.linkis.manager.am.node.heartbeat| 3m|node.heartbeat|
|linkis-manager-common|wds.linkis.manager.rm.lock.release.timeou|5m| lock.release.timeou|
|linkis-manager-common|wds.linkis.manager.rm.lock.release.check.interval| 5m |release.check.interval|
|linkis-manager-common|wds.linkis.rm.client.core.max| 10|client.core.max|
|linkis-manager-common|wds.linkis.rm.client.memory.max|20g|client.memory.max|
|linkis-manager-common|wds.linkis.rm.instance|10|rm.instance|
|linkis-manager-common|wds.linkis.rm.yarnqueue.cores.max|150|yarnqueue.cores.max |
|linkis-manager-common|wds.linkis.rm.yarnqueue.memory.max| 450g| memory.max|
|linkis-manager-common|wds.linkis.rm.yarnqueue.instance.max|30| yarnqueue.instance.max|
|linkis-manager-common|wds.linkis.rm.yarnqueue| default |rm.yarnqueue|
|linkis-manager-common|wds.linkis.rm.cluster| default|rm.cluster|
|linkis-manager-common|wds.linkis.rm.user.module.wait.used|60 * 10L|module.wait.used|
|linkis-manager-common|wds.linkis.rm.user.module.wait.used| -1L |module.wait.used|
|linkis-manager-common|wds.linkis.rm.module.completed.scan.interval| 10000L|module.completed.scan.interval|
|linkis-manager-common|wds.linkis.rm.engine.scan.interval|120000L|engine.scan.interval|
|linkis-manager-common|wds.linkis.rm.engine.release.threshold| 120000L| release.threshold|
|linkis-manager-common|wds.linkis.rm.alert.system.id|5136| alert.system.id|
|linkis-manager-common|wds.linkis.rm.alert.default.um| hadoop |alert.default.um|
|linkis-manager-common|wds.linkis.rm.alert.ims.url| 127.0.0.1|alert.ims.url|
|linkis-manager-common|wds.linkis.rm.alert.duplication.interval|1200L|alert.duplication.interval|
|linkis-manager-common|wds.linkis.rm.alert.contact.group| q01/hadoop,q02/hadoop |alert.contact.group|
|linkis-manager-common|wds.linkis.rm.alert.enabled| false|alert.enabled|
|linkis-manager-common|wds.linkis.rm.alert.default.contact| hadoop|alert.default.contact||
|linkis-manager-common|wds.linkis.hive.maintain.time.key|wds.linkis.hive.maintain.time|hive.maintain.time.key|
|linkis-manager-common|wds.linkis.rm.default.yarn.cluster.name| default |yarn.cluster.name|
|linkis-manager-common|wds.linkis.rm.default.yarn.cluster.type| Yarn|yarn.cluster.type|
|linkis-manager-common|wds.linkis.rm.external.retry.num|3|external.retry.num|
|linkis-manager-common|wds.linkis.rm.default.yarn.webaddress.delimiter| ; | yarn.webaddress.delimiter|
