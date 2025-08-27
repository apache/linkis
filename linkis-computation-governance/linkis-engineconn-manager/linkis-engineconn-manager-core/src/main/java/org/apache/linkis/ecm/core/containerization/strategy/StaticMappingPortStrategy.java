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

package org.apache.linkis.ecm.core.containerization.strategy;

import org.apache.linkis.ecm.core.conf.ContainerizationConf;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class StaticMappingPortStrategy implements MappingPortStrategy {

  private static final AtomicInteger currentIndex = new AtomicInteger(0);

  @Override
  public int availablePort() throws IOException {
    return getNewPort(10);
  }

  public int getNewPort(int retryNum) throws IOException {
    int[] portRange = getPortRange();
    if (retryNum == 0) {
      throw new IOException(
          "No available port in the portRange: "
              + ContainerizationConf.ENGINE_CONN_CONTAINERIZATION_MAPPING_STATTIC_PORT_RANGE()
                  .getValue());
    }
    moveIndex();
    int minPort = portRange[0];
    int newPort = minPort + currentIndex.get() - 1;
    ServerSocket socket = null;
    try {
      socket = new ServerSocket(newPort);
    } catch (Exception e) {
      return getNewPort(--retryNum);
    } finally {
      IOUtils.close(socket);
    }
    return newPort;
  }

  private synchronized void moveIndex() {
    int poolSize = getPoolSize();
    currentIndex.set(currentIndex.get() % poolSize + 1);
  }

  private int[] getPortRange() {
    String portRange =
        ContainerizationConf.ENGINE_CONN_CONTAINERIZATION_MAPPING_STATTIC_PORT_RANGE().getValue();

    return Arrays.stream(portRange.split("-")).mapToInt(Integer::parseInt).toArray();
  }

  private int getPoolSize() {
    int[] portRange = getPortRange();
    int minPort = portRange[0];
    int maxPort = portRange[1];

    return maxPort - minPort + 1;
  }
}
