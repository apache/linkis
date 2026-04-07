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

package org.apache.linkis.entrance.protocol

import org.apache.linkis.protocol.BroadcastProtocol

import org.junit.jupiter.api.{Assertions, Test}
import org.junit.jupiter.api.DisplayName

/**
 * EntranceGroupCacheClearBroadcast 单元测试
 *
 * 测试覆盖:
 *   - 广播协议正确继承 BroadcastProtocol
 *   - 字段正确赋值
 *   - throwsIfAnyFailed 属性为 false（不影响offline流程）
 */
@DisplayName("Entrance Group缓存清除广播协议测试")
class EntranceGroupCacheClearBroadcastTest {

  @Test
  @DisplayName("应该正确创建广播消息")
  def testCreateBroadcastMessage(): Unit = {
    val instance = "localhost:8080"
    val timestamp = 1712345678900L

    val broadcast = EntranceGroupCacheClearBroadcast(instance, timestamp)

    Assertions.assertEquals(instance, broadcast.instance, "实例标识应该匹配")
    Assertions.assertEquals(timestamp, broadcast.timestamp, "时间戳应该匹配")
  }

  @Test
  @DisplayName("应该是BroadcastProtocol的子类型")
  def testExtendsBroadcastProtocol(): Unit = {
    val broadcast = EntranceGroupCacheClearBroadcast("test-instance", System.currentTimeMillis())

    Assertions.assertTrue(
      broadcast.isInstanceOf[BroadcastProtocol],
      "EntranceGroupCacheClearBroadcast应该继承BroadcastProtocol"
    )
  }

  @Test
  @DisplayName("throwsIfAnyFailed应该始终为false")
  def testThrowsIfAnyFailedIsFalse(): Unit = {
    val broadcast = EntranceGroupCacheClearBroadcast("test-instance", System.currentTimeMillis())

    Assertions.assertFalse(
      broadcast.throwsIfAnyFailed,
      "throwsIfAnyFailed应该为false，即使部分实例接收失败也不影响offline流程"
    )
  }

  @Test
  @DisplayName("应该支持不同的实例标识格式")
  def testDifferentInstanceFormats(): Unit = {
    // 测试不同的实例标识格式
    val instances =
      Seq("localhost:8080", "192.168.1.100:9001", "entrance-service-1", "service-instance:8080")

    instances.foreach { instance =>
      val broadcast = EntranceGroupCacheClearBroadcast(instance, System.currentTimeMillis())
      Assertions.assertEquals(instance, broadcast.instance, s"实例标识 '$instance' 应该匹配")
    }
  }

  @Test
  @DisplayName("时间戳应该使用毫秒级精度")
  def testTimestampPrecision(): Unit = {
    val before = System.currentTimeMillis()
    val broadcast = EntranceGroupCacheClearBroadcast("test-instance", System.currentTimeMillis())
    val after = System.currentTimeMillis()

    Assertions.assertTrue(
      broadcast.timestamp >= before && broadcast.timestamp <= after,
      "时间戳应该在合理范围内"
    )
  }

  @Test
  @DisplayName("case class应该正确实现equals和hashCode")
  def testCaseClassEquality(): Unit = {
    val instance = "test-instance"
    val timestamp = 1712345678900L

    val broadcast1 = EntranceGroupCacheClearBroadcast(instance, timestamp)
    val broadcast2 = EntranceGroupCacheClearBroadcast(instance, timestamp)
    val broadcast3 = EntranceGroupCacheClearBroadcast("other-instance", timestamp)

    Assertions.assertEquals(broadcast1, broadcast2, "相同参数创建的实例应该相等")
    Assertions.assertEquals(broadcast1.hashCode, broadcast2.hashCode, "相同实例的hashCode应该相等")
    Assertions.assertNotEquals(broadcast1, broadcast3, "不同参数创建的实例应该不相等")
  }

}
