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

package org.apache.linkis.storage.script;

import org.apache.linkis.common.io.MetaData;

public class ScriptMetaData implements MetaData {
  private Variable[] variables;

  public ScriptMetaData(Variable[] variables) {
    this.variables = variables;
  }

  @Override
  public MetaData cloneMeta() {
    return new ScriptMetaData(variables.clone());
  }

  public Variable[] getMetaData() {
    return variables;
  }

  public void setMetaData(Variable[] variables) {
    this.variables = variables;
  }
}
