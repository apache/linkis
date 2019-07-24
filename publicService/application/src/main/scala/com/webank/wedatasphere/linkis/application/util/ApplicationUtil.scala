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

package com.webank.wedatasphere.linkis.application.util

import java.text.SimpleDateFormat
import java.util.Date

import com.webank.wedatasphere.linkis.application.conf.{ApplicationConfiguration, ApplicationScalaConfiguration}

/**
  * Created by johnnwang on 2019/2/14.
  */
object ApplicationUtil {
  def getFlowsJson(user:String,date:Date):String ={
    val initExamplePath = ApplicationScalaConfiguration.INIT_EXAMPLE_PATH.getValue.toString + user + "/application/dataStudio/"
    val sqlName = ApplicationScalaConfiguration.INIT_EXAMPLE_SQL_NAME.getValue.toString
    val scalaName = ApplicationScalaConfiguration.INIT_EXAMPLE_SCALA_NAME.getValue.toString
    val spyName = ApplicationScalaConfiguration.INIT_EXAMPLE_SPY_NAME.getValue.toString
    val hqlName = ApplicationScalaConfiguration.INIT_EXAMPLE_HQL_NAME.getValue.toString
    val pythonName = ApplicationScalaConfiguration.INIT_EXAMPLE_PYTHON_NAME.getValue.toString
    val formateDate =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)
    s"""[{"id":1,"name":"Default business process(默认业务流程)","createTime":"$formateDate","lastUpdateTime":"","description":"Default business process(默认业务流程)","version":"1.0.0","owner":"$user","canPublished":false,"params":{},"relations":[],"projectChildren":[],"flowChildren":[],"nodeChildren":{"dataExchange":[],"dataStudio":[{"id":1,"name":"$sqlName","type":"${sqlName.substring(sqlName.lastIndexOf(".")+1)}","createTime":"$formateDate","lastUpdateTime":"","description":"","version":"1.0.0","owner":"$user","content":{"scriptPath":"${initExamplePath + sqlName}"}},{"id":2,"name":"$scalaName","type":"${scalaName.substring(scalaName.lastIndexOf(".")+1)}","createTime":"$formateDate","lastUpdateTime":"","description":"","version":"1.0.0","owner":"$user","content":{"scriptPath":"${initExamplePath + scalaName}"}},{"id":3,"name":"$spyName","type":"${spyName.substring(spyName.lastIndexOf(".")+1)}","createTime":"$formateDate","lastUpdateTime":"","description":"","version":"1.0.0","owner":"$user","content":{"scriptPath":"${initExamplePath + spyName}"}},{"id":4,"name":"$hqlName","type":"${hqlName.substring(hqlName.lastIndexOf(".")+1)}","createTime":"$formateDate","lastUpdateTime":"","description":"","version":"1.0.0","owner":"$user","content":{"scriptPath":"${initExamplePath + hqlName}"}},{"id":5,"name":"$pythonName","type":"${pythonName.substring(pythonName.lastIndexOf(".")+1)}","createTime":"$formateDate","lastUpdateTime":"","description":"","version":"1.0.0","owner":"$user","content":{"scriptPath":"${initExamplePath + pythonName}"}}],"dataBI":[],"resources":[]}}]"""
  }
}