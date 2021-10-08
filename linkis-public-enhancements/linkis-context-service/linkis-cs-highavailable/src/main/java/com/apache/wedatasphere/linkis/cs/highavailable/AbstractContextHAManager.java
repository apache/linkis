/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.apache.wedatasphere.linkis.cs.highavailable;

import com.apache.wedatasphere.linkis.cs.common.entity.source.HAContextID;
import com.apache.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.apache.wedatasphere.linkis.cs.highavailable.ha.BackupInstanceGenerator;
import com.apache.wedatasphere.linkis.cs.highavailable.ha.ContextHAChecker;
import com.apache.wedatasphere.linkis.cs.highavailable.ha.ContextHAIDGenerator;

public abstract class AbstractContextHAManager implements ContextHAManager {

    public abstract ContextHAIDGenerator getContextHAIDGenerator();

    public abstract ContextHAChecker getContextHAChecker();

    public abstract BackupInstanceGenerator getBackupInstanceGenerator();

    public abstract HAContextID convertProxyHAID(HAContextID contextID) throws CSErrorException;
}
