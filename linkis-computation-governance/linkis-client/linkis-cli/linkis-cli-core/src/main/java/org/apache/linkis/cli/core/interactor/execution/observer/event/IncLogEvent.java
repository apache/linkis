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
 
package org.apache.linkis.cli.core.interactor.execution.observer.event;

import org.apache.linkis.cli.common.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.execution.jobexec.JobSubmitExec;


public class IncLogEvent extends SingleObserverEvent {
    @Override
    public final void notifyObserver(LinkisClientEvent event, Object message) {
        if (message instanceof JobSubmitExec) {
            JobSubmitExec dataCopy;
            try {
                dataCopy = ((JobSubmitExec) message).getCopy(); // avoid concurrent modification
            } catch (Exception e) {
                throw new LinkisClientRuntimeException("EVT0004", ErrorLevel.ERROR, CommonErrMsg.EventErr, "cannot get deep copy of " + message.getClass().getCanonicalName(), e);
            }
            super.notifyObserver(this, dataCopy);
        } else {
            throw new LinkisClientRuntimeException("EVT0005", ErrorLevel.ERROR, CommonErrMsg.EventErr, "cannot notify observer: event message is not instnce of JobSubmitExec");
        }
    }

}