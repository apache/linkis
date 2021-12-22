package org.apache.linkis.datasource.client.response

import org.apache.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition
import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.dws.response.DWSResult

import java.util
import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/datasource/key_define/type/(\\S+)")
class GetKeyTypeDatasourceResult extends DWSResult{
  @BeanProperty var key_define: util.List[java.util.Map[String, Any]] = _

  def getDataSourceParamKeyDefinitions: util.List[DataSourceParamKeyDefinition] ={
    import scala.collection.JavaConverters._
    key_define.asScala.map(x=>{
      val str = DWSHttpClient.jacksonJson.writeValueAsString(x)
      DWSHttpClient.jacksonJson.readValue(str, classOf[DataSourceParamKeyDefinition])
    }).asJava
  }
}
