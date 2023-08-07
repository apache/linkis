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

package org.apache.linkis.cs.common.entity.metadata;

import org.apache.linkis.cs.common.annotation.KeywordMethod;

import java.util.List;

public class CSPartition implements Partition {

  private String name;
  private String alias;
  private String type;
  private String comment;
  private String express;
  private String rule;
  private Boolean isPrimary;
  private Integer length;
  private List<String> value;

  @Override
  public Integer getLength() {
    return length;
  }

  @Override
  public void setLength(Integer length) {
    this.length = length;
  }

  @Override
  @KeywordMethod
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getAlias() {
    return alias;
  }

  @Override
  public void setAlias(String alias) {
    this.alias = alias;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String getComment() {
    return comment;
  }

  @Override
  public void setComment(String comment) {
    this.comment = comment;
  }

  @Override
  public String getExpress() {
    return express;
  }

  @Override
  public void setExpress(String express) {
    this.express = express;
  }

  @Override
  public String getRule() {
    return rule;
  }

  @Override
  public void setRule(String rule) {
    this.rule = rule;
  }

  @Override
  public Boolean getPrimary() {
    return isPrimary;
  }

  @Override
  public void setPrimary(Boolean primary) {
    isPrimary = primary;
  }

  public List<String> getValue() {
    return value;
  }

  public void setValue(List<String> value) {
    this.value = value;
  }
}
