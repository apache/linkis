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

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.common.conf.EngineConnConf

import org.apache.commons.lang3.StringUtils

import java.io._
import java.util
import java.util.Collections
import java.util.regex.Pattern

class YarnAppIdExtractor extends Logging {

  private val appIdList: util.Set[String] = Collections.synchronizedSet(new util.HashSet[String]())

  private val regex = EngineConnConf.SPARK_ENGINE_CONN_YARN_APP_ID_PARSE_REGEX.getValue
  private val pattern = Pattern.compile(regex)

  def appendLineToExtractor(content: String): Unit = {
    if (StringUtils.isBlank(content)) return
    val yarnAppIDMatcher = pattern.matcher(content)
    if (yarnAppIDMatcher.find) {
      val yarnAppID = yarnAppIDMatcher.group(2)
      appIdList.add(yarnAppID)
    }
  }

  private def doExtractYarnAppId(content: String): Array[String] = {

    if (StringUtils.isBlank(content)) return new Array[String](0)
    // spark: Starting|Submitted|Activating.{1,100}(application_\d{13}_\d+)
    // sqoop, importtsv: Submitted application application_1609166102854_970911

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

  def getExtractedYarnAppIds(): util.List[String] = {
    appIdList.synchronized {
      new util.ArrayList[String](appIdList)
    }
  }

}
