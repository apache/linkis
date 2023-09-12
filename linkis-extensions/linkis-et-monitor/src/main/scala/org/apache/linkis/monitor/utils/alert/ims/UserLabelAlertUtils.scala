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

object UserLabelAlertUtils extends Logging {

  def getAlerts(prefix: String, userCreator: String): util.Map[String, AlertDesc] = {
    val replaceParams: util.HashMap[String, String] = new util.HashMap[String, String]
    replaceParams.put("$userCreator", userCreator)
    MonitorAlertUtils.getAlerts(prefix, replaceParams)
  }

}
