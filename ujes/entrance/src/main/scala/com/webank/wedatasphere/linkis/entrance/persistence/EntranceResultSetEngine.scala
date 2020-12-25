/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.entrance.persistence
import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.entrance.execute.StorePathExecuteRequest
import com.webank.wedatasphere.linkis.entrance.job.EntranceExecutionJob
import com.webank.wedatasphere.linkis.scheduler.executer.{AliasOutputExecuteResponse, OutputExecuteResponse}
import com.webank.wedatasphere.linkis.scheduler.queue.Job
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetWriter}
import com.webank.wedatasphere.linkis.storage.utils.{FileSystemUtils, StorageUtils}
import org.apache.commons.io.IOUtils

/**
  * Created by enjoyyin on 2018/11/1.
  */
class EntranceResultSetEngine extends ResultSetEngine {
  override def persistResultSet(job: Job, executeCompleted: OutputExecuteResponse): String = executeCompleted match {
    case AliasOutputExecuteResponse(alias, output) =>
      if(ResultSetFactory.getInstance.isResultSetPath(output)) output
      else {
        val resultSet = ResultSetFactory.getInstance.getResultSetByContent(output)
        var user:String="hadoop"
        val storePath = job match {
          case j: EntranceExecutionJob => j.jobToExecuteRequest() match {
            case s: StorePathExecuteRequest => {
              user = j.getUser
              s.storePath
            }
            case _ => null
          }
          case _ => null
        }
        if(storePath != null) {
          //TODO Remove _ stitching(去掉_拼接)
          val path = if(alias.contains("_")) resultSet.getResultSetPath(new FsPath(storePath),  alias) else resultSet.getResultSetPath(new FsPath(storePath),  "_" + alias)

          FileSystemUtils.createNewFile(path,true)
          val writer = ResultSetWriter.getResultSetWriter(resultSet, 0, path,StorageUtils.getJvmUser)
          Utils.tryFinally {
            writer.addMetaDataAndRecordString(output)
            writer.flush()
          }{
            IOUtils.closeQuietly(writer)
           /* val fileSystem = FSFactory.getFsByProxyUser(path,user).asInstanceOf[FileSystem]
            fileSystem.init(null)
            fileSystem.setOwner(path,user)
            Utils.tryQuietly(fileSystem.close())*/
          }
          path.getSchemaPath
        } else null
      }
  }
}
