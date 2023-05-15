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

package org.apache.linkis.entrance.persistence

import org.apache.linkis.common.io.{FsPath, MetaData, Record}
import org.apache.linkis.common.io.resultset.ResultSet
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.exception.{EntranceErrorCode, EntranceErrorException}
import org.apache.linkis.entrance.execute.StorePathExecuteRequest
import org.apache.linkis.entrance.job.{EntranceExecuteRequest, EntranceExecutionJob}
import org.apache.linkis.entrance.scheduler.cache.CacheOutputExecuteResponse
import org.apache.linkis.governance.common.entity.job.SubJobDetail
import org.apache.linkis.scheduler.executer.{AliasOutputExecuteResponse, OutputExecuteResponse}
import org.apache.linkis.scheduler.queue.Job
import org.apache.linkis.storage.resultset.{ResultSetFactory, ResultSetWriterFactory}
import org.apache.linkis.storage.utils.FileSystemUtils

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils

class EntranceResultSetEngine extends ResultSetEngine with Logging {

  override def persistResultSet(job: Job, executeCompleted: OutputExecuteResponse): String = {

    executeCompleted match {
      case AliasOutputExecuteResponse(alias, output) =>
        if (ResultSetFactory.getInstance.isResultSetPath(output)) getDir(output)
        else {
          throw new EntranceErrorException(
            EntranceErrorCode.RESULT_NOT_PERSISTED_ERROR.getErrCode,
            EntranceErrorCode.RESULT_NOT_PERSISTED_ERROR.getDesc
          )
        }
      case CacheOutputExecuteResponse(alias, output) =>
        if (ResultSetFactory.getInstance.isResultSetPath(output)) {
          getDir(output)
        } else {
          throw new EntranceErrorException(
            EntranceErrorCode.RESULT_NOT_PERSISTED_ERROR.getErrCode,
            EntranceErrorCode.RESULT_NOT_PERSISTED_ERROR.getDesc
          )
        }
    }
  }

  private def getDir(str: String): String = {
    if (str.endsWith("/")) {
      str
    } else {
      val arr = str.split("/").filter(StringUtils.isNotBlank)
      if (arr.length <= 2) {
        return str
      } else {
        str.substring(0, str.lastIndexOf("/"))
      }
    }
  }

}
