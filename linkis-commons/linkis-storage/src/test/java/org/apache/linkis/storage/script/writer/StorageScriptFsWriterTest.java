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

package org.apache.linkis.storage.script.writer;

import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.storage.script.ScriptFsWriter;
import org.apache.linkis.storage.script.ScriptMetaData;
import org.apache.linkis.storage.script.ScriptRecord;
import org.apache.linkis.storage.script.Variable;
import org.apache.linkis.storage.script.VariableParser;
import org.apache.linkis.storage.source.FileSource;
import org.apache.linkis.storage.source.FileSource$;

import org.apache.commons.math3.util.Pair;

import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StorageScriptFsWriterTest {

  //    {"fileName":"229cf765-6839-4c82-829d-1907c2ccf668.sql","scriptContent":"select
  // ${qq};\n--@set test=123\nselect ${test};\n--@set qq=222\nselect
  // ${qq};\n","projectName":"test1122a1","metadata":{"variable":{"qq":"123"},"configuration":{"special":{},"runtime":{},"startup":{}}},"resourceId":"c9755cad-619b-4c1c-9204-cc4bb9836194"}
  //

  String scriptContent =
      ""
          + "select ${qq};\n"
          + "--@set test=123\n"
          + "select ${test};\n"
          + "--@set qq=222\n"
          + "select ${qq};\n"
          + "--\n"
          + "--\n"
          + "select 1;";
  String metaData =
      "{\"variable\":{\"qq\":\"123\"},\"configuration\":{\"special\":{},\"runtime\":{},\"startup\":{}}}";

  String resultMetaData = "{\"variable\":{\"qq\":\"123\"}}";

  String resultString =
      ""
          + "--@set qq=123\n"
          + "--\n"
          + "select ${qq};\n"
          + "--@set test=123\n"
          + "select ${test};\n"
          + "--@set qq=222\n"
          + "select ${qq};\n"
          + "--\n"
          + "--\n"
          + "select 1;";

  Map<String, Object> params;

  {
    try {
      params = new ObjectMapper().readValue(metaData, HashMap.class);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  String fileName = "229cf765-6839-4c82-829d-1907c2ccf668.sql";

  //        return Message.ok().data("resourceId", resourceId).data("version", version);

  @Test
  void TestSave() {
    ScriptFsWriter writer =
        StorageScriptFsWriter.getScriptFsWriter(new FsPath(fileName), "UTF-8", null);
    Variable[] v = VariableParser.getVariables(params);
    List<Variable> variableList =
        Arrays.stream(v)
            .filter(var -> !StringUtils.isEmpty(var.value()))
            .collect(Collectors.toList());
    try {

      MetaData metaData = new ScriptMetaData(variableList.toArray(new Variable[0]));
      writer.addMetaData(metaData);
      writer.addRecord(new ScriptRecord(scriptContent));
      InputStream inputStream = writer.getInputStream();

      String text =
          new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
              .lines()
              .collect(Collectors.joining("\n"));
      Assertions.assertEquals(text, resultString);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  void TestOpen() throws FileNotFoundException {

    // /api/rest_j/v1/filesystem/openScriptFromBML?fileName=229cf765-6839-4c82-829d-1907c2ccf668.sql&resourceId=c9755cad-619b-4c1c-9204-cc4bb9836194&version=v000008&creator=&projectName=test1122a1

    String filePath = this.getClass().getResource("/scritpis-test.sql").getFile().toString();

    File file = new File(filePath);

    InputStream inputStream = new FileInputStream(file);

    FileSource fileSource = FileSource$.MODULE$.create(new FsPath(fileName), inputStream);
    Pair<Object, ArrayList<String[]>> collect = fileSource.collect()[0];

    String scriptRes = collect.getSecond().get(0)[0];
    String metadataRes = new Gson().toJson(collect.getFirst());

    Assertions.assertEquals(scriptRes, scriptContent + "\n");

    Assertions.assertEquals(metadataRes, resultMetaData);
  }
}
