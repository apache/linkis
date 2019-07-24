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

package com.webank.wedatasphere.linkis.resourcemanager.notify

/**
  * Created by shanhuang on 9/11/18.
  */
trait DistributedQueue[T] {

  @throws[Exception]
  def offer(value: T): Unit

  @throws[Exception]
  def poll(): T

  @throws[Exception]
  def peek(): T

  @throws[Exception]
  def copyToArray(): Array[T]

  def destroy(): Unit
}
