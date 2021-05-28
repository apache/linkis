package com.webank.wedatasphere.linkis.ujes.client.response

import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult

import scala.beans.BeanProperty

/**
 * @author alexyang
 * @date 2020/12/21
 * @description
 */
@DWSHttpMessageResult("/api/rest_j/v\\d+/filesystem/openLog")
class OpenLogResult extends DWSResult {

  /**
   * log[0] - info
   * log[1] - warn
   * log[2] - error
   * log[3] - all (info + warn + error)
   */
  @BeanProperty
  var log: Array[String] = _

}
