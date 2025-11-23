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

package org.apache.linkis.orchestrator.strategy

import org.apache.linkis.orchestrator.execution.ArrayResultSetTaskResponse
import org.apache.linkis.orchestrator.plans.physical.ExecTask

import scala.collection.mutable

/**
 */
trait ResultSetExecTask extends ExecTask {

  /**
   * This method is called by ExecTaskRunner, ExecTask cannot be called directly, which will cause
   * repeated placement of execution results 该方法由ExecTaskRunner进行调用，ExecTask不能直接调用，会导致重复放置执行结果
   * @param resultSetTaskResponse
   */
  def addResultSet(resultSetTaskResponse: ArrayResultSetTaskResponse): Unit = {
    val resultMap = if (null != getPhysicalContext.get(ResultSetExecTask.RESULT_MAP_KEY)) {
      getPhysicalContext
        .get(ResultSetExecTask.RESULT_MAP_KEY)
        .asInstanceOf[mutable.Map[String, ArrayResultSetTaskResponse]]
    } else {
      new mutable.HashMap[String, ArrayResultSetTaskResponse]()
    }
    resultMap.put(getId, resultSetTaskResponse)
    getPhysicalContext.set(ResultSetExecTask.RESULT_MAP_KEY, resultMap)
    removeResultSet()
  }

  /**
   * Remove the response of the child node 移除子节点的response
   * @return
   */
  def removeResultSet(): Unit = {
    val map = getPhysicalContext.get(ResultSetExecTask.RESULT_MAP_KEY)
    if (null != map) {
      val resultMap = map.asInstanceOf[mutable.Map[String, ArrayResultSetTaskResponse]]
      getChildren.foreach(execTask => resultMap.remove(execTask.getId))
    }
  }

  def getChildrenResultSet(): Map[String, ArrayResultSetTaskResponse] = {
    val map = getPhysicalContext.get(ResultSetExecTask.RESULT_MAP_KEY)
    if (null != map) {
      val resultMap = map.asInstanceOf[mutable.Map[String, ArrayResultSetTaskResponse]]
      val childrenResults = getChildren
        .map(execTask => execTask.getId -> resultMap.get(execTask.getId))
        .filter(_._2.isDefined)
        .map { tuple =>
          tuple._1 -> tuple._2.get
        }
        .toMap
      return childrenResults
    }
    null
  }

}

object ResultSetExecTask {

  val RESULT_MAP_KEY = "RESULT_MAP_KEY"

}
