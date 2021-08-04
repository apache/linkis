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

package com.webank.wedatasphere.linkis.rpc.interceptor.common

import java.net.ConnectException

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.exception.LinkisRetryException
import com.webank.wedatasphere.linkis.common.utils.RetryHandler
import com.webank.wedatasphere.linkis.protocol.RetryableProtocol
import com.webank.wedatasphere.linkis.rpc.exception.DWCRPCRetryException
import com.webank.wedatasphere.linkis.rpc.interceptor.{RPCInterceptor, RPCInterceptorChain, RPCInterceptorExchange, ServiceInstanceRPCInterceptorChain}
import com.webank.wedatasphere.linkis.rpc.utils.RPCUtils
import feign.RetryableException
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component


@Component
class RetryableRPCInterceptor extends RPCInterceptor {
  override val order: Int = 20

//  private val commonRetryHandler = new RPCRetryHandler
//  commonRetryHandler.setRetryInfo(new RetryableProtocol{})
//
//  private def isCommonRetryHandler(retry: RetryableProtocol): Boolean = retry.maxPeriod == commonRetryHandler.getRetryMaxPeriod &&
//    retry.period == commonRetryHandler.getRetryPeriod && retry.retryNum == commonRetryHandler.getRetryNum &&
//    (retry.retryExceptions.isEmpty || commonRetryHandler.getRetryExceptions.containsSlice(retry.retryExceptions))

  override def intercept(interceptorExchange: RPCInterceptorExchange, chain: RPCInterceptorChain): Any = interceptorExchange.getProtocol match {
    case retry: RetryableProtocol =>
      val retryName = retry.getClass.getSimpleName
//      if(isCommonRetryHandler(retry)) commonRetryHandler.retry(chain.handle(interceptorExchange), retryName)
//      else {
        val retryHandler = new RPCRetryHandler
        retryHandler.setRetryInfo(retry, chain)
        retryHandler.retry(chain.handle(interceptorExchange), retryName)
//      }
    case _ => chain.handle(interceptorExchange)
  }

  class RPCRetryHandler extends RetryHandler {
    addRetryException(classOf[ConnectException])
    addRetryException(classOf[RetryableException])
    private var serviceInstance: Option[ServiceInstance] = None
    def setRetryInfo(retry: RetryableProtocol, chain: RPCInterceptorChain): Unit ={
      setRetryNum(retry.retryNum)
      setRetryPeriod(retry.period)
      setRetryMaxPeriod(retry.maxPeriod)
      retry.retryExceptions.foreach(addRetryException)
      chain match {
        case s: ServiceInstanceRPCInterceptorChain => serviceInstance = Option(s.getServiceInstance)
        case _ =>
      }
    }

    private def isNoServiceException(t: Throwable): Boolean = RPCUtils.isReceiverNotExists(t)

    override def exceptionCanRetry(t: Throwable): Boolean = t match {
      case _: DWCRPCRetryException => true
      case r: LinkisRetryException => r.getErrCode == DWCRPCRetryException.RPC_RETRY_ERROR_CODE
      case _ => (serviceInstance.exists(s => StringUtils.isBlank(s.getInstance)) && isNoServiceException(t)) || super.exceptionCanRetry(t)
    }
  }
}
object RetryableRPCInterceptor {
  def isRetryableProtocol(message: Any): Boolean = message match {
    case protocol: RetryableProtocol => true
    case _ => false
  }
}