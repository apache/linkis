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

package org.apache.linkis.metadata.errorcode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LinkisMetadataErrorCodeSummaryTest {

  @Test
  @DisplayName("enumTest")
  public void enumTest() {

    int unrecognizedCode = LinkisMetadataErrorCodeSummary.UNRECOGNIZED_IMPORT_TYPE.getErrorCode();
    int importHiveCode = LinkisMetadataErrorCodeSummary.IMPORT_HIVE_SOURCE_IS_NULL.getErrorCode();
    int hiveCreateNullCode = LinkisMetadataErrorCodeSummary.HIVE_CREATE_IS_NULL.getErrorCode();
    int hiveCreateTableNullCode =
        LinkisMetadataErrorCodeSummary.HIVE_CREATE__TABLE_IS_NULL.getErrorCode();
    int partitionCode = LinkisMetadataErrorCodeSummary.PARTITION_IS_NULL.getErrorCode();
    int expressCode = LinkisMetadataErrorCodeSummary.EXPRESS_CODE.getErrorCode();

    Assertions.assertTrue(57895 == unrecognizedCode);
    Assertions.assertTrue(57895 == importHiveCode);
    Assertions.assertTrue(57895 == hiveCreateNullCode);
    Assertions.assertTrue(57895 == hiveCreateTableNullCode);
    Assertions.assertTrue(57895 == partitionCode);
    Assertions.assertTrue(57895 == expressCode);
  }
}
