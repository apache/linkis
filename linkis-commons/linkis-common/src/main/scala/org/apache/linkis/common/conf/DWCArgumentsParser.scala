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

package org.apache.linkis.common.conf

import org.apache.linkis.common.utils.{ParameterUtils, Logging}

import org.apache.commons.lang3.StringUtils

import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object DWCArgumentsParser {
  protected val DWC_CONF = "--engineconn-conf"
  protected val SPRING_CONF = "--spring-conf"
  private var dwcOptionMap = Map.empty[String, String]

  private[linkis] def setDWCOptionMap(dwcOptionMap: Map[String, String]) = this.dwcOptionMap =
    dwcOptionMap

  def getDWCOptionMap: Map[String, String] = dwcOptionMap

  def parse(args: Array[String]): DWCArgumentsParser = {
    val optionParser = new DWCArgumentsParser
    ParameterUtils.parseStartupParams(
      args,
      (prefix, key, value) => {
        optionParser.setConf(s"--$prefix-conf", key, value)
      }
    )
    optionParser.validate()
    optionParser
  }

  def formatToArray(optionParser: DWCArgumentsParser): Array[String] = {
    val options = ArrayBuffer[String]()
    def write(confMap: Map[String, String], optionType: String): Unit = confMap.foreach {
      case (key, value) =>
        if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
          options += optionType
          options += (key + "=" + value)
        }
    }
    write(optionParser.getDWCConfMap, DWC_CONF)
    write(optionParser.getSpringConfMap, SPRING_CONF)
    options.toArray
  }

  def formatToArray(
      springOptionMap: Map[String, String],
      dwcOptionMap: Map[String, String]
  ): Array[String] =
    formatToArray(new DWCArgumentsParser().setSpringConf(springOptionMap).setDWCConf(dwcOptionMap))

  def format(optionParser: DWCArgumentsParser): String = formatToArray(optionParser).mkString(" ")

  def format(springOptionMap: Map[String, String], dwcOptionMap: Map[String, String]): String =
    formatToArray(springOptionMap, dwcOptionMap).mkString(" ")

  def formatSpringOptions(springOptionMap: Map[String, String]): Array[String] = {
    val options = ArrayBuffer[String]()
    springOptionMap.foreach { case (key, value) =>
      if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
        options += ("--" + key + "=" + value)
      }
    }
    options.toArray
  }

}

class DWCArgumentsParser {
  import DWCArgumentsParser._
  private val dwcOptionMap = new mutable.HashMap[String, String]()
  private val springOptionMap = new mutable.HashMap[String, String]()
  def getSpringConfMap: Map[String, String] = springOptionMap.toMap
  def getSpringConfs: java.util.Map[String, String] = springOptionMap.asJava
  def getDWCConfMap: Map[String, String] = dwcOptionMap.toMap

  def setConf(optionType: String, key: String, value: String): DWCArgumentsParser = {
    optionType match {
      case DWC_CONF =>
        dwcOptionMap += key -> value
      case SPRING_CONF =>
        springOptionMap += key -> value
    }
    this
  }

  def setSpringConf(optionMap: Map[String, String]): DWCArgumentsParser = {
    if (optionMap != null) this.springOptionMap ++= optionMap
    this
  }

  def setDWCConf(optionMap: Map[String, String]): DWCArgumentsParser = {
    if (optionMap != null) this.dwcOptionMap ++= optionMap
    this
  }

  def validate(): Unit = {}
}
