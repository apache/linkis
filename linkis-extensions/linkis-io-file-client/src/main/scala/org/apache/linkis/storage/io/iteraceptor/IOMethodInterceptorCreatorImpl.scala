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

package org.apache.linkis.storage.io.iteraceptor

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.storage.io.{IOMethodInterceptorCreator, IOMethodInterceptorFactory}
import org.apache.linkis.storage.io.client.IOClient

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cglib.proxy.MethodInterceptor
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component("ioMethod")
class IOMethodInterceptorCreatorImpl extends IOMethodInterceptorCreator with Logging {

  @Autowired
  private var ioClient: IOClient = _

  @PostConstruct
  def init(): Unit = {
    logger.info("IOMethodInterceptorCreatorImpl finished init")
    IOMethodInterceptorFactory.register(this)
  }

  override def createIOMethodInterceptor(fsName: String): MethodInterceptor = {
    val ioMethodInterceptor = new IOMethodInterceptor(fsName)
    ioMethodInterceptor.setIoClient(ioClient)
    ioMethodInterceptor
  }

}
