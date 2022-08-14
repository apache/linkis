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

package org.apache.linkis.manager.common.operator

import org.apache.linkis.common.utils.{ClassUtils, Logging}
import org.apache.linkis.governance.common.exception.GovernanceErrorException
import org.apache.linkis.manager.common.protocol.OperateRequest

trait OperatorFactory {

  def getOperatorName(parameters: Map[String, Any]): String

  def getOperatorRequest(parameters: Map[String, Any]): Operator

}

object OperatorFactory {

  private val operatorFactory = new OperatorFactoryImpl

  def apply(): OperatorFactory = operatorFactory

}

import scala.collection.convert.WrapAsScala._

class OperatorFactoryImpl extends OperatorFactory with Logging {

  private val operators: Map[String, _ <: Operator] = ClassUtils.reflections
    .getSubTypesOf(classOf[Operator])
    .filterNot(ClassUtils.isInterfaceOrAbstract)
    .flatMap { clazz =>
      val operator = clazz.newInstance()
      operator.getNames.map(name => name -> operator)
    }
    .toMap

  logger.info("Launched operators list => " + operators)

  override def getOperatorName(parameters: Map[String, Any]): String =
    OperateRequest.getOperationName(parameters)

  override def getOperatorRequest(parameters: Map[String, Any]): Operator = {
    val operatorName = getOperatorName(parameters)
    if (operators.contains(operatorName)) operators(operatorName)
    else throw new GovernanceErrorException(20030, s"Cannot find operator named $operatorName.")
  }

}
