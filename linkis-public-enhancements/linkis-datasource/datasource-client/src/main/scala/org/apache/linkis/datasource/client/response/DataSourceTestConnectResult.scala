package org.apache.linkis.datasource.client.response

import org.apache.linkis.httpclient.dws.response.DWSResult

import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/datasource/(\\S+)/(\\S+)/op/connect")
class DataSourceTestConnectResult extends DWSResult{
  @BeanProperty var ok: Boolean = _
}
