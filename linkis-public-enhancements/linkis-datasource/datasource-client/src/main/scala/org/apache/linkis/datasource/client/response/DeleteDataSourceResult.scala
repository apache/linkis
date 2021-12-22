package org.apache.linkis.datasource.client.response

import org.apache.linkis.httpclient.dws.response.DWSResult

import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/datasource/info/(\\S+)")
class DeleteDataSourceResult extends DWSResult{
  @BeanProperty var remove_id: Long = _
}
