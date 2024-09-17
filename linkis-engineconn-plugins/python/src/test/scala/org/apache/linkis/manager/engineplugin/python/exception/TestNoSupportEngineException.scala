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

package org.apache.linkis.manager.engineplugin.python.exception

import org.junit.jupiter.api.{Assertions, Test}

class TestNoSupportEngineException {

  @Test
  def testNoSupportEngineException: Unit = {
    val errorMsg = "NoSupportEngine"
    val exception = new NoSupportEngineException(50010, errorMsg)
    Assertions.assertEquals(50010, exception.getErrCode)
    Assertions.assertEquals(errorMsg, exception.getDesc)
  }

  @Test
  def testPythonSessionStartFailedException: Unit = {
    val errorMsg = "PythonSessionStartFailed"
    val exception = PythonSessionStartFailedExeception(errorMsg)
    Assertions.assertEquals(errorMsg, exception.getDesc)
  }

  @Test
  def testPythonSessionNullException: Unit = {
    val errorMsg = "PythonSessionNull"
    val exception = new PythonSessionNullException(50011, errorMsg)
    Assertions.assertEquals(50011, exception.getErrCode)
    Assertions.assertEquals(errorMsg, exception.getDesc)
  }

  @Test
  def testPythonEngineException: Unit = {
    val errorMsg = "PythonEngine"
    val exception = new PythonEngineException(50012, errorMsg)
    Assertions.assertEquals(50012, exception.getErrCode)
    Assertions.assertEquals(errorMsg, exception.getDesc)
  }

  @Test
  def testQueryFailedException: Unit = {
    val errorMsg = "QueryFailed"
    val exception = new QueryFailedException(60001, errorMsg)
    Assertions.assertEquals(60001, exception.getErrCode)
    Assertions.assertEquals(errorMsg, exception.getDesc)
  }

  @Test
  def testPythonExecuteError: Unit = {
    val errorMsg = "PythonExecuteError"
    val exception = new PythonExecuteError(60006, errorMsg)
    Assertions.assertEquals(60006, exception.getErrCode)
    Assertions.assertEquals(errorMsg, exception.getDesc)
  }

  @Test
  def testSessionStartFailedException: Unit = {
    val errorMsg = "SessionStartFailed"
    val exception = new SessionStartFailedException(60002, errorMsg)
    Assertions.assertEquals(60002, exception.getErrCode)
    Assertions.assertEquals(errorMsg, exception.getDesc)
  }

  @Test
  def testExecuteException: Unit = {
    val errorMsg = "ExecuteException"
    val exception = new ExecuteException(60003, errorMsg)
    Assertions.assertEquals(60003, exception.getErrCode)
    Assertions.assertEquals(errorMsg, exception.getDesc)
  }

  @Test
  def testEngineException: Unit = {
    val errorMsg = "EngineException"
    val exception = new EngineException(60004, errorMsg)
    Assertions.assertEquals(60004, exception.getErrCode)
    Assertions.assertEquals(errorMsg, exception.getDesc)
  }

}
