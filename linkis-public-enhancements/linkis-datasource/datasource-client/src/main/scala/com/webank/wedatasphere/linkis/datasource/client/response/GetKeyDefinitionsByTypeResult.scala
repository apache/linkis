package com.webank.wedatasphere.linkis.datasource.client.response

import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition
import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult

import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/datasource/env/type/(\\S+)")
class GetKeyDefinitionsByTypeResult extends DWSResult{
  @BeanProperty var key_define: List[DataSourceParamKeyDefinition] = _
}
