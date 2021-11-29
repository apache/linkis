package org.apache.linkis.datasource.client.response

import org.apache.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import org.apache.linkis.httpclient.dws.response.DWSResult

import java.util
import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/metadata/tables/(\\S+)/db/(\\S+)")
class MetadataGetTablesResult extends DWSResult{
  @BeanProperty var tables: util.List[String] = _
}
