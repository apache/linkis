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

package org.apache.linkis.storage.io;

import org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary;
import org.apache.linkis.storage.exception.StorageWarnException;

import org.springframework.cglib.proxy.MethodInterceptor;

public class IOMethodInterceptorFactory {

  private static IOMethodInterceptorCreator interceptorCreator = null;

  private IOMethodInterceptorFactory() {}

  /**
   * This method is called when ioClient is initialized. ioClient初始化时会调用该方法
   *
   * @param interceptorCreator
   */
  public static void register(IOMethodInterceptorCreator interceptorCreator) {
    IOMethodInterceptorFactory.interceptorCreator = interceptorCreator;
  }

  public static MethodInterceptor getIOMethodInterceptor(String fsName)
      throws StorageWarnException {
    if (IOMethodInterceptorFactory.interceptorCreator == null) {
      throw new StorageWarnException(
          LinkisStorageErrorCodeSummary.MUST_REGISTER_TOM.getErrorCode(),
          LinkisStorageErrorCodeSummary.MUST_REGISTER_TOM.getErrorDesc());
    }
    return IOMethodInterceptorFactory.interceptorCreator.createIOMethodInterceptor(fsName);
  }
}
