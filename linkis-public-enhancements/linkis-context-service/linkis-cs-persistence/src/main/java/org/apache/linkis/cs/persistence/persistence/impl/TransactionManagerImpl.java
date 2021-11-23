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
 
package org.apache.linkis.cs.persistence.persistence.impl;

import org.apache.linkis.cs.persistence.persistence.TransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;


@Component
public class TransactionManagerImpl implements TransactionManager {

    @Autowired
    private PlatformTransactionManager txManager;

    @Override
    public Object begin() {
        return txManager.getTransaction(new DefaultTransactionDefinition());
    }

    @Override
    public void rollback(Object object) {
        TransactionStatus status = (TransactionStatus) object;
        txManager.rollback(status);
    }

    @Override
    public void commit(Object object) {
        TransactionStatus status = (TransactionStatus) object;
        txManager.commit(status);
    }

    @Override
    public void onTransaction() {
        // TODO: 2020/2/17  
    }
}
