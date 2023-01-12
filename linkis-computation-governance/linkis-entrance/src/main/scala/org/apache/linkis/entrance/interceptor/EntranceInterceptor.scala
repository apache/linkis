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

package org.apache.linkis.entrance.interceptor

import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.governance.common.entity.job.JobRequest

trait EntranceInterceptor {

  /**
   * The apply function is to supplement the information of the incoming parameter task, making the
   * content of this task more complete. Additional information includes: database information
   * supplement, custom variable substitution, code check, limit limit, etc.
   * apply函数是对传入参数task进行信息的补充，使得这个task的内容更加完整。 补充的信息包括: 数据库信息补充、自定义变量替换、代码检查、limit限制等。
   *
   * @param task
   * @param logAppender
   *   Used to cache the necessary reminder logs and pass them to the upper layer(用于缓存必要的提醒日志，传给上层)
   * @return
   */
  @throws[ErrorException]
  def apply(task: JobRequest, logAppender: java.lang.StringBuilder): JobRequest

}
