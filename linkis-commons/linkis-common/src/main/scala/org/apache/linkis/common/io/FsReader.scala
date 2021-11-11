/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.common.io

import java.io.{Closeable, IOException}


abstract class FsReader[K <: MetaData, V <: Record] extends Closeable {
  @throws[IOException]
  def getRecord: Record
  @throws[IOException]
  def getMetaData: MetaData
  @throws[IOException]
  def skip(recordNum: Int): Int
  @throws[IOException]
  def getPosition: Long
  @throws[IOException]
  def hasNext: Boolean

  /**
    * Number of unread bytes remaining(剩余未读bytes数)
    *
    * @return Number of unread bytes remaining(剩余未读bytes数)
    * @throws IOException If the acquisition fails, an exception is thrown.(如获取失败，则抛出异常)
    */
  @throws[IOException]
  def available: Long
}
