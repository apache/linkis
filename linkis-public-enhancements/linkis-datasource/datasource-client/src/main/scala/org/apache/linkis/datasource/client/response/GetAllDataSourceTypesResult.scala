package org.apache.linkis.datasource.client.response

import org.apache.linkis.datasourcemanager.common.domain.DataSourceType
import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import org.apache.linkis.httpclient.dws.response.DWSResult

import java.util
import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/datasource/type/all")
class GetAllDataSourceTypesResult extends DWSResult{
  @BeanProperty var type_list: java.util.List[java.util.Map[String, Any]] = _

  def getAllDataSourceType: util.List[DataSourceType]={
    import scala.collection.JavaConverters._

    type_list.asScala.map(x=>{
      val str = DWSHttpClient.jacksonJson.writeValueAsString(x)
      DWSHttpClient.jacksonJson.readValue(str, classOf[DataSourceType])
    }).asJava
  }
}
