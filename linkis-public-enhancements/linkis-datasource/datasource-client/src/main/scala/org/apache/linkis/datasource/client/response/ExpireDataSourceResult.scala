package org.apache.linkis.datasource.client.response

import org.apache.linkis.httpclient.dws.response.DWSResult

import scala.beans.BeanProperty

@DWSHttpMessageResult("/api/rest_j/v\\d+/datasource/info/(\\S+)/expire")
class ExpireDataSourceResult extends DWSResult{
    @BeanProperty var expire_id: Long = _
}
