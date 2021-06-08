package com.webank.wedatasphere.linkis.manager.client.response

import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult
import com.webank.wedatasphere.linkis.manager.client.request.UserAction

trait LinkisManagerResult extends DWSResult with UserAction{

}
