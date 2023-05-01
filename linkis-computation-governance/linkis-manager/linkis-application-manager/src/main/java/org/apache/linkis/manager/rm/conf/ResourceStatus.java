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

package org.apache.linkis.manager.rm.conf;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.manager.common.entity.resource.Resource;

public enum ResourceStatus {

  /**
   * normal: left resource/max resource < normalThreshold warn: normalThreshold <= left resource/max
   * resource < warnThreshold critical: left resource/max resource >= warnThreshold
   */
  normal,
  warn,
  critical;

  public static Float normalThreshold =
      CommonVars.apply("wds.linkis.resourcemanager.resource.threashold.normal", 0.5f).getValue();

  public static Float warnThreshold =
      CommonVars.apply("wds.linkis.resourcemanager.resource.threashold.warn", 0.2f).getValue();

  public static ResourceStatus measure(Resource leftResource, Resource maxResource) {
    if (leftResource != null && maxResource != null) {
      if (leftResource.less(maxResource.multiplied(warnThreshold))) {
        return critical;
      } else if (leftResource.less(maxResource.multiplied(normalThreshold))) {
        return warn;
      } else {
        return normal;
      }
    }
    return normal;
  }
}
