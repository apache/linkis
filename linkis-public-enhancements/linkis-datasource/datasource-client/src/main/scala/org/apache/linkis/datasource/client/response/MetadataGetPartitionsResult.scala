package org.apache.linkis.datasource.client.response

import org.apache.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import org.apache.linkis.httpclient.dws.response.DWSResult
import org.apache.linkis.metadatamanager.common.domain.MetaPartitionInfo

import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/metadata/partitions/(\\S+)/db/(\\S+)/table/(\\S+)")
class MetadataGetPartitionsResult extends DWSResult{
  @BeanProperty var props: MetaPartitionInfo = _
}
