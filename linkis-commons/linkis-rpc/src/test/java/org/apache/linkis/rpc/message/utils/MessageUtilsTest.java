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

package org.apache.linkis.rpc.message.utils;

import org.apache.linkis.rpc.message.method.MethodExecuteWrapper;
import org.apache.linkis.rpc.message.parser.ServiceMethod;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageUtilsTest {

  private List<ServiceMethod> notRepeatedServiceMethods;
  private List<ServiceMethod> repeatedServiceMethods;

  @BeforeEach
  void setUp() {
    notRepeatedServiceMethods = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      ServiceMethod tmp = new ServiceMethod();
      tmp.setOrder(i);
      notRepeatedServiceMethods.add(tmp);
    }
    repeatedServiceMethods = new ArrayList<>();
    repeatedServiceMethods.addAll(notRepeatedServiceMethods);
    for (int i = 0; i < 10; i++) {
      ServiceMethod tmp = new ServiceMethod();
      tmp.setOrder(i);
      repeatedServiceMethods.add(tmp);
    }
  }

  @Test
  void getMinOrders() {
    List<MethodExecuteWrapper> notRepeatedWrapper = new ArrayList<>();
    for (ServiceMethod serviceMethod : notRepeatedServiceMethods) {
      notRepeatedWrapper.add(new MethodExecuteWrapper(serviceMethod));
    }
    List<MethodExecuteWrapper> result = MessageUtils.getMinOrders(notRepeatedWrapper);
    assertThat(result).singleElement().isNotNull();
    assertThat(result.get(0).getOrder()).isZero();

    List<MethodExecuteWrapper> repeatedWrapper = new ArrayList<>();
    for (ServiceMethod serviceMethod : repeatedServiceMethods) {
      repeatedWrapper.add(new MethodExecuteWrapper(serviceMethod));
    }
    result = MessageUtils.getMinOrders(repeatedWrapper);
    assertThat(result).size().isEqualTo(2);
    assertThat(result.get(0).getOrder()).isZero();
  }

  @Test
  void orderIsLast() {
    int maxOrder = Integer.MAX_VALUE;
    assertThat(MessageUtils.orderIsLast(maxOrder, notRepeatedServiceMethods)).isTrue();
    assertThat(MessageUtils.orderIsLast(11, notRepeatedServiceMethods)).isTrue();
    assertThat(MessageUtils.orderIsLast(5, notRepeatedServiceMethods)).isFalse();
  }

  @Test
  void repeatOrder() {
    assertThat(MessageUtils.repeatOrder(notRepeatedServiceMethods)).isNull();
    assertThat(MessageUtils.repeatOrder(repeatedServiceMethods)).isZero();
  }
}
