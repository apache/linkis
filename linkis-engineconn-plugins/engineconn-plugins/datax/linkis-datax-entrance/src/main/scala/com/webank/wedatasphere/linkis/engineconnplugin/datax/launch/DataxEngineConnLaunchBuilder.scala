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

package com.webank.wedatasphere.linkis.engineconnplugin.datax.launch

import com.google.gson.Gson
import com.webank.wedatasphere.linkis.engineconnplugin.datax.client.exception.JobExecutionException
import com.webank.wedatasphere.linkis.manager.common.protocol.bml.BmlResource
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.process.JavaProcessEngineConnLaunchBuilder

import scala.collection.JavaConversions._
import java.util

class DataxEngineConnLaunchBuilder extends JavaProcessEngineConnLaunchBuilder{
  val READER = "reader"
  val WRITER = "writer"
  override protected def getBmlResources(implicit engineConnBuildRequest: EngineConnBuildRequest): util.List[BmlResource] = {
    val params = new Gson().fromJson(engineConnBuildRequest.engineConnCreationDesc.description,classOf[util.Map[String, String]])
    if(params.get(READER) == null||params.get(WRITER)==null){
      throw new JobExecutionException("The Datax Engine Shoud Be Set Reader And Writer")
    }
    val filterList: List[String] = List(READER+"-"+params.get(READER)+".zip",
      WRITER+"-"+params.get(WRITER)+".zip",
      "core.zip",
      "conf.zip",
      "lib.zip")
    super.getBmlResources.filter(b=>filterList.contains(b.getFileName))
  }
}
