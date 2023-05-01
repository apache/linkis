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

package org.apache.linkis.manager.common.entity.resource;

import org.apache.linkis.manager.common.exception.ResourceWarnException;

import java.text.MessageFormat;
import java.util.HashMap;

import static org.apache.linkis.manager.common.errorcode.ManagerCommonErrorCodeSummary.NOT_RESOURCE_POLICY;
import static org.apache.linkis.manager.common.errorcode.ManagerCommonErrorCodeSummary.NOT_RESOURCE_TYPE;

public abstract class Resource {
  public static Resource initResource(ResourceType resourceType) {
    switch (resourceType) {
      case CPU:
        return new CPUResource(0);
      case Memory:
        return new MemoryResource(0);
      case Load:
        return new LoadResource(0, 0);
      case Instance:
        return new InstanceResource(0);
      case LoadInstance:
        return new LoadInstanceResource(0, 0, 0);
      case Yarn:
        return new YarnResource(0, 0, 0, "default", "");
      case DriverAndYarn:
        return new DriverAndYarnResource(
            new LoadInstanceResource(0, 0, 0), new YarnResource(0, 0, 0, "default", ""));
      case Special:
        return new SpecialResource(new HashMap<String, Object>());
      case Default:
        return new LoadResource(0, 0);
      default:
        throw new ResourceWarnException(
            NOT_RESOURCE_POLICY.getErrorCode(), NOT_RESOURCE_POLICY.getErrorDesc());
    }
  }

  public static Resource getZeroResource(Resource resource) {
    if (resource instanceof MemoryResource) {
      return new MemoryResource(0);
    } else if (resource instanceof InstanceResource) {
      return new InstanceResource(0);
    } else if (resource instanceof CPUResource) {
      return new CPUResource(0);
    } else if (resource instanceof LoadResource) {
      return new LoadResource(0, 0);
    } else if (resource instanceof LoadInstanceResource) {
      return new LoadInstanceResource(0, 0, 0);
    } else if (resource instanceof YarnResource) {
      return new YarnResource(0, 0, 0, "default", "");
    } else if (resource instanceof DriverAndYarnResource) {
      DriverAndYarnResource driverAndYarnResource = (DriverAndYarnResource) resource;
      if (driverAndYarnResource.getYarnResource() != null
          && driverAndYarnResource.getYarnResource().getQueueName() != null) {
        return new DriverAndYarnResource(
            new LoadInstanceResource(0, 0, 0),
            new YarnResource(0, 0, 0, driverAndYarnResource.getYarnResource().getQueueName(), ""));
      } else {
        return new DriverAndYarnResource(
            new LoadInstanceResource(0, 0, 0), new YarnResource(0, 0, 0, "default", ""));
      }
    } else if (resource instanceof SpecialResource) {
      return new SpecialResource(new HashMap<String, Object>());
    } else {
      throw new ResourceWarnException(
          NOT_RESOURCE_TYPE.getErrorCode(),
          MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc(), resource.getClass()));
    }
  }

  public abstract Resource add(Resource r);

  public abstract Resource minus(Resource r);

  public abstract Resource multiplied(Resource r);

  public abstract Resource multiplied(float rate);

  public abstract Resource divide(Resource r);

  public abstract Resource divide(int rate);

  public abstract boolean moreThan(Resource r);

  /**
   * Part is greater than(部分大于)
   *
   * @param r
   * @return
   */
  public abstract boolean caseMore(Resource r);

  public abstract boolean equalsTo(Resource r);

  public abstract boolean notLess(Resource r);

  public abstract boolean less(Resource r);

  public Resource add(Resource r, float rate) {
    return this.add(r.multiplied(rate));
  }

  public Resource minus(Resource r, float rate) {
    return this.minus(r.multiplied(rate));
  }

  public Resource divide(Resource r, int rate) {
    return this.divide(rate).divide(r);
  }

  public Resource multiplied(double rate) {
    return this.multiplied((float) rate);
  }

  public abstract String toJson();
}
