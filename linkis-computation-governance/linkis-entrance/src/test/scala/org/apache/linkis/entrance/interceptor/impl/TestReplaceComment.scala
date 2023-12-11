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

package org.apache.linkis.entrance.interceptor.impl

import org.junit.jupiter.api.{Assertions, Test}

class TestReplaceComment {

  @Test
  def TestRepComm: Unit = {
    val realCode = "drop table if exists default.test;" +
      "create table default.test(" +
      "id varchar(11) comment '这是注释测试分号;这是注释测试分号;'," +
      "id1 string comment '测试'," +
      "id2 string COMMENT '码值说明:2-高;3-中;4-低;'" +
      ");"
    val expectCode = "drop table if exists default.test;" +
      "create table default.test(" +
      "id varchar(11) comment '这是注释测试分号\\;这是注释测试分号\\;'," +
      "id1 string comment '测试'," +
      "id2 string COMMENT '码值说明:2-高\\;3-中\\;4-低\\;'" +
      ");"

    Assertions.assertEquals(SQLCommentHelper.replaceComment(realCode), expectCode)
  }

}
