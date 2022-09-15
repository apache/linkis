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

package org.apache.linkis.governance.common.utils

import org.apache.commons.lang3.StringUtils

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait EngineConnArguments {

  def getSpringConfMap: Map[String, String]

  def getEngineConnConfMap: Map[String, String]

}

trait EngineConnArgumentsBuilder {

  def addSpringConf(confMap: Map[String, String]): EngineConnArgumentsBuilder

  def addSpringConf(key: String, value: String): EngineConnArgumentsBuilder

  def addEngineConnConf(confMap: Map[String, String]): EngineConnArgumentsBuilder

  def addEngineConnConf(key: String, value: String): EngineConnArgumentsBuilder

  def build(): EngineConnArguments

}

object EngineConnArgumentsBuilder {
  def newBuilder(): EngineConnArgumentsBuilder = new DefaultEngineConnArgumentsBuilder
}

class DefaultEngineConnArgumentsBuilder extends EngineConnArgumentsBuilder {

  private val engineConnOptionMap = new mutable.HashMap[String, String]()
  private val springOptionMap = new mutable.HashMap[String, String]()

  override def addSpringConf(confMap: Map[String, String]): EngineConnArgumentsBuilder = {
    springOptionMap ++= confMap
    this
  }

  override def addSpringConf(key: String, value: String): EngineConnArgumentsBuilder = {
    springOptionMap += key -> value
    this
  }

  override def addEngineConnConf(confMap: Map[String, String]): EngineConnArgumentsBuilder = {
    engineConnOptionMap ++= confMap
    this
  }

  override def addEngineConnConf(key: String, value: String): EngineConnArgumentsBuilder = {
    engineConnOptionMap += key -> value
    this
  }

  override def build(): EngineConnArguments = new EngineConnArguments {
    override def getSpringConfMap: Map[String, String] = springOptionMap.toMap
    override def getEngineConnConfMap: Map[String, String] = engineConnOptionMap.toMap
  }

}

trait EngineConnArgumentsParser {

  def parseToObj(args: Array[String]): EngineConnArguments

  def parseToArgs(engineConnArguments: EngineConnArguments): Array[String]

}

object EngineConnArgumentsParser {

  val LABEL_PREFIX = "label."

  private val parser = new DefaultEngineConnArgumentsParser

  def getEngineConnArgumentsParser: EngineConnArgumentsParser = parser
}

class DefaultEngineConnArgumentsParser extends EngineConnArgumentsParser {

  protected val ENGINE_CONN_CONF = "--engineconn-conf"
  protected val SPRING_CONF = "--spring-conf"

  protected val keyValueRegex = "([^=]+)=(.+)".r

  override def parseToObj(args: Array[String]): EngineConnArguments = {
    var i = 0
    val argumentsBuilder = new DefaultEngineConnArgumentsBuilder
    while (i < args.length) {
      args(i) match {
        case ENGINE_CONN_CONF =>
          addKeyValue(
            args(i + 1),
            (key, value) => {
              argumentsBuilder.addEngineConnConf(key, value)
              i += 1
            }
          )
        case SPRING_CONF =>
          addKeyValue(
            args(i + 1),
            (key, value) => {
              argumentsBuilder.addSpringConf(key, value)
              i += 1
            }
          )
        case _ =>
          throw new IllegalArgumentException(s"illegal command line, ${args(i)} cannot recognize.")
      }
      i += 1
    }
    argumentsBuilder.build()
  }

  private def addKeyValue(keyValue: String, argumentsBuilder: (String, String) => Unit): Unit =
    keyValue match {
      case keyValueRegex(key, value) =>
        argumentsBuilder(key, value)
      case _ =>
        throw new IllegalArgumentException("illegal command line, format: --conf key=value.")
    }

  override def parseToArgs(engineConnArguments: EngineConnArguments): Array[String] = {
    val options = ArrayBuffer[String]()
    def write(confMap: Map[String, String], optionType: String): Unit = confMap.foreach {
      case (key, value) =>
        if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
          options += optionType
          options += (key + "=" + value)
        }
    }
    write(engineConnArguments.getEngineConnConfMap, ENGINE_CONN_CONF)
    write(engineConnArguments.getSpringConfMap, SPRING_CONF)
    options.toArray
  }

}
