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
 
package org.apache.linkis.cs.server.service;

import org.apache.linkis.cs.common.entity.listener.ContextIDListenerDomain;
import org.apache.linkis.cs.common.entity.listener.ContextKeyListenerDomain;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.listener.callback.imp.ContextKeyValueBean;

import java.util.List;

public abstract class ContextListenerService extends AbstractService {

    public abstract void onBind(ContextID contextID, ContextIDListenerDomain listener) throws CSErrorException;

    public abstract void onBind(ContextID contextID, ContextKey contextKey, ContextKeyListenerDomain listener) throws CSErrorException;

    public abstract List<ContextKeyValueBean> heartbeat(String source);

}
