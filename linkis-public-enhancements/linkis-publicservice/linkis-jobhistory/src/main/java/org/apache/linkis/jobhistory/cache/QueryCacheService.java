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
 
package org.apache.linkis.jobhistory.cache;

import org.apache.linkis.governance.common.entity.job.JobRequest;
import org.apache.linkis.governance.common.entity.task.RequestPersistTask;
import org.apache.linkis.governance.common.protocol.job.JobReq;
import org.apache.linkis.jobhistory.cache.domain.TaskResult;
import org.apache.linkis.protocol.query.cache.RequestDeleteCache;
import org.apache.linkis.protocol.query.cache.RequestReadCache;

public interface QueryCacheService {

    Boolean needCache(JobRequest jobRequest);

    void writeCache(JobRequest jobRequest);

    TaskResult readCache(RequestReadCache requestReadCache);

    void deleteCache(RequestDeleteCache requestDeleteCache);
}
