## linkis-storage configure


| Module Name (Service Name) | Parameter Name | Default Value | Description |Used|
| -------- | -------- | ----- |----- |  -----   |
|linkis-storage|wds.linkis.storage.proxy.user| | storage.proxy.user |true|
|linkis-storage|wds.linkis.storage.root.user| hadoop |storage.root.user|true|
|linkis-storage|wds.linkis.storage.hdfs.root.user| hadoop |hdfs.root.user|true|
|linkis-storage|wds.linkis.storage.local.root.user| root | storage.local.root.user  |true|
|linkis-storage|wds.linkis.storage.fileSystem.group|bdap |fileSystem.group|true|
|linkis-storage|wds.linkis.storage.rs.file.type | utf-8 |file.type |true|
|linkis-storage|wds.linkis.storage.rs.file.suffix| .dolphin_ | file.suffix  |true|
|linkis-storage|wds.linkis.storage.result.set.package|org.apache.linkis.storage.resultset |package|true|
|linkis-storage|wds.linkis.storage.result.set.classes| txt.TextResultSet,table.TableResultSet,io.IOResultSet,html.HtmlResultSet,picture.PictureResultSet  | classes |true|
|linkis-storage|wds.linkis.storage.build.fs.classes| org.apache.linkis.storage.factory.impl.BuildHDFSFileSystem,org.apache.linkis.storage.factory.impl.BuildLocalFileSystem |classes|true|
|linkis-storage|wds.linkis.storage.is.share.node|true |share.node|true|
|linkis-storage|wds.linkis.storage.enable.io.proxy|false| proxy |true|
|linkis-storage|wds.linkis.storage.io.user|root |io.user |true|
|linkis-storage|wds.linkis.storage.io.fs.num| 1000 * 60 * 10   |fs.num  |true|
|linkis-storage|wds.linkis.storage.io.read.fetch.size|  100k |fetch.size |true|
|linkis-storage|wds.linkis.storage.io.write.cache.size |64k | cache.size  |true|
|linkis-storage|wds.linkis.storage.io.default.creator|  IDE| default.creator |true|
|linkis-storage|wds.linkis.storage.io.fs.re.init|re-init |re.init|false|
|linkis-storage|wds.linkis.storage.io.init.retry.limit| 10  | limit |true|
|linkis-storage|wds.linkis.storage.fileSystem.hdfs.group| hadoop |hdfs.group|false|
|linkis-storage|wds.linkis.double.fraction.length| 30|fraction.length|true|
|linkis-storage|wds.linkis.storage.hdfs.prefix_check.enable| true | check.enable|true|
|linkis-storage|wds.linkis.storage.hdfs.prefxi.remove| true |prefxi.remove|true|
|linkis-storage|wds.linkis.fs.hdfs.impl.disable.cache|  false |disable.cache |true|
|linkis-storage|wds.linkis.hdfs.rest.errs|  |rest.errs|true|
|linkis-storage|wds.linkis.resultset.row.max.str | 2m  | max.str |true|
|linkis-storage|wds.linkis.storage.file.type | dolphin,sql,scala,py,hql,python,out,log,text,sh,jdbc,ngql,psql,fql,tsql | file.type |true|
