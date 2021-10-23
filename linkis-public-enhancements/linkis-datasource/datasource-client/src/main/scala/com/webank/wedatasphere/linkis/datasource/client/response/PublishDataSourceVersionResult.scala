package com.webank.wedatasphere.linkis.datasource.client.response

import com.webank.wedatasphere.linkis.datasource.client.config.DatasourceClientConfig._
import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult

@DWSHttpMessageResult(s"/api/rest_j/v\\d+/${DATA_SOURCE_SERVICE_MODULE.getValue}/publish/(\\S+)/(\\S+)")
class PublishDataSourceVersionResult extends DWSResult{

}
