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

package com.webank.wedatasphere.linkis.cs.highavailable.ha.instancealias.impl

import com.google.common.cache.CacheBuilder
import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException
import com.webank.wedatasphere.linkis.cs.highavailable.conf.ContextHighAvailableConf
import com.webank.wedatasphere.linkis.cs.highavailable.exception.CSErrorCode
import com.webank.wedatasphere.linkis.cs.highavailable.ha.instancealias.InstanceAliasConverter
import com.webank.wedatasphere.linkis.instance.label.client.InstanceLabelClient
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.route.RouteLabel
import com.webank.wedatasphere.linkis.rpc.Sender
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

import java.util
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import scala.collection.JavaConverters.asScalaBufferConverter


@Component
class RouteLabelInstanceAliasConverter extends InstanceAliasConverter with Logging {

  private val MAX_ID_INSTANCE_CACHE_SUM = 100000

  private var instanceLabelClient: InstanceLabelClient = _

  private val thisInstance = Sender.getThisServiceInstance

  private val insAliasCache = CacheBuilder.newBuilder()
    .maximumSize(MAX_ID_INSTANCE_CACHE_SUM)
    .expireAfterWrite(ContextHighAvailableConf.CS_ALIAS_CACHE_EXPIRE_TIMEMILLS.getValue, TimeUnit.MILLISECONDS)
    .build[String, String]()
  private val aliasInsCache = CacheBuilder.newBuilder()
    .maximumSize(MAX_ID_INSTANCE_CACHE_SUM)
    .expireAfterWrite(ContextHighAvailableConf.CS_ALIAS_CACHE_EXPIRE_TIMEMILLS.getValue, TimeUnit.MILLISECONDS)
    .build[String, String]()

  @PostConstruct
  private def init(): Unit = {
    this.instanceLabelClient = new InstanceLabelClient()
  }

  override def instanceToAlias(instance: String): String = {
    if (StringUtils.isNotBlank(instance)) {
      val alias = insAliasCache.getIfPresent(instance)
      if (null != alias) {
        return alias
      }
      var labels: util.List[Label[_]] = null
      Utils.tryCatch {
        labels = instanceLabelClient.getLabelFromInstance(ServiceInstance(thisInstance.getApplicationName, instance))
      } {
        case e: Exception =>
          val msg = s"GetLabelFromInstance for instance : ${instance} failed, ${e.getMessage}"
          error(msg, e)
          throw new CSErrorException(CSErrorCode.CS_RPC_ERROR, msg, e)
      }
      if (null != labels && labels.size() > 0) {
        val routeLabels = labels.asScala.filter(_ != null).filter(l => LabelKeyConstant.ROUTE_KEY.equals(l.getLabelKey) && l.getStringValue.startsWith(ContextHighAvailableConf.CONTEXTSERVICE_PREFIX.getValue))
        if (routeLabels.size != 1) {
          warn(s"Instance ${instance} has no or more than one route label : ${routeLabels.map(_.getStringValue)}")
        }
        if (routeLabels.size >= 1) {
          val alias = routeLabels.head.getStringValue
          insAliasCache.put(instance, alias)
          routeLabels.head.getStringValue
        } else {
          val msg = s"No routeLabel got for instance : ${instance}"
          error(msg)
          throw new CSErrorException(CSErrorCode.INVALID_INSTANCE_ALIAS, msg)
        }
      } else {
        val msg = s"Null routeLabel got for instance : ${instance}"
        error(msg)
        throw new CSErrorException(CSErrorCode.INVALID_INSTANCE_ALIAS, msg)
      }
    } else {
      throw new CSErrorException(CSErrorCode.INVALID_INSTANCE, "Invalid null instance.")
    }
  }

  override def aliasToInstance(alias: String): String = {
    if (StringUtils.isNotBlank(alias)) {
      val instance = aliasInsCache.getIfPresent(alias)
      if (StringUtils.isNotBlank(instance)) {
        return instance
      }
      val routeLabel = LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel[RouteLabel](LabelKeyConstant.ROUTE_KEY, alias)
      val labels = new util.ArrayList[Label[_]]
      labels.add(routeLabel)
      var insList: util.List[ServiceInstance] = null
      Utils.tryCatch {
        insList = instanceLabelClient.getInstanceFromLabel(labels)
      } {
        case e: Exception =>
          val msg = s"GetInsFromLabel rpc failed : ${e.getMessage}"
          error(msg, e)
          throw new CSErrorException(CSErrorCode.CS_RPC_ERROR, msg, e)
      }
      if (null != insList) {
        if (insList.size() >= 1) {
          if (insList.size() > 1) {
            warn(s"Got ${insList.size()} instances more than 1 from alias ${alias}.")
          }
          val ins = insList.get(0).getInstance
          aliasInsCache.put(alias, ins)
          ins
        } else {
          val msg = s"Got no instances form alias ${alias}."
          error(msg)
          throw new CSErrorException(CSErrorCode.INVALID_INSTANCE, msg)
        }
      } else {
        val msg = s"Got no instances form alias ${alias}."
        error(msg)
        throw new CSErrorException(CSErrorCode.INVALID_INSTANCE, msg)
      }
    } else {
      throw new CSErrorException(CSErrorCode.INVALID_INSTANCE_ALIAS, "Invalid null alias.")
    }
  }

  override def checkAliasFormatValid(alias: String): Boolean = StringUtils.isNotBlank(alias)
}
