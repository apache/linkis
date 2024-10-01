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

package org.apache.linkis.engineconn.computation.executor.hook

import org.apache.linkis.engineconn.common.creation.{
  DefaultEngineCreationContext,
  EngineCreationContext
}
import org.apache.linkis.engineconn.common.engineconn.DefaultEngineConn
import org.apache.linkis.manager.label.entity.engine.CodeLanguageLabel

import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, verify, when}

// 单元测试案例
class PythonModuleLoadEngineConnHookTest {

  @Test
  def testAfterExecutionExecute(): Unit = {
    // 创建模拟对象
    val mockEngineCreationContext = new DefaultEngineCreationContext
    val mockEngineConn = mock[DefaultEngineConn]
    val hook = new PythonSparkEngineHook

    // 设置模拟行为
    var labels = new CodeLanguageLabel
    labels.setCodeType("spark")

    // 执行测试方法
    hook.afterExecutionExecute(mockEngineCreationContext, mockEngineConn)

  }

  @Test
  def testAfterEngineServerStartFailed(): Unit = {
    // 创建模拟对象
    val mockEngineCreationContext = mock[EngineCreationContext]
    val mockThrowable = mock[Throwable]
    val hook = new PythonSparkEngineHook

    // 设置模拟行为
    var labels = new CodeLanguageLabel
    labels.setCodeType("spark")

    // 执行测试方法
    hook.afterEngineServerStartFailed(mockEngineCreationContext, mockThrowable)

  }

  @Test
  def testBeforeCreateEngineConn(): Unit = {
    // 创建模拟对象

    // 验证调用

  }

  @Test
  def testBeforeExecutionExecute(): Unit = {
    // 创建模拟对象
    val mockEngineCreationContext = mock[EngineCreationContext]
    val mockEngineConn = mock[DefaultEngineConn]
    val hook = new PythonSparkEngineHook

    // 执行测试方法
    hook.beforeExecutionExecute(mockEngineCreationContext, mockEngineConn)

  }

}
