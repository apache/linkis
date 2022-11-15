## linkis-application-manager configure


| Module Name (Service Name) | Parameter Name | Default Value | Description |Used|
| -------- | -------- | ----- |----- |  -----   |
|linkis-application-manager|wds.linkis.label.node.long.lived.label.keys |tenant|lived.label.keys|
|linkis-application-manager|wds.linkis.governance.admin.operations|  |admin.operations|
|linkis-application-manager|wds.linkis.manager.am.engine.start.max.time|11m|start.max.time|
|linkis-application-manager|wds.linkis.manager.am.engine.rest.start.max.time| 40s|start.max.time|
|linkis-application-manager|wds.linkis.manager.am.engine.reuse.max.time|5m |engine.reuse.max.time|
|linkis-application-manager|wds.linkis.manager.am.engine.reuse.count.limit| 2|reuse.count.limit  |
|linkis-application-manager|wds.linkis.manager.am.node.heartbeat|5m| node.heartbeat|
|linkis-application-manager|wds.linkis.manager.am.default.node.owner| hadoop |node.owner|
|linkis-application-manager|wds.linkis.manager.am.stop.engine.wait| 5m| stop.engine.wait |
|linkis-application-manager|wds.linkis.manager.am.stop.em.wait| 5m|stop.em.wait|
|linkis-application-manager|wds.linkis.manager.am.em.label.init.wait|5m|label.init.wait|
|linkis-application-manager|wds.linkis.manager.am.em.new.wait.mills|1000 * 60L| wait.mills|
|linkis-application-manager|wds.linkis.engineconn.debug.mode.enable|jdbc,es,presto,io_file,appconn,openlookeng,trino| debug.mode.enable|
|linkis-application-manager|wds.linkis.multi.user.engine.user|   |engine.user|
|linkis-application-manager|wds.linkis.manager.am.engine.locker.max.time|  1000 * 60 * 5|locker.max.time|
|linkis-application-manager|wds.linkis.manager.am.can.retry.logs|already in use;Cannot allocate memory|retry.logs|
|linkis-application-manager|wds.linkis.ecm.launch.max.thread.size| 200 |thread.size|
|linkis-application-manager|wds.linkis.async.stop.engine.size| 20|engine.size|
|linkis-application-manager|wds.linkis.ec.maintain.time.key|   |maintain.time.key|
|linkis-application-manager|wds.linkis.ec.maintain.time.work.start.time| 8|work.start.time|
|linkis-application-manager|wds.linkis.ec.maintain.time.work.end.time|19|maintain.time.work.end.time|
linkis-application-manager|wds.linkis.manager.am.node.create.time|12m| node.create.time|
|linkis-application-manager|wds.linkis.manager.am.node.heartbeat|  12m |node.heartbeat|
|linkis-application-manager|wds.linkis.manager.am.engine.kill.timeout| 2m |engine.kill.timeout|
|linkis-application-manager|wds.linkis.manager.am.em.kill.timeout| 2m|em.kill.timeout|
|linkis-application-manager|wds.linkis.manager.monitor.async.poll.size| 5 |async.poll.size|
|linkis-application-manager|wds.linkis.manager.am.ecm.heartbeat| 1m|am.ecm.heartbeat|
|linkis-application-manager|wds.linkis.manager.rm.kill.engine.wait|30s   |kill.engine.wait|
|linkis-application-manager|wds.linkis.manager.rm.request.enable| true|request.enable|
|linkis-application-manager|wds.linkis.manager.rm.lock.wait|5 * 60 * 1000|rm.lock.wait|
|linkis-application-manager|wds.linkis.manager.rm.debug.enable|  false |rm.debug.enable|
|linkis-application-manager|wds.linkis.manager.rm.debug.log.path| file:///tmp/linkis/rmLog" |rm.debug.log.path|
|linkis-application-manager|wds.linkis.manager.rm.external.resource.regresh.time| 30m|regresh.time|
|linkis-application-manager|wds.linkis.configuration.engine.type|   |engine.type|
|linkis-application-manager|wds.linkis.manager.rm.resource.action.record| true|resource.action.record|

