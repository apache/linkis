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

package org.apache.linkis.gateway.ujes.route.contextservice

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.cs.common.entity.source.ContextIDParser
import org.apache.linkis.cs.common.utils.CSHighAvailableUtils
import org.apache.linkis.instance.label.service.InsLabelServiceAdapter
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.protocol.label.LabelInsQueryRequest

import org.apache.commons.lang3.StringUtils

import org.springframework.stereotype.Component

import javax.annotation.Resource

import java.util

/**
 * Description: If id is correct format of ContextHAId, will parse it to get the instance and backup
 * instances.
 */
@Component
class ContextIdParserImpl extends ContextIDParser with Logging {

  @Resource
  private var insLabelService: InsLabelServiceAdapter = _

  override def parse(contextId: String): util.List[String] = {

    if (CSHighAvailableUtils.checkHAIDBasicFormat(contextId)) {
      val instances = new util.ArrayList[String](2)
      val haContextID = CSHighAvailableUtils.decodeHAID(contextId)
      val mainInstance = getInstanceByAlias(haContextID.getInstance())
      if (null != mainInstance) {
        instances.add(mainInstance.getInstance)
      } else {
        logger.error(s"parse HAID instance invalid. haIDKey : " + contextId)
      }
      val backupInstance = getInstanceByAlias(haContextID.getBackupInstance())
      if (null != backupInstance) {
        instances.add(backupInstance.getInstance)
      } else {
        logger.error("parse HAID backupInstance invalid. haIDKey : " + contextId)
      }
      instances
    } else {
      new util.ArrayList[String](0)
    }
  }

  // todo same as that in RouteLabelInstanceAliasConverter
  private def getInstanceByAlias(alias: String): ServiceInstance = {
    if (StringUtils.isNotBlank(alias)) {
      Utils.tryAndError {
        val request = new LabelInsQueryRequest()
        val labelMap = new util.HashMap[String, Any]()
        labelMap.put(LabelKeyConstant.ROUTE_KEY, alias)
        request.setLabels(labelMap.asInstanceOf[util.HashMap[String, Object]])
        var serviceInstance: ServiceInstance = null
        val labels = new util.ArrayList[Label[_]]()
        labels.add(
          LabelBuilderFactoryContext.getLabelBuilderFactory
            .createLabel[Label[_]](LabelKeyConstant.ROUTE_KEY, alias)
        )
        val insList = insLabelService.searchInstancesByLabels(labels)
        if (null != insList && !insList.isEmpty) {
          serviceInstance = insList.get(0)
        }
        serviceInstance
      }
    } else {
      null
    }
  }

}
