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
package com.webank.wedatasphere.linkis.bml.common

import com.webank.wedatasphere.linkis.common.exception.ErrorException

/**
  * created by cooperyang on 2019/5/26
  * Description:
  */
case class POSTActionFailException() extends ErrorException(70025, "物料库客户端请求失败"){

}


case class POSTResultNotMatchException() extends ErrorException(70021, "物料库客户端POST请求返回的result不匹配")


case class IllegalPathException() extends ErrorException(70035, "传入物料库的目录不存在或非法")


case class BmlResponseErrorException(errorMessage:String) extends ErrorException(70038, errorMessage)


case class GetResultNotMatchException() extends ErrorException(70078, "物料库客户端get请求返回的result不匹配")

case class BmlClientFailException(errorMsg:String) extends ErrorException(70081, "物料库客户端出现错误")