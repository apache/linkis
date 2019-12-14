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
import org.slf4j.LoggerFactory


/**
  * created by cooperyang on 2019/3/18
  * Description:
  */
class HiveAddJarsEngineHook extends EngineHook {

  private var jars:String = _
  private val JARS = "jars"
  private val logger = LoggerFactory.getLogger(classOf[HiveAddJarsEngineHook])
  private val addSql = "add jar "
  override def beforeCreateEngine(params: JMap[String, String]): JMap[String, String] = {
    import scala.collection.JavaConversions._
//    params foreach {
//      case (k, v) => logger.info(s"params key is $k, value is $v")
//    }
    params foreach {
      case (key,value) => if (JARS.equals(key)) jars = value
    }
    logger.info(s"jarArray is {}", jars)
    params
  }

  override def afterCreatedEngine(executor: EngineExecutor): Unit = {
    if (StringUtils.isEmpty(jars)) {
      logger.warn("hive added jars is empty")
      return
    }
    jars.split(",") foreach{
       jar =>
         try{
           logger.info("begin to run hive sql {}", addSql + jar)
           executor.asInstanceOf[HiveEngineExecutor].executeLine(new EngineExecutorContext(executor), addSql + jar)
         }catch{
           case t:Throwable => logger.error(s"run hive sql ${addSql + jar} failed", t)
         }
    }
  }
}
