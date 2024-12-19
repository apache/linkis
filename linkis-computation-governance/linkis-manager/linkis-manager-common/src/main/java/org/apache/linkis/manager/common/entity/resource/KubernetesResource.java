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
public class KubernetesResource extends Resource {
  private final long memory;
  private final long cores;
  private final String namespace;

  private KubernetesResource(Resource r) {
    if (r instanceof KubernetesResource) {
      KubernetesResource t = (KubernetesResource) r;
      this.memory = t.memory;
      this.cores = t.cores;
      this.namespace = t.namespace;
    } else if (r instanceof MemoryResource) {
      MemoryResource m = (MemoryResource) r;
      this.memory = m.getMemory();
      this.cores = 0;
      this.namespace = "default";
    } else if (r instanceof CPUResource) {
      CPUResource c = (CPUResource) r;
      this.memory = 0;
      this.cores = c.getCores();
      this.namespace = "default";
    } else {
      this.memory = Long.MAX_VALUE;
      this.cores = Long.MAX_VALUE;
      this.namespace = "default";
    }
  }

  public KubernetesResource() {
    this(Long.MAX_VALUE, Long.MAX_VALUE, "default");
  }

  public KubernetesResource(long memory, long cores, String namespace) {
    this.memory = memory;
    this.cores = cores;
    this.namespace = namespace;
  }

  public KubernetesResource(long memory, long cores) {
    this.memory = memory;
    this.cores = cores;
    this.namespace = "default";
  }

  public KubernetesResource add(Resource r) {
    KubernetesResource temp = new KubernetesResource(r);
    return new KubernetesResource(
        this.memory + temp.memory, this.cores + temp.cores, this.namespace);
  }

  public KubernetesResource minus(Resource r) {
    KubernetesResource temp = new KubernetesResource(r);
    return new KubernetesResource(
        this.memory - temp.memory, this.cores - temp.cores, this.namespace);
  }

  public KubernetesResource multiplied(Resource r) {
    KubernetesResource temp = new KubernetesResource(r);
    return new KubernetesResource(
        this.memory * temp.memory, this.cores * temp.cores, this.namespace);
  }

  public KubernetesResource multiplied(float rate) {
    return new KubernetesResource(
        (long) (this.memory * rate), Math.round(this.cores * rate), this.namespace);
  }

  public KubernetesResource divide(Resource r) {
    KubernetesResource temp = new KubernetesResource(r);
    return new KubernetesResource(
        this.memory / temp.memory, this.cores / temp.cores, this.namespace);
  }

  public KubernetesResource divide(int rate) {
    return new KubernetesResource(this.memory / rate, this.cores / rate, this.namespace);
  }

  public boolean moreThan(Resource r) {
    KubernetesResource temp = new KubernetesResource(r);
    return this.memory > temp.memory && this.cores > temp.cores;
  }

  public boolean caseMore(Resource r) {
    KubernetesResource temp = new KubernetesResource(r);
    return this.memory > temp.memory || this.cores > temp.cores;
  }

  public boolean equalsTo(Resource r) {
    KubernetesResource temp = new KubernetesResource(r);
    return this.memory == temp.memory && this.cores == temp.cores;
  }

  @Override
  public boolean notLess(Resource r) {
    KubernetesResource temp = new KubernetesResource(r);
    return this.memory >= temp.memory
        && this.cores >= temp.cores
        && this.namespace.equals(temp.namespace);
  }

  @Override
  public boolean less(Resource r) {
    return !notLess(r);
  }

  @Override
  public int compare(Resource r) {
    KubernetesResource temp = new KubernetesResource(r);
    if (this.getMemory() > temp.getMemory()) {
      return 1;
    } else if (this.getMemory() < temp.getMemory()) {
      return -1;
    } else {
      // If memory is equal, compare cores
      return Long.compare(this.getCores(), temp.getCores());
    }
  }

  @Override
  public String toJson() {
    return String.format(
        "{\"namespace\":\"%s\",\"memory\":\"%s\",\"cpu\":%d}",
        namespace, ByteTimeUtils.bytesToString(this.memory), this.cores);
  }

  @Override
  public String toString() {
    return this.toJson();
  }

  public long getMemory() {
    return memory;
  }

  public long getCores() {
    return cores;
  }

  public String getNamespace() {
    return namespace;
  }
}
