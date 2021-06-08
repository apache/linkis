package com.webank.wedatasphere.linkis.manager.client.response

import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult
import com.webank.wedatasphere.linkis.manager.client.request.UserAction

@DWSHttpMessageResult("/api/rest_j/v\\d+/linkisManager/stopEngine")
class EngineStopResult extends DWSResult with UserAction{

}
