/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.server.utils

import com.webank.wedatasphere.linkis.common.conf.CommonVars
import scala.collection.JavaConverters._

object LinkisMainHelper {

  private val SPRING_STAR = "spring."

  val SERVER_NAME_KEY = "serviceName"

  def formatPropertyFiles(serviceName: String): Unit = {
    sys.props.put("wds.linkis.configuration", "linkis.properties")
    sys.props.put("wds.linkis.server.conf", s"$serviceName.properties")
  }

  def formatPropertyFiles(mainPropertiesName: String, serviceName: String): Unit = {
    sys.props.put("wds.linkis.configuration", s"$mainPropertiesName.properties")
    sys.props.put("wds.linkis.server.conf", s"$serviceName.properties")
  }

  //TODO wait for linkis re-written
  @Deprecated
  def addExtraPropertyFiles(filePaths: String *): Unit = {
    sys.props.put("wds.linkis.server.confs", filePaths.mkString(","))
  }

  def getExtraSpringOptions(profilesName: String): Array[String] = {
    s"--spring.profiles.active=$profilesName" +: CommonVars.properties.asScala.filter { case (k, v) => k != null && k.startsWith(SPRING_STAR)}
      .map { case (k, v) =>
        val realKey = k.substring(SPRING_STAR.length)
        s"--$realKey=$v"
      }.toArray
  }

}
