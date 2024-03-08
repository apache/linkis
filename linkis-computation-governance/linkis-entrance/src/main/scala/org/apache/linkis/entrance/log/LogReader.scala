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

package org.apache.linkis.entrance.log

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.exception.LogReadFailedException
import org.apache.linkis.entrance.utils.LogHelper

import org.apache.commons.io.{IOUtils, LineIterator}
import org.apache.commons.lang3.StringUtils

import java.io.{Closeable, InputStream, IOException}

import scala.util.matching.Regex

abstract class LogReader(charset: String) extends Closeable with Logging {
  import LogReader._

  @throws[IOException]
  def getInputStream: InputStream

  def getCharset: String = charset

  /**
   * Get logs and sort by log level(获取日志，并按照日志级别分类)
   * @param logs
   *   Required length must be 4(要求长度必须为4)
   * @param fromLine
   * @param size
   * @return
   *   The index of 0-3 means the following:(0-3的index表示意思如下：) 0 ERROR level log(ERROR级别的日志) 1 Warn
   *   level log(Warn级别的日志) 2 INFO level log(INFO级别的日志) 3 All logs(所有的日志)
   */
  def readArray(logs: Array[String], fromLine: Int, size: Int = 100): Int = {
    if (logs.length != 4) {
      throw new LogReadFailedException(" The length of the log must be 4(logs的长度必须为4)")
    }
    val error = new StringBuilder
    val warning = new StringBuilder
    val info = new StringBuilder
    val all = new StringBuilder
    val read = readLog(
      singleLog => {
        val length = 1
        if (StringUtils.isNotBlank(singleLog)) {
          singleLog match {
            case ERROR_HEADER1() | ERROR_HEADER2() =>
              concatLog(length, singleLog, error, all)
            case WARN_HEADER1() | WARN_HEADER2() =>
              if (!LogHelper.isExcludeLog(singleLog)) {
                concatLog(length, singleLog, warning, all)
              }
            case INFO_HEADER1() | INFO_HEADER2() =>
              if (!LogHelper.isExcludeLog(singleLog)) {
                concatLog(length, singleLog, info, all)
              }
            case _ =>
              if (!LogHelper.isExcludeLog(singleLog)) {
                concatLog(length, singleLog, info, all)
              }
          }
        }
      },
      fromLine,
      size
    )
    if (error.nonEmpty) error.setLength(error.size - 1)
    if (warning.nonEmpty) warning.setLength(warning.size - 1)
    if (info.nonEmpty) info.setLength(info.size - 1)
    if (all.nonEmpty) all.setLength(all.size - 1)
    logs(0) = error.toString()
    logs(1) = warning.toString()
    logs(2) = info.toString()
    logs(3) = all.toString()
    read
  }

  private def concatLog(length: Int, log: String, flag: StringBuilder, all: StringBuilder): Unit = {
    if (length == 1) {
      flag ++= log ++= "\n"
      all ++= log ++= "\n"
    } else {
      flag ++= log ++= "\n\n"
      all ++= log ++= "\n\n"
    }
  }

  def read(logs: java.lang.StringBuilder, fromLine: Int, size: Int = 100): Int = {
    logs.setLength(0)
    val read = readLog(
      (r: String) => {
        logs.append(r)
        logs.append("\n")
      },
      fromLine,
      size
    )
    if (logs.length() > 0) logs.setLength(logs.length() - 1)
    read
  }

  protected def readLog(deal: String => Unit, fromLine: Int, size: Int = 100): Int = {

    // fromline param with begin 1 ,if set 0 missing first line
    val from = if (fromLine < 1) 1 else fromLine
    var line = 1
    var read = 0
    val inputStream = getInputStream
    val lineIterator = IOUtils.lineIterator(inputStream, charset)
    Utils.tryFinally(while (lineIterator.hasNext && (read < size || size < 0)) {
      val r = lineIterator.next()
      if (line >= from) {
        deal(r)
        read += 1
      }
      line += 1
    }) {
      if (null != lineIterator) {
        LineIterator.closeQuietly(lineIterator)
      }
      if (null != inputStream) {
        IOUtils.closeQuietly(inputStream)
      }
    }
    read
  }

}

object LogReader {
  val ERROR_HEADER1: Regex = "[0-9\\-]{10,10} [0-9:]{8,8}.?\\d{0,3} SYSTEM-ERROR ".r.unanchored
  val ERROR_HEADER2: Regex = "[0-9\\-/]{10,10} [0-9:]{8,8}.?\\d{0,3} ERROR ".r.unanchored
  val ERROR_HEADER3: Regex = "[0-9\\-/]{10,10} [0-9:]{8,8}.?\\d{0,3} ERROR ".r.unanchored
  val WARN_HEADER1: Regex = "[0-9\\-]{10,10} [0-9:]{8,8}.?\\d{0,3} SYSTEM-WARN ".r.unanchored
  val WARN_HEADER2: Regex = "[0-9\\-/]{10} [0-9:]{8}.?\\d{0,3} WARN ".r.unanchored
  val INFO_HEADER1: Regex = "[0-9\\-]{10,10} [0-9:]{8,8}.?\\d{0,3} SYSTEM-INFO ".r.unanchored
  val INFO_HEADER2: Regex = "[0-9\\-/]{10,10} [0-9:]{8,8}.?\\d{0,3} INFO ".r.unanchored
}
