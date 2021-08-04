package com.webank.wedatasphere.linkis.computation.client.once.result

import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult

/**
  * Created by enjoyyin on 2021/6/8.
  */
@DWSHttpMessageResult("/api/rest_j/v\\d+/linkisManager/killEngineConn")
class KillEngineConnResult extends LinkisManagerResult