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
 
package org.apache.linkis.cli.application.interactor.job;

import org.apache.linkis.cli.application.data.ProcessedData;
import org.apache.linkis.cli.application.interactor.command.template.ProcessInputUtil;
import org.apache.linkis.cli.application.interactor.command.template.TestSparkCmdTemplate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class LinkisJobBuilderTest {
    LinkisJobBuilder builder;

    @Before
    public void before() throws Exception {
        String[] cmdStr = new String[]{"-u", "hadoop",
                "-pwd", "1234",
                "-c", "src/test/resources/conf/user.properties",
                "--cmd", "show tables",
                "--split", "\',\'",
                "--queue", "q05",
                "--name", "testApp",
//      "--hiveconf", "/path/...",
//      "--num-executors", "4",
                "--executor-cores", "4",
                "--executor-memory", "4G",
                "--shuffle-partitions", "200",
                "-confMap", "kk=vv,kkk=vvv,spark.executor.instances=8"
        };
        ProcessedData data = ProcessInputUtil.generateProcessedData(cmdStr, new TestSparkCmdTemplate());
//  builder = new LinkisJobBuilder()
//      .setIdentifier(data.getIdentifier())
//      .setCmdType(data.getCmdType())
//      .setExecutionType(data.getExecutionType())
//      .setSubExecutionType(data.getSubExecutionType())
//      .setStdVarAccess(data.getStdVarAccess())
//      .setSysVarAccess(data.getSysVarAccess());
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: setIdentifier(String identifier)
     */
    @Test
    public void testSetIdentifier() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: build()
     */
    @Test
    public void testBuild() throws Exception {
//TODO: Test goes here...
//  LinkisJob job = builder.build();
//  System.out.println(CommonUtils.GSON.toJson(job));
//  Assert.assertEquals(job.getParamConfMap().get("spark.executor.instances"), "8");
    }

    /**
     * Method: setStdVarAccess(VarAccess varAccess)
     */
    @Test
    public void testSetStdVarAccess() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: setSysVarAccess(VarAccess varAccess)
     */
    @Test
    public void testSetSysVarAccess() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getTargetNewInstance()
     */
    @Test
    public void testGetTargetNewInstance() throws Exception {
//TODO: Test goes here... 
    }


} 
