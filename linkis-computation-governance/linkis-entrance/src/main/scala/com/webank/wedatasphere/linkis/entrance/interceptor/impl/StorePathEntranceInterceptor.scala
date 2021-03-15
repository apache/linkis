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

package com.webank.wedatasphere.linkis.entrance.interceptor.impl

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.cache.GlobalConfigurationKeyValueCache
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration
import com.webank.wedatasphere.linkis.entrance.interceptor.EntranceInterceptor
import com.webank.wedatasphere.linkis.governance.common.entity.task.RequestPersistTask
import com.webank.wedatasphere.linkis.protocol.task.Task
import org.apache.commons.lang.time.DateFormatUtils


class StorePathEntranceInterceptor extends EntranceInterceptor with Logging {
  /**
    * The apply function is to supplement the information of the incoming parameter task, making the content of this task more complete.
    *   * Additional information includes: database information supplement, custom variable substitution, code check, limit limit, etc.
    * apply函数是对传入参数task进行信息的补充，使得这个task的内容更加完整。
    * 补充的信息包括: 数据库信息补充、自定义变量替换、代码检查、limit限制等
    *
    * @param task
    * @return
    */
  override def apply(task: Task, logAppender: java.lang.StringBuilder): Task = task match {
    case persistTask: RequestPersistTask =>
      val globalConfig = Utils.tryAndWarn(GlobalConfigurationKeyValueCache.getCacheMap(persistTask))
      var parentPath: String = null
      if (null != globalConfig && globalConfig.containsKey(EntranceConfiguration.RESULT_SET_STORE_PATH.key))
        parentPath = EntranceConfiguration.RESULT_SET_STORE_PATH.getValue(globalConfig)
      else {
        parentPath = EntranceConfiguration.RESULT_SET_STORE_PATH.getValue
        if (!parentPath.endsWith("/")) parentPath += "/"
        parentPath += persistTask.getUmUser
      }
      if (!parentPath.endsWith("/")) parentPath += "/dwc/"
      parentPath += DateFormatUtils.format(System.currentTimeMillis, "yyyyMMdd") + "/" +
        persistTask.getRequestApplicationName + "/" + persistTask.getTaskID
      persistTask.setResultLocation(parentPath)
      persistTask
    case _ => task
  }
}
