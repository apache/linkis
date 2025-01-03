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

package org.apache.linkis.storage.io.iteraceptor

import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.entrance.BindEngineLabel
import org.apache.linkis.storage.domain.{FsPathListWithError, MethodEntity, MethodEntitySerializer}
import org.apache.linkis.storage.errorcode.LinkisIoFileClientErrorCodeSummary._
import org.apache.linkis.storage.exception.{FSNotInitException, StorageErrorException}
import org.apache.linkis.storage.io.client.IOClient
import org.apache.linkis.storage.io.utils.IOClientUtils
import org.apache.linkis.storage.resultset.{ResultSetFactory, ResultSetReader, ResultSetWriter}
import org.apache.linkis.storage.resultset.io.{IOMetaData, IORecord}
import org.apache.linkis.storage.utils.{StorageConfiguration, StorageUtils}

import org.apache.commons.lang3.StringUtils

import org.springframework.cglib.proxy.{MethodInterceptor, MethodProxy}

import java.io.{InputStream, IOException, OutputStream}
import java.lang.reflect.Method
import java.net.InetAddress

import scala.beans.BeanProperty
import scala.collection.JavaConverters._

import com.google.gson.reflect.TypeToken

class IOMethodInterceptor(fsType: String) extends MethodInterceptor with Logging {

  @BeanProperty var ioClient: IOClient = _

  private var properties: java.util.Map[String, String] = new java.util.HashMap[String, String]()

  private var inited = false

  private var closed = false

  private var id: Long = -1L

  private val iOEngineExecutorMaxFreeTime = StorageConfiguration.IO_FS_EXPIRE_TIME.getValue
  private val fetchSize = StorageConfiguration.IO_PROXY_READ_FETCH_SIZE.getValue.toLong
  private val cacheSize = StorageConfiguration.IO_PROXY_WRITE_CACHE_SIZE.getValue.toLong.toInt

  private var lastAccessTime = System.currentTimeMillis()

  private val bindEngineLabel: BindEngineLabel = {
    val label = IOClientUtils.getLabelBuilderFactory.createLabel[BindEngineLabel](
      LabelKeyConstant.BIND_ENGINE_KEY
    )
    label.setJobGroupId(IOClientUtils.generateJobGrupID())
  }

  def getProxyUser: String = StorageConfiguration.PROXY_USER.getValue(properties)

  def getCreatorUser: String = StorageUtils.getJvmUser

  def getLocalIP: String = {
    var ip = Utils.tryQuietly(InetAddress.getLocalHost.getHostAddress)
    if (ip.contains("/")) ip = ip.split("/")(0)
    ip
  }

  /**
   * Call io-client to execute the corresponding method, except init. 调用io-client执行相应的方法，除了init都走该方法
   * @param methodName
   * @param params
   * @return
   */
  def executeMethod(methodName: String, params: Array[AnyRef]): String = {
    val res = Utils.tryCatch(
      ioClient.execute(
        getProxyUser,
        MethodEntity(id, fsType, getCreatorUser, getProxyUser, getLocalIP, methodName, params),
        bindEngineLabel
      )
    ) { t: Throwable =>
      if (t.isInstanceOf[FSNotInitException]) {
        logger.error(s"The Fs of user:$getProxyUser need re-init:")
        initFS()
        executeMethod(methodName, params)
      } else throw t
    }
    res
  }

  def initFS(methodName: String = "init"): Unit = {
    if (!properties.containsKey(StorageConfiguration.PROXY_USER.key)) {
      throw new StorageErrorException(NO_PROXY_USER.getErrorCode, NO_PROXY_USER.getErrorDesc)
    }
    bindEngineLabel.setIsJobGroupHead("true")
    bindEngineLabel.setIsJobGroupEnd("false")
    val res = ioClient.executeWithRetry(
      getProxyUser,
      MethodEntity(
        id,
        fsType,
        getCreatorUser,
        getProxyUser,
        getLocalIP,
        methodName,
        Array(properties)
      ),
      bindEngineLabel
    )
    id = Utils.tryCatch(StorageUtils.deserializerResultToString(res).toLong) { t: Throwable =>
      logger.error(s"Failed to init fs,init return:(${res}")
      throw t
    }
    if (id != -1) {
      inited = true
      bindEngineLabel.setIsJobGroupEnd("false")
      bindEngineLabel.setIsJobGroupHead("false")
    } else {
      throw new StorageErrorException(
        FAILED_TO_INIT_USER.getErrorCode,
        s"Failed to init FS for user:$getProxyUser "
      )
    }
  }

  def beforeOperation(): Unit = {
    if (closed) {
      throw new StorageErrorException(
        ENGINE_CLOSED_IO_ILLEGAL.getErrorCode,
        s"$fsType storage($id) engine($bindEngineLabel) has been closed, IO operation was illegal."
      )
    }
    if (System.currentTimeMillis() - lastAccessTime >= iOEngineExecutorMaxFreeTime) synchronized {
      if (System.currentTimeMillis() - lastAccessTime >= iOEngineExecutorMaxFreeTime) {
        initFS()
        logger.info(
          s"since the $fsType storage($id) engine($bindEngineLabel) is free for too long time, re-inited it in beforeOperation"
        )
      }
    }
    lastAccessTime = System.currentTimeMillis()
  }

  override def intercept(
      o: scala.Any,
      method: Method,
      args: Array[AnyRef],
      methodProxy: MethodProxy
  ): AnyRef = {
    if (closed && method.getName != "close") {
      throw new StorageErrorException(
        STORAGE_HAS_BEEN_CLOSED.getErrorCode,
        s"$fsType storage has been closed."
      )
    }
    if (System.currentTimeMillis() - lastAccessTime >= iOEngineExecutorMaxFreeTime) synchronized {
      method.getName match {
        case "init" =>
        case "storageName" => return fsType
        case "setUser" =>
          properties.put(StorageConfiguration.PROXY_USER.key, args(0).asInstanceOf[String]);
          return Unit
        case _ =>
          if (inited) {
            initFS()
            logger.info(s"since the $fsType storage($id) is free for too long time, re-inited it.")
          }
      }
    }
    lastAccessTime = System.currentTimeMillis()
    method.getName match {
      case "init" =>
        val user =
          if (properties.containsKey(StorageConfiguration.PROXY_USER.key)) {
            StorageConfiguration.PROXY_USER.getValue(properties)
          } else {
            null
          }
        if (args.length > 0 && args(0).isInstanceOf[java.util.Map[String, String]]) {
          properties.putAll(args(0).asInstanceOf[java.util.Map[String, String]])
        }
        if (StringUtils.isNoneBlank(user)) {
          properties.put(StorageConfiguration.PROXY_USER.key, user)
        }
        initFS()
        logger.warn(s"For user($user)inited a $fsType storage($id) .")
        Unit
      case "fsName" => fsType
      case "setUser" =>
        properties.put(StorageConfiguration.PROXY_USER.key, args(0).asInstanceOf[String]); Unit
      case "read" =>
        if (!inited) throw new IllegalAccessException("storage has not been inited.")
        new IOInputStream(args)
      case "write" =>
        if (!inited) throw new IllegalAccessException("storage has not been inited.")
        new IOOutputStream(args)
      case "renameTo" =>
        if (!inited || args.length < 2) {
          throw new IllegalAccessException("storage has not been inited.")
        }
        val params =
          args.map(MethodEntitySerializer.serializerJavaObject(_)).map(_.asInstanceOf[AnyRef])
        executeMethod(method.getName, params)
        new java.lang.Boolean(true)
      case "list" =>
        if (!inited || args.length < 1) {
          throw new IllegalAccessException("storage has not been inited.")
        }
        val params =
          Array(MethodEntitySerializer.serializerJavaObject(args(0))).map(_.asInstanceOf[AnyRef])
        val msg = executeMethod(method.getName, params)
        MethodEntitySerializer.deserializerToJavaObject[java.util.List[FsPath]](
          StorageUtils.deserializerResultToString(msg),
          new TypeToken[java.util.List[FsPath]]() {}.getType
        )
      case "listPathWithError" =>
        if (!inited || args.length < 1) {
          throw new IllegalAccessException("storage has not been inited.")
        }
        val params =
          Array(MethodEntitySerializer.serializerJavaObject(args(0))).map(_.asInstanceOf[AnyRef])
        val msg = executeMethod(method.getName, params)
        MethodEntitySerializer.deserializerToJavaObject[FsPathListWithError](
          StorageUtils.deserializerResultToString(msg),
          new TypeToken[FsPathListWithError]() {}.getType
        )
      case "toString" =>
        this.toString
      case "finalize" =>
        logger.info("no support method")
        Unit
      case _ =>
        if (!inited) throw new IllegalAccessException("storage has not been inited.")
        if (method.getName == "close") {
          closed = true
          bindEngineLabel.setIsJobGroupEnd("true")
          bindEngineLabel.setIsJobGroupHead("false")
        }
        val returnType = method.getReturnType
        if (args.length > 0) args(0) = MethodEntitySerializer.serializerJavaObject(args(0))
        val msg = executeMethod(method.getName, args)
        if (returnType == Void.TYPE) return Unit
        val result = MethodEntitySerializer.deserializerToJavaObject(
          StorageUtils.deserializerResultToString(msg),
          returnType
        )
        result.asInstanceOf[AnyRef]
    }

  }

  class IOInputStream(args: Array[AnyRef]) extends InputStream {
    private var fetched: Array[Byte] = _
    private var index = 0
    private var position = 0L
    private var markPosition = 0L
    private var readable = true
    private var canContinueFetch = true

    private def fetch: Unit = {
      if (!canContinueFetch) {
        readable = false
        fetched = null
        return
      }
      fetched = null
      beforeOperation()
      val params =
        Array(MethodEntitySerializer.serializerJavaObject(args(0)), position, fetchSize)
          .map(_.asInstanceOf[AnyRef])
      val fetchedMsg = executeMethod("read", params)
      if (StringUtils.isNotEmpty(fetchedMsg)) {
        val resultSet = ResultSetFactory.getInstance.getResultSetByType(ResultSetFactory.IO_TYPE)
        val reader = ResultSetReader.getResultSetReader(resultSet, fetchedMsg)
        val metaData = reader.getMetaData.asInstanceOf[IOMetaData]
        while (reader.hasNext) {
          fetched = reader.getRecord.asInstanceOf[IORecord].value
        }
        if (metaData.len == -1 || metaData.len < fetchSize) canContinueFetch = false
        index = 0
        position += fetched.length
      } else readable = false
    }

    override def read(): Int = synchronized {
      if (!readable) return -1
      if (fetched == null || index == fetched.length) {
        fetch
      }
      if (fetched == null || fetched.length == 0) return -1
      val v = fetched(index)
      index += 1
      v & 0xff
    }

    override def available(): Int = {
      if (!readable) return 0
      if (!canContinueFetch) {
        return fetched.length - index
      }
      beforeOperation()
      val params = Array(MethodEntitySerializer.serializerJavaObject(args(0)), position)
        .map(_.asInstanceOf[AnyRef])
      val available = if (fetched != null) fetched.length - index else 0
      val msg = StorageUtils.deserializerResultToString(executeMethod("available", params))
      val len = if (StringUtils.isEmpty(msg)) {
        0
      } else {
        Utils.tryAndError(msg.toInt)
      }
      available + len
    }

    override def skip(n: Long): Long = synchronized {
      if (!readable) return 0
      var left = n
      var _available = 0
      if (fetched != null) {
        _available = fetched.length - index
        if (_available > left) {
          index += left.toInt
          return left
        } else if (_available == left) {
          fetched = null
          return left
        } else {
          left -= _available
          fetched = null
        }
      }
      val skipped = _available
      _available = available()
      if (_available <= left) {
        position += _available
        canContinueFetch = false
        skipped + _available
      } else {
        position += left
        fetch
        n
      }
    }

    override def reset(): Unit = {
      index = 0
      position = markPosition
      fetched = null
      readable = true
      canContinueFetch = true
      markPosition = 0
    }

    override def mark(readLimit: Int): Unit = {
      markPosition = readLimit
    }

  }

  class IOOutputStream(args: Array[AnyRef]) extends OutputStream {
    private val cached = new Array[Byte](cacheSize)
    private var index = 0
    private var firstWrite = true

    override def write(b: Int): Unit = cached synchronized {
      if (index >= cacheSize) write
      cached(index) = b.toByte
      index += 1
    }

    private def write = {
      beforeOperation()
      val overwrite =
        if (!args(1).toString.toBoolean) false
        else if (firstWrite) {
          firstWrite = false
          true
        } else false
      val resultSet = ResultSetFactory.getInstance.getResultSetByType(ResultSetFactory.IO_TYPE)
      val writer = ResultSetWriter.getResultSetWriter(resultSet, Long.MaxValue, null)
      writer.addMetaData(new IOMetaData(0, index))
      writer.addRecord(new IORecord(cached.slice(0, index)))
      val params: Array[AnyRef] = Array(
        MethodEntitySerializer.serializerJavaObject(args(0)),
        overwrite.asInstanceOf[AnyRef],
        writer.toString()
      )
      val msg = executeMethod("write", params)
      if (msg == IOClientUtils.SUCCESS) {
        index = 0
      } else throw new IOException(msg)
    }

    override def flush(): Unit = cached synchronized {
      if (index > 0) {
        write
      }
    }

    override def close(): Unit = flush()
  }

}
