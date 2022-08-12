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

package org.apache.linkis.computation.client.operator

import org.apache.linkis.common.utils.ClassUtils
import org.apache.linkis.ujes.client.exception.UJESJobException

trait OperatorFactory {

  def createOperatorByName(operatorName: String): Operator[_]

}

object OperatorFactory {

  private val operatorFactory = new OperatorFactoryImpl

  def apply(): OperatorFactory = operatorFactory

}

import scala.collection.convert.WrapAsScala._

class OperatorFactoryImpl extends OperatorFactory {

  private val operatorClasses: Map[String, Class[_ <: Operator[_]]] = ClassUtils.reflections
    .getSubTypesOf(classOf[Operator[_]])
    .filterNot(ClassUtils.isInterfaceOrAbstract)
    .map { clazz =>
      clazz.newInstance().getName -> clazz
    }
    .toMap

  override def createOperatorByName(operatorName: String): Operator[_] = operatorClasses
    .get(operatorName)
    .map(_.newInstance())
    .getOrElse(throw new UJESJobException(s"Cannot find $operatorName operator."))

}
