package com.webank.wedatasphere.linkis.rpc.instancealias.impl

import java.util

import com.webank.wedatasphere.linkis.DataWorkCloudApplication
import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.rpc.conf.RPCConfiguration
import com.webank.wedatasphere.linkis.rpc.instancealias.{InstanceAliasConverter, InstanceAliasManager}
import com.webank.wedatasphere.linkis.rpc.sender.eureka.EurekaRPCServerLoader
import com.webank.wedatasphere.linkis.rpc.utils.RPCUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._

/**
 * @Author alexyang
 * @Date 2020/2/18
 */
@Component
class InstanceAliasManagerImpl extends InstanceAliasManager with Logging {

  private val serverLoader = new EurekaRPCServerLoader()

  private val mainInstance = DataWorkCloudApplication.getServiceInstance

  @Autowired
  var instanceAliasConverter: InstanceAliasConverter = _

  override def getAliasByInstance(instance: String): String  = {
    instanceAliasConverter.instanceToAlias(instance)
  }

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

