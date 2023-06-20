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

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.manager.common.errorcode.ManagerCommonErrorCodeSummary.OPERATION_MULTIPLIED;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DriverAndYarnResource extends Resource {

  private static final Logger logger = LoggerFactory.getLogger(DriverAndYarnResource.class);

  private final LoadInstanceResource loadInstanceResource;
  private final YarnResource yarnResource;

  public DriverAndYarnResource(
      LoadInstanceResource loadInstanceResource, YarnResource yarnResource) {
    this.loadInstanceResource = loadInstanceResource;
    this.yarnResource = yarnResource;
  }

  public DriverAndYarnResource() {
    this(
        new LoadInstanceResource(Long.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE),
        new YarnResource(Long.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, "default", ""));
  }

  public LoadInstanceResource getLoadInstanceResource() {
    return this.loadInstanceResource;
  }

  private DriverAndYarnResource(Resource r) {
    if (r instanceof DriverAndYarnResource) {
      DriverAndYarnResource t = (DriverAndYarnResource) r;
      this.loadInstanceResource = t.loadInstanceResource;
      this.yarnResource = t.yarnResource;
    } else if (r instanceof YarnResource) {
      this.loadInstanceResource = new LoadInstanceResource(0, 0, 0);
      this.yarnResource = (YarnResource) r;
    } else if (r instanceof LoadInstanceResource) {
      this.loadInstanceResource = (LoadInstanceResource) r;
      this.yarnResource = new YarnResource(0, 0, 0, "default", "");
    } else if (r instanceof LoadResource) {
      LoadResource l = (LoadResource) r;
      this.loadInstanceResource = new LoadInstanceResource(l.getMemory(), l.getCores(), 0);
      this.yarnResource = new YarnResource(0, 0, 0, "default", "");
    } else if (r instanceof MemoryResource) {
      MemoryResource m = (MemoryResource) r;
      this.loadInstanceResource = new LoadInstanceResource(m.getMemory(), 0, 0);
      this.yarnResource = new YarnResource(0, 0, 0, "default", "");
    } else if (r instanceof CPUResource) {
      CPUResource c = (CPUResource) r;
      this.loadInstanceResource = new LoadInstanceResource(0, c.getCores(), 0);
      this.yarnResource = new YarnResource(0, 0, 0, "default", "");
    } else {
      this.loadInstanceResource =
          new LoadInstanceResource(Long.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
      this.yarnResource =
          new YarnResource(Long.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, "default", "");
    }
  }

  public YarnResource getYarnResource() {
    return yarnResource;
  }

  public boolean isModuleOperate(Resource r) {
    return false; // TODO This method needs to return false by default, and this method needs to be
    // removed later
  }

  public boolean isModuleOperate() {
    return yarnResource != null && !StringUtils.isEmpty(yarnResource.getQueueName());
  }

  @Override
  public DriverAndYarnResource add(Resource resource) {
    DriverAndYarnResource r = new DriverAndYarnResource(resource);
    if (isModuleOperate(r)) {
      return new DriverAndYarnResource(
          loadInstanceResource.add(r.getLoadInstanceResource()), yarnResource);
    } else {
      return new DriverAndYarnResource(
          loadInstanceResource.add(r.getLoadInstanceResource()),
          yarnResource.add(r.getYarnResource()));
    }
  }

  @Override
  public DriverAndYarnResource minus(Resource resource) {
    DriverAndYarnResource r = new DriverAndYarnResource(resource);
    if (isModuleOperate(r)) {
      return new DriverAndYarnResource(
          loadInstanceResource.minus(r.getLoadInstanceResource()), yarnResource);
    } else {
      return new DriverAndYarnResource(
          loadInstanceResource.minus(r.getLoadInstanceResource()),
          yarnResource.minus(r.getYarnResource()));
    }
  }

  @Override
  public Resource multiplied(Resource r) {
    throw new ResourceWarnException(
        OPERATION_MULTIPLIED.getErrorCode(), OPERATION_MULTIPLIED.getErrorDesc());
  }

  @Override
  public Resource multiplied(float rate) {
    if (isModuleOperate()) {
      return new DriverAndYarnResource(loadInstanceResource.multiplied(rate), yarnResource);
    } else {
      return new DriverAndYarnResource(
          loadInstanceResource.multiplied(rate), yarnResource.multiplied(rate));
    }
  }

  @Override
  public Resource divide(Resource r) {
    throw new ResourceWarnException(
        OPERATION_MULTIPLIED.getErrorCode(), OPERATION_MULTIPLIED.getErrorDesc());
  }

  @Override
  public Resource divide(int rate) {
    if (isModuleOperate()) {
      return new DriverAndYarnResource(loadInstanceResource.divide(rate), yarnResource);
    } else {
      return new DriverAndYarnResource(
          loadInstanceResource.divide(rate), yarnResource.divide(rate));
    }
  }

  @Override
  public boolean moreThan(Resource resource) {
    DriverAndYarnResource r = new DriverAndYarnResource(resource);
    if (isModuleOperate(r)) {
      return loadInstanceResource.moreThan(r.loadInstanceResource);
    } else {
      return loadInstanceResource.moreThan(r.loadInstanceResource)
          && yarnResource.moreThan(r.yarnResource);
    }
  }

  @Override
  public boolean caseMore(Resource resource) {
    DriverAndYarnResource r = new DriverAndYarnResource(resource);
    if (isModuleOperate(r)) {
      return loadInstanceResource.caseMore(r.loadInstanceResource);
    } else {
      return loadInstanceResource.caseMore(r.loadInstanceResource)
          || yarnResource.caseMore(r.yarnResource);
    }
  }

  @Override
  public boolean equalsTo(Resource resource) {
    DriverAndYarnResource r = new DriverAndYarnResource(resource);
    if (isModuleOperate(r)) {
      return loadInstanceResource.equalsTo(r.loadInstanceResource);
    } else {
      return loadInstanceResource.equalsTo(r.loadInstanceResource)
          && yarnResource.equalsTo(r.yarnResource);
    }
  }

  @Override
  public boolean notLess(Resource resource) {
    DriverAndYarnResource r = new DriverAndYarnResource(resource);
    if (isModuleOperate(r)) {
      return loadInstanceResource.notLess(r.loadInstanceResource);
    } else {
      return loadInstanceResource.notLess(r.loadInstanceResource)
          && yarnResource.notLess(r.yarnResource);
    }
  }

  @Override
  public boolean less(Resource r) {
    return !notLess(r);
  }

  public String toJson() {
    String load = "null";
    String yarn = "null";
    if (loadInstanceResource != null) {
      load = loadInstanceResource.toJson();
    }
    if (yarnResource != null) {
      yarn = yarnResource.toJson();
    }
    return String.format("{\"driver\":%s, \"yarn\":%s}", load, yarn);
  }

  public String toString() {
    return String.format(
        "Driver resources(Driver资源)：%s, Queue resource(队列资源):%s",
        loadInstanceResource, yarnResource);
  }
}
