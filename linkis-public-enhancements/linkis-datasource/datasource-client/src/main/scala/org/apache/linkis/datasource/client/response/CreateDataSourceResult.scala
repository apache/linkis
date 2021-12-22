package org.apache.linkis.datasource.client.response

import org.apache.linkis.httpclient.dws.response.DWSResult

import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/datasource/info/json")
class CreateDataSourceResult extends DWSResult{
  @BeanProperty var insert_id: Long = _
}
