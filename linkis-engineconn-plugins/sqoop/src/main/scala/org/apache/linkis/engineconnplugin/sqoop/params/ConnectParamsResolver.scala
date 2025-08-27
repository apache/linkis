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

package org.apache.linkis.engineconnplugin.sqoop.params

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.datasourcemanager.common.util.json.Json
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconnplugin.sqoop.context.SqoopParamsConfiguration

import org.apache.commons.lang3.StringUtils

import java.util

import scala.collection.JavaConverters._

/**
 * Connect param
 */
class ConnectParamsResolver extends SqoopParamsResolver with Logging {

  /**
   * main method build connect from host,port,and for example:
   * --connect "jdbc:mysql://{host}:{port}/xxx?{params}"
   * @param params
   *   input
   * @return
   */
  override def resolve(
      params: util.Map[String, String],
      context: EngineCreationContext
  ): util.Map[String, String] = {
    info(s"Invoke resolver: ${this.getClass.getSimpleName}")
    Option(params.get(SqoopParamsConfiguration.SQOOP_PARAM_CONNECT.getValue)).foreach(connect => {
      val newConnectStr = params.asScala.foldLeft(connect) {
        case (newConnect, kv) => {
          var paramKey = kv._1
          var paramValue = kv._2
          if (Option(paramKey).isDefined && Option(paramValue).isDefined) {
            if (paramKey.equals(SqoopParamsConfiguration.SQOOP_PARAM_CONNECT_PARAMS.getValue)) {
              Utils.tryQuietly {
                val connectMap: util.Map[String, String] = Json.fromJson(
                  paramValue,
                  classOf[util.Map[String, String]],
                  classOf[String],
                  classOf[String]
                )
                paramValue = connectMap.asScala.foldLeft("") { case (connectStr, mapItem) =>
                  val item = s"${mapItem._1}=${mapItem._2}"
                  if (StringUtils.isNotBlank(connectStr)) connectStr + "&" + item else item
                }
              }
            }
            if (paramKey.startsWith(SqoopParamsConfiguration.SQOOP_PARAM_PREFIX.getValue)) {
              paramKey =
                paramKey.substring(SqoopParamsConfiguration.SQOOP_PARAM_PREFIX.getValue.length)
            }
            newConnect.replace(s"{$paramKey}", paramValue)
          } else newConnect
        }
      }
      info(s"connect string => $newConnectStr")
      params.put(SqoopParamsConfiguration.SQOOP_PARAM_CONNECT.getValue, newConnectStr)
    })
    params
  }

}
