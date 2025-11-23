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

package org.apache.linkis.storage.utils

import org.apache.linkis.common.io.{Fs, FsPath}
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.hadoop.common.conf.HadoopConf
import org.apache.linkis.storage.{LineMetaData, LineRecord}
import org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary.CONFIGURATION_NOT_READ
import org.apache.linkis.storage.exception.StorageFatalException
import org.apache.linkis.storage.resultset.{ResultSetFactory, ResultSetReader, ResultSetWriter}

import org.apache.commons.lang3.StringUtils

import java.io.{Closeable, File, InputStream, OutputStream}
import java.lang.reflect.Method
import java.text.NumberFormat

import scala.collection.mutable

object StorageUtils extends Logging {

  val HDFS = "hdfs"
  val FILE = "file"
  val OSS = "oss"
  val S3 = "s3"
  val BLOB = "https"

  val FILE_SCHEMA = "file://"
  val HDFS_SCHEMA = "hdfs://"
  val OSS_SCHEMA = "oss://"
  val S3_SCHEMA = "s3://"
  val BLOB_SCHEMA = "https://"

  private val nf = NumberFormat.getInstance()
  nf.setGroupingUsed(false)
  nf.setMaximumFractionDigits(StorageConfiguration.DOUBLE_FRACTION_LEN.getValue)

  def doubleToString(value: Double): String = {
    if (value.isNaN) {
      "NaN"
    } else {
      nf.format(value)
    }
  }

  def loadClass[T](classStr: String, op: T => String): Map[String, T] = {
    val _classes = classStr.split(",")
    val classes = mutable.LinkedHashMap[String, T]()
    for (clazz <- _classes) {
      Utils.tryAndError {
        val obj = Utils.getClassInstance[T](clazz.trim)
        classes += op(obj) -> obj
      }
    }
    classes.toMap
  }

  /**
   * Get the corresponding class by passing in the subclass and package name(通过传入子类和包名获得对应的class)
   * @param classStr：Class
   *   name(类名)
   * @param pge:Class
   *   package name(类的包名)
   * @param op：Get
   *   key value(获取键值)
   * @tparam T
   * @return
   */
  def loadClasses[T](
      classStr: String,
      pge: String,
      op: Class[T] => String
  ): Map[String, Class[T]] = {
    val _classes: Array[String] =
      if (StringUtils.isEmpty(pge)) classStr.split(",")
      else classStr.split(",").map { value: String => pge + "." + value }
    val classes = mutable.LinkedHashMap[String, Class[T]]()
    for (clazz <- _classes) {
      Utils.tryAndError({
        val _class =
          Thread.currentThread.getContextClassLoader.loadClass(clazz.trim).asInstanceOf[Class[T]]
        classes += op(_class) -> _class
      })
    }
    classes.toMap
  }

  /**
   * Get the suffix of the file name(获得文件名的后缀)
   * @param path
   * @return
   */
  def pathToSuffix(path: String): String = {
    val fileName = new File(path).getName
    if ((fileName != null) && (fileName.length > 0)) {
      val dot: Int = fileName.lastIndexOf('.')
      if ((dot > -1) && (dot < (fileName.length - 1))) return fileName.substring(dot + 1)
    }
    fileName
  }

  /**
   * Reflection calling method(反射调用方法)
   * @param obj
   * @param method
   * @param args
   * @return
   */
  def invoke(obj: Any, method: Method, args: Array[AnyRef]): Any = {
    method.invoke(obj, args)
  }

  /**
   * Serialized string is a result set of type Text(序列化字符串为Text类型的结果集)
   * @param value
   * @return
   */
  def serializerStringToResult(value: String): String = {
    val resultSet = ResultSetFactory.getInstance.getResultSetByType(ResultSetFactory.TEXT_TYPE)
    val writer = ResultSetWriter.getResultSetWriter(resultSet, Long.MaxValue, null)
    val metaData = new LineMetaData()
    val record = new LineRecord(value)
    writer.addMetaData(metaData)
    writer.addRecord(record)
    val res = writer.toString()
    Utils.tryQuietly(writer.close())
    res
  }

  /**
   * The result set of serialized text is a string(序列化text的结果集为字符串)
   * @param result
   * @return
   */
  def deserializerResultToString(result: String): String = {
    val resultSet = ResultSetFactory.getInstance.getResultSetByType(ResultSetFactory.TEXT_TYPE)
    val reader = ResultSetReader.getResultSetReader(resultSet, result)
    reader.getMetaData
    val sb = new StringBuilder
    while (reader.hasNext) {
      val record = reader.getRecord.asInstanceOf[LineRecord]
      sb.append(record.getLine)
    }
    val value = sb.toString()
    Utils.tryQuietly(reader.close())
    value
  }

  def close(outputStream: OutputStream): Unit = {
    close(outputStream, null, null)
  }

  def close(inputStream: InputStream): Unit = {
    close(null, inputStream, null)
  }

  def close(fs: Fs): Unit = {
    close(null, null, fs)
  }

  def close(outputStream: OutputStream, inputStream: InputStream, fs: Fs): Unit = {
    Utils.tryFinally(if (outputStream != null) outputStream.close())()
    Utils.tryFinally(if (inputStream != null) inputStream.close())()
    Utils.tryFinally(if (fs != null) fs.close())()
  }

  def close(closeable: Closeable): Unit = {
    Utils.tryFinally(if (closeable != null) closeable.close())()
  }

  def getJvmUser: String = System.getProperty("user.name")

  def isHDFSNode: Boolean = {
    val confPath = new File(HadoopConf.hadoopConfDir)
    // TODO IO-client mode need return false
    if (!confPath.exists() || confPath.isFile) {
      throw new StorageFatalException(
        CONFIGURATION_NOT_READ.getErrorCode,
        CONFIGURATION_NOT_READ.getErrorDesc
      )
    } else true
  }

  /**
   * Returns the FsPath by determining whether the path is a schema. By default, the FsPath of the
   * file is returned. 通过判断path是否为schema来返回FsPath，默认返回file的FsPath
   * @param path
   * @return
   */
  def getFsPath(path: String): FsPath = {
    if (
        path.startsWith(FILE_SCHEMA) || path.startsWith(HDFS_SCHEMA) || path.startsWith(BLOB_SCHEMA)
    ) new FsPath(path)
    else {
      new FsPath(FILE_SCHEMA + path)
    }
  }

  def readBytes(inputStream: InputStream, bytes: Array[Byte], len: Int): Int = {
    var count = 0
    var readLen = 0
    // 当使用s3存储结果文件时时，com.amazonaws.services.s3.model.S3InputStream无法正确读取.dolphin文件。需要在循环条件添加:
    // readLen >= 0
    // To resolve the issue when using S3 to store result files and
    // com.amazonaws.services.s3.model.S3InputStream to read .dolphin files, you need to add the
    // condition readLen >= 0 in the loop.
    while (readLen < len && readLen >= 0) {
      count = inputStream.read(bytes, readLen, len - readLen)
      if (count == -1 && inputStream.available() < 1) return readLen
      readLen += count
    }
    readLen
  }

  def isIOProxy(): Boolean = {
    StorageConfiguration.ENABLE_IO_PROXY.getValue
  }

  def isHDFSPath(fsPath: FsPath): Boolean = {
    HDFS.equals(fsPath.getFsType)
  }

}
