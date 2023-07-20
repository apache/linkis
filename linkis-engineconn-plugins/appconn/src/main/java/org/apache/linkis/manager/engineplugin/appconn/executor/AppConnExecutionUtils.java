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

package org.apache.linkis.manager.engineplugin.appconn.executor;

import java.util.List;
import java.util.Map;

import com.webank.wedatasphere.dss.common.label.DSSLabel;
import com.webank.wedatasphere.dss.standard.app.development.listener.ref.RefExecutionRequestRef;
import com.webank.wedatasphere.dss.standard.app.development.operation.RefExecutionOperation;
import com.webank.wedatasphere.dss.standard.app.development.service.RefExecutionService;
import com.webank.wedatasphere.dss.standard.app.development.utils.DevelopmentOperationUtils;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef;

/**
 * @author enjoyyin
 * @date 2022-03-18
 * @since 0.5.0
 */
public class AppConnExecutionUtils {

  public static ResponseRef tryToOperation(
      RefExecutionService refExecutionService,
      String contextId,
      String projectName,
      ExecutionRequestRefContextImpl executionRequestRefContext,
      List<DSSLabel> dssLabels,
      String name,
      String type,
      String userName,
      Workspace workspace,
      Map<String, Object> refJobContent,
      Map<String, Object> variables) {
    return DevelopmentOperationUtils.tryRefJobContentRequestRefOperation(
        () -> refExecutionService,
        developmentService -> refExecutionService.getRefExecutionOperation(),
        refJobContentRequestRef -> refJobContentRequestRef.setRefJobContent(refJobContent),
        dssContextRequestRef -> dssContextRequestRef.setContextId(contextId),
        projectRefRequestRef -> projectRefRequestRef.setProjectName(projectName),
        (developmentOperation, developmentRequestRef) -> {
          RefExecutionRequestRef requestRef = (RefExecutionRequestRef) developmentRequestRef;
          requestRef
              .setExecutionRequestRefContext(executionRequestRefContext)
              .setVariables(variables)
              .setDSSLabels(dssLabels)
              .setName(name)
              .setType(type)
              .setUserName(userName)
              .setWorkspace(workspace);
          return ((RefExecutionOperation) developmentOperation).execute(requestRef);
        },
        "execute node " + name + " with type " + type);
  }
}
