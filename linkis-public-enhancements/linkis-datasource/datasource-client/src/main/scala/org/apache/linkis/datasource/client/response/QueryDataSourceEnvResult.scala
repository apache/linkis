package org.apache.linkis.datasource.client.response

import org.apache.linkis.datasourcemanager.common.domain.DataSourceEnv
import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import org.apache.linkis.httpclient.dws.response.DWSResult

import java.util
import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/datasource/env")
class QueryDataSourceEnvResult extends DWSResult{
  @BeanProperty var query_list: java.util.List[java.util.Map[String, Any]] = _

  def getDataSourceEnv: util.List[DataSourceEnv]={
    import scala.collection.JavaConverters._

    query_list.asScala.map(x=>{
      val str = DWSHttpClient.jacksonJson.writeValueAsString(x)
      DWSHttpClient.jacksonJson.readValue(str, classOf[DataSourceEnv])
    }).asJava
  }
}
