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

package org.apache.linkis.cs.common.serialize.impl.value;

import org.apache.linkis.cs.common.entity.resource.LinkisBMLResource;
import org.apache.linkis.cs.common.exception.CSErrorException;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LinkisBMLResourceSerializerTest {

  @Test
  @DisplayName("getTypeTest")
  public void getTypeTest() {

    LinkisBMLResourceSerializer bmlResourceSerializer = new LinkisBMLResourceSerializer();
    String type = bmlResourceSerializer.getType();
    Assertions.assertNotNull(type);
  }

  @Test
  @DisplayName("fromJsonTest")
  public void fromJsonTest() throws CSErrorException {

    LinkisBMLResource linkisBMLResource = new LinkisBMLResource();
    linkisBMLResource.setUser("hadoop");
    linkisBMLResource.setResourceId("12345678");

    String json = new Gson().toJson(linkisBMLResource);

    LinkisBMLResourceSerializer bmlResourceSerializer = new LinkisBMLResourceSerializer();
    LinkisBMLResource resource = bmlResourceSerializer.fromJson(json);

    Assertions.assertNotNull(resource);
  }

  @Test
  @DisplayName("acceptsTest")
  public void acceptsTest() {
    LinkisBMLResource linkisBMLResource = new LinkisBMLResource();
    LinkisBMLResourceSerializer bmlResourceSerializer = new LinkisBMLResourceSerializer();
    boolean accepts = bmlResourceSerializer.accepts(linkisBMLResource);
    Assertions.assertTrue(accepts);
  }
}
