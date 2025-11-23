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

package org.apache.linkis.cli.application.exception.error;

public enum ErrorLevel {
  /** warn 1 error 2 fatal 3 */
  INFO(0, "info"),
  WARN(1, "warn"),
  ERROR(2, "error"),
  FATAL(3, "fatal"),
  RETRY(4, "retry");
  private int level;
  private String name;

  private ErrorLevel(int level, String name) {
    this.name = name;
    this.level = level;
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "ExceptionLevel{" + "level=" + level + ", name='" + name + '\'' + '}';
  }
}
