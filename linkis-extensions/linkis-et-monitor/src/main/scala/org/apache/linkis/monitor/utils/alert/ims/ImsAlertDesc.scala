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

import org.apache.linkis.monitor.constants.Constants
import org.apache.linkis.monitor.utils.ScanUtils
import org.apache.linkis.monitor.utils.alert.AlertDesc

import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.StringUtils

import java.util
import java.util.HashSet

import scala.collection.JavaConverters._

import ImsAlertLevel.ImsAlertLevel
import ImsAlertWay.ImsAlertWay

case class ImsAlertDesc(
    var subSystemId: String,
    var alertTitle: String,
    var alertObj: String,
    var alertInfo: String,
    alertLevel: ImsAlertLevel = ImsAlertLevel.INFO,
    alertIp: String,
    canRecover: Int = 0, // 默认0，为1时，需要有对应的恢复告警
    alertWays: util.Set[ImsAlertWay] = new HashSet[ImsAlertWay],
    var alertReceivers: util.Set[String] = new HashSet[String],
    var numHit: Int = 0,
    var hitIntervalMs: Long = 0L
) extends AlertDesc {

  override val toString: String = {
    val sb = new StringBuilder
    sb.append("sub_system_id=").append(subSystemId).append("&alert_title=").append(alertTitle)
    if (alertLevel != null) sb.append("&alert_level=").append(alertLevel.toString)
    if (StringUtils.isNotEmpty(alertObj)) sb.append("&alert_obj=").append(alertObj)
    if (StringUtils.isNotEmpty(alertInfo)) {
      sb.append("&alert_info=")
        .append(alertInfo)
        .append(
          "[freq_info] hit " + numHit + " time(s) within " + hitIntervalMs / 1000 / 60 + " mins"
        )
    }
    if (canRecover == 0 || canRecover == 1) sb.append("&can_recover=").append(canRecover)
    if (alertWays != null && alertWays.size > 0) {
      sb.append("&alert_way=")
      sb.append(alertWays.asScala.map(_.toString).mkString(","))
    }
    if (alertReceivers != null && alertReceivers.size > 0) {
      sb.append("&alert_reciver=")
      sb.append(alertReceivers.asScala.mkString(","))
    }
    if (alertIp != null) {
      sb.append("&alert_ip=").append(alertIp)

    }
    sb.toString
  }

  val toMap: Map[String, String] = {
    val map = scala.collection.mutable.Map[String, String]()
    map += "sub_system_id" -> subSystemId
    map += "alert_title" -> alertTitle
    if (alertLevel != null) map += "alert_level" -> alertLevel.toString
    if (StringUtils.isNotEmpty(alertObj)) map += "alert_obj" -> alertObj
    if (StringUtils.isNotEmpty(alertInfo)) {
      map += "alert_info" + "[freq_info] hit " + numHit + " time(s) within " + hitIntervalMs / 1000 / 60 + " mins" -> alertInfo
    }
    if (canRecover == 0 || canRecover == 1) map += "can_recover" -> canRecover.toString
    if (alertWays != null && alertWays.size > 0) {
      map += "alert_way" -> alertWays.asScala.map(_.toString).mkString(",")
    }
    if (alertReceivers != null && alertReceivers.size > 0) {
      map += "alert_reciver" -> alertReceivers.asScala.mkString(",")
    }
    map.toMap
  }

  val toImsRequest: ImsRequest = {
    val params = validate()
    val alertEntity = AlertEntity(
      params(0).asInstanceOf[String],
      params(1).asInstanceOf[String],
      params(
        3
      ) + "[freq_info] hit " + numHit + " time(s) within " + hitIntervalMs / 1000 / 60 + " mins",
      alertWays.asScala.map(_.toString).mkString(","),
      params(4).asInstanceOf[util.Set[String]].asScala.mkString(","),
      alertLevel.toString,
      params(2).asInstanceOf[String],
      canRecover.toString
    )

    val alertEntityList = new util.ArrayList[AlertEntity]
    alertEntityList.add(alertEntity)

    ImsRequest(alertEntityList)
  }

  def validate(): Array[Any] = {
    assert(StringUtils.isNumeric(subSystemId) && subSystemId.length == 4)
    assert(StringUtils.isNotEmpty(alertTitle))
    val newAlertTitle = if (alertTitle.length > 100) {
      alertTitle.substring(0, 96) + "... ..."
    } else {
      alertTitle
    }
    val newAlertObj = if (StringUtils.isNotEmpty(alertObj) && alertObj.length >= 50) {
      alertObj = alertObj.substring(0, 36) + "... ..."
    } else {
      alertObj
    }
    val newAlertInfo =
      if (
          StringUtils.isNotEmpty(alertInfo) && ScanUtils.getNumOfLines(
            alertInfo
          ) > Constants.ALERT_IMS_MAX_LINES
      ) {
        StringUtils.substring(
          alertInfo,
          0,
          ScanUtils.getFirstIndexSkippingLines(alertInfo, Constants.ALERT_IMS_MAX_LINES)
        ) + "... ...\n"
      } else {
        alertInfo
      }
    val newAlertReceivers =
      if (CollectionUtils.isNotEmpty(alertReceivers) && alertReceivers.size > 15) {
        alertReceivers.asScala.take(15)
      } else {
        alertReceivers
      }

    Array(subSystemId, newAlertTitle, newAlertObj, newAlertInfo, newAlertReceivers)
  }

}
