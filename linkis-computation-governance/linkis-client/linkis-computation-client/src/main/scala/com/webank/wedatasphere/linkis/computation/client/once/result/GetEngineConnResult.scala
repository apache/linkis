package com.webank.wedatasphere.linkis.computation.client.once.result

import java.util

import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult

/**
  * Created by enjoyyin on 2021/6/8.
  */
@DWSHttpMessageResult("/api/rest_j/v\\d+/linkisManager/getEngineConn")
class GetEngineConnResult extends LinkisManagerResult {

  private var engineConnNode: util.Map[String, Any] = _

  def setEngine(engine: util.Map[String, Any]): Unit = {
    this.engineConnNode = engine
  }

  def getNodeInfo: util.Map[String, Any] = engineConnNode

}
