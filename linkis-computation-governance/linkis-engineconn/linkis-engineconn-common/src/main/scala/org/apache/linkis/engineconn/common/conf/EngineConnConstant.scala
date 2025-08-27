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

package org.apache.linkis.engineconn.common.conf

object EngineConnConstant {

  val MAX_TASK_NUM = 10000

  val SPRING_CONF_MAP_NAME = "SpringConfMap"

  val MAX_EXECUTOR_ID_NAME = "MaxExecutorId"

  var hiveLogReg = "The url to track the job: http://.*?/proxy/(application_[0-9]+_[0-9]+)/"

  val YARN_LOG_URL = "Yarn application url:"
}
