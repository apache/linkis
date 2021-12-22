package org.apache.linkis.datasource.client.config

import org.apache.linkis.common.conf.CommonVars

object DatasourceClientConfig {
  var METADATA_SERVICE_MODULE: CommonVars[String] = CommonVars.apply("wds.linkis.server.mdm.module.name", "metadata")

  var DATA_SOURCE_SERVICE_MODULE: CommonVars[String] = CommonVars.apply("wds.linkis.server.dsm.module.name", "datasource")
}
