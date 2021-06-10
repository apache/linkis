package com.webank.wedatasphere.linkis.manager.client.response

import com.webank.wedatasphere.linkis.manager.client.request.UserAction
import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult

@DWSHttpMessageResult("/api/rest_j/v\\d+/linkisManager/createEngine")
class EngineCreateResult extends DWSResult with UserAction{

}
