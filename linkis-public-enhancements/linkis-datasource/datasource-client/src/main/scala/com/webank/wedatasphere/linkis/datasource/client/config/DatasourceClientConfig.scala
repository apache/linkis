package com.webank.wedatasphere.linkis.datasource.client.config

import com.webank.wedatasphere.linkis.common.conf.CommonVars

object DatasourceClientConfig {
  var METADATA_SERVICE_MODULE: CommonVars[String] = CommonVars.apply("wds.linkis.server.mdm.module.name", "metadatamanager")

  var DATA_SOURCE_SERVICE_MODULE: CommonVars[String] = CommonVars.apply("wds.linkis.server.dsm.module.name", "datasourcemanager")

}
