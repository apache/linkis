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

package org.apache.linkis.engineconnplugin.sqoop.resource

import org.apache.linkis.manager.common.entity.resource.{DriverAndYarnResource, LoadInstanceResource, Resource, YarnResource}
import org.apache.linkis.manager.engineplugin.common.resource.AbstractEngineResourceFactory

import java.util

class SqoopEngineConnResourceFactory extends AbstractEngineResourceFactory{
  override protected def getRequestResource(properties: util.Map[String, String]): Resource = {
    /*new DriverAndYarnResource(
      new LoadInstanceResource(OverloadUtils.getProcessMaxMemory,
        1,
        1),
      new YarnResource(LINKIS_SQOOP_TASK_MANAGER_MEMORY.getValue*1, LINKIS_SQOOP_TASK_MANAGER_CPU_CORES.getValue, 1, LINKIS_QUEUE_NAME.getValue)
    )*/
    new LoadInstanceResource(1,
      1,
      1)
  }
}
