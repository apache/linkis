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
 
package org.apache.linkis.engineplugin.spark.extension

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext

import scala.collection.mutable.ArrayBuffer


trait SparkPreExecutionHook {
  def hookName: String

  def callPreExecutionHook(engineExecutionContext: EngineExecutionContext, code: String): String
}

object SparkPreExecutionHook extends Logging{
  private val preHooks = ArrayBuffer[SparkPreExecutionHook]()

  def register(preExecutionHook: SparkPreExecutionHook):Unit = {
    info(s"Get a preExecutionHook of ${preExecutionHook.hookName} register")
    preHooks.append(preExecutionHook)
  }

  def getSparkPreExecutionHooks():Array[SparkPreExecutionHook] = {
    preHooks.toArray
  }
}