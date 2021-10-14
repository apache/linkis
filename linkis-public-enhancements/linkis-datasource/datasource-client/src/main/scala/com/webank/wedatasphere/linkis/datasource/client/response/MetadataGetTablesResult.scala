package com.webank.wedatasphere.linkis.datasource.client.response

import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult

import java.util
import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/metadatamanager/tables/(\\S+)/db/(\\S+)")
class MetadataGetTablesResult extends DWSResult{
  @BeanProperty var tables: util.List[String] = _
}
