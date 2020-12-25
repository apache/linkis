/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engine.cs


import java.util
import java.util.regex.Pattern

import com.webank.wedatasphere.linkis.cs.client.service.CSResourceService
import com.webank.wedatasphere.linkis.engine.PropertiesExecuteRequest
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

/**
  * @author peacewong
  * @date 2020/3/5 19:42
  */
class CSResourceParser {

  private val pb = Pattern.compile("cs://[^\\s\"]+[$\\s]{0,1}", Pattern.CASE_INSENSITIVE)

  private val PREFIX = "cs://"

  private def getPreFixResourceNames(code: String): Array[String] = {
    val bmlResourceNames = new ArrayBuffer[String]()
    val mb = pb.matcher(code)
    while (mb.find) bmlResourceNames.append(mb.group.trim)
    bmlResourceNames.toArray
  }

  def parse(executeRequest: PropertiesExecuteRequest, code: String, contextIDValueStr: String, nodeNameStr: String): String = {

    //TODO getBMLResource peaceWong
    val bmlResourceList = CSResourceService.getInstance().getUpstreamBMLResource(contextIDValueStr, nodeNameStr)

    val parsedResources = new util.ArrayList[util.Map[String, Object]]()
    val preFixResourceNames = getPreFixResourceNames(code)

    val preFixNames = new ArrayBuffer[String]()
    val parsedNames = new ArrayBuffer[String]()
    preFixResourceNames.foreach { preFixResourceName =>
      val resourceName = preFixResourceName.replace(PREFIX, "").trim
      val bmlResourceOption = bmlResourceList.find(_.getDownloadedFileName.equals(resourceName))
      if (bmlResourceOption.isDefined) {
        val bmlResource = bmlResourceOption.get
        val map = new util.HashMap[String, Object]()
        map.put("resourceId", bmlResource.getResourceId)
        map.put("version", bmlResource.getVersion)
        map.put("fileName", resourceName)
        parsedResources.add(map)
        preFixNames.append(preFixResourceName)
        parsedNames.append(resourceName)
      }

    }
    executeRequest.properties.put("resources", parsedResources)
    StringUtils.replaceEach(code, preFixNames.toArray, parsedNames.toArray)
  }

}
