package com.webank.wedatasphere.linkis.datasource.client.response

import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.{DataSource, DatasourceVersion}
import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult

import java.util
import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/datasourcesmanager/(\\S+)/versions")
class GetDataSourceVersionsResult extends DWSResult{
  @BeanProperty var versions: util.List[java.util.Map[String, Any]] = _

  def getDatasourceVersion: util.List[DatasourceVersion]={
    import scala.collection.JavaConverters._
    versions.asScala.map(x=>{
      val str = DWSHttpClient.jacksonJson.writeValueAsString(x)
      DWSHttpClient.jacksonJson.readValue(str, classOf[DatasourceVersion])
    }).asJava
  }
}
