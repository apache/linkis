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

package org.apache.linkis.ujes.jdbc

import java.sql.ParameterMetaData

class LinkisParameterMetaData(parameterCount: Int) extends ParameterMetaData {

  override def getParameterCount: Int = parameterCount

  override def isNullable(param: Int): Int = 1

  override def isSigned(param: Int): Boolean = true

  override def getPrecision(param: Int): Int = Int.MaxValue

  override def getScale(param: Int): Int = 10

  override def getParameterType(param: Int): Int = 12

  override def getParameterTypeName(param: Int): String = "VARCHAR"

  override def getParameterClassName(param: Int): String = "java.lang.String"

  override def getParameterMode(param: Int): Int = 1

  override def unwrap[T](iface: Class[T]): T = iface.cast(this)

  override def isWrapperFor(iface: Class[_]): Boolean = iface.isInstance(this)
}
