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

import org.apache.linkis.governance.common.entity.job.JobRequest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SQLExplainTest {

  @Test
  void isSelectCmdNoLimit() {

    String code = "SELECT * from dual WHERE (1=1)LIMIT 1;";
    boolean res = SQLExplain.isSelectCmdNoLimit(code);
    Assertions.assertEquals(false, res);

    code = "SELECT * from dual";
    res = SQLExplain.isSelectCmdNoLimit(code);
    Assertions.assertEquals(true, res);

    code = "SELECT * from dual LIMIT 1;";
    res = SQLExplain.isSelectCmdNoLimit(code);
    Assertions.assertEquals(false, res);
  }

  @Test
  void isSelectOverLimit() {
    String code = "SELECT * from dual WHERE (1=1)LIMIT 5001;";
    boolean res = SQLExplain.isSelectOverLimit(code);
    Assertions.assertEquals(true, res);

    code = "SELECT * from dual";
    res = SQLExplain.isSelectOverLimit(code);
    Assertions.assertEquals(false, res);

    code = "SELECT * from dual LIMIT 4000;";
    res = SQLExplain.isSelectOverLimit(code);
    Assertions.assertEquals(false, res);
  }

  /**
   * 未修复前代码进行拼接sql时，输出的sql为 select id, name,
   * array_join(array_intersect(map_keys(info),array['abs','oda'],' limit 5000; ') as infos from
   * ods.dim_ep22
   */
  @Test
  void splicingLimitSql() {
    String code =
        "select\n"
            + "id,\n"
            + "name,\n"
            + "array_join(array_intersect(map_keys(info),array['abs','oda'],';') as infos\n"
            + "from ods.dim_ep22";
    StringBuilder logAppender = new StringBuilder();
    JobRequest jobRequest = new JobRequest();
    SQLExplain.dealSQLLimit(code, jobRequest, logAppender);
    Assertions.assertEquals(code + " limit 5000", jobRequest.getExecutionCode());
  }
}
