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

package org.apache.linkis.engineconn.acessible.executor.utils

import org.apache.linkis.DataWorkCloudApplication.getApplicationContext
import org.apache.linkis.engineconn.acessible.executor.info.DefaultNodeHealthyInfoManager
import org.apache.linkis.manager.common.entity.enumeration.NodeHealthy

object AccessibleExecutorUtils {

  val manager: DefaultNodeHealthyInfoManager =
    getApplicationContext.getBean(classOf[DefaultNodeHealthyInfoManager])

  def currentEngineIsUnHealthy(): Boolean = {
    manager != null && manager.getNodeHealthy() == NodeHealthy.UnHealthy
  }

}
