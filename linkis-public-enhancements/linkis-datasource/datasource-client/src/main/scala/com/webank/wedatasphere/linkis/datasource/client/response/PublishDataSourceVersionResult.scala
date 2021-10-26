package com.webank.wedatasphere.linkis.datasource.client.response

import com.webank.wedatasphere.linkis.datasource.client.config.DatasourceClientConfig._
import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult

@DWSHttpMessageResult("/api/rest_j/v\\d+/datasourcemanager/publish/(\\S+)/(\\S+)")
class PublishDataSourceVersionResult extends DWSResult{

}
