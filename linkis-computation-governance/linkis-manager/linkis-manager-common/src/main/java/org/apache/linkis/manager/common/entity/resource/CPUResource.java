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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CPUResource extends Resource {
  private int cores;

  public CPUResource() {
    this(Integer.MAX_VALUE);
  }

  public CPUResource(int cores) {
    this.cores = cores;
  }

  public int getCores() {
    return cores;
  }

  public void setCores(int cores) {
    this.cores = cores;
  }

  private CPUResource toCPUResource(Resource r) {
    CPUResource cpuResource = new CPUResource(Integer.MAX_VALUE);
    if (r instanceof CPUResource) {
      cpuResource = (CPUResource) r;
    }
    return cpuResource;
  }

  protected Resource toResource(int cores) {
    return new CPUResource(cores);
  }

  @Override
  public Resource add(Resource r) {
    CPUResource cpuResource = toCPUResource(r);
    return new CPUResource(this.getCores() + cpuResource.getCores());
  }

  @Override
  public Resource minus(Resource r) {
    CPUResource cpuResource = toCPUResource(r);
    return new CPUResource(this.getCores() - cpuResource.getCores());
  }

  @Override
  public Resource multiplied(Resource r) {
    CPUResource cpuResource = toCPUResource(r);
    return new CPUResource(this.getCores() * cpuResource.getCores());
  }

  @Override
  public Resource multiplied(float rate) {
    float cores = this.getCores() * rate;
    return new CPUResource((int) cores);
  }

  @Override
  public Resource divide(Resource r) {
    CPUResource cpuResource = toCPUResource(r);
    return new CPUResource(this.getCores() / cpuResource.getCores());
  }

  @Override
  public Resource divide(int rate) {
    return new CPUResource(this.getCores() / rate);
  }

  @Override
  public boolean moreThan(Resource r) {
    CPUResource cpuResource = toCPUResource(r);
    return this.getCores() > cpuResource.getCores();
  }

  /**
   * Part is greater than(部分大于)
   *
   * @param r
   * @return
   */
  @Override
  public boolean caseMore(Resource r) {
    return moreThan(r);
  }

  @Override
  public boolean equalsTo(Resource r) {
    CPUResource cpuResource = toCPUResource(r);
    return cores == cpuResource.getCores();
  }

  @Override
  public boolean notLess(Resource r) {
    CPUResource cpuResource = toCPUResource(r);
    return this.getCores() >= cpuResource.getCores();
  }

  @Override
  public boolean less(Resource r) {
    return !notLess(r);
  }

  @Override
  public String toJson() {
    return String.format(" \"cpu\":%s ", cores);
  }

  @Override
  public String toString() {
    return toJson();
  }
}
