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
 
package org.apache.linkis.rpc.sender

import java.lang.reflect.Field

import com.netflix.client.ClientRequest
import com.netflix.client.config.IClientConfig
import com.netflix.loadbalancer.reactive.LoadBalancerCommand
import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.conf.{Configuration => DWCConfiguration}
import org.apache.linkis.protocol.Protocol
import org.apache.linkis.rpc.interceptor.{RPCInterceptor, RPCLoadBalancer, ServiceInstanceRPCInterceptorChain}
import org.apache.linkis.rpc.transform.RPCConsumer
import org.apache.linkis.rpc.{BaseRPCSender, RPCMessageEvent, RPCSpringBeanCache}
import org.apache.linkis.server.{BDPJettyServerHelper, Message}
import feign._
import org.apache.commons.lang.StringUtils
import org.springframework.cloud.netflix.ribbon.ServerIntrospector
import org.springframework.cloud.openfeign.ribbon.{CachingSpringLoadBalancerFactory, FeignLoadBalancer, LoadBalancerFeignClient}


private[rpc] class SpringMVCRPCSender private[rpc](private[rpc] val serviceInstance: ServiceInstance)
  extends BaseRPCSender(serviceInstance.getApplicationName) {

  import SpringCloudFeignConfigurationCache._

  override protected def getRPCInterceptors: Array[RPCInterceptor] = RPCSpringBeanCache.getRPCInterceptors

  override protected def createRPCInterceptorChain() = new ServiceInstanceRPCInterceptorChain(0, getRPCInterceptors, serviceInstance)

  protected def getRPCLoadBalancers: Array[RPCLoadBalancer] = RPCSpringBeanCache.getRPCLoadBalancers

  override protected def doBuilder(builder: Feign.Builder): Unit = {
    val client = getClient.asInstanceOf[LoadBalancerFeignClient]
    val newClient = new LoadBalancerFeignClient(client.getDelegate, new CachingSpringLoadBalancerFactory(getClientFactory) {
      override def create(clientName: String): FeignLoadBalancer = {
        val serverIntrospector = getClientFactory.getInstance(clientName, classOf[ServerIntrospector])
        new FeignLoadBalancer(getClientFactory.getLoadBalancer(clientName), getClientFactory.getClientConfig(clientName), serverIntrospector) {
          override def customizeLoadBalancerCommandBuilder(request: FeignLoadBalancer.RibbonRequest, config: IClientConfig,
                                                           builder: LoadBalancerCommand.Builder[FeignLoadBalancer.RibbonResponse]): Unit = {
            val instance = if(getRPCLoadBalancers.isEmpty) None else {
              val requestBody = SpringMVCRPCSender.getRequest(request).body()
              val requestStr = new String(requestBody, DWCConfiguration.BDP_ENCODING.getValue)
              val obj = RPCConsumer.getRPCConsumer.toObject(BDPJettyServerHelper.gson.fromJson(requestStr, classOf[Message]))
              obj match {
                case protocol: Protocol =>
                  var serviceInstance: Option[ServiceInstance] = None
                  for (lb <- getRPCLoadBalancers if serviceInstance.isEmpty)
                    serviceInstance = lb.choose(protocol, SpringMVCRPCSender.this.serviceInstance, getLoadBalancer)
                  serviceInstance.foreach(f =>
                    info("origin serviceInstance: " + SpringMVCRPCSender.this.serviceInstance + ", chose serviceInstance: " + f)) //TODO just for test
                  serviceInstance
                case _ => None
              }
            }
            instance.orElse(Option(SpringMVCRPCSender.this.serviceInstance)).filter(s => StringUtils.isNotBlank(s.getInstance))
              .foreach { serviceInstance =>
                val server = RPCSpringBeanCache.getRPCServerLoader.getServer(getLoadBalancer, serviceInstance)
                builder.withServer(server)
              }
          }
        }
      }
    }, getClientFactory)
    super.doBuilder(builder)
    builder.contract(getContract)
      .encoder(getEncoder).decoder(getDecoder)
      .client(newClient).requestInterceptor(getRPCTicketIdRequestInterceptor)
  }


  /**
    * Deliver is an asynchronous method that requests the target microservice asynchronously, ensuring that the target microservice is requested once,
    * but does not guarantee that the target microservice will successfully receive the request.
    * deliver是一个异步方法，该方法异步请求目标微服务，确保一定会请求目标微服务一次，但不保证目标微服务一定能成功接收到本次请求。
    * @param message Requested parameters(请求的参数)
    */
  override def deliver(message: Any): Unit = getRPCSenderListenerBus.post(RPCMessageEvent(message, serviceInstance))

  override def equals(obj: Any): Boolean = if(obj == null) false
    else obj match {
      case sender: SpringMVCRPCSender => sender.serviceInstance == serviceInstance
      case _ => false
    }

  override def hashCode(): Int = serviceInstance.hashCode()

  override val toString: String = if(StringUtils.isBlank(serviceInstance.getInstance)) s"RPCSender(${serviceInstance.getApplicationName})"
    else s"RPCSender($getApplicationName, ${serviceInstance.getInstance})"
}
private object SpringMVCRPCSender {
  private var requestField: Field = _
  def getRequest(req: ClientRequest): Request = {
    if(requestField == null) synchronized {
      if(requestField == null) {
        requestField = req.getClass.getDeclaredField("request")
        requestField.setAccessible(true)
      }
    }
    requestField.get(req).asInstanceOf[Request]
  }
}