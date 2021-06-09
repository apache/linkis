/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.cli.application.driver.transformer;

import com.webank.wedatasphere.linkis.cli.common.entity.execution.jobexec.JobExec;
import com.webank.wedatasphere.linkis.cli.common.entity.execution.jobexec.JobStatus;
import com.webank.wedatasphere.linkis.cli.common.exception.LinkisClientRuntimeException;
import com.webank.wedatasphere.linkis.cli.core.presenter.model.JobExecModel;
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @description: transform data to be compatible with {@link JobExec}
 */
public interface DriverTransformer {
    String convertJobID(String taskID);

    JobStatus convertJobStatus(String jobStatus);

    JobExec convertAndUpdateExecData(JobExec execData, DWSResult result) throws LinkisClientRuntimeException;

    JobExecModel convertAndUpdateModel(JobExecModel model, DWSResult result) throws LinkisClientRuntimeException;

    List<LinkedHashMap<String, String>> convertResultMeta(Object rawMetaData) throws LinkisClientRuntimeException;

    List<List<String>> convertResultContent(Object rawContent) throws LinkisClientRuntimeException;
}