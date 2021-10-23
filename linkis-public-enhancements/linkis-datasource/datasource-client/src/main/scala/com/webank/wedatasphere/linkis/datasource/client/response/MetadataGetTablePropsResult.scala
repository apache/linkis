package com.webank.wedatasphere.linkis.datasource.client.response

import com.webank.wedatasphere.linkis.datasource.client.config.DatasourceClientConfig._
import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult

import java.util
import scala.beans.BeanProperty

@DWSHttpMessageResult(s"/api/rest_j/v\\d+/${METADATA_SERVICE_MODULE.getValue}/props/(\\S+)/db/(\\S+)/table/(\\S+)")
class MetadataGetTablePropsResult extends DWSResult{
  @BeanProperty var props: util.Map[String, String] = _
}
