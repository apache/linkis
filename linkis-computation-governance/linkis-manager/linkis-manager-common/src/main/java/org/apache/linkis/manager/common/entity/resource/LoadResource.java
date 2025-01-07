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
public class LoadResource extends Resource {
  private final long memory;
  private final int cores;

  private LoadResource(Resource r) {
    if (r instanceof LoadResource) {
      LoadResource t = (LoadResource) r;
      this.memory = t.memory;
      this.cores = t.cores;
    } else if (r instanceof MemoryResource) {
      MemoryResource m = (MemoryResource) r;
      this.memory = m.getMemory();
      this.cores = 0;
    } else if (r instanceof CPUResource) {
      CPUResource c = (CPUResource) r;
      this.memory = 0;
      this.cores = c.getCores();
    } else {
      this.memory = Long.MAX_VALUE;
      this.cores = Integer.MAX_VALUE;
    }
  }

  public LoadResource() {
    this(Long.MAX_VALUE, Integer.MAX_VALUE);
  }

  public LoadResource(long memory, int cores) {
    this.memory = memory;
    this.cores = cores;
  }

  public LoadResource add(Resource r) {
    LoadResource temp = new LoadResource(r);
    return new LoadResource(this.memory + temp.memory, this.cores + temp.cores);
  }

  public LoadResource minus(Resource r) {
    LoadResource temp = new LoadResource(r);
    return new LoadResource(this.memory - temp.memory, this.cores - temp.cores);
  }

  public LoadResource multiplied(Resource r) {
    LoadResource temp = new LoadResource(r);
    return new LoadResource(this.memory * temp.memory, this.cores * temp.cores);
  }

  public LoadResource multiplied(float rate) {
    return new LoadResource((long) (this.memory * rate), Math.round(this.cores * rate));
  }

  public LoadResource divide(Resource r) {
    LoadResource temp = new LoadResource(r);
    return new LoadResource(this.memory / temp.memory, this.cores / temp.cores);
  }

  public LoadResource divide(int rate) {
    return new LoadResource(this.memory / rate, this.cores / rate);
  }

  public boolean moreThan(Resource r) {
    LoadResource temp = new LoadResource(r);
    return this.memory > temp.memory && this.cores > temp.cores;
  }

  public boolean caseMore(Resource r) {
    LoadResource temp = new LoadResource(r);
    return this.memory > temp.memory || this.cores > temp.cores;
  }

  public boolean equalsTo(Resource r) {
    LoadResource temp = new LoadResource(r);
    return this.memory == temp.memory && this.cores == temp.cores;
  }

  @Override
  public boolean notLess(Resource r) {
    LoadResource temp = new LoadResource(r);
    return this.memory >= temp.memory && this.cores >= temp.cores;
  }

  @Override
  public boolean less(Resource r) {
    return !notLess(r);
  }

  @Override
  public int compare(Resource r) {
    LoadResource temp = new LoadResource(r);

    if (this.getMemory() > temp.getMemory()) {
      return 1;
    } else if (this.getMemory() < temp.getMemory()) {
      return -1;
    } else {
      // If memory is equal, compare cores
      return Integer.compare(this.getCores(), temp.getCores());
    }
  }

  @Override
  public String toJson() {
    return String.format(
        "{\"memory\":\"%s\",\"cpu\":%d}", ByteTimeUtils.bytesToString(this.memory), this.cores);
  }

  @Override
  public String toString() {
    return this.toJson();
  }

  public long getMemory() {
    return memory;
  }

  public int getCores() {
    return cores;
  }
}
