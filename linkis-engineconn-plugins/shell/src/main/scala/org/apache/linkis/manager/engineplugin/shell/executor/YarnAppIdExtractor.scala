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

package org.apache.linkis.manager.engineplugin.shell.executor

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.common.conf.EngineConnConf

import org.apache.commons.lang3.StringUtils

import java.io._
import java.util
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern

class YarnAppIdExtractor extends Thread with Logging {
  val MAX_BUFFER: Int = 32 * 1024 * 1024 // 32MB

  val buff: StringBuilder = new StringBuilder

  val appIdList: util.List[String] = new util.ArrayList[String]()

  val shouldStop: AtomicBoolean = new AtomicBoolean(false)

  def appendLineToExtractor(content: String): Unit = {
    buff.synchronized {
      if (content.length + buff.length > MAX_BUFFER) {
        logger.warn(
          s"input exceed max-buffer-size, will abandon some part of input. maybe will lost some yarn-app-id."
        )
        buff
          .append(StringUtils.substring(content, 0, MAX_BUFFER - buff.length))
          .append(System.lineSeparator())
      } else {
        buff.append(content).append(System.lineSeparator())
      }
    }
  }

  def startExtraction(): Unit = {
    this.start()
  }

  def onDestroy(): Unit = {
    shouldStop.set(true)
    this.interrupt()
    buff.synchronized {
      buff.setLength(0)
    }
  }

  private def doExtractYarnAppId(content: String): Array[String] = {

    if (StringUtils.isBlank(content)) return new Array[String](0)
    // spark: Starting|Submitted|Activating.{1,100}(application_\d{13}_\d+)
    // sqoop, importtsv: Submitted application application_1609166102854_970911
    val regex = EngineConnConf.SPARK_ENGINE_CONN_YARN_APP_ID_PARSE_REGEX.getValue
    val pattern = Pattern.compile(regex)

    val stringReader = new StringReader(content)

    val reader = new BufferedReader(stringReader)

    val ret = new util.ArrayList[String]

    var line = reader.readLine()
    while ({
      line != null
    }) { // match application_xxx_xxx
      val mApp = pattern.matcher(line)
      if (mApp.find) {
        val candidate1 = mApp.group(2)
        if (!ret.contains(candidate1)) {
          ret.add(candidate1)
        }
      }
      line = reader.readLine
    }
    stringReader.close()
    ret.toArray(new Array[String](ret.size()))
  }

  override def run(): Unit = {
    while (!shouldStop.get()) {
      var content: String = ""
      buff.synchronized {
        content = buff.toString()
        buff.setLength(0)
      }
      if (StringUtils.isNotBlank(content)) {
        val appIds = doExtractYarnAppId(content)
        if (appIds != null && appIds.length != 0) {
          logger.info(s"Retrieved new yarn application Id：" + appIds.mkString(" "))
          addYarnAppIds(appIds)
        }
        logger.debug(s"Yarn-appid-extractor is running")
      }
      Utils.sleepQuietly(200L)
    }
  }

  def addYarnAppId(yarnAppId: String): Unit = {
    appIdList.synchronized {
      appIdList.add(yarnAppId)
    }
  }

  def addYarnAppIds(yarnAppIds: Array[String]): Unit = {
    if (yarnAppIds != null && !yarnAppIds.isEmpty) {
      appIdList.synchronized {
        yarnAppIds.foreach(id =>
          if (!appIdList.contains(id)) {
            appIdList.add(id)
            // input application id to logs/stderr
            logger.info(s"Submitted application $id")
          }
        )
      }
    }
  }

  def getExtractedYarnAppIds(): Array[String] = {
    appIdList.synchronized {
      appIdList.toArray(new Array[String](appIdList.size()))
    }
  }

}
