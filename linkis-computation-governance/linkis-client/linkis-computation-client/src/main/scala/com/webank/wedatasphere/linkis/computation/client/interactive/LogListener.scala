package com.webank.wedatasphere.linkis.computation.client.interactive

import java.util

/**
  * Created by enjoyyin on 2021/6/1.
  */
trait LogListener {

  def onLogUpdate(logs: util.List[String]): Unit

}
