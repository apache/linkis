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
import org.apache.linkis.monitor.utils.alert.{AlertDesc, PooledAlertSender}
import org.apache.linkis.monitor.utils.alert.AlertDesc
import org.apache.linkis.monitor.utils.log.LogUtils

import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

import java.text.SimpleDateFormat
import java.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

class PooledImsAlertSender(alertUrl: String) extends PooledAlertSender with Logging {

  protected val httpClient = HttpClients.createDefault

  private val mapper =
    new ObjectMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"))

  /**
   * describes actual actions for sending an alert
   *
   * @return
   *   true if it is a success
   */

  /**
   * describes actual actions for sending an alert
   *
   * @return
   *   true if it is a success
   */
  override def doSendAlert(alertDesc: AlertDesc): Boolean = {
    if (!alertDesc.isInstanceOf[ImsAlertDesc]) {
      logger.warn("wrong alertDesc dataType: " + alertDesc.getClass.getCanonicalName)
      return false
    }
    logger.info("sending an alert to IMS, information: " + alertDesc)
    val imsRequest = alertDesc.asInstanceOf[ImsAlertDesc].toImsRequest

    mapper.registerModule(DefaultScalaModule)
    val paramContent = Utils.tryCatch(mapper.writeValueAsString(imsRequest)) { t =>
      logger.warn("ignore alert: " + imsRequest, t)
      return false
    }
    if (paramContent.isEmpty) {
      logger.warn("alertParams is empty, will not send alarm")
      return false
    }

    val requestConfig = RequestConfig.DEFAULT

    val entity = new StringEntity(
      paramContent,
      ContentType.create(ContentType.APPLICATION_JSON.getMimeType, "UTF-8")
    )
    entity.setContentEncoding("UTF-8")

    val httpPost = new HttpPost(alertUrl)

    httpPost.setConfig(requestConfig)
    httpPost.setEntity(entity)

    val response = Utils.tryAndErrorMsg(httpClient.execute(httpPost))("send alert to IMS failed")

    if (response != null) {
      val responseInfo = EntityUtils.toString(response.getEntity, "UTF-8")
      logger.info("Alert: " + paramContent + "Response: " + responseInfo)
      LogUtils.stdOutLogger.info("Alert: " + paramContent + "Response: " + responseInfo)
      if (response.getStatusLine.getStatusCode == 200) return true
    }
    false
  }

  override def shutdown(waitComplete: Boolean = true, timeoutMs: Long = -1): Unit = {
    super.shutdown(waitComplete, timeoutMs)
    httpClient.close
  }

}
