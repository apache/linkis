## spark configure


| Module Name (Service Name) | Parameter Name | Default Value | Description |Used|
| -------- | -------- | ----- |----- |  -----   |
|spark|linkis.spark.etl.support.hudi|false|spark.etl.support.hudi|
|spark|linkis.bgservice.store.prefix|hdfs:///tmp/bdp-ide/|bgservice.store.prefix|
|spark|linkis.bgservice.store.suffix|  |bgservice.store.suffix|
|spark|wds.linkis.dolphin.decimal.precision|32 |dolphin.decimal.precision|
|spark|wds.linkis.dolphin.decimal.scale| 10 | dolphin.decimal.scale|
|spark|wds.linkis.park.extension.max.pool|2 |extension.max.pool|
|spark|wds.linkis.process.threadpool.max| 100|process.threadpool.max  |
|spark|wds.linkis.engine.spark.session.hook| | spark.session.hook|
|spark|wds.linkis.engine.spark.spark-loop.init.time| 120s |spark.spark-loop.init.time|
|spark|wds.linkis.engine.spark.language-repl.init.time| 30s| spark.language-repl.init.time |
|spark|wds.linkis.spark.sparksubmit.path| spark-submit|spark.sparksubmit.path|
|spark|wds.linkis.spark.output.line.limit|10| spark.output.line.limit|
|spark|wds.linkis.spark.useHiveContext|true| spark.useHiveContext |
|spark|wds.linkis.enginemanager.core.jar|  | enginemanager.core.jar|
|spark|wds.linkis.ecp.spark.default.jar|linkis-engineconn-core-1.2.0.jar|spark.default.jar|
|spark|wds.linkis.dws.ujes.spark.extension.timeout| 3000L |spark.extension.timeout|
|spark|wds.linkis.engine.spark.fraction.length| 30 |spark.fraction.length|
|spark|wds.linkis.show.df.max.res| |show.df.max.res|
|spark|wds.linkis.mdq.application.name| linkis-ps-datasource |mdq.application.name||
|spark|wds.linkis.dolphin.limit.len| 5000|dolphin.limit.len|
|spark|wds.linkis.spark.engine.is.viewfs.env| true | spark.engine.is.viewfs.env|
|spark|wds.linkis.spark.engineconn.fatal.log|error writing class;OutOfMemoryError|spark.engineconn.fatal.log|
|spark|wds.linkis.spark.engine.scala.replace_package_header.enable| true |spark.engine.scala.replace_package_header.enable|


The spark-excel package may cause class conflicts,need to download separately,put it in spark lib
wget https://repo1.maven.org/maven2/com/crealytics/spark-excel-2.12.17-3.2.2_2.12/3.2.2_0.18.1/spark-excel-2.12.17-3.2.2_2.12-3.2.2_0.18.1.jar
cp spark-excel-2.12.17-3.2.2_2.12-3.2.2_0.18.1.jar {LINKIS_HOME}/lib/linkis-engineconn-plugins/spark/dist/3.2.1/lib

spark3 is not supported by native rocketmq-spark, and the source code needs to be modified, which can be downloaded directly from the link below
https://github.com/ChengJie1053/spark3-rocketmq-connector-jar