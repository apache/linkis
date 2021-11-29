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
 
package org.apache.linkis.cs.server.protocol;

import org.apache.linkis.server.Message;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;


public class RestResponseProtocol implements HttpResponseProtocol<Message> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final Object lock = new Object();

    private Message message;

    private Object responseData;

    @Override
    public void waitForComplete() throws InterruptedException {
        synchronized (lock) {
            lock.wait();
        }
    }

    @Override
    public void waitTimeEnd(long mills) throws InterruptedException {
        logger.info(String.format("start to wait %smills until job complete", mills));
        synchronized (lock) {
            lock.wait(mills);
        }
    }

    @Override
    public void notifyJob() {
        logger.info("notify the job");
        synchronized (lock) {
            lock.notify();
        }
    }

    @Override
    public Message get() {
        return this.message;
    }

    @Override
    public void set(Message message) {
        this.message = message;
    }

    @Override
    public Object getResponseData() {
        return this.responseData;
    }

    @Override
    public void setResponseData(Object responseData) {
        this.responseData = responseData;
    }

    public void ok(String msg) {
        if (message == null) {
            message = new Message();
        }
        if (StringUtils.isEmpty(msg)) {
            message.setMessage("OK");
        } else {
            message.setMessage(msg);
        }
    }

    public void error(String msg, Throwable t) {
        if (message == null) {
            message = new Message();
            message.setStatus(1);
        }
        message.setMessage(msg);
        if (t != null) {
            message.$less$less("stack", ExceptionUtils.getFullStackTrace(t));
        }
    }
}
