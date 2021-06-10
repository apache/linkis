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

package com.webank.wedatasphere.linkis.jobhistory.cache;

import com.webank.wedatasphere.linkis.governance.common.entity.job.JobRequest;
import com.webank.wedatasphere.linkis.governance.common.entity.task.RequestPersistTask;
import com.webank.wedatasphere.linkis.governance.common.protocol.job.JobReq;
import com.webank.wedatasphere.linkis.jobhistory.cache.domain.TaskResult;
import com.webank.wedatasphere.linkis.protocol.query.cache.RequestDeleteCache;
import com.webank.wedatasphere.linkis.protocol.query.cache.RequestReadCache;

public interface QueryCacheService {

    Boolean needCache(JobRequest jobRequest);

    void writeCache(JobRequest jobRequest);

    TaskResult readCache(RequestReadCache requestReadCache);

    void deleteCache(RequestDeleteCache requestDeleteCache);
}
