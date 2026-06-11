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

package org.apache.linkis.hadoop.common.utils

import java.util.concurrent.{ConcurrentHashMap, Executors, TimeUnit}

import scala.collection.JavaConverters._

import org.junit.jupiter.api.{Assertions, Test}

class HDFSUtilsKeytabCacheTest {

  private def getCreateKeytabCacheKeyMethod = {
    val method =
      HDFSUtils.getClass.getDeclaredMethod("createKeytabCacheKey", classOf[String], classOf[String])
    method.setAccessible(true)
    method
  }

  @Test
  def testKeytabSuffixConstant: Unit = {
    Assertions.assertNotNull(HDFSUtils.KEYTAB_SUFFIX)
    Assertions.assertEquals(".keytab", HDFSUtils.KEYTAB_SUFFIX)
  }

  @Test
  def testCreateKeytabCacheKeyWithNullLabel: Unit = {
    val method = getCreateKeytabCacheKeyMethod
    // null label → key is just userName
    val key = method.invoke(HDFSUtils, "testuser", null).asInstanceOf[String]
    Assertions.assertEquals("testuser", key)
  }

  @Test
  def testCreateKeytabCacheKeyWithLabel: Unit = {
    val method = getCreateKeytabCacheKeyMethod
    // non-null label → key is "userName#label"
    val key = method.invoke(HDFSUtils, "testuser", "cluster1").asInstanceOf[String]
    Assertions.assertEquals("testuser#cluster1", key)
  }

  @Test
  def testCreateKeytabCacheKeySameUserSameLabel: Unit = {
    val method = getCreateKeytabCacheKeyMethod
    val key1 = method.invoke(HDFSUtils, "testuser", null).asInstanceOf[String]
    val key2 = method.invoke(HDFSUtils, "testuser", null).asInstanceOf[String]
    Assertions.assertEquals(key1, key2)
  }

  @Test
  def testCreateKeytabCacheKeyDifferentUsers: Unit = {
    val method = getCreateKeytabCacheKeyMethod
    val key1 = method.invoke(HDFSUtils, "user1", null).asInstanceOf[String]
    val key2 = method.invoke(HDFSUtils, "user2", null).asInstanceOf[String]
    Assertions.assertNotEquals(key1, key2)
    Assertions.assertTrue(key1.contains("user1"))
    Assertions.assertTrue(key2.contains("user2"))
  }

  @Test
  def testCreateKeytabCacheKeyDifferentLabels: Unit = {
    val method = getCreateKeytabCacheKeyMethod
    val key1 = method.invoke(HDFSUtils, "testuser", "cluster1").asInstanceOf[String]
    val key2 = method.invoke(HDFSUtils, "testuser", "cluster2").asInstanceOf[String]
    Assertions.assertNotEquals(key1, key2)
    Assertions.assertTrue(key1.contains("cluster1"))
    Assertions.assertTrue(key2.contains("cluster2"))
  }

  @Test
  def testNullLabelDifferentFromDefaultLabel: Unit = {
    val method = getCreateKeytabCacheKeyMethod
    // null label → "testuser", "default" label → "testuser#default", they are different
    val key1 = method.invoke(HDFSUtils, "testuser", null).asInstanceOf[String]
    val key2 = method.invoke(HDFSUtils, "testuser", "default").asInstanceOf[String]
    Assertions.assertNotEquals(key1, key2)
  }

  @Test
  def testConcurrentCreateKeytabCacheKey: Unit = {
    val method = getCreateKeytabCacheKeyMethod
    val userName = "testuser_concurrent"
    val threadCount = 10

    val executor = Executors.newFixedThreadPool(threadCount)
    val resultKeys = new ConcurrentHashMap[String, String]()

    try {
      val futures = (0 until threadCount).map { _ =>
        executor.submit(new Runnable {
          override def run(): Unit = {
            val key = method.invoke(HDFSUtils, userName, null).asInstanceOf[String]
            resultKeys.put(key, key)
          }
        })
      }
      futures.foreach(_.get())
    } finally {
      executor.shutdown()
      executor.awaitTermination(5, TimeUnit.SECONDS)
    }

    // null label → key is just userName, all threads should get the same key
    Assertions.assertEquals(1, resultKeys.size())
    Assertions.assertTrue(resultKeys.containsKey(userName))
  }

}
