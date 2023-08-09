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

import org.apache.linkis.common.utils.ByteTimeUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoadInstanceResource extends Resource {
  private final long memory;
  private final int cores;
  private final int instances;

  private LoadInstanceResource(Resource r) {
    if (r instanceof LoadInstanceResource) {
      LoadInstanceResource t = (LoadInstanceResource) r;
      this.memory = t.memory;
      this.cores = t.cores;
      this.instances = t.instances;
    } else if (r instanceof LoadResource) {
      LoadResource l = (LoadResource) r;
      this.memory = l.getMemory();
      this.cores = l.getCores();
      this.instances = 0;
    } else if (r instanceof MemoryResource) {
      MemoryResource m = (MemoryResource) r;
      this.memory = m.getMemory();
      this.cores = 0;
      this.instances = 0;
    } else if (r instanceof CPUResource) {
      CPUResource c = (CPUResource) r;
      this.memory = 0;
      this.cores = c.getCores();
      this.instances = 0;
    } else if (r instanceof DriverAndYarnResource) {
      DriverAndYarnResource d = (DriverAndYarnResource) r;
      this.memory = d.getLoadInstanceResource().getMemory();
      this.cores = d.getLoadInstanceResource().getCores();
      this.instances = d.getLoadInstanceResource().getInstances();
    } else if (r instanceof DriverAndKubernetesResource) {
      DriverAndKubernetesResource d = (DriverAndKubernetesResource) r;
      this.memory = d.getLoadInstanceResource().getMemory();
      this.cores = d.getLoadInstanceResource().getCores();
      this.instances = d.getLoadInstanceResource().getInstances();
    } else {
      this.memory = Long.MAX_VALUE;
      this.cores = Integer.MAX_VALUE;
      this.instances = Integer.MAX_VALUE;
    }
  }

  public LoadInstanceResource() {
    this(Long.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  public LoadInstanceResource(long memory, int cores, int instances) {
    this.memory = memory;
    this.cores = cores;
    this.instances = instances;
  }

  public LoadInstanceResource add(Resource r) {
    LoadInstanceResource temp = new LoadInstanceResource(r);
    return new LoadInstanceResource(
        this.memory + temp.memory, this.cores + temp.cores, this.instances + temp.instances);
  }

  public LoadInstanceResource minus(Resource r) {
    LoadInstanceResource temp = new LoadInstanceResource(r);
    return new LoadInstanceResource(
        this.memory - temp.memory, this.cores - temp.cores, this.instances - temp.instances);
  }

  public LoadInstanceResource multiplied(Resource r) {
    LoadInstanceResource temp = new LoadInstanceResource(r);
    return new LoadInstanceResource(
        this.memory * temp.memory, this.cores * temp.cores, this.instances * temp.instances);
  }

  public LoadInstanceResource multiplied(float rate) {
    return new LoadInstanceResource(
        (long) (this.memory * rate), Math.round(this.cores * rate), (int) (this.instances * rate));
  }

  public LoadInstanceResource divide(Resource r) {
    LoadInstanceResource temp = new LoadInstanceResource(r);
    return new LoadInstanceResource(
        this.memory / temp.memory, this.cores / temp.cores, this.instances / temp.instances);
  }

  public LoadInstanceResource divide(int rate) {
    return new LoadInstanceResource(this.memory / rate, this.cores / rate, this.instances / rate);
  }

  public boolean moreThan(Resource r) {
    LoadInstanceResource temp = new LoadInstanceResource(r);
    return this.memory > temp.memory && this.cores > temp.cores && this.instances > temp.instances;
  }

  public boolean caseMore(Resource r) {
    LoadInstanceResource temp = new LoadInstanceResource(r);
    return this.memory > temp.memory || this.cores > temp.cores || this.instances > temp.instances;
  }

  public boolean equalsTo(Resource r) {
    LoadInstanceResource temp = new LoadInstanceResource(r);
    return this.memory == temp.memory
        && this.cores == temp.cores
        && this.instances == temp.instances;
  }

  @Override
  public boolean notLess(Resource r) {
    LoadInstanceResource temp = new LoadInstanceResource(r);
    return this.memory >= temp.memory
        && this.cores >= temp.cores
        && this.instances >= temp.instances;
  }

  @Override
  public boolean less(Resource r) {
    return !notLess(r);
  }

  @Override
  public String toJson() {
    return String.format(
        "{\"instance\":%d,\"memory\":\"%s\",\"cpu\":%d}",
        this.instances, ByteTimeUtils.bytesToString(this.memory), this.cores);
  }

  @Override
  public String toString() {
    return String.format(
        "Number of instances(实例数)：%d，(RAM)内存：%s ,cpu: %s",
        this.getInstances(), this.getCores(), this.getMemory());
  }

  public long getMemory() {
    return memory;
  }

  public int getCores() {
    return cores;
  }

  public int getInstances() {
    return instances;
  }
}
