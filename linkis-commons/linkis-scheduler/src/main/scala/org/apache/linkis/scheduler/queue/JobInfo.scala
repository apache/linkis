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

package org.apache.linkis.scheduler.queue

/**
 * JobInfo Is the job information that the server sends to the front end.（是服务端传给前端的job信息） id: Job
 * execution id（job执行id） output: Result set（结果集） state: Job status（job状态） progress: Job execution
 * progress（job执行的进度） metric: Some common information about the job, including the startup time, the
 * time that has been run, etc.（job的一些常用信息，包含启动时间，已经运行的时间等）
 */
class JobInfo(id: String, output: String, state: String, progress: Float, metric: String) {
  def getId: String = id
  def getOutput: String = output
  def getState: String = state
  def getProgress: Float = progress
  def getMetric: String = metric
}
