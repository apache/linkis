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
 
package org.apache.linkis.cs.execution.fetcher;

import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.contextcache.ContextCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ContextTypeContextSearchFetcher extends AbstractContextCacheFetcher{

    private static final Logger logger = LoggerFactory.getLogger(ContextTypeContextSearchFetcher.class);

    ContextType contextType;

    public ContextTypeContextSearchFetcher(ContextCacheService contextCacheService, ContextType contextType) {
        super(contextCacheService);
        this.contextType = contextType;
    }

    private ContextTypeContextSearchFetcher(ContextCacheService contextCacheService) {
        super(contextCacheService);
    }

    @Override
    public List<ContextKeyValue> fetch(ContextID contextID) {
        return contextCacheService.getAllByType(contextID, contextType);
    }
}
