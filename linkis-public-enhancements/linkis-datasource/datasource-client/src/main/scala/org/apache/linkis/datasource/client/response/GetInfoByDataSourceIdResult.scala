package org.apache.linkis.datasource.client.response

import org.apache.linkis.datasourcemanager.common.domain.DataSource
import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import org.apache.linkis.httpclient.dws.response.DWSResult

import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/datasource/info/(\\S+)")
class GetInfoByDataSourceIdResult extends DWSResult{
  @BeanProperty var info: java.util.Map[String, Any] = _

  def getDataSource: DataSource={
    val str = DWSHttpClient.jacksonJson.writeValueAsString(info)
    DWSHttpClient.jacksonJson.readValue(str, classOf[DataSource])
  }
}
