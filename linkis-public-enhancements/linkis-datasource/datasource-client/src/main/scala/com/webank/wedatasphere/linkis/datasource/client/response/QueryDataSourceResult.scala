package com.webank.wedatasphere.linkis.datasource.client.response

import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSource
import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult

import java.util
import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/datasource/info")
class QueryDataSourceResult extends DWSResult{
  @BeanProperty var query_list: util.List[java.util.Map[String, Any]] = _

  def getAllDataSource: util.List[DataSource]={
    import scala.collection.JavaConverters._
    query_list.asScala.map(x=>{
      val str = DWSHttpClient.jacksonJson.writeValueAsString(x)
      DWSHttpClient.jacksonJson.readValue(str, classOf[DataSource])
    }).asJava
  }
}
