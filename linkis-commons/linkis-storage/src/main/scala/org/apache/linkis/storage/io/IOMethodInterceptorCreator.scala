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
 
package org.apache.linkis.storage.io

import org.apache.linkis.storage.exception.StorageErrorException
import net.sf.cglib.proxy.MethodInterceptor


trait IOMethodInterceptorCreator {

  def createIOMethodInterceptor(fsName: String): MethodInterceptor
}

object IOMethodInterceptorCreator {

  var interceptorCreator: IOMethodInterceptorCreator = null


  /**
    * This method is called when ioClient is initialized.
    * ioClient初始化时会调用该方法
    *
    * @param interceptorCreator
    */
  def register(interceptorCreator: IOMethodInterceptorCreator): Unit = {
    this.interceptorCreator = interceptorCreator
  }

  def getIOMethodInterceptor(fsName: String): MethodInterceptor = {
    if (interceptorCreator == null) throw new StorageErrorException(52004, "You must register IOMethodInterceptorCreator before you can use proxy mode.(必须先注册IOMethodInterceptorCreator，才能使用代理模式)")
    interceptorCreator.createIOMethodInterceptor(fsName)
  }
}