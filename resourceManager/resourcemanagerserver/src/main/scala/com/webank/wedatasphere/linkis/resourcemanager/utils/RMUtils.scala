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

package com.webank.wedatasphere.linkis.resourcemanager.utils

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}

/**
  * Created by shanhuang on 9/11/18.
  */
object RMUtils extends Logging {

  def getClassInstance[T](className: String): T = Utils.tryThrow(
    Thread.currentThread.getContextClassLoader.loadClass(className).asInstanceOf[Class[T]].newInstance())(t => {
    error(s"Failed to instance: $className ", t)
    throw t
  })

  def buildLock(value: String): String = value.intern()
}
