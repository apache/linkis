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

package org.apache.linkis.server.ticket

import org.apache.linkis.common.utils.Logging

import java.util.concurrent.ConcurrentHashMap

class UserTicketService extends ConcurrentHashMap[String, Long] with Logging {

  private val userTicketRedisKey: String = "user_ticket_redis_key"

  def asScala: Map[String, Long] = {
    getAll.asInstanceOf[Map[String, Long]]
  }

  def getAll: ConcurrentHashMap[String, Long] = {
    val result: ConcurrentHashMap[String, Long] = new ConcurrentHashMap[String, Long]
    val resource = RedisClient.getResource
    val ticketMap = resource.hgetAll(userTicketRedisKey)
    RedisClient.closeResource(resource)
    for (k <- ticketMap.keySet().asInstanceOf[Set[String]]) {
      result.put(k, ticketMap.get(k).toLong)
    }
    result
  }

  override def put(key: String, value: Long): Long = {
    val resource = RedisClient.getResource
    resource.hset(userTicketRedisKey, key, value.toString)
    RedisClient.closeResource(resource)
    value
  }

  override def containsKey(key: Any): Boolean = {
    get(key.toString) != 0
  }

  override def get(key: Any): Long = {
    logger.info(s"get login user $key")
    val resource = RedisClient.getResource
    val lastAccess = resource.hget(userTicketRedisKey, key.toString)
    RedisClient.closeResource(resource)
    if (null == lastAccess) {
      return 0
    }
    lastAccess.toLong
  }

  override def getOrDefault(key: Any, defaultValue: Long): Long = {
    val lastAccess = get(key.toString)
    if (lastAccess > 0) {
      lastAccess
    } else {
      defaultValue
    }
  }

  override def remove(key: Any): Long = {
    val resource = RedisClient.getResource
    val value = resource.hdel(userTicketRedisKey, key.toString)
    RedisClient.closeResource(resource)
    value
  }

}
