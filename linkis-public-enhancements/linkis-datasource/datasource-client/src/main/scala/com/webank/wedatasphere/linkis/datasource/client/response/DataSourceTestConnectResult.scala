package com.webank.wedatasphere.linkis.datasource.client.response

import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult

import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/datasource/(\\S+)/(\\S+)/op/connect")
class DataSourceTestConnectResult extends DWSResult{
  @BeanProperty var ok: Boolean = _
}
