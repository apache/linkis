/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.errorcode.client.result

import java.util

import com.webank.wedatasphere.linkis.errorcode.common.{CommonConf, LinkisErrorCode}
import com.webank.wedatasphere.linkis.httpclient.dws.annotation.DWSHttpMessageResult
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult


abstract class ErrorCodeResult extends DWSResult


@DWSHttpMessageResult("/api/rest_j/v\\d+/errorcode/getAllErrorCodes")
class ErrorCodeGetAllResult extends ErrorCodeResult{


  private var errorCodes:java.util.List[LinkisErrorCode] = new util.ArrayList[LinkisErrorCode]()
  def getErrorCodes:java.util.List[LinkisErrorCode] = errorCodes
  def setErrorCodes(errorCodes:java.util.List[util.LinkedHashMap[String, Object]]):Unit = {
    import scala.collection.JavaConversions._
    errorCodes.foreach(map =>{
      val errorCode = map.get("errorCode").asInstanceOf[String]
      val errorDesc = map.get("errorDesc").asInstanceOf[String]
      val errorType = map.get("errorType").asInstanceOf[Int]
      val errorRegexStr = map.get("errorRegexStr").asInstanceOf[String]
      this.errorCodes.add(new LinkisErrorCode(errorCode, errorDesc, errorRegexStr, errorType))
    })
  }
}



