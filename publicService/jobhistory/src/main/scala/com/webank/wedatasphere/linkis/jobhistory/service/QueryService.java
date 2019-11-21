/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.jobhistory.service;

import com.webank.wedatasphere.linkis.protocol.query.*;
import com.webank.wedatasphere.linkis.jobhistory.entity.QueryTask;
import com.webank.wedatasphere.linkis.jobhistory.entity.QueryTaskVO;

import java.util.Date;
import java.util.List;

/**
 * Created by johnnwang on 2019/2/25.
 */
public interface QueryService {

    ResponsePersist add(RequestInsertTask requestInsertTask);

    ResponsePersist change(RequestUpdateTask requestUpdateTask);

    ResponsePersist query(RequestQueryTask requestQueryTask);

    QueryTaskVO getTaskByID(Long taskID,String userName);

    List<QueryTask> search(Long taskID, String username, String status, Date sDate, Date eDate, String executeApplicationName);
}
