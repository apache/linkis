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

package org.apache.linkis.metadata.query.common.service;

import java.io.Closeable;
import java.util.Collections;
import java.util.Map;

public interface BaseMetadataService {

  /**
   * Get connection
   *
   * @param params connect params
   * @return
   */
  MetadataConnection<? extends Closeable> getConnection(String operator, Map<String, Object> params)
      throws Exception;

  /**
   * Get connection information (default empty)
   *
   * @param operator operator
   * @param params connect params
   * @param queryParams query params
   * @return information
   */
  default Map<String, String> getConnectionInfo(
      String operator, Map<String, Object> params, Map<String, String> queryParams) {
    return Collections.emptyMap();
  }
}
