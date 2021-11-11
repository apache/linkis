/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.cs.highavailable.ha.instancealias.impl

import java.util
import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.rpc.conf.RPCConfiguration
import org.apache.linkis.cs.highavailable.ha.instancealias.{InstanceAliasConverter, InstanceAliasManager}
import org.apache.linkis.rpc.sender.eureka.EurekaRPCServerLoader
import org.apache.linkis.rpc.utils.RPCUtils
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._

@Component
class InstanceAliasManagerImpl extends InstanceAliasManager with Logging {

  private val serverLoader = new EurekaRPCServerLoader()

  @Autowired
  var instanceAliasConverter: InstanceAliasConverter = _

  override def getInstanceByAlias(alias: String): ServiceInstance = {
    val serviceID = getContextServiceID()
    if (null == serviceID) {
      return null
    }
    val instances = serverLoader.getServiceInstances(serviceID)
    if (null == instances || instances.isEmpty) {
      error(s"None serviec instances for Context Service ID : " + serviceID)
      return null
    }
    val targetInstance = instanceAliasConverter.aliasToInstance(alias)
    if (StringUtils.isBlank(targetInstance)) {
      return null
    }
    instances.foreach(ins => {
      if (ins.getInstance.equalsIgnoreCase(targetInstance)) {
        return ins
      }
    })
    null
  }

  def getContextServiceID(): String = {
    RPCUtils.findService (RPCConfiguration.CONTEXT_SERVICE_APPLICATION_NAME.getValue, list => {
    val services = list.filter (_.contains (RPCConfiguration.CONTEXT_SERVICE_APPLICATION_NAME.getValue) )
      services.headOption
    }).getOrElse(null)
  }

  @Deprecated
  override def refresh(): Unit = {

  }

  override def getAllInstanceList(): util.List[ServiceInstance] = {
    val serviceID = getContextServiceID()
    if (null == serviceID) {
      return new util.ArrayList[ServiceInstance](0)
    }
    serverLoader.getServiceInstances(serviceID).toList
  }

  override def isInstanceAliasValid(alias: String): Boolean = {
    if (!instanceAliasConverter.checkAliasFormatValid(alias)) {
      return false
    }
    if (null != getInstanceByAlias(alias)) {
      true
    } else {
      false
    }
  }

  override def getAliasByServiceInstance(instance: ServiceInstance): String = {
    if (null == instance) {
      return null
    }
    instanceAliasConverter.instanceToAlias(instance.getInstance)
  }
}

