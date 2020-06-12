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

package com.webank.wedatasphere.linkis.filesystem.service

import java.util.concurrent.{Callable, FutureTask, TimeUnit}

import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.filesystem.cache.FsCache
import com.webank.wedatasphere.linkis.filesystem.conf.WorkSpaceConfiguration
import com.webank.wedatasphere.linkis.filesystem.entity.FSInfo
import com.webank.wedatasphere.linkis.filesystem.exception.WorkspaceExceptionManager
import com.webank.wedatasphere.linkis.storage.FSFactory
import com.webank.wedatasphere.linkis.storage.fs.FileSystem
import org.springframework.stereotype.Service

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent._

/**
  * Created by johnnwang on 2019/2/11.
  */
@Service
class FsService extends Logging {


  def getFileSystemCache(user: String, fsPath: FsPath): FileSystem = {
    if (FsCache.fsInfo.get(user) != null) {
      //The outer layer does not add more judgments, it is also ok, it is to lock the user's fs group.(外层不加多个判断也ok，都是要锁用户的fs组)
      FsCache.fsInfo.get(user) synchronized {
        if (FsCache.fsInfo.get(user).filter(_.fs.fsName().equals(fsPath.getFsType)).isEmpty) {
          FsCache.fsInfo.get(user) += produceFSInfo(user, fsPath)
        } else {
          FsCache.fsInfo.get(user).filter(_.fs.fsName().equals(fsPath.getFsType))(0).lastAccessTime = System.currentTimeMillis()
        }
      }
    } else {
      FsCache.fsInfo synchronized {
        if (FsCache.fsInfo.get(user) == null) {
          FsCache.fsInfo += user -> ArrayBuffer(produceFSInfo(user, fsPath))
        }
      }
      //(43-49) Prevent file and hdfs from entering 37 lines at the same time, causing 51 lines to report the cross mark
      //（43-49）防止file和hdfs同时进到37行，导致51行报下角标越界
      if (FsCache.fsInfo.get(user).filter(_.fs.fsName().equals(fsPath.getFsType)).isEmpty) {
        FsCache.fsInfo.get(user) synchronized {
          if (FsCache.fsInfo.get(user).filter(_.fs.fsName().equals(fsPath.getFsType)).isEmpty) {
            FsCache.fsInfo.get(user) += produceFSInfo(user, fsPath)
          }
        }
      }
    }
    FsCache.fsInfo(user).filter(_.fs.fsName().equals(fsPath.getFsType))(0).fs
  }

  def getFileSystem(user: String, fsPath: FsPath): FileSystem = {
    var fs:FileSystem = null
    val start = System.currentTimeMillis()
    val task: FutureTask[FileSystem] = new FutureTask[FileSystem](new Callable[FileSystem] {
      override def call(): FileSystem = {
        fs = Utils.tryAndError(getFileSystemCache(user, fsPath))
        fs
      }
    })
    WorkSpaceConfiguration.executorService.execute(task)
    val timeout: java.lang.Long = WorkSpaceConfiguration.FILESYSTEM_GET_TIMEOUT.getValue
    try {
      task.get(timeout, TimeUnit.MILLISECONDS)
    } catch {
      case e: InterruptedException => error("Failed to getFileSystem", e); task.cancel(true); null
      case e: ExecutionException => error("Failed to getFileSystem", e); task.cancel(true); null
      case e: TimeoutException => error("Failed to getFileSystem", e); task.cancel(true); null
    } finally {
      val end = System.currentTimeMillis()
      info(s"${user} gets the ${fsPath.getFsType} type filesystem using a total of ${end - start} milliseconds(${user}获取${fsPath.getFsType}类型的filesystem一共使用了${end - start}毫秒)")
    }
    if(fs == null) throw WorkspaceExceptionManager.createException(80002,timeout,timeout)
    fs
  }


  def produceFSInfo(user: String, fsPath: FsPath): FSInfo = {
    try {
      val fs = FSFactory.getFs(fsPath).asInstanceOf[FileSystem]
      fs.init(null)
      new FSInfo(user, fs, System.currentTimeMillis())
    } catch {
      //If rpc fails to get fs, for example, io-engine restarts or hangs.(如果rpc获取fs失败了 比如io-engine重启或者挂掉)
      case e: Exception => {
        error("Requesting IO-Engine to initialize fileSystem failed", e)
        //todo Clean up the cache(清理缓存 目前先遗留)
        /*FsCache.fsInfo.foreach{
          case (_,list) =>list synchronized list.filter(f =>true).foreach(f =>list -=f)
        }*/
        throw WorkspaceExceptionManager.createException(80001)
      }
    }
  }
}
