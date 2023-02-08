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

package org.apache.linkis.engineconn.computation.concurrent.monitor;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.utils.HardwareUtils;

import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class HardwareMonitorService implements MonitorService {

  private static Logger LOG = LoggerFactory.getLogger(HardwareMonitorService.class);

  private static CommonVars<Double> MEMORY_MAX_USAGE =
      CommonVars.apply("linkis.engineconn.concurrent.max.memory.usage", 0.95);

  private static CommonVars<Double> CPU_MAX_USAGE =
      CommonVars.apply("linkis.engineconn.concurrent.max.cpu.usage", 0.99);

  private Double lastLoadAverageUsage = 0D;

  private Double lastMemoryUsage = 0D;

  @Override
  public boolean isAvailable() {

    double memoryUsage = HardwareUtils.memoryUsage();

    double loadAverageUsage = HardwareUtils.memoryUsage();

    Double maxMemoryUsage = MEMORY_MAX_USAGE.getValue();
    Double maxCpuUsage = CPU_MAX_USAGE.getValue();

    boolean isUnavailable =
        (memoryUsage > maxMemoryUsage && lastMemoryUsage > maxMemoryUsage)
            || (loadAverageUsage > maxCpuUsage && lastLoadAverageUsage > maxCpuUsage);
    if (isUnavailable) {
      LOG.warn(
          "current load average {} is too high or memory usage {} is too high, over max cpu load avg={} and memory usage {}",
          loadAverageUsage,
          memoryUsage,
          maxCpuUsage,
          maxMemoryUsage);
    }

    lastLoadAverageUsage = loadAverageUsage;

    lastMemoryUsage = memoryUsage;

    return !isUnavailable;
  }
}
