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
 
package org.apache.linkis.jobhistory.service;

import org.apache.linkis.governance.common.entity.job.JobRequest;
import org.apache.linkis.governance.common.protocol.conf.EntranceInstanceConfRequest;
import org.apache.linkis.governance.common.protocol.job.*;
import org.apache.linkis.jobhistory.entity.JobHistory;
import org.apache.linkis.rpc.Sender;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public interface JobHistoryQueryService {

    JobRespProtocol add(JobReqInsert jobReqInsert);

    JobRespProtocol change(JobReqUpdate jobReqUpdate);

    ArrayList<JobRespProtocol> batchChange(JobReqBatchUpdate jobReqUpdate);

    JobRespProtocol query(JobReqQuery jobReqQuery);

    JobHistory getJobHistoryByIdAndName(Long jobID, String userName);

    List<JobHistory> search(Long jobId, String username, String creator, String status, Date sDate, Date eDate, String engineType, Long startJobId, String instance);

    Integer countUndoneTasks(String username, String creator, Date sDate, Date eDate, String engineType, Long startJobId);

    JobHistory searchOne(Long jobId, Date sDate, Date eDate);

    List<JobRequest> getQueryVOList(List<JobHistory> list);

    void changeObserveInfoById(JobHistory jobHistory);

    void clearUndoneTasksByEntranceInstance(EntranceInstanceConfRequest request, Sender sender);
}
