package com.webank.wedatasphere.linkis.computation.client.once.result

import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult
import com.webank.wedatasphere.linkis.ujes.client.request.UserAction

/**
  * Created by enjoyyin on 2021/6/8.
  */
trait LinkisManagerResult extends DWSResult with UserAction
