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

package org.apache.linkis.engineconnplugin.flink.client.shims;

import org.apache.flink.table.catalog.CatalogManager;
import org.apache.flink.table.catalog.FunctionCatalog;
import org.apache.flink.table.module.ModuleManager;

public class SessionState {

  public final CatalogManager catalogManager;
  public final ModuleManager moduleManager;
  public final FunctionCatalog functionCatalog;

  private SessionState(
      CatalogManager catalogManager, ModuleManager moduleManager, FunctionCatalog functionCatalog) {
    this.catalogManager = catalogManager;
    this.moduleManager = moduleManager;
    this.functionCatalog = functionCatalog;
  }

  public static SessionState of(
      CatalogManager catalogManager, ModuleManager moduleManager, FunctionCatalog functionCatalog) {
    return new SessionState(catalogManager, moduleManager, functionCatalog);
  }
}
