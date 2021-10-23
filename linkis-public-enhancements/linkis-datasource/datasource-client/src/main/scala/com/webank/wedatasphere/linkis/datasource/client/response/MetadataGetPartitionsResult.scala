package com.webank.wedatasphere.linkis.datasource.client.response

import com.webank.wedatasphere.linkis.datasource.client.config.DatasourceClientConfig._
import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult
import com.webank.wedatasphere.linkis.metadatamanager.common.domain.MetaPartitionInfo

import scala.beans.BeanProperty

@DWSHttpMessageResult(s"/api/rest_j/v\\d+/${METADATA_SERVICE_MODULE.getValue}/partitions/(\\S+)/db/(\\S+)/table/(\\S+)")
class MetadataGetPartitionsResult extends DWSResult{
  @BeanProperty var props: MetaPartitionInfo = _
}
