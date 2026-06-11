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

package org.apache.linkis.entrance.listener

import org.apache.linkis.entrance.scheduler.EntranceGroupFactory
import org.apache.linkis.protocol.BroadcastProtocol
import org.apache.linkis.protocol.label.EntranceGroupCacheClearBroadcast
import org.apache.linkis.rpc.Sender

import org.junit.jupiter.api.{AfterEach, Assertions, BeforeEach, Test}
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.function.Executable
import org.mockito.{ArgumentMatchers, Mockito}
import org.mockito.Mockito._

/**
 * EntranceGroupCacheClearBroadcastListener 单元测试
 *
 * 测试覆盖:
 *   - 正确处理 EntranceGroupCacheClearBroadcast 广播消息
 *   - 调用 EntranceGroupFactory.clearAllGroupCache() 清除缓存
 *   - 记录适当的日志
 *   - 异常处理不影响广播流程
 *   - 忽略其他类型的广播消息
 */
@DisplayName("Entrance Group缓存清除广播监听器测试")
class EntranceGroupCacheClearBroadcastListenerTest {

  private var listener: EntranceGroupCacheClearBroadcastListener = _
  private var mockGroupFactory: EntranceGroupFactory = _

  @BeforeEach
  def setUp(): Unit = {
    listener = new EntranceGroupCacheClearBroadcastListener()
    mockGroupFactory = Mockito.mock(classOf[EntranceGroupFactory])
    // 使用反射设置 entranceGroupFactory（因为是@Autowired且var类型）
    val field = listener.getClass.getDeclaredField("entranceGroupFactory")
    field.setAccessible(true)
    field.set(listener, mockGroupFactory)
  }

  @AfterEach
  def tearDown(): Unit = {
    Mockito.reset(mockGroupFactory)
  }

  @Test
  @DisplayName("应该处理EntranceGroupCacheClearBroadcast消息")
  def testHandleEntranceGroupCacheClearBroadcast(): Unit = {
    val instance = "localhost:8080"
    val timestamp = System.currentTimeMillis()
    val broadcast = EntranceGroupCacheClearBroadcast(instance, timestamp)
    val sender = Sender.getSender("linkis-entrance")

    // 执行
    listener.onBroadcastEvent(broadcast, sender)

    // 验证
    verify(mockGroupFactory, times(1)).clearAllGroupCache()
  }

  @Test
  @DisplayName("应该忽略其他类型的广播消息")
  def testIgnoreOtherBroadcastMessages(): Unit = {
    // 创建一个mock的BroadcastProtocol，但不是EntranceGroupCacheClearBroadcast
    val otherBroadcast = Mockito.mock(classOf[BroadcastProtocol])
    val sender = Sender.getSender("linkis-entrance")

    // 执行 - 不应该抛出异常
    Assertions.assertDoesNotThrow(new Executable {
      override def execute(): Unit = listener.onBroadcastEvent(otherBroadcast, sender)
    })

    // 验证 - 不应该调用clearAllGroupCache
    verify(mockGroupFactory, never()).clearAllGroupCache()
  }

  @Test
  @DisplayName("当clearAllGroupCache抛出异常时应该捕获并记录错误")
  def testHandleExceptionWhenClearCacheFails(): Unit = {
    val instance = "localhost:8080"
    val broadcast = EntranceGroupCacheClearBroadcast(instance, System.currentTimeMillis())
    val sender = Sender.getSender("linkis-entrance")

    // 模拟clearAllGroupCache抛出异常
    when(mockGroupFactory.clearAllGroupCache())
      .thenThrow(new RuntimeException("Cache clear failed"))

    // 执行 - 不应该抛出异常
    Assertions.assertDoesNotThrow(new Executable {
      override def execute(): Unit = listener.onBroadcastEvent(broadcast, sender)
    })

    // 验证 - 方法仍然被调用
    verify(mockGroupFactory, times(1)).clearAllGroupCache()
  }

  @Test
  @DisplayName("应该正确处理来自不同实例的广播消息")
  def testHandleBroadcastFromDifferentInstances(): Unit = {
    val instances = Seq("localhost:8080", "192.168.1.100:9001", "entrance-service-1")

    instances.foreach { instance =>
      val broadcast = EntranceGroupCacheClearBroadcast(instance, System.currentTimeMillis())
      val sender = Sender.getSender("linkis-entrance")

      // 重置mock
      Mockito.reset(mockGroupFactory)

      // 执行
      listener.onBroadcastEvent(broadcast, sender)

      // 验证
      verify(mockGroupFactory, times(1)).clearAllGroupCache()
    }
  }

  @Test
  @DisplayName("多个广播消息应该被独立处理")
  def testHandleMultipleBroadcastMessages(): Unit = {
    val sender = Sender.getSender("linkis-entrance")

    // 发送多个广播
    val broadcasts = (1 to 5).map { i =>
      EntranceGroupCacheClearBroadcast(s"instance-$i", System.currentTimeMillis())
    }

    broadcasts.foreach { broadcast =>
      listener.onBroadcastEvent(broadcast, sender)
    }

    // 验证 - 每个广播都触发了一次缓存清除
    verify(mockGroupFactory, times(5)).clearAllGroupCache()
  }

}
