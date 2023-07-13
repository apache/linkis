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

package org.apache.linkis.engineconn.computation.executor.cs

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.cs.client.service.CSResourceService
import org.apache.linkis.engineconn.common.conf.EngineConnConf
import org.apache.linkis.governance.common.utils.GovernanceConstant

import org.apache.commons.lang3.StringUtils

import java.util
import java.util.regex.Pattern

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

class CSResourceParser extends Logging {

  private val pb = Pattern.compile("cs://[^\\s\"]+[$\\s]{0,1}", Pattern.CASE_INSENSITIVE)

  private val PREFIX = "cs://"

  private def getPreFixResourceNames(code: String): Array[String] = {
    val bmlResourceNames = new ArrayBuffer[String]()
    val mb = pb.matcher(code)
    while (mb.find) bmlResourceNames.append(mb.group.trim)
    bmlResourceNames.toArray
  }

  def parse(
      props: util.Map[String, Object],
      code: String,
      contextIDValueStr: String,
      nodeNameStr: String
  ): String = {

    val bmlResourceList =
      CSResourceService.getInstance().getUpstreamBMLResource(contextIDValueStr, nodeNameStr)

    val parsedResources = new util.ArrayList[util.Map[String, Object]]()
    val preFixResourceNames = getPreFixResourceNames(code)

    val preFixNames = new ArrayBuffer[String]()
    val parsedNames = new ArrayBuffer[String]()
    val prefixName = System.currentTimeMillis().toString + "_"
    preFixResourceNames.foreach { preFixResourceName =>
      val resourceName = preFixResourceName.replace(PREFIX, "").trim
      val bmlResourceOption =
        bmlResourceList.asScala.find(_.getDownloadedFileName.equals(resourceName))
      if (bmlResourceOption.isDefined) {
        val replacementName = EngineConnConf.getEngineTmpDir + prefixName + resourceName
        val bmlResource = bmlResourceOption.get
        val map = new util.HashMap[String, Object]()
        map.put(GovernanceConstant.TASK_RESOURCE_ID_STR, bmlResource.getResourceId)
        map.put(GovernanceConstant.TASK_RESOURCE_VERSION_STR, bmlResource.getVersion)
        map.put(GovernanceConstant.TASK_RESOURCE_FILE_NAME_STR, replacementName)
        parsedResources.add(map)
        preFixNames.append(preFixResourceName)
        parsedNames.append(replacementName)
        logger.warn(s"Replace cs file from {$preFixResourceName} to {$replacementName}")
      }
    }
    props.put(GovernanceConstant.TASK_RESOURCES_STR, parsedResources)
    StringUtils.replaceEach(code, preFixNames.toArray, parsedNames.toArray)
  }

}
