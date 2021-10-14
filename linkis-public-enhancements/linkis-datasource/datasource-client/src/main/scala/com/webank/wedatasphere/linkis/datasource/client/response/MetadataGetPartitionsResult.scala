package com.webank.wedatasphere.linkis.datasource.client.response

import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult
import com.webank.wedatasphere.linkis.metadatamanager.common.domain.MetaPartitionInfo

import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/metadatamanager/partitions/(\\S+)/db/(\\S+)/table/(\\S+)")
class MetadataGetPartitionsResult extends DWSResult{
  @BeanProperty var props: MetaPartitionInfo = _
}
