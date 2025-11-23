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

package org.apache.linkis.entrance.interceptor.impl;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

public class TestCommentHelper {
  String sqlCode =
      ""
          + "--注解\n"
          + "select * from table;\n"
          + "   --注解 \n"
          + "--注解\n"
          + "   select \"--注解\" as test\n"
          + " --@set yy=123\n"
          + "  --注解";

  String scalaCode =
      ""
          + "// 注解\n"
          + "print(1+1)\n"
          + "//@set yy=123\n"
          + " print(2)\n"
          + " // 注解 \n"
          + "// test\n"
          + "print(\"//注解测试\")";

  String scalaCodeRes = "print(1+1)\n" + "print(2)\n" + "print(\"//注解测试\")";

  @Test
  void sqlDealCommentTest() {
    String code = SQLCommentHelper.dealComment(sqlCode);
    // System.out.println(code);
  }

  @Test
  void scalaDealCommentTest() {
    String code = ScalaCommentHelper.dealComment(scalaCode);
    String[] lines =
        Arrays.stream(code.split("\n"))
            .map(String::trim)
            .filter(x -> StringUtils.isNotBlank(x))
            .toArray(String[]::new);
    String result = String.join("\n", lines);
    // assertEquals(result,scalaCodeRes);
  }
}
