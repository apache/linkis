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

package org.apache.linkis.storage.source

import org.apache.linkis.common.io.{FsWriter, MetaData, Record}

import org.apache.commons.io.IOUtils
import org.apache.commons.math3.util.Pair

import java.util

import scala.collection.JavaConverters._

abstract class AbstractFileSource(var fileSplits: Array[FileSplit]) extends FileSource {

  override def shuffle(s: Record => Record): FileSource = {
    fileSplits.foreach(_.shuffler = s)
    this
  }

  override def page(page: Int, pageSize: Int): FileSource = {
    fileSplits.foreach(_.page(page, pageSize))
    this
  }

  override def addParams(params: util.Map[String, String]): FileSource = {
    fileSplits.foreach(_.addParams(params))
    this
  }

  override def addParams(key: String, value: String): FileSource = {
    fileSplits.foreach(_.addParams(key, value))
    this
  }

  override def getFileSplits: Array[FileSplit] = this.fileSplits

  override def getParams: util.Map[String, String] =
    fileSplits.map(_.params.asScala).foldLeft(Map[String, String]())(_ ++ _).asJava

  override def write[K <: MetaData, V <: Record](fsWriter: FsWriter[K, V]): Unit =
    fileSplits.foreach(_.write(fsWriter))

  override def close(): Unit = this.fileSplits.foreach(IOUtils.closeQuietly)

  override def collect(): Array[Pair[Object, util.ArrayList[Array[String]]]] =
    fileSplits.map(_.collect())

  override def getTotalLine: Int = this.fileSplits.map(_.totalLine).sum

  override def getTypes: Array[String] = this.fileSplits.map(_.`type`)

  override def getFileInfo(needToCountRowNumber: Int = 5000): Array[Pair[Int, Int]] =
    fileSplits.map(_.getFileInfo(needToCountRowNumber))

}
