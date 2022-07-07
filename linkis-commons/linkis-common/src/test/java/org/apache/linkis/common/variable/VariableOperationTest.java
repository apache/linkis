/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VariableOperationTest {

    private static final Date date = new Date(1648892107169L);
    private static final ZonedDateTime zonedDateTime = VariableOperationUtils.toZonedDateTime(date);

    @Test
    public void testJsonFormat() throws VariableOperationFailedException {
        String jsonOld =
                "{\"name\":\"${yyyyMMdd%-1d}\",\"address\":{\"street\":\"${yyyyMMdd%-1y}\"},\"links\":[{\"name\":\"${yyyyMMdd%-1M}\"}]}";
        String jsonNew = VariableOperationUtils.replaces(zonedDateTime, jsonOld);
        System.out.println(jsonOld + "\n" + jsonNew);
        assertEquals(
                jsonNew,
                "{\"name\":\"\\\"20220401\\\"\",\"address\":{\"street\":\"\\\"20210402\\\"\"},\"links\":[{\"name\":\"\\\"20220302\\\"\"}]}");
    }

    @Test
    public void testTextFormat() throws VariableOperationFailedException {
        String strOld = "abc${yyyyMMdd%-1d}def";
        String strNew = VariableOperationUtils.replaces(zonedDateTime, strOld);
        System.out.println(strOld + "\n" + strNew);
        assertEquals(strNew, "abc20220401def");
    }
}
