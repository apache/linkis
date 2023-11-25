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

import org.apache.linkis.server.conf.SessionHAConfiguration

import org.apache.commons.lang3.StringUtils
import org.apache.commons.pool2.impl.GenericObjectPoolConfig

import scala.collection.JavaConverters.setAsJavaSetConverter

import redis.clients.jedis.{Jedis, JedisPool, JedisSentinelPool}

object RedisClient {

  private val RedisTimeOut: Int = 30000 // ms
  private val MaxTotal = 300
  private val MaxIdle = 100
  private val MinIdle = 1

  @transient private var Pool: JedisPool = _
  @transient private var PoolSentinal: JedisSentinelPool = _

  makePool(RedisTimeOut, MaxTotal, MaxIdle, MinIdle)

  def makePool(redisTimeout: Int, maxTotal: Int, maxIdle: Int, minIdle: Int): Unit = {
    makePool(redisTimeout, maxTotal, maxIdle, minIdle, 10000)
  }

  def makePool(
      redisTimeout: Int,
      maxTotal: Int,
      maxIdle: Int,
      minIdle: Int,
      maxWaitMillis: Long
  ): Unit = {
    if (Pool == null) {
      val poolConfig = getGenericObjectPoolConfig(maxTotal, maxIdle, minIdle, maxWaitMillis)

      if (
          StringUtils.isNotBlank(SessionHAConfiguration.RedisSentinalMaster) && StringUtils
            .isNotBlank(SessionHAConfiguration.RedisSentinalServer)
      ) {
        val nodes = SessionHAConfiguration.RedisSentinalServer.split(",").toSet[String]
        PoolSentinal = new JedisSentinelPool(
          SessionHAConfiguration.RedisSentinalMaster,
          nodes.asJava,
          poolConfig,
          redisTimeout,
          SessionHAConfiguration.RedisPassword
        )
      } else {
        Pool = new JedisPool(
          poolConfig,
          SessionHAConfiguration.RedisHost,
          SessionHAConfiguration.RedisPort,
          redisTimeout,
          SessionHAConfiguration.RedisPassword
        )
      }

      shutdownHooks()
    }
  }

  def shutdownHooks(): Unit = {
    val hook = new Thread {

      override def run(): Unit = {
        if (null != Pool) {
          Pool.destroy()
        }
        if (null != PoolSentinal) {
          PoolSentinal.destroy()
        }
      }
    }
    sys.addShutdownHook(hook.run())
  }

  def getGenericObjectPoolConfig(
      maxTotal: Int,
      maxIdle: Int,
      minIdle: Int,
      maxWaitMillis: Long
  ): GenericObjectPoolConfig[Jedis] = {
    val poolConfig = new GenericObjectPoolConfig[Jedis]()
    poolConfig.setMaxTotal(maxTotal)
    poolConfig.setMaxIdle(maxIdle)
    poolConfig.setMinIdle(minIdle)
    poolConfig.setTestOnBorrow(true)
    poolConfig.setTestOnReturn(true)
    poolConfig.setMaxWaitMillis(maxWaitMillis)
    poolConfig
  }

  def getResource: Jedis = {
    if (null != Pool) {
      return Pool.getResource
    }
    PoolSentinal.getResource
  }

  def closeResource(jedis: Jedis): Unit = {
    if (null != jedis) {
      jedis.close()
    }
  }

}
