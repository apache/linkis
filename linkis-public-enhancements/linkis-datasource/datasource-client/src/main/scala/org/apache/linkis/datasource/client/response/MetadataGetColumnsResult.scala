package org.apache.linkis.datasource.client.response

import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import org.apache.linkis.httpclient.dws.response.DWSResult
import org.apache.linkis.metadatamanager.common.domain.MetaColumnInfo

import java.util
import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/metadata/columns/(\\S+)/db/(\\S+)/table/(\\S+)")
class MetadataGetColumnsResult extends DWSResult{
  @BeanProperty var columns: util.List[java.util.Map[String, Any]] = _

  def getAllColumns: util.List[MetaColumnInfo]={
    import scala.collection.JavaConverters._
    columns.asScala.map(x=>{
      val str = DWSHttpClient.jacksonJson.writeValueAsString(x)
      DWSHttpClient.jacksonJson.readValue(str, classOf[MetaColumnInfo])
    }).asJava
  }
}
