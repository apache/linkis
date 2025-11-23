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

package org.apache.linkis.cs.persistence;

import org.apache.linkis.cs.persistence.entity.ExtraFieldClass;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ExtraFieldClassTest {

  public void test01() {
    /*String json = "{\n" +
    "  \"z\": 100.0,\n" +
    "  \"x\": 200,\n" +
    "  \"c\": 300,\n" +
    "  \"v\":\"中文\",\n" +
    "  \"b\":true\n" +
    "}";*/
    Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(new TypeToken<List<Object>>() {}.getType(), new MapTypeAdapter())
            .create();
    /*        Map<String, Object> map = gson.fromJson(json, new TypeToken<List<Object>>() {
    }.getType());
    map.forEach((k,v) -> System.out.println(v.getClass().getName()));*/
    ExtraFieldClass extraFieldClass = new ExtraFieldClass();
    extraFieldClass.addFieldValue(666);
    String json = gson.toJson(extraFieldClass);
    System.out.println(json);
    ExtraFieldClass extraFieldClass1 = gson.fromJson(json, ExtraFieldClass.class);
    extraFieldClass1.getFieldValues().forEach(f -> System.out.println(f.getClass().getName()));
  }
}
