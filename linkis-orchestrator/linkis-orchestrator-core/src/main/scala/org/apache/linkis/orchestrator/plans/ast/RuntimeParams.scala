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

package org.apache.linkis.orchestrator.plans.ast

import java.util

/**
 */
trait RuntimeParams {

  def getVariables: util.Map[String, AnyRef]

  def getContext: util.Map[String, AnyRef]

  def getDataSources: util.Map[String, AnyRef]

  @deprecated
  def getSpecials: util.Map[String, AnyRef]

  def getJobs: util.Map[String, AnyRef]

  def getMap(key: String): util.Map[String, AnyRef]

  def get(key: String): Any

  def toMap: util.Map[String, AnyRef]

}

class RuntimeParamsImpl(
    runtimeMap: util.Map[String, AnyRef],
    variable: util.Map[String, AnyRef],
    specials: util.Map[String, AnyRef]
) extends RuntimeParams {

  private var context: util.Map[String, AnyRef] = _
  private var dataSources: util.Map[String, AnyRef] = _
  private var jobs: util.Map[String, AnyRef] = _

  def init(): Unit = {
    dataSources = getSubMap(runtimeMap, QueryParams.DATA_SOURCE_KEY)
    context = getSubMap(runtimeMap, QueryParams.CONTEXT_KEY)
    initContextMap(runtimeMap, context) // just for compatible with old usage.
    jobs = getSubMap(runtimeMap, QueryParams.JOB_KEY)
  }

  init()

  @deprecated
  private def initContextMap(
      runtime: util.Map[String, AnyRef],
      context: util.Map[String, AnyRef]
  ): Unit = {
    if (context.isEmpty && runtime.containsKey(QueryParams.CONTEXT_KEY_FOR_ID)) {
      context.put(QueryParams.CONTEXT_KEY_FOR_ID, runtime.get(QueryParams.CONTEXT_KEY_FOR_ID))
      if (runtime.containsKey(QueryParams.CONTEXT_KEY_FOR_NODE_NAME)) {
        context.put(
          QueryParams.CONTEXT_KEY_FOR_NODE_NAME,
          runtime.get(QueryParams.CONTEXT_KEY_FOR_NODE_NAME)
        )
      }
    }
  }

  private def getSubMap(params: util.Map[String, AnyRef], key: String): util.Map[String, AnyRef] = {
    if (null != params.get(key)) {
      params.get(key).asInstanceOf[util.Map[String, AnyRef]]
    } else {
      new util.HashMap[String, AnyRef]
    }
  }

  override def getVariables: util.Map[String, AnyRef] = variable

  override def getContext: util.Map[String, AnyRef] = context

  override def getDataSources: util.Map[String, AnyRef] = dataSources

  override def getSpecials: util.Map[String, AnyRef] = specials

  override def getJobs: util.Map[String, AnyRef] = jobs

  override def getMap(key: String): util.Map[String, AnyRef] = runtimeMap.get(key) match {
    case map: util.Map[String, AnyRef] => map
    case _ => new util.HashMap[String, AnyRef]
  }

  override def get(key: String): Any = runtimeMap.get(key)

  override def toMap: util.Map[String, AnyRef] = runtimeMap
}
