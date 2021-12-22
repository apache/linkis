package org.apache.linkis.datasource.client.response

import org.apache.linkis.httpclient.dws.response.DWSResult

@DWSHttpMessageResult("/api/rest_j/v\\d+/datasource/publish/(\\S+)/(\\S+)")
class PublishDataSourceVersionResult extends DWSResult{

}
