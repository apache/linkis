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
public class MemoryResource extends Resource {

  private final long memory;

  public MemoryResource() {
    this(Long.MAX_VALUE);
  }

  public MemoryResource(long memory) {
    this.memory = memory;
  }

  private MemoryResource toMemoryResource(Resource r) {
    if (r instanceof MemoryResource) {
      return (MemoryResource) r;
    } else {
      return new MemoryResource(Long.MAX_VALUE);
    }
  }

  @Override
  public Resource add(Resource resource) {
    MemoryResource r = toMemoryResource(resource);
    return new MemoryResource(memory + r.getMemory());
  }

  @Override
  public Resource minus(Resource resource) {
    MemoryResource r = toMemoryResource(resource);
    return new MemoryResource(memory - r.getMemory());
  }

  @Override
  public Resource multiplied(Resource resource) {
    MemoryResource r = toMemoryResource(resource);
    return new MemoryResource(memory * r.getMemory());
  }

  @Override
  public Resource multiplied(float rate) {
    return new MemoryResource((long) (memory * rate));
  }

  @Override
  public Resource divide(Resource resource) {
    MemoryResource r = toMemoryResource(resource);
    return new MemoryResource(memory / r.getMemory());
  }

  @Override
  public Resource divide(int rate) {
    return new MemoryResource(memory / rate);
  }

  @Override
  public boolean moreThan(Resource resource) {
    MemoryResource r = toMemoryResource(resource);
    return memory > r.getMemory();
  }

  @Override
  public boolean notLess(Resource resource) {
    MemoryResource r = toMemoryResource(resource);
    return memory >= r.getMemory();
  }

  @Override
  public boolean less(Resource r) {
    return !notLess(r);
  }

  @Override
  public int compare(Resource resource) {
    MemoryResource r = toMemoryResource(resource);
    return Long.compare(this.getMemory(), r.getMemory());
  }

  @Override
  public boolean caseMore(Resource r) {
    return moreThan(r);
  }

  @Override
  public boolean equalsTo(Resource resource) {
    MemoryResource r = toMemoryResource(resource);
    return memory == r.getMemory();
  }

  @Override
  public String toJson() {
    return " \"memory\":\"" + ByteTimeUtils.bytesToString(memory) + " ";
  }

  @Override
  public String toString() {
    return toJson();
  }

  public long getMemory() {
    return memory;
  }
}
