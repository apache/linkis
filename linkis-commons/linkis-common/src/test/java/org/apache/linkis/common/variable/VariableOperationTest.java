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

package org.apache.linkis.common.variable;

import org.apache.linkis.common.exception.VariableOperationFailedException;
import org.apache.linkis.common.utils.VariableOperationUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VariableOperationTest {

  private static final Date date = new Date(1648892107169L);
  private static final ZonedDateTime zonedDateTime =
      VariableOperationUtils.toZonedDateTime(date, ZoneId.of("Asia/Shanghai"));

  @Test
  public void testSqlFormat() throws VariableOperationFailedException {
    String jsonOld = "select &{yyyy-MM}";
    String jsonNew = VariableOperationUtils.replaces(zonedDateTime, jsonOld);
    System.out.println(jsonNew);
    assertEquals(jsonNew, "select 2022-04");
  }

  @Test
  public void testJsonFormat() throws VariableOperationFailedException {
    String jsonOld =
        "{\"name\":\"&{yyyyMMdd%-1d}\",\"address\":{\"street\":\"&{yyyyMMdd%-1y}\"},\"links\":[{\"name\":\"&{yyyyMMdd%-1M}\"}]}";
    String jsonNew = VariableOperationUtils.replaces(zonedDateTime, jsonOld);
    System.out.println(jsonOld + "\n" + jsonNew);
    assertEquals(
        jsonNew,
        "{\"name\":\"\\\"20220401\\\"\",\"address\":{\"street\":\"\\\"20210402\\\"\"},\"links\":[{\"name\":\"\\\"20220302\\\"\"}]}");
  }

  @Test
  public void testJsonArrayFormat() throws VariableOperationFailedException {
    String jsonOld = "[{\"name\":[\"&{yyyyMMdd%-1d}\"],\"address\":[\"&{yyyyMMdd%-1d}\"]}]";
    String jsonNew = VariableOperationUtils.replaces(zonedDateTime, jsonOld);
    System.out.println(jsonOld + "\n" + jsonNew);
    assertEquals(jsonNew, "[{\"name\":[\"\\\"20220401\\\"\"],\"address\":[\"\\\"20220401\\\"\"]}]");
  }

  @Test
  public void testTextFormat() throws VariableOperationFailedException {
    String strOld = "abc&{yyyyMMdd%-1d}def";
    String strNew = VariableOperationUtils.replaces(zonedDateTime, strOld);
    System.out.println(strOld + "\n" + strNew);
    assertEquals(strNew, "abc20220401def");
  }

  @Test
  public void testText2Format() throws VariableOperationFailedException {
    String str = "dss_autotest.demo_data{ds=20220516}";
    String strNew = VariableOperationUtils.replaces(zonedDateTime, str);
    assertEquals(strNew, str);
  }

  @Test
  public void testText3Format() throws VariableOperationFailedException {
    String str = "dss_autotest.demo_data${a1}";
    String strNew = VariableOperationUtils.replaces(zonedDateTime, str);
    assertEquals(strNew, str);
  }

  @Test
  public void testJsonFormatThread() throws Exception {
    String jsonOld =
        "hql|show tables\n"
            + "hql|show tables\n"
            + "hql|show tables\n"
            + "hql|show tables\n"
            + "hql|show tables\n"
            + "scala|val s=sqlContext.sql(\\\"show tables\\\")\\nshow(s)\\n\n"
            + "shell|sleep 100\\nfunction example {\\n echo $[$(date +%s%N)/1000000]\\n}\n"
            + "shell|ifconfig\n"
            + "shell|echo ${f}|\"variable\":{\"f\":\"linkis\"}\n"
            + "python|print(\\\"：hello world\\\") \\ndef world(id):\\n     print(id); \\n     world(${f})|\"variable\":{\"f\":\"36\"}\n"
            + "python|#!/usr/bin/python\\n# -*- coding:utf-8 -*-\\nimport time\\nimport sys,os\\nimport json\\n\\nargs='{\\\"user_name\\\": \\\"zychen\\\"}'\\nprint(args)\\ndict = json.loads(args)\\nusername = dict.get(\\\"user_name\\\")\\nprint(username)\n"
            + "python|import sys\\nprint (\\\"Python Version {}\\\".format(str(sys.version).replace('\\\\n', '')))";

    ExecutorService threadPool = Executors.newFixedThreadPool(10);
    for (int i = 0; i < 10; i++) {
      threadPool.execute(
          () -> {
            try {
              String jsonNew = VariableOperationUtils.replaces(zonedDateTime, jsonOld);
              assertEquals(jsonNew, jsonOld);
            } catch (VariableOperationFailedException e) {
              e.printStackTrace();
              throw new RuntimeException(e);
            }
          });
    }

    threadPool.shutdown();
    while (!threadPool.isTerminated()) {
      Thread.sleep(1000);
    }
  }
}
