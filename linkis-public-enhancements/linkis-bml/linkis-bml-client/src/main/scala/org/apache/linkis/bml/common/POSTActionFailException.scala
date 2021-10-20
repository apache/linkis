/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.bml.common

import org.apache.linkis.common.exception.ErrorException

case class POSTActionFailException() extends ErrorException(70025, "material house client request failed (物料库客户端请求失败)"){

}


case class POSTResultNotMatchException() extends ErrorException(70021, "The result returned by the repository client POST request does not match(物料库客户端POST请求返回的result不匹配)")


case class IllegalPathException() extends ErrorException(70035, "The catalog that was passed into the store does not exist or is illegal(传入物料库的目录不存在或非法)")


case class BmlResponseErrorException(errorMessage:String) extends ErrorException(70038, errorMessage)


case class GetResultNotMatchException() extends ErrorException(70078, "The result returned by the repository client GET request does not match(物料库客户端GET请求返回的result不匹配)")

case class BmlClientFailException(errorMsg:String) extends ErrorException(70081, "An error occurred in the material client(物料库客户端出现错误)")