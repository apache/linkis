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

package org.apache.linkis.metadata.query.common.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/** The meta information of partition */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaPartitionInfo implements Serializable {
  private List<String> partKeys = new ArrayList<>();

  private String name;
  /** Partition tree */
  private PartitionNode root;

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PartitionNode {
    /** Node name */
    private String name;
    /** Key: partition value Value: child partition node */
    private Map<String, PartitionNode> partitions = new HashMap<>();

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Map<String, PartitionNode> getPartitions() {
      return partitions;
    }

    public void setPartitions(Map<String, PartitionNode> partitions) {
      this.partitions = partitions;
    }
  }

  public List<String> getPartKeys() {
    return partKeys;
  }

  public void setPartKeys(List<String> partKeys) {
    this.partKeys = partKeys;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PartitionNode getRoot() {
    return root;
  }

  public void setRoot(PartitionNode root) {
    this.root = root;
  }
}
