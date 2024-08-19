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

package org.apache.linkis.configuration.enumeration;

public enum BoundaryTypeEnum {
  /*
  0  none
  1 with mix
  2 with max
  3 min and max both
   */
  NONE(0),
  WITH_MIX(1),
  WITH_MAX(2),
  WITH_BOTH(3);

  private Integer id;

  BoundaryTypeEnum(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return this.id;
  }
}
