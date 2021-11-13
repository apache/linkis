/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.common.conf

import org.apache.commons.lang.StringUtils

import scala.collection.mutable.ArrayBuffer
import scala.collection.{JavaConversions, mutable}


object DWCArgumentsParser {
  protected val DWC_CONF = "--engineconn-conf"
  protected val SPRING_CONF = "--spring-conf"
  private var dwcOptionMap = Map.empty[String, String]

  private[linkis] def setDWCOptionMap(dwcOptionMap: Map[String, String]) = this.dwcOptionMap = dwcOptionMap
  def getDWCOptionMap = dwcOptionMap

  def parse(args: Array[String]): DWCArgumentsParser = {
    val keyValueRegex = "([^=]+)=(.+)".r
    var i = 0
    val optionParser = new DWCArgumentsParser
    while(i < args.length) {
      args(i) match {
        case DWC_CONF | SPRING_CONF =>
          args(i + 1) match {
            case keyValueRegex(key, value) =>
              optionParser.setConf(args(i), key, value)
              i += 1
            case _ => throw new IllegalArgumentException("illegal commond line, format: --conf key=value.")
          }
        case _ => throw new IllegalArgumentException(s"illegal commond line, ${args(i)} cannot recognize.")
      }
      i += 1
    }
    optionParser.validate()
    optionParser
  }

  def formatToArray(optionParser: DWCArgumentsParser): Array[String] = {
    val options = ArrayBuffer[String]()
    def write(confMap: Map[String, String], optionType: String): Unit = confMap.foreach { case (key, value) =>
      if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
        options += optionType
        options += (key + "=" + value)
      }
    }
    write(optionParser.getDWCConfMap, DWC_CONF)
    write(optionParser.getSpringConfMap, SPRING_CONF)
    options.toArray
  }
  def formatToArray(springOptionMap: Map[String, String], dwcOptionMap: Map[String, String]): Array[String] =
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
  def getSpringConfMap = springOptionMap.toMap
  def getSpringConfs = JavaConversions.mapAsJavaMap(springOptionMap)
  def getDWCConfMap = dwcOptionMap.toMap
  def setConf(optionType: String, key: String, value: String) = {
    optionType match {
      case DWC_CONF =>
        dwcOptionMap += key -> value
      case SPRING_CONF =>
        springOptionMap += key -> value
    }
    this
  }
  def setSpringConf(optionMap: Map[String, String]): DWCArgumentsParser = {
    if(optionMap != null) this.springOptionMap ++= optionMap
    this
  }
  def setDWCConf(optionMap: Map[String, String]): DWCArgumentsParser = {
    if(optionMap != null) this.dwcOptionMap ++= optionMap
    this
  }
  def validate() = {}
}