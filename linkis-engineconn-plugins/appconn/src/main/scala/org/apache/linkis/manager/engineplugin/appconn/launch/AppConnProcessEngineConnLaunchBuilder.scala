/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.engineplugin.appconn.launch

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.manager.common.protocol.bml.BmlResource
import org.apache.linkis.manager.common.protocol.bml.BmlResource.BmlResourceVisibility
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest
import org.apache.linkis.manager.engineplugin.common.launch.process.JavaProcessEngineConnLaunchBuilder

import java.util

import scala.collection.convert.wrapAsJava._
import scala.collection.convert.wrapAsScala._

import com.webank.wedatasphere.dss.appconn.manager.AppConnManager
import com.webank.wedatasphere.dss.appconn.manager.impl.AbstractAppConnManager
import com.webank.wedatasphere.dss.appconn.manager.service.AppConnInfoService

class AppConnProcessEngineConnLaunchBuilder extends JavaProcessEngineConnLaunchBuilder {

  override protected def getBmlResources(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): util.List[BmlResource] = {
    super.getBmlResources
  }

  override def enablePublicModule: Boolean = true

}
