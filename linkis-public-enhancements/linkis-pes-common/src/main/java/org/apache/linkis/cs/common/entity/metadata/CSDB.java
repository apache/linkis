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
import org.apache.linkis.cs.common.entity.enumeration.DBType;

public class CSDB implements DB {

  private String name;

  private DBType dbType;

  private String[] lables;

  private String comment;

  private String owners;

  public static DB build() {
    return null;
  };

  @Override
  @KeywordMethod
  public String getName() {
    return this.name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public DBType getDbType() {
    return this.dbType;
  }

  @Override
  public void setDbType(DBType dbType) {
    this.dbType = dbType;
  }

  @Override
  @KeywordMethod
  public String getOwners() {
    return this.owners;
  }

  @Override
  public void setOwners(String owners) {
    this.owners = owners;
  }

  @Override
  public String getComment() {
    return this.comment;
  }

  @Override
  public void setComment(String comment) {
    this.comment = comment;
  }

  @Override
  public String[] getLables() {
    return this.lables;
  }

  @Override
  public void setLables(String[] lables) {
    this.lables = lables;
  }
}
