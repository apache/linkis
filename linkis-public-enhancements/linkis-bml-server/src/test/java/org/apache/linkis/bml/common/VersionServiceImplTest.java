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

package org.apache.linkis.bml.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VersionServiceImplTest {

  @Test
  @DisplayName("testCheckBmlResourceStoragePrefixPathIfChanged")
  public void testUpdateVersion() {
    String path = "hdfs:///data/linkis/linkis/20220609/b4fd8f59-9492-4a0f-a074-9ac573a69b60";
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    String dateStr = format.format(new Date());
    ResourceHelper hdfsResourceHelper = new HdfsResourceHelper();
    String newPath =
        hdfsResourceHelper.generatePath(
            "linkis", path.substring(path.lastIndexOf("/") + 1), new HashMap<>());
    assertEquals(
        newPath,
        "hdfs:///apps-data/linkis/bml/" + dateStr + "/b4fd8f59-9492-4a0f-a074-9ac573a69b60");
  }
}
