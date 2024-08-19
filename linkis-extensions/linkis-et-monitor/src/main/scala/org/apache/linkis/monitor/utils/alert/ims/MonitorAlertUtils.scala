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

package org.apache.linkis.monitor.utils.alert.ims

import org.apache.linkis.common.utils.{JsonUtils, Logging, Utils}
import org.apache.linkis.monitor.constants.Constants
import org.apache.linkis.monitor.jobhistory.exception.AnomalyScannerException
import org.apache.linkis.monitor.utils.alert.AlertDesc

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils

import java.io.{BufferedReader, File, FileInputStream, InputStream, InputStreamReader}
import java.text.SimpleDateFormat
import java.util
import java.util.Properties

import scala.collection.JavaConverters._

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object MonitorAlertUtils extends Logging {

  private val mapper = {
    val ret = new ObjectMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"))
    ret.registerModule(DefaultScalaModule)
    ret.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    ret
  }

  val properties = {
    val url = getClass.getClassLoader.getResource(Constants.ALERT_PROPS_FILE_PATH)
    if (url == null) {
      throw new AnomalyScannerException(
        21304,
        "Failed to load alerts from alert properties. Alert properties file does not exist: " + Constants.ALERT_PROPS_FILE_PATH
      )
    }
    logger.info("reading alert properties from: " + url.getFile)
    val properties = new Properties()
    var inputStream: InputStream = null
    var reader: InputStreamReader = null
    var buff: BufferedReader = null
    Utils.tryFinally {
      Utils.tryCatch {
        inputStream = new FileInputStream(new File(url.getFile))
        reader = new InputStreamReader(inputStream, "UTF-8")
        buff = new BufferedReader(reader)
        properties.load(buff)
      } { t =>
        {
          throw new AnomalyScannerException(
            21304,
            "Failed to load alerts from alert properties. Cause: " + ExceptionUtils.getMessage(t)
          )
        }
      }
    } {
      IOUtils.closeQuietly(buff)
      IOUtils.closeQuietly(reader)
      IOUtils.closeQuietly(inputStream)
    }
    properties.asScala
  }

  def getAlerts(prefix: String, params: util.Map[String, String]): util.Map[String, AlertDesc] = {
    val ret = new util.HashMap[String, AlertDesc]()

    for ((k: String, v: String) <- properties) {
      if (ret.containsKey(k)) {
        logger.warn("found duplicate key in alert properties, accept only the first one")
      } else if (StringUtils.startsWith(k, prefix)) {
        val data = mapper.readValue(v, classOf[ImsAlertPropFileData])
        var alertInfo = new String(
          new StringBuilder().append(data.alertInfo).toString().getBytes(),
          "utf-8"
        ).replace("$name", data.alertReceivers)
        val interator = params.keySet.iterator
        while (interator.hasNext) {
          val key = interator.next
          val value = params.get(key)
          alertInfo = alertInfo.replace(key, value)
        }
        val receivers = {
          val set: util.Set[String] = new util.HashSet[String]
          if (StringUtils.isNotBlank(data.alertReceivers)) {
            data.alertReceivers.split(",").map(r => set.add(r))
          }
          if (!params.containsKey("$alteruser")) {
            Constants.ALERT_DEFAULT_RECEIVERS.foreach(e => {
              if (StringUtils.isNotBlank(e)) {
                set.add(e)
              }
            })
          } else {
            set.add(params.get("$alteruser"))
          }
          if (StringUtils.isNotBlank(params.get("receiver"))) {
            params.get("receiver").split(",").map(r => set.add(r))
          }
          set
        }

        val subSystemId = params.getOrDefault("subSystemId", Constants.ALERT_SUB_SYSTEM_ID)
        val alertTitle = params.getOrDefault("title", data.alertTitle)
        val alertLevel =
          if (StringUtils.isNotBlank(data.alertLevel)) {
            ImsAlertLevel.withName(params.getOrDefault("monitorLevel", data.alertLevel))
          } else {
            ImsAlertLevel.withName(params.getOrDefault("monitorLevel", ImsAlertLevel.WARN.toString))
          }

        val alertDesc = Utils.tryAndWarn(
          ImsAlertDesc(
            subSystemId,
            alertTitle,
            data.alertObj,
            alertInfo,
            alertLevel,
            null,
            0, {
              val set: util.Set[ImsAlertWay.Value] = new util.HashSet[ImsAlertWay.Value]
              if (StringUtils.isNotBlank(data.alertWays)) {
                data.alertWays
                  .split(",")
                  .map(alertWayStr => set.add(ImsAlertWay.withName(alertWayStr)))
              }
              set
            },
            receivers
          )
        )
        val realK = StringUtils.substringAfter(k, prefix)
        ret.put(realK, alertDesc)
      }
    }
    ret
  }

}
