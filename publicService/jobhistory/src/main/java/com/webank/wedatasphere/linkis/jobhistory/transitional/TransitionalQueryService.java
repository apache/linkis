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

package com.webank.wedatasphere.linkis.jobhistory.transitional;

import com.webank.wedatasphere.linkis.protocol.query.RequestUpdateTask;
import com.webank.wedatasphere.linkis.protocol.query.ResponsePersist;
import com.webank.wedatasphere.linkis.jobhistory.dao.TaskMapper;
import com.webank.wedatasphere.linkis.jobhistory.exception.QueryException;
import com.webank.wedatasphere.linkis.jobhistory.service.impl.QueryServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

/**
 * Created by johnnwang on 2019/6/6.
 */
@Component
public class TransitionalQueryService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private QueryServiceImpl queryServiceImpl;

    @Transactional
    public ResponsePersist change(RequestUpdateTask requestUpdateTask) {
        ResponsePersist persist = new ResponsePersist();
        try {
            if (requestUpdateTask.getStatus() != null) {
                String oldStatus = taskMapper.selectTaskStatusForUpdate(requestUpdateTask.getTaskID());
                if (oldStatus != null && !shouldUpdate(oldStatus, requestUpdateTask.getStatus()))
                    throw new QueryException(requestUpdateTask.getTaskID() + "The task state in the database is(数据库中的task状态为)：" + oldStatus + "The updated task status is(更新的task状态为)：" + requestUpdateTask.getStatus() + "Update failed!(更新失败)！");
            }
            taskMapper.updateTask(queryServiceImpl.requestPersistTaskTask2QueryTask(requestUpdateTask));
            HashMap<String, Object> map = new HashMap<>();
            map.put("taskID", requestUpdateTask.getTaskID());
            persist.setStatus(0);
            persist.setData(map);
        } catch (Exception e) {
            logger.error(e.getMessage());
            persist.setStatus(1);
            persist.setMsg(e.getMessage());
        }
        return persist;
    }

    private boolean shouldUpdate(String oldStatus, String newStatus) {
        return TaskStatus.valueOf(oldStatus).ordinal() <= TaskStatus.valueOf(newStatus).ordinal();
    }
}
