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

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.computation.client.once.simple.SubmittableSimpleOnceJob;
import org.apache.linkis.computation.client.utils.LabelKeyUtils;

public class SeatunnelOnceJobTest {
  public static void main(String[] args) {
    LinkisJobClient.config().setDefaultServerUrl("http://127.0.0.1:9001");
    String code =
        "\n"
            + "env {\n"
            + "  spark.app.name = \"SeaTunnel\"\n"
            + "  spark.executor.instances = 2\n"
            + "  spark.executor.cores = 1\n"
            + "  spark.executor.memory = \"1g\"\n"
            + "}\n"
            + "\n"
            + "source {\n"
            + " FakeSource {\n"
            + "   result_table_name = \"fake\"\n"
            + "   row.num = 16\n"
            + "   schema = {\n"
            + "      fields {\n"
            + "         name = \"string\"\n"
            + "         age = \"int\"\n"
            + "      }\n"
            + "   }\n"
            + " }\n"
            + "}\n"
            + "\n"
            + "transform {\n"
            + "}\n"
            + "\n"
            + "sink {\n"
            + "  Console {}\n"
            + "}";
    SubmittableSimpleOnceJob onceJob =
        LinkisJobClient.once()
            .simple()
            .builder()
            .setCreateService("seatunnel-Test")
            .setMaxSubmitTime(300000)
            .addLabel(LabelKeyUtils.ENGINE_TYPE_LABEL_KEY(), "seatunnel-2.3.1")
            .addLabel(LabelKeyUtils.USER_CREATOR_LABEL_KEY(), "hadoop-seatunnel")
            .addLabel(LabelKeyUtils.ENGINE_CONN_MODE_LABEL_KEY(), "once")
            .addStartupParam(Configuration.IS_TEST_MODE().key(), true)
            .addExecuteUser("hadoop")
            .addJobContent("runType", "sspark")
            .addJobContent("code", code)
            .addJobContent("master", "local[4]")
            .addJobContent("deploy-mode", "client")
            .addSource("jobName", "OnceJobTest")
            .build();
    onceJob.submit();
    System.out.println(onceJob.getId());

    onceJob.waitForCompleted();
    System.out.println(onceJob.getStatus());
    LinkisJobMetrics jobMetrics = onceJob.getJobMetrics();
    System.out.println(jobMetrics.getMetrics());
  }
}
