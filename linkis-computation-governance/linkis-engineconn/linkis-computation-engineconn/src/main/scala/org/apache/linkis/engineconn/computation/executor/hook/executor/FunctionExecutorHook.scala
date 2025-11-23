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

package org.apache.linkis.engineconn.computation.executor.hook.executor

import org.apache.linkis.engineconn.computation.executor.hook.UDFLoad
import org.apache.linkis.engineconn.core.hook.ExecutorHook
import org.apache.linkis.engineconn.executor.entity.Executor
import org.apache.linkis.manager.label.entity.engine.RunType
import org.apache.linkis.manager.label.entity.engine.RunType.RunType
import org.apache.linkis.udf.utils.ConstantVar
import org.apache.linkis.udf.vo.UDFInfoVo

class PyFunctionEngineHook extends UDFLoad with ExecutorHook {
  override val udfType: BigInt = ConstantVar.FUNCTION_PY
  override val category: String = ConstantVar.FUNCTION
  override val runType = RunType.PYSPARK

  override protected def constructCode(udfInfo: UDFInfoVo): String = {
    "%py\n" + readFile(
      udfInfo.getCreateUser,
      udfInfo.getBmlResourceId,
      udfInfo.getBmlResourceVersion
    )
  }

  override def getHookName(): String = "PyFunctionEngineHook"

  override def afterExecutorInit(executor: Executor): Unit = {
    logger.info("start to register pyspark function ")
    loadFunctions(executor)
    logger.info("Finished to register pyspark function ")
  }

  override def isAccepted(codeType: String): Boolean = runType.toString.equalsIgnoreCase(codeType)
}

class ScalaFunctionEngineHook extends UDFLoad with ExecutorHook {
  override val udfType: BigInt = ConstantVar.FUNCTION_SCALA
  override val category: String = ConstantVar.FUNCTION
  override val runType = RunType.SCALA

  override protected def constructCode(udfInfo: UDFInfoVo): String = {
    "%scala\n" + readFile(
      udfInfo.getCreateUser,
      udfInfo.getBmlResourceId,
      udfInfo.getBmlResourceVersion
    )
  }

  override def getHookName(): String = "ScalaFunctionEngineHook"

  override def afterExecutorInit(executor: Executor): Unit = {
    logger.info("start to register scala function ")
    loadFunctions(executor)
    logger.info("Finished to register scala function ")
  }

  override def isAccepted(codeType: String): Boolean = runType.toString.equalsIgnoreCase(codeType)
}
