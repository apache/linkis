package org.apache.linkis.datasource.client.response

import org.apache.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import org.apache.linkis.httpclient.dws.response.DWSResult

import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/datasource/(\\S+)/connect_params")
class GetConnectParamsByDataSourceIdResult extends DWSResult{
  @BeanProperty var connectParams: java.util.Map[String, Any] = _
}
