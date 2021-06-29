/*
 *
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.linkis.manager.am.service.em

import com.webank.wedatasphere.linkis.manager.common.protocol.em.RegisterEMRequest
import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext


trait EMRegisterService {


  /**
    * EM注册请求的第一个处理的请求，用于插入Instance信息
    *
    * @param emRegister
    */
  def addEMNodeInstance(emRegister: RegisterEMRequest, scm: ServiceMethodContext): Unit

  /**
    * EM注册插入的初始Metrics信息
    *
    * @param emRegister
    */
  def addEMNodeMetrics(emRegister: RegisterEMRequest): Unit

}
