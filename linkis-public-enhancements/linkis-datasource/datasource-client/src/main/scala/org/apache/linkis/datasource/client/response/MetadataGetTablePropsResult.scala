package org.apache.linkis.datasource.client.response

import org.apache.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import org.apache.linkis.httpclient.dws.response.DWSResult

import java.util
import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/metadata/props/(\\S+)/db/(\\S+)/table/(\\S+)")
class MetadataGetTablePropsResult extends DWSResult{
  @BeanProperty var props: util.Map[String, String] = _
}
