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

package org.apache.linkis.server.restful

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.server.{catchIt, Message}

import org.springframework.stereotype.Component
import org.springframework.web.context.request.{RequestContextHolder, ServletRequestAttributes}

import javax.servlet.http.HttpServletResponse

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.{Around, Aspect, Pointcut}

@Aspect
@Component
class SpringRestfulCatchAOP extends Logging {

  @Pointcut(
    "@annotation(org.springframework.web.bind.annotation.RequestMapping) && execution(public org.apache.linkis.server.Message *(..)))"
  )
  def springRestfulResponseCatch(): Unit = {}

  @Around("springRestfulResponseCatch()")
  def dealResponseRestful(proceedingJoinPoint: ProceedingJoinPoint): Object = {
    val resp: Message = catchIt {
      proceedingJoinPoint.proceed() match {
        case message: Message => message
        case _ => Message.error("Failed to dealResponse get null message")
      }
    }
    // convert http status code
    val response = getCurrentHttpResponse
    if (response != null) {
      getCurrentHttpResponse.setStatus(Message.messageToHttpStatus(resp))
    }
    resp
  }

  def getCurrentHttpResponse: HttpServletResponse = {
    val requestAttributes = RequestContextHolder.getRequestAttributes
    requestAttributes match {
      case attributes: ServletRequestAttributes =>
        attributes.getResponse
      case _ => null
    }
  }

}
