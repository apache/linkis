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

package org.apache.linkis.engineconnplugin.sqoop.launch

import org.apache.linkis.manager.common.protocol.bml.BmlResource
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest
import org.apache.linkis.manager.engineplugin.common.launch.process.{JavaProcessEngineConnLaunchBuilder, LaunchConstants}
import scala.collection.JavaConversions._
import java.util

class SqoopEngineConnLaunchBuilder extends JavaProcessEngineConnLaunchBuilder{
  override protected def ifAddHadoopClassPath: Boolean = true
  override protected def ifAddHbaseClassPath: Boolean = true
  override protected def getBmlResources(implicit engineConnBuildRequest: EngineConnBuildRequest): util.List[BmlResource] = {
    val filterList: List[String] = List( "lib2.zip",
      "conf.zip",
      "lib.zip")
    super.getBmlResources.filter(b=>filterList.contains(b.getFileName))
  }
}
