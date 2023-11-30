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

package org.apache.linkis.manager.engineplugin.io.executor;

import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext;
import org.apache.linkis.scheduler.executer.AliasOutputExecuteResponse;
import org.apache.linkis.scheduler.executer.ExecuteResponse;
import org.apache.linkis.storage.domain.MethodEntity;
import org.apache.linkis.storage.domain.MethodEntitySerializer;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class IoEngineConnExecutorTest {

  @Test
  public void testExecuteLine() {
    // test init
    IoEngineConnExecutor ioEngineConnExecutor = new IoEngineConnExecutor(1, Integer.MAX_VALUE);
    EngineExecutionContext engineExecutionContext =
        new EngineExecutionContext(ioEngineConnExecutor, "hadoop");
    engineExecutionContext.setJobId("jobId-1");
    Object[] objects = new Object[10];
    MethodEntity methodEntity =
        new MethodEntity(0L, "file", "hadoop", "hadoop", "localhost", "init", objects);
    AliasOutputExecuteResponse executeResponse =
        (AliasOutputExecuteResponse)
            ioEngineConnExecutor.executeLine(
                engineExecutionContext, MethodEntitySerializer.serializer(methodEntity));
    Assertions.assertThat(executeResponse).isNotNull();
    Assertions.assertThat(executeResponse.alias()).isEqualTo("0");

    // test write
    String filePath = this.getClass().getResource("/testIoResult.dolphin").getFile().toString();
    FsPath fsPath = new FsPath(filePath);
    String fsPathStr = MethodEntitySerializer.serializerJavaObject(fsPath);
    objects = new Object[3];
    objects[0] = fsPathStr;
    objects[1] = true;
    objects[2] = "dolphin000000000300000000040,110000000016aGVsbG8gd29ybGQ=";
    methodEntity = new MethodEntity(0L, "file", "hadoop", "hadoop", "localhost", "write", objects);
    ExecuteResponse writeResponse =
        ioEngineConnExecutor.executeLine(
            engineExecutionContext, MethodEntitySerializer.serializer(methodEntity));
    System.out.println(writeResponse);
    Assertions.assertThat(executeResponse).isNotNull();

    // test read
    objects = new Object[1];
    objects[0] = fsPathStr;
    methodEntity = new MethodEntity(0L, "file", "hadoop", "hadoop", "localhost", "read", objects);
    AliasOutputExecuteResponse readResponse =
        (AliasOutputExecuteResponse)
            ioEngineConnExecutor.executeLine(
                engineExecutionContext, MethodEntitySerializer.serializer(methodEntity));
    Assertions.assertThat(readResponse).isNotNull();
    Assertions.assertThat(readResponse.output())
        .isEqualTo("dolphin000000000300000000040,110000000016aGVsbG8gd29ybGQ=");
  }
}
