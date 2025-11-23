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

package org.apache.linkis.variable.entity;

import java.util.ArrayList;
import java.util.List;

public class VarTree {
  private Long id;
  private Long parentID;
  private String name;
  private String description;
  private Long appID;
  private List<VarTree> childrens = new ArrayList<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getParentID() {
    return parentID;
  }

  public void setParentID(Long parentID) {
    this.parentID = parentID;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getAppID() {
    return appID;
  }

  public void setAppID(Long appID) {
    this.appID = appID;
  }

  public List<VarTree> getChildrens() {
    return childrens;
  }

  public void setChildrens(List<VarTree> childrens) {
    this.childrens = childrens;
  }
}
