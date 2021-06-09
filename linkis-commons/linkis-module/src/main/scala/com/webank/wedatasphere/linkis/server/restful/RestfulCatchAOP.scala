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

package com.webank.wedatasphere.linkis.server.restful

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.server.{Message, catchIt}
import javax.ws.rs.core.Response
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.{Around, Aspect, Pointcut}
import org.springframework.stereotype.Component


@Aspect
@Component
class RestfulCatchAOP extends Logging {

  @Pointcut("@annotation(javax.ws.rs.Path) && execution(public com.webank.wedatasphere.linkis.server.Message *(..))")
  def restfulMessageCatch() : Unit = {}

  @Around("restfulMessageCatch()")
  def dealMessageRestful(proceedingJoinPoint: ProceedingJoinPoint): Object = catchIt {
    proceedingJoinPoint.proceed().asInstanceOf[Message]
  }

  @Pointcut("@annotation(javax.ws.rs.Path) && execution(public javax.ws.rs.core.Response *(..)))")
  def restfulResponseCatch() : Unit = {}

  @Around("restfulResponseCatch()")
  def dealResponseRestful(proceedingJoinPoint: ProceedingJoinPoint): Object = {
    val resp: Response = catchIt {
      return proceedingJoinPoint.proceed()
    }
    resp
  }

}
