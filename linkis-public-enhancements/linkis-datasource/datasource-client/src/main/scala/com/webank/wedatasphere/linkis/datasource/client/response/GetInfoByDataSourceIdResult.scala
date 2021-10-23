package com.webank.wedatasphere.linkis.datasource.client.response

import com.webank.wedatasphere.linkis.datasource.client.config.DatasourceClientConfig._
import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSource
import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult

import scala.beans.BeanProperty

@DWSHttpMessageResult(s"/api/rest_j/v\\d+/${DATA_SOURCE_SERVICE_MODULE.getValue}/info/(\\S+)")
class GetInfoByDataSourceIdResult extends DWSResult{
  @BeanProperty var info: java.util.Map[String, Any] = _

  def getDataSource: DataSource={
    val str = DWSHttpClient.jacksonJson.writeValueAsString(info)
    DWSHttpClient.jacksonJson.readValue(str, classOf[DataSource])
  }
}
