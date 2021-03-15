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

/*
 * created by cooperyang on 2019/07/24.
 */

package com.webank.wedatasphere.linkis.httpclient.dws.request

import com.webank.wedatasphere.linkis.httpclient.dws.request.RestType.RestType

trait DWSHttpAction {

  private var dwsVersion: String = _

  def setDWSVersion(dwsVersion: String): Unit = this.dwsVersion = dwsVersion

  def getPrefixURL: String = "/api/" + getRestType + "/" + dwsVersion

  protected def getRestType: RestType = RestType.JERSEY

  def suffixURLs: Array[String]

  def getURL: String = getPrefixURL + "/" + suffixURLs.mkString("/")

}