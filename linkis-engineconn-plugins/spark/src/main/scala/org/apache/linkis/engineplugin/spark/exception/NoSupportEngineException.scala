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

package org.apache.linkis.engineplugin.spark.exception

import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.engineplugin.spark.errorcode.SparkErrorCodeSummary._

/**
 */
case class NoSupportEngineException(errCode: Int, desc: String)
    extends ErrorException(errCode, desc)

case class NotSupportSparkTypeException(errorCode: Int, desc: String)
    extends ErrorException(errorCode, desc)

case class NotSupportSparkSqlTypeException(desc: String)
    extends ErrorException(INVALID_CREATE_SPARKSQL.getErrorCode, desc)

case class NotSupportSparkPythonTypeException(desc: String)
    extends ErrorException(INVALID_CREATE_SPARKPYTHON.getErrorCode, desc)

case class NotSupportSparkScalaTypeException(desc: String) extends ErrorException(420003, desc)

case class NotSupportSparkDataCalcTypeException(desc: String) extends ErrorException(420004, desc)
