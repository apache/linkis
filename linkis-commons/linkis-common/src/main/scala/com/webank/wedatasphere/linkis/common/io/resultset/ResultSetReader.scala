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

package com.webank.wedatasphere.linkis.common.io.resultset

import java.io.{ByteArrayInputStream, InputStream}

import com.webank.wedatasphere.linkis.common.io.{FsReader, MetaData, Record}


abstract class ResultSetReader[K <: MetaData, V <: Record](resultSet: ResultSet[K, V],
                                                           inputStream: InputStream) extends FsReader {
  def this(resultSet: ResultSet[K, V], data: String) = this(resultSet,
    new ByteArrayInputStream(data.getBytes(resultSet.charset)))
  def this(data: String) = this(null, data)
}