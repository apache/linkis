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

package com.webank.wedatasphere.linkis.jobhistory.dao;

import com.webank.wedatasphere.linkis.jobhistory.entity.QueryTask;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by johnnwang on 2018/10/10.
 */
public interface TaskMapper {

    List<QueryTask> selectTask(QueryTask queryTask);

    void insertTask(QueryTask queryTask);

    void updateTask(QueryTask queryTask);

    List<QueryTask> search(@Param("taskID") Long taskID, @Param("umUser") String username, @Param("status") List<String> status,
                           @Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("executeApplicationName") String executeApplicationName);

    String selectTaskStatusForUpdate(Long taskID);
}
