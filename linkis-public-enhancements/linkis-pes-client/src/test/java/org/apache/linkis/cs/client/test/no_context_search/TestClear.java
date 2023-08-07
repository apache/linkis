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

package org.apache.linkis.cs.client.test.no_context_search;

import org.apache.linkis.cs.client.ContextClient;
import org.apache.linkis.cs.client.builder.ContextClientFactory;

import java.util.List;

public class TestClear {

  public static void main(String[] args) throws Exception {

    String createTimeStart = "2022-05-26 22:04:00";

    String createTimeEnd = "2022-06-01 24:00:00";

    ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();

    List<String> idList =
        contextClient.searchHAIDByTime(
            createTimeStart, createTimeEnd, null, null, null, null, 0, 0);

    for (String id : idList) {
      System.out.println(id);
    }

    System.out.println("Got " + idList.size() + " ids.");

    if (idList.size() > 0) {
      String id1 = idList.get(0);
      System.out.println("will clear context of id : " + id1);
    }

    //        List<String> tmpList = new ArrayList<>();
    //        tmpList.add(id1);
    //        int num = contextClient.batchClearContextByHAID(tmpList);
    //        System.out.println("Succeed to clear  " + num + " ids.");
    //
    int num1 =
        contextClient.batchClearContextByTime(
            createTimeStart, createTimeEnd, null, null, null, null);
    System.out.println("Succeed to clear  " + num1 + " ids by time.");
  }
}
