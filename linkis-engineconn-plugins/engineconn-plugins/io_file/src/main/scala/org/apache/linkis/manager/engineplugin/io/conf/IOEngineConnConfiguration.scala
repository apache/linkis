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
 
package org.apache.linkis.manager.engineplugin.io.conf

import org.apache.linkis.common.conf.CommonVars

object IOEngineConnConfiguration {

  val EXTRA_JAVA_OTS = CommonVars[String]("wds.linkis.engine.io.opts", " -Dfile.encoding=UTF-8 ")

  val IO_FS_MAX = CommonVars[Int]("wds.linkis.storage.io.fs.num", 5)

  val IO_FS_CLEAR_TIME = CommonVars[Long]("wds.linkis.storage.io.fs.clear.time", 1000L)

  val IO_FS_ID_LIMIT = CommonVars[Long]("wds.linkis.storage.io.fs.id.limit", Long.MaxValue/3*2)

  val OUTPUT_LIMIT = CommonVars[Int]("wds.linkis.engineconn.io.output.limit", Int.MaxValue)

  val DEFAULT_VERSION = CommonVars[String]("wds.linkis.engineconn.io.version", "*")

  val IO_FILE_CONCURRENT_LIMIT = CommonVars[Int]("wds.linkis.engineconn.io_file.concurrent.limit", 100)

}
