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

package org.apache.linkis.engineplugin.spark.utils

import org.apache.linkis.engineconn.common.conf.{EngineConnConf, EngineConnConstant}

import org.apache.spark.sql.DataFrame

import java.util.concurrent.TimeUnit

import com.google.common.cache.{Cache, CacheBuilder}

case class DataFrameResponse(dataFrame: DataFrame, hasMoreData: Boolean)

object DirectPushCache {

  private val resultSet: Cache[String, DataFrame] = CacheBuilder
    .newBuilder()
    .expireAfterAccess(EngineConnConf.ENGINE_TASK_EXPIRE_TIME.getValue, TimeUnit.MILLISECONDS)
    .maximumSize(EngineConnConstant.MAX_TASK_NUM)
    .build()

  // This method is not idempotent. After fetching a result set of size fetchSize each time, the corresponding results will be removed from the cache.
  def fetchResultSetOfDataFrame(taskId: String, fetchSize: Int): DataFrameResponse = {
    val df = DirectPushCache.resultSet.getIfPresent(taskId)
    if (df == null) {
      throw new IllegalAccessException(s"Task $taskId not exists in resultSet cache.")
    } else {
      val batchDf = df.limit(fetchSize)
      if (batchDf.count() < fetchSize) {
        // All the data in df has been consumed.
        DirectPushCache.resultSet.invalidate(taskId)
        DataFrameResponse(batchDf, hasMoreData = false)
      } else {
        // Update df with consumed one.
        DirectPushCache.resultSet.put(taskId, df.except(batchDf))
        DataFrameResponse(batchDf, hasMoreData = true)
      }
    }
  }

  def isTaskCached(taskId: String): Boolean = {
    DirectPushCache.resultSet.getIfPresent(taskId) != null
  }

  def submitExecuteResult(taskId: String, df: DataFrame): Unit = {
    DirectPushCache.resultSet.put(taskId, df)
  }

}
