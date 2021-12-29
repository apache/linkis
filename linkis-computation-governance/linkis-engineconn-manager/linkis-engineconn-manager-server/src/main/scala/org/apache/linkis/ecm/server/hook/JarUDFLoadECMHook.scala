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
 
package org.apache.linkis.ecm.server.hook
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.ecm.core.engineconn.EngineConn
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest
import org.apache.linkis.manager.engineplugin.common.launch.process.{LaunchConstants, ProcessEngineConnLaunchRequest}
import org.apache.linkis.udf.UDFClient
import org.apache.commons.lang.StringUtils

class JarUDFLoadECMHook extends ECMHook with Logging {

  override def beforeLaunch(request: EngineConnLaunchRequest, conn: EngineConn): Unit = {
    request match {
      case pel: ProcessEngineConnLaunchRequest =>
        info("start loading UDFs")
        val udfInfos = UDFClient.getUdfInfos(request.user,"udf").filter{ info => info.getUdfType == 0 && info.getExpire == false && StringUtils.isNotBlank(info.getPath) && info.getLoad == true }
        udfInfos.foreach{ udfInfo =>
          LaunchConstants.addPathToUDFPath(pel.environment, udfInfo.getPath)
        }
    }
  }

  override def afterLaunch(conn: EngineConn): Unit = {

  }

  override def getName: String = "JarUDFLoadECMHook"
}
