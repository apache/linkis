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

package org.apache.linkis.computation.client;

import org.apache.linkis.computation.client.interactive.SubmittableInteractiveJob;
import org.apache.linkis.manager.label.conf.LabelCommonConfig;

/** A test class for submit a sql to hive engineConn. */
public class InteractiveJobTest {

  public static void main(String[] args) {
    // TODO First, set the right gateway url.
    LinkisJobClient.config().setDefaultServerUrl("http://127.0.0.1:9001");
    // TODO Secondly, please modify the executeUser
    SubmittableInteractiveJob job =
        LinkisJobClient.interactive()
            .builder()
            .setEngineType("hive-" + LabelCommonConfig.HIVE_ENGINE_VERSION.getValue())
            .setRunTypeStr("sql")
            .setCreator("IDE")
            .setCode("show tables")
            .addExecuteUser("hadoop")
            .setMaxRetry(2) // automatic retry number
            .build();
    job.addLogListener(new TestLogListener());
    // 3. Submit Job to Linkis
    job.submit();
    // 4. Wait for Job completed
    job.waitForCompleted();
    // 5. Get results from iterators.
    ResultSetIterator iterator = job.getResultSetIterables()[0].iterator();
    System.out.println(iterator.getMetadata());
    while (iterator.hasNext()) {
      System.out.println(iterator.next());
    }
  }
}
