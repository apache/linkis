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
 
package org.apache.linkis.engineconn.computation.executor.conf

import org.apache.linkis.common.conf.{ByteType, CommonVars}


object ComputationExecutorConf {

  val ENGINE_RESULT_SET_MAX_CACHE = CommonVars("wds.linkis.engine.resultSet.cache.max", new ByteType("0k"))


  val ENGINE_LOCK_DEFAULT_EXPIRE_TIME = CommonVars("wds.linkis.engine.lock.expire.time", 2 * 60 * 1000)

  val ENGINE_MAX_TASK_EXECUTE_NUM = CommonVars("wds.linkis.engineconn.max.task.execute.num", 0)


  val ENGINE_PROGRESS_FETCH_INTERVAL = CommonVars("wds.linkis.engineconn.progresss.fetch.interval-in-seconds", 5)

  val UDF_LOAD_FAILED_IGNORE = CommonVars("wds.linkis.engineconn.udf.load.ignore", true)

  val ENGINE_CONCURRENT_THREAD_NUM = CommonVars("wds.linkis.engineconn.concurrent.thread.num", 20)

}
