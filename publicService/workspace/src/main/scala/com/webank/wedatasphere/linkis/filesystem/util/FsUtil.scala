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

package com.webank.wedatasphere.linkis.filesystem.util

import java.util
import java.util.concurrent.{Executors, LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

import com.webank.wedatasphere.linkis.common.conf.CommonVars
import com.webank.wedatasphere.linkis.storage.script.Variable

import scala.collection.mutable.ArrayBuffer

/**
  * Created by johnnwang on 2019/2/25.
  */
object FsUtil {

  val FILESYSTEM_GET_TIMEOUT = CommonVars("wds.linkis.workspace.filesystem.get.timeout",2000)
  val FILESYSTEM_FS_THREAD_NUM = CommonVars("wds.linkis.workspace.filesystem.thread.num",10)
  val FILESYSTEM_FS_THREAD_CACHE = CommonVars("wds.linkis.workspace.filesystem.thread.cache",1000)
  //val executorService = Executors.newFixedThreadPool(FILESYSTEM_FS_THREAD_NUM.getValue)
  val executorService =
  new ThreadPoolExecutor(FILESYSTEM_FS_THREAD_NUM.getValue,
    FILESYSTEM_FS_THREAD_NUM.getValue, 0L,
    TimeUnit.MILLISECONDS, new LinkedBlockingQueue[Runnable](FILESYSTEM_FS_THREAD_CACHE.getValue))

  @Deprecated
  def getVariables(variable: java.util.LinkedHashMap[String, Object],configuration:java.util.LinkedHashMap[String, Object]): Array[Variable] = {
    import scala.collection.JavaConversions._
    val variables = new ArrayBuffer[Variable]
    if(variable != null) variable foreach {
      case (k, v) => {
          variables += new Variable("variable", null, k, v.toString)
      }
    }
    if(configuration !=null) configuration foreach {
      case (k, v) => {
          v.asInstanceOf[java.util.HashMap[String, String]] foreach {
            case (ck, cv) => variables += new Variable("configuration", k, ck, cv)
          }
      }
    }
    variables.toArray
  }

  @Deprecated
  def getParams(variables :Array[Variable]):java.util.Map[String,Object] = {
    val map = new util.HashMap[String,Object]
    val variableMap = new util.HashMap[String,String]
    val configMap = new util.HashMap[String,Object]
    val startMap = new util.HashMap[String,String]
    val runMap = new util.HashMap[String,String]
    val specialMap = new util.HashMap[String,String]
    variables.filter(_.sortParent.equals("variable")).foreach(f => variableMap.put(f.key,f.value))
    val configuration = variables.filter(_.sortParent.equals("configuration"))
    configuration.filter(_.sort.equals("startup")).foreach(f=>startMap.put(f.key,f.value))
    configuration.filter(_.sort.equals("runtime")).foreach(f=>runMap.put(f.key,f.value))
    configuration.filter(_.sort.equals("special")).foreach(f=>specialMap.put(f.key,f.value))
    configMap.put("startup",startMap)
    configMap.put("runtime",runMap)
    configMap.put("special",specialMap)
    map.put("variable",variableMap)
    map.put("configuration",configMap)
    map
  }
}
