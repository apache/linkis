package org.apache.linkis.datasource.client.response

import org.apache.linkis.httpclient.dws.response.DWSResult

import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/datasource/parameter/(\\S+)/json")
class UpdateDataSourceParameterResult extends DWSResult{
  @BeanProperty var version: Long = _
}
