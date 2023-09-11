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

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.monitor.constants.Constants
import org.apache.linkis.monitor.utils.alert.AlertDesc

import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils

import java.net.InetAddress
import java.util
import java.util.HashSet

import scala.collection.JavaConverters._

import ImsAlertWay.ImsAlertWay

object PooledImsAlertUtils extends Logging {

  private val sender: PooledImsAlertSender = {
    val ret = new PooledImsAlertSender(Constants.ALERT_IMS_URL)
    ret.start()
    ret
  }

  private val localIp = InetAddress.getLocalHost.getHostAddress

  def addAlertAndLogException(message: String): Unit = Utils.tryAndError(addAlert(message))

  def addAlert(message: String): Unit = addExceptionAlert(message, null, null)

  def addExceptionAlert(message: String, t: Throwable): Unit =
    addExceptionAlert(message, t, null)

  def addExceptionAlertAndLogException(message: String, t: Throwable): Unit =
    Utils.tryAndError(addExceptionAlert(message, t, null))

  def addExceptionAlert(message: String, t: Throwable, alertWays: util.Set[ImsAlertWay]): Unit = {
    val alertObj =
      if (StringUtils.isEmpty(message) && t != null) t.getMessage
      else if (StringUtils.isEmpty(message)) {
        throw new NullPointerException("both message and exception are null!")
      } else {
        message
      }
    val _alertWays =
      if (CollectionUtils.isNotEmpty(alertWays)) alertWays else new HashSet[ImsAlertWay]()
    val (alertInfo, alertLevel) = if (t != null) {
      _alertWays.add(ImsAlertWay.Email)
      _alertWays.add(ImsAlertWay.WXWork)
      _alertWays.add(ImsAlertWay.WeChat)
      (ExceptionUtils.getRootCauseMessage(t), ImsAlertLevel.MAJOR)
    } else {
      _alertWays.add(ImsAlertWay.WXWork)
      (message, ImsAlertLevel.WARN)
    }
    val alertDesc = new ImsAlertDesc(
      Constants.ALERT_SUB_SYSTEM_ID,
      "BDP Alert",
      alertObj,
      alertInfo,
      alertLevel,
      localIp,
      0,
      _alertWays
    )
    addAlert(alertDesc)
  }

  def addAlert(alertDesc: AlertDesc): Unit = {
    if (!alertDesc.isInstanceOf[ImsAlertDesc]) {
      logger.warn("Ignore wrong alertDesc. DataType: " + alertDesc.getClass.getCanonicalName)
    } else {
      sender.addAlertToPool(alertDesc)
      logger.info("successfully added alert")
    }
  }

  def addAlertAndLogException(alertDesc: ImsAlertDesc): Unit =
    Utils.tryAndError(addAlert(alertDesc))

  def clearAlert(alertDesc: ImsAlertDesc): Unit = {
    assert(alertDesc.canRecover == 1)
    assert(alertDesc.alertLevel == ImsAlertLevel.CLEAR)
    sender.addAlertToPool(alertDesc)
  }

  def shutDown(waitComplete: Boolean = true, timeoutMs: Long = -1): Unit = {
    sender.shutdown(waitComplete, timeoutMs)
  }

}
