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

package org.apache.linkis.filesystem.service

import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.filesystem.cache.FsCache
import org.apache.linkis.filesystem.conf.WorkSpaceConfiguration
import org.apache.linkis.filesystem.entity.FSInfo
import org.apache.linkis.filesystem.exception.{WorkSpaceException, WorkspaceExceptionManager}
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.fs.FileSystem

import org.springframework.stereotype.Service

import java.util.concurrent.{Callable, ExecutionException, FutureTask, TimeoutException, TimeUnit}

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

@Service
class FsService extends Logging {

  def getFileSystemCache(user: String, fsPath: FsPath): FileSystem = {
    if (FsCache.fsInfo.get(user) != null) {
      // The outer layer does not add more judgments, it is also ok, it is to lock the user's fs group.(外层不加多个判断也ok，都是要锁用户的fs组)
      FsCache.fsInfo.get(user) synchronized {
        if (!FsCache.fsInfo.get(user).exists(_.fs.fsName().equals(fsPath.getFsType))) {
          FsCache.fsInfo.get(user) += produceFSInfo(user, fsPath)
        } else {
          FsCache.fsInfo
            .get(user)
            .filter(_.fs.fsName().equals(fsPath.getFsType))(0)
            .lastAccessTime = System.currentTimeMillis()
        }
      }
    } else {
      FsCache.fsInfo synchronized {
        if (FsCache.fsInfo.get(user) == null) {
          FsCache.fsInfo.put(user, ArrayBuffer(produceFSInfo(user, fsPath)))
        }
      }
      // (43-49) Prevent file and hdfs from entering 37 lines at the same time, causing 51 lines to report the cross mark
      // （43-49）防止file和hdfs同时进到37行，导致51行报下角标越界
      if (!FsCache.fsInfo.get(user).exists(_.fs.fsName().equals(fsPath.getFsType))) {
        FsCache.fsInfo.get(user) synchronized {
          if (!FsCache.fsInfo.get(user).exists(_.fs.fsName().equals(fsPath.getFsType))) {
            FsCache.fsInfo.get(user) += produceFSInfo(user, fsPath)
          }
        }
      }
    }
    FsCache.fsInfo.asScala(user).filter(_.fs.fsName().equals(fsPath.getFsType))(0).fs
  }

  @throws(classOf[WorkSpaceException])
  def getFileSystem(user: String, fsPath: FsPath): FileSystem = {
    var fs: FileSystem = null
    val start = System.currentTimeMillis()
    val task: FutureTask[FileSystem] = new FutureTask[FileSystem](new Callable[FileSystem] {
      override def call(): FileSystem = {
        fs = Utils.tryAndError(getFileSystemCache(user, fsPath))
        fs
      }
    })
    WorkSpaceConfiguration.executorService.execute(task)
    val timeout: Long = WorkSpaceConfiguration.FILESYSTEM_GET_TIMEOUT.getValue
    try {
      task.get(timeout, TimeUnit.MILLISECONDS)
    } catch {
      case e: InterruptedException =>
        logger.error("Failed to getFileSystem", e); task.cancel(true); null
      case e: ExecutionException =>
        logger.error("Failed to getFileSystem", e); task.cancel(true); null
      case e: TimeoutException =>
        logger.error("Failed to getFileSystem", e); task.cancel(true); null
    } finally {
      val end = System.currentTimeMillis()
      logger.info(
        s"${user} gets the ${fsPath.getFsType} type filesystem using a total of ${end - start} milliseconds(${user}获取${fsPath.getFsType}类型的filesystem一共使用了${end - start}毫秒)"
      )
    }
    if (fs == null) {
      throw WorkspaceExceptionManager.createException(
        80002,
        timeout.asInstanceOf[AnyRef],
        timeout.asInstanceOf[AnyRef]
      )
    }
    fs
  }

  def produceFSInfo(user: String, fsPath: FsPath): FSInfo = {
    try {
      // todo to compatible proxy user(后续将兼容代理用户权限)
      val fs = FSFactory.getFsByProxyUser(fsPath, user).asInstanceOf[FileSystem]
      // val fs = FSFactory.getFs(fsPath).asInstanceOf[FileSystem]
      fs.init(null)
      new FSInfo(user, fs, System.currentTimeMillis())
    } catch {
      // If rpc fails to get fs, for example, io-engine restarts or hangs.(如果rpc获取fs失败了 比如io-engine重启或者挂掉)
      case e: Exception =>
        logger.error("Requesting IO-Engine to initialize fileSystem failed", e)
        // todo Clean up the cache(清理缓存 目前先遗留)
        /* FsCache.fsInfo.foreach{
          case (_,list) =>list synchronized list.filter(f =>true).foreach(f =>list -=f)
        } */
        throw WorkspaceExceptionManager.createException(80001)
    }
  }

}
