/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.engine

import java.text.SimpleDateFormat
import java.util.Date

import com.webank.wedatasphere.linkis.DataWorkCloudApplication
import com.webank.wedatasphere.linkis.common.conf.DWCArgumentsParser
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.engine.conf.EngineConfiguration
import com.webank.wedatasphere.linkis.server.conf.ServerConfiguration
import org.apache.commons.lang.StringUtils
import org.slf4j.LoggerFactory

/**
  * Created by enjoyyin on 2018/10/19.
  */
object DataWorkCloudEngineApplication {

  val userName:String = System.getProperty("user.name")
  val hostName:String = Utils.getComputerName
  val appName:String = EngineConfiguration.ENGINE_SPRING_APPLICATION_NAME.getValue
  val prefixName:String = EngineConfiguration.ENGINE_LOG_PREFIX.getValue
  val timeStamp:Long = System.currentTimeMillis()
  private val timeFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss")
  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
  val time:String = timeFormat.format(new Date(timeStamp))
  val date:String = dateFormat.format(new Date(timeStamp))

  val isTimeStampSuffix:Boolean = "true".equalsIgnoreCase(EngineConfiguration.ENGINE_LOG_TIME_STAMP_SUFFIX.getValue)
  val shortLogFile:String =
    if (isTimeStampSuffix) appName + "_" + hostName + "_" + userName + "_"  + time + ".log"
    else appName + "_" + hostName + "_" + userName + ".log"
  val logName:String =
    if(isTimeStampSuffix) prefixName + "/" + userName + "/" + shortLogFile
    else prefixName + "/" + shortLogFile
  System.setProperty("engineLogFile", logName)
  System.setProperty("shortEngineLogFile", shortLogFile)
//  System.setProperty("engineLogFile", logName)
//  val context:LoggerContext = LogManager.getContext(false).asInstanceOf[LoggerContext]
//  val path:String = getClass.getResource("/").getPath
//  val log4j2XMLFile:File = new File(path + "/log4j2-engine.xml")
//  val configUri:URI = log4j2XMLFile.toURI
//  context.setConfigLocation(configUri)
  private val logger = LoggerFactory.getLogger(getClass)
  logger.info(s"Now log4j2 Rolling File is set to be $logName")
  logger.info(s"Now shortLogFile is set to be $shortLogFile")
  def main(args: Array[String]): Unit = {
    val parser = DWCArgumentsParser.parse(args)
    DWCArgumentsParser.setDWCOptionMap(parser.getDWCConfMap)
    val existsExcludePackages = ServerConfiguration.BDP_SERVER_EXCLUDE_PACKAGES.getValue
    if(StringUtils.isEmpty(existsExcludePackages))
      DataWorkCloudApplication.setProperty(ServerConfiguration.BDP_SERVER_EXCLUDE_PACKAGES.key, "com.webank.wedatasphere.linkis.enginemanager")
    else
      DataWorkCloudApplication.setProperty(ServerConfiguration.BDP_SERVER_EXCLUDE_PACKAGES.key, existsExcludePackages + ",com.webank.wedatasphere.linkis.enginemanager")
    DataWorkCloudApplication.main(DWCArgumentsParser.formatSpringOptions(parser.getSpringConfMap))
  }
}