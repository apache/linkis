/*
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

package com.webank.wedatasphere.linkis.server.restful

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.server.{Message, catchIt}
import javax.servlet.http.HttpServletResponse
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.{Around, Aspect, Pointcut}
import org.springframework.stereotype.Component
import org.springframework.web.context.request.{RequestContextHolder, ServletRequestAttributes}


@Aspect
@Component
class SpringRestfulCatchAOP extends Logging {

  @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) && execution(public com.webank.wedatasphere.linkis.server.Message *(..)))")
  def springRestfulResponseCatch() : Unit = {}

  @Around("springRestfulResponseCatch()")
  def dealResponseRestful(proceedingJoinPoint: ProceedingJoinPoint): Object = {
    val resp: Message = catchIt {
      return proceedingJoinPoint.proceed()
    }
    // convert http status code
    getCurrentHttpResponse.setStatus(Message.messageToHttpStatus(resp))
    resp
  }

  def getCurrentHttpResponse: HttpServletResponse = {
    val requestAttributes = RequestContextHolder.getRequestAttributes
    if (requestAttributes.isInstanceOf[ServletRequestAttributes]) {
      val response = requestAttributes.asInstanceOf[ServletRequestAttributes].getResponse
      return response
    }
    null
  }

}
