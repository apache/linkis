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

package org.apache.linkis.engineconnplugin.flink.client.sql.parser;

import java.util.Arrays;
import java.util.Objects;

public class SqlCommandCall {
  public final SqlCommand command;
  public final String[] operands;

  public SqlCommandCall(SqlCommand command, String[] operands) {
    this.command = command;
    this.operands = operands;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SqlCommandCall that = (SqlCommandCall) o;
    return command == that.command && Arrays.equals(operands, that.operands);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(command);
    result = 31 * result + Arrays.hashCode(operands);
    return result;
  }

  @Override
  public String toString() {
    return command + "(" + Arrays.toString(operands) + ")";
  }
}
