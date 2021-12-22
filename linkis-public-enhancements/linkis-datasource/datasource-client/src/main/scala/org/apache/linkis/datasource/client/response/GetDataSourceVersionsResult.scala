package org.apache.linkis.datasource.client.response

import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.dws.response.DWSResult

import java.util
import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/datasource/(\\S+)/versions")
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
