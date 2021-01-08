package com.webank.wedatasphere.linkis.gateway.ujes.route

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.cs.common.entity.source.{ContextID, ContextIDParser}
import com.webank.wedatasphere.linkis.cs.common.protocol.ContextHTTPConstant
import com.webank.wedatasphere.linkis.cs.common.serialize.helper.ContextSerializationHelper
import com.webank.wedatasphere.linkis.gateway.http.GatewayContext
import com.webank.wedatasphere.linkis.gateway.route.AbstractGatewayRouter
import com.webank.wedatasphere.linkis.gateway.springcloud.SpringCloudGatewayConfiguration.{API_URL_PREFIX, normalPath}
import com.webank.wedatasphere.linkis.rpc.interceptor.ServiceInstanceUtils
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.util.Random
import scala.util.matching.Regex

/**
 * created by cooperyang on 2020/2/19
 * Description:
 */
@Component
class HaContextGatewayRouter extends AbstractGatewayRouter{

  @Autowired
  private var contextIDParser: ContextIDParser = _
  private val serializationHelper = ContextSerializationHelper.getInstance();

  override def route(gatewayContext: GatewayContext): ServiceInstance = {

    if (gatewayContext.getGatewayRoute.getRequestURI.contains("contextservice")){
      val params: util.HashMap[String, String] = gatewayContext.getGatewayRoute.getParams
      if (!gatewayContext.getRequest.getQueryParams.isEmpty) {
        for ((k, vArr) <- gatewayContext.getRequest.getQueryParams) {
          if (vArr.nonEmpty) {
            params.putIfAbsent(k, vArr.head)
          }
        }
      }
      if (gatewayContext.getRequest.getHeaders.containsKey(ContextHTTPConstant.CONTEXT_ID_STR)) {
        params.putIfAbsent(ContextHTTPConstant.CONTEXT_ID_STR, gatewayContext.getRequest.getHeaders.get(ContextHTTPConstant.CONTEXT_ID_STR)(0))
      }
      if (null == params || params.isEmpty) {
        dealContextCreate(gatewayContext)
      } else {
        var contextId : String = null
        for ((key, value) <- params) {
          if (key.equalsIgnoreCase(ContextHTTPConstant.CONTEXT_ID_STR)) {
            contextId = value
            }
        }
        if (StringUtils.isNotBlank(contextId)) {
          dealContextAccess(contextId.toString, gatewayContext)
        } else {
          dealContextCreate(gatewayContext)
        }
      }
    }else{
      null
    }
  }

  def dealContextCreate(gatewayContext:GatewayContext):ServiceInstance = {
    val serviceId =  findService(HaContextGatewayRouter.CONTEXT_SERVICE_STR, list => {
      val services = list.filter(_.contains(HaContextGatewayRouter.CONTEXT_SERVICE_STR))
      services.headOption
    })
    val serviceInstances = ServiceInstanceUtils.getRPCServerLoader.getServiceInstances(serviceId.orNull)
    if (serviceInstances.size > 0) {
      val index = new Random().nextInt(serviceInstances.size)
      serviceInstances(index)
    } else {
      logger.error(s"No valid instance for service : " + serviceId.orNull)
      null
    }
  }

  def dealContextAccess(contextIdStr:String, gatewayContext: GatewayContext):ServiceInstance = {
    val contextId : String = {
      var tmpId : String = null
      if (serializationHelper.accepts(contextIdStr)) {
        val contextID : ContextID = serializationHelper.deserialize(contextIdStr).asInstanceOf[ContextID]
        if (null != contextID) {
          tmpId = contextID.getContextId
        } else {
          error(s"Deserializate contextID null. contextIDStr : " + contextIdStr)
        }
      } else {
        error(s"ContxtIDStr cannot be deserialized. contextIDStr : " + contextIdStr)
      }
      if (null == tmpId) {
        contextIdStr
      } else {
        tmpId
      }
    }
    val instances = contextIDParser.parse(contextId)
    var serviceId:Option[String] = None
    serviceId = findService(HaContextGatewayRouter.CONTEXT_SERVICE_STR, list => {
      val services = list.filter(_.contains(HaContextGatewayRouter.CONTEXT_SERVICE_STR))
        services.headOption
      })
    val serviceInstances = ServiceInstanceUtils.getRPCServerLoader.getServiceInstances(serviceId.orNull)
    if (instances.size() > 0) {
      serviceId.map(ServiceInstance(_, instances.get(0))).orNull
    } else if (serviceInstances.size > 0) {
      serviceInstances(0)
    } else {
      logger.error(s"No valid instance for service : " + serviceId.orNull)
      null
    }
  }

}


object HaContextGatewayRouter{
  val CONTEXT_ID_STR:String = "contextId"
  val CONTEXT_SERVICE_STR:String = "contextservice"
  val CONTEXT_REGEX: Regex = (normalPath(API_URL_PREFIX) + "rest_[a-zA-Z][a-zA-Z_0-9]*/(v\\d+)/contextservice/" + ".+").r
}
