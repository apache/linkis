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

package org.apache.linkis.cs.common.serialize.impl.value.data;

import org.apache.linkis.cs.common.entity.data.LinkisJobData;
import org.apache.linkis.cs.common.exception.CSErrorException;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LinkisJobDataSerializerTest {

  @Test
  @DisplayName("getTypeTest")
  public void getTypeTest() {

    LinkisJobDataSerializer serializer = new LinkisJobDataSerializer();
    String type = serializer.getType();
    Assertions.assertNotNull(type);
  }

  @Test
  @DisplayName("fromJsonTest")
  public void fromJsonTest() throws CSErrorException {

    LinkisJobData linkisJobData = new LinkisJobData();
    linkisJobData.setJobID(1L);

    LinkisJobDataSerializer serializer = new LinkisJobDataSerializer();
    LinkisJobData jobData = serializer.fromJson(new Gson().toJson(linkisJobData));

    Assertions.assertNotNull(jobData);
  }

  @Test
  @DisplayName("acceptsTest")
  public void acceptsTest() {

    LinkisJobData linkisJobData = new LinkisJobData();
    LinkisJobDataSerializer serializer = new LinkisJobDataSerializer();
    boolean accepts = serializer.accepts(linkisJobData);
    Assertions.assertTrue(accepts);
  }
}
