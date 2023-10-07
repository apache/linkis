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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.manager.common.errorcode.ManagerCommonErrorCodeSummary.OPERATION_MULTIPLIED;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DriverAndKubernetesResource extends Resource {

  private static final Logger logger = LoggerFactory.getLogger(DriverAndKubernetesResource.class);

  private final LoadInstanceResource loadInstanceResource;
  private final KubernetesResource kubernetesResource;

  public DriverAndKubernetesResource(
      LoadInstanceResource loadInstanceResource, KubernetesResource kubernetesResource) {
    this.loadInstanceResource = loadInstanceResource;
    this.kubernetesResource = kubernetesResource;
  }

  public DriverAndKubernetesResource() {
    this(
        new LoadInstanceResource(Long.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE),
        new KubernetesResource(Long.MAX_VALUE, Long.MAX_VALUE));
  }

  public LoadInstanceResource getLoadInstanceResource() {
    return this.loadInstanceResource;
  }

  private DriverAndKubernetesResource(Resource r) {
    if (r instanceof DriverAndKubernetesResource) {
      DriverAndKubernetesResource t = (DriverAndKubernetesResource) r;
      this.loadInstanceResource = t.loadInstanceResource;
      this.kubernetesResource = t.kubernetesResource;
    } else if (r instanceof KubernetesResource) {
      this.loadInstanceResource = new LoadInstanceResource(0, 0, 0);
      this.kubernetesResource = (KubernetesResource) r;
    } else if (r instanceof LoadInstanceResource) {
      this.loadInstanceResource = (LoadInstanceResource) r;
      this.kubernetesResource = new KubernetesResource(0, 0);
    } else if (r instanceof LoadResource) {
      LoadResource l = (LoadResource) r;
      this.loadInstanceResource = new LoadInstanceResource(l.getMemory(), l.getCores(), 0);
      this.kubernetesResource = new KubernetesResource(0, 0);
    } else if (r instanceof MemoryResource) {
      MemoryResource m = (MemoryResource) r;
      this.loadInstanceResource = new LoadInstanceResource(m.getMemory(), 0, 0);
      this.kubernetesResource = new KubernetesResource(0, 0);
    } else if (r instanceof CPUResource) {
      CPUResource c = (CPUResource) r;
      this.loadInstanceResource = new LoadInstanceResource(0, c.getCores(), 0);
      this.kubernetesResource = new KubernetesResource(0, 0);
    } else {
      this.loadInstanceResource =
          new LoadInstanceResource(Long.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
      this.kubernetesResource = new KubernetesResource(Long.MAX_VALUE, Long.MAX_VALUE);
    }
  }

  public KubernetesResource getKubernetesResource() {
    return kubernetesResource;
  }

  public boolean isModuleOperate(Resource r) {
    return false; // TODO This method needs to return false by default, and this method needs to be
    // removed later
  }

  public boolean isModuleOperate() {
    return kubernetesResource != null;
  }

  @Override
  public DriverAndKubernetesResource add(Resource resource) {
    DriverAndKubernetesResource r = new DriverAndKubernetesResource(resource);
    if (isModuleOperate(r)) {
      return new DriverAndKubernetesResource(
          loadInstanceResource.add(r.getLoadInstanceResource()), kubernetesResource);
    } else {
      return new DriverAndKubernetesResource(
          loadInstanceResource.add(r.getLoadInstanceResource()),
          kubernetesResource.add(r.getKubernetesResource()));
    }
  }

  @Override
  public DriverAndKubernetesResource minus(Resource resource) {
    DriverAndKubernetesResource r = new DriverAndKubernetesResource(resource);
    if (isModuleOperate(r)) {
      return new DriverAndKubernetesResource(
          loadInstanceResource.minus(r.getLoadInstanceResource()), kubernetesResource);
    } else {
      return new DriverAndKubernetesResource(
          loadInstanceResource.minus(r.getLoadInstanceResource()),
          kubernetesResource.minus(r.getKubernetesResource()));
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
      return new DriverAndKubernetesResource(
          loadInstanceResource.multiplied(rate), kubernetesResource);
    } else {
      return new DriverAndKubernetesResource(
          loadInstanceResource.multiplied(rate), kubernetesResource.multiplied(rate));
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
      return new DriverAndKubernetesResource(loadInstanceResource.divide(rate), kubernetesResource);
    } else {
      return new DriverAndKubernetesResource(
          loadInstanceResource.divide(rate), kubernetesResource.divide(rate));
    }
  }

  @Override
  public boolean moreThan(Resource resource) {
    DriverAndKubernetesResource r = new DriverAndKubernetesResource(resource);
    if (isModuleOperate(r)) {
      return loadInstanceResource.moreThan(r.loadInstanceResource);
    } else {
      return loadInstanceResource.moreThan(r.loadInstanceResource)
          && kubernetesResource.moreThan(r.kubernetesResource);
    }
  }

  @Override
  public boolean caseMore(Resource resource) {
    DriverAndKubernetesResource r = new DriverAndKubernetesResource(resource);
    if (isModuleOperate(r)) {
      return loadInstanceResource.caseMore(r.loadInstanceResource);
    } else {
      return loadInstanceResource.caseMore(r.loadInstanceResource)
          || kubernetesResource.caseMore(r.kubernetesResource);
    }
  }

  @Override
  public boolean equalsTo(Resource resource) {
    DriverAndKubernetesResource r = new DriverAndKubernetesResource(resource);
    if (isModuleOperate(r)) {
      return loadInstanceResource.equalsTo(r.loadInstanceResource);
    } else {
      return loadInstanceResource.equalsTo(r.loadInstanceResource)
          && kubernetesResource.equalsTo(r.kubernetesResource);
    }
  }

  @Override
  public boolean notLess(Resource resource) {
    DriverAndKubernetesResource r = new DriverAndKubernetesResource(resource);
    if (isModuleOperate(r)) {
      return loadInstanceResource.notLess(r.loadInstanceResource);
    } else {
      return loadInstanceResource.notLess(r.loadInstanceResource)
          && kubernetesResource.notLess(r.kubernetesResource);
    }
  }

  @Override
  public boolean less(Resource r) {
    return !notLess(r);
  }

  @Override
  public int compare(Resource resource) {
    DriverAndKubernetesResource r = new DriverAndKubernetesResource(resource);
    if (isModuleOperate(r)) {
      return loadInstanceResource.compare(r.loadInstanceResource);
    } else {
      if (loadInstanceResource.getMemory() > r.loadInstanceResource.getMemory()) {
        return 1;
      } else if (loadInstanceResource.getMemory() < r.loadInstanceResource.getMemory()) {
        return -1;
      } else {
        // If memory is equal, compare cores
        if (loadInstanceResource.getCores() > r.loadInstanceResource.getCores()) {
          return 1;
        } else if (loadInstanceResource.getCores() < r.loadInstanceResource.getCores()) {
          return -1;
        } else {
          // If cores are equal, compare instances
          if (loadInstanceResource.getInstances() > r.loadInstanceResource.getInstances()) {
            return 1;
          } else if (loadInstanceResource.getInstances() < r.loadInstanceResource.getInstances()) {
            return -1;
          } else {
            if (kubernetesResource.getMemory() > r.kubernetesResource.getMemory()) {
              return 1;
            } else if (kubernetesResource.getMemory() < r.kubernetesResource.getMemory()) {
              return -1;
            } else {
              return Long.compare(kubernetesResource.getCores(), r.kubernetesResource.getCores());
            }
          }
        }
      }
    }
  }

  public String toJson() {
    String load = "null";
    String kubernetes = "null";
    if (loadInstanceResource != null) {
      load = loadInstanceResource.toJson();
    }
    if (kubernetesResource != null) {
      kubernetes = kubernetesResource.toJson();
    }
    return String.format("{\"driver\":%s, \"kubernetes\":%s}", load, kubernetes);
  }

  public String toString() {
    return String.format(
        "Driver resources(Driver资源)：%s, Kubernetes resource(K8S资源):%s",
        loadInstanceResource, kubernetesResource);
  }
}
