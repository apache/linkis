package com.webank.wedatasphere.linkis.datasource.client.response


import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult
import com.webank.wedatasphere.linkis.metadatamanager.common.domain.MetaColumnInfo

import java.util
import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/metadatamanager/columns/(\\S+)/db/(\\S+)/table/(\\S+)")
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
