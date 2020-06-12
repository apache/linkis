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

package com.webank.wedatasphere.linkis.engine.hive.hook

import com.webank.wedatasphere.linkis.engine.execute.{EngineExecutor, EngineExecutorContext, EngineHook}
import com.webank.wedatasphere.linkis.engine.hive.executor.HiveEngineExecutor
import com.webank.wedatasphere.linkis.server.JMap
import org.apache.commons.lang.StringUtils
import org.slf4j.{Logger, LoggerFactory}

/**
  * created by cooperyang on 2019/4/22
  * Description:
  */
class UseDatabaseEngineHook extends EngineHook{


  private var useDataBaseSql:String = _

  private val USER:String = "user"

  private val logger:Logger = LoggerFactory.getLogger(classOf[UseDatabaseEngineHook])

  override def beforeCreateEngine(params: JMap[String, String]): JMap[String, String] = {
    val user:String = params.get(USER)
    val database = if (StringUtils.isNotEmpty(user)){ user + "_ind" } else { "default" }
    useDataBaseSql = "use " + database
    params
  }


  override def afterCreatedEngine(executor: EngineExecutor): Unit = {
    logger.info(s"hive client begin to run init_code $useDataBaseSql")
    try{
      executor.asInstanceOf[HiveEngineExecutor].executeLine(new EngineExecutorContext(executor), useDataBaseSql)
    }catch{
      case t:Throwable => logger.error(s"hive client runs init sql $useDataBaseSql failed, reason:",t)
    }
  }

}
