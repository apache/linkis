package com.webank.wedatasphere.linkis.datasource.client.response

import com.webank.wedatasphere.linkis.datasource.client.config.DatasourceClientConfig.DATA_SOURCE_SERVICE_MODULE
import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.{DataSourceParamKeyDefinition}
import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult

import java.util
import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/datasourcemanager/key_define/type/(\\S+)")
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
