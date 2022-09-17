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

package org.apache.linkis.engineplugin.server.interceptor

import org.apache.linkis.manager.engineplugin.common.launch.entity.{
  EngineConnBuildRequest,
  RicherEngineConnBuildRequest
}

trait EngineConnLaunchInterceptor {

  /**
   * 补充资源文件信息，如：UDF、用户jar、Python文件等 补充启动参数信息等
   *
   * @param engineConnBuildRequest
   */
  def intercept(engineConnBuildRequest: EngineConnBuildRequest): RicherEngineConnBuildRequest

}
