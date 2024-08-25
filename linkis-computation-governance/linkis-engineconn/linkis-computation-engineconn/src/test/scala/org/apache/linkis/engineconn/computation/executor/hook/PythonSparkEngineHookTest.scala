/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineconn.computation.executor.hook

import org.mockito.Mockito

import scala.tools.fusesource_embedded.jansi.AnsiRenderer.test


class PythonSparkEngineHookTest extends AnyFunSuite {

  test("constructCode 返回正确的加载代码") {
    val pythonModuleInfo = new PythonModuleInfoVO
    pythonModuleInfo.setPath("file:///path/to/module.py")

    val hook = new PythonSparkEngineHook
    val result = hook.constructCode(pythonModuleInfo)

    assert(result == "sc.addPyFile('file:///path/to/module.py')")
  }

  test("constructCode 返回正确的日志信息") {
    val pythonModuleInfo = new PythonModuleInfoVO
    pythonModuleInfo.setPath("file:///path/to/module.py")

    val hook = new PythonSparkEngineHook
    val logger = Mockito.mock(classOf[org.slf4j.Logger])
    hook.logger = logger

    hook.constructCode(pythonModuleInfo)

    val expectedLog = "pythonLoadCode: sc.addPyFile('file:///path/to/module.py')"
    Mockito.verify(logger).info(expectedLog)
  }
}
