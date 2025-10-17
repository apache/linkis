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

package org.apache.linkis.monitor.jobhistory

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.monitor.constants.Constants
import org.apache.linkis.monitor.core.pac.AbstractDataFetcher
import org.apache.linkis.monitor.jobhistory.dao.JobHistoryMapper
import org.apache.linkis.monitor.jobhistory.exception.AnomalyScannerException

import org.apache.commons.lang3.StringUtils

import java.util
import java.util.Date

class JobHistoryDataFetcher(args: Array[Any], mapper: JobHistoryMapper)
    extends AbstractDataFetcher
    with Logging {

  /**
   * retrieve JobHistory Data starts from startTimeMs and ends at startTimeMs + intervalsMs
   *
   * @return
   */
  /**
   * get arguments for querying data
   *
   * @return
   */
  override def getArgs(): Array[Any] = args

  /**
   *   1. get Data given some arguments
   */
  override def getData(): util.List[scala.Any] = {
    if (!args.isInstanceOf[Array[String]]) {
      throw new AnomalyScannerException(
        21304,
        "Wrong input for JobHistoryDataFetcher. DataType: " + args.getClass.getCanonicalName
      )
    }
    if (args != null) {
      val start = args(0).asInstanceOf[String].toLong
      val end = args(1).asInstanceOf[String].toLong
      // 根据参数数量进行不同的处理
      args.length match {
        // 参数数量为2，则数据库查询仅筛选开始和结束时间
        case 2 =>
          mapper
            .search(null, null, null, new Date(start), new Date(end), null)
            .asInstanceOf[util.List[scala.Any]]
        // 参数数量为4，根据第四个参数进行不同的查询
        case 4 =>
          val id = args(2).asInstanceOf[String].toLong
          val parm = args(3).asInstanceOf[String]
          parm match {
            // 筛选任务包含id，时间，已完成状态任务
            case "finished_job" =>
              val list = new util.ArrayList[String]()
              Constants.DATA_FINISHED_JOB_STATUS_ARRAY.foreach(list.add)
              mapper
                .searchByCacheAndUpdateTime(id, null, list, new Date(start), new Date(end), null)
                .asInstanceOf[util.List[scala.Any]]
            // 筛选任务包含id，时间，未完成状态任务
            case "unfinished_job" =>
              var list = new util.ArrayList[String]()
              Constants.DATA_UNFINISHED_JOB_STATUS_ARRAY.foreach(list.add)
              mapper
                .searchByCache(id, null, list, new Date(start), new Date(end), null)
                .asInstanceOf[util.List[scala.Any]]
            // 筛选任务包含id，时间
            case _ =>
              mapper
                .searchByCache(id, null, null, new Date(start), new Date(end), null)
                .asInstanceOf[util.List[scala.Any]]
          }
        case _ =>
          throw new AnomalyScannerException(
            21304,
            "Wrong input for JobHistoryDataFetcher. Data: " + args
          )
      }
    } else {
      throw new AnomalyScannerException(
        21304,
        "Wrong input for JobHistoryDataFetcher. Data: " + args
      )
    }
  }

}
