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
 
package org.apache.linkis.cs.persistence;

import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.persistence.persistence.ContextKeyListenerPersistence;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ContextKeyListenerTest {
    AnnotationConfigApplicationContext context = null;
    ContextKeyListenerPersistence contextKeyListenerPersistence = null;

    public void before() {
        context = new AnnotationConfigApplicationContext(Scan.class);
        contextKeyListenerPersistence = context.getBean(ContextKeyListenerPersistence.class);
    }


    public void testcreateContextKeyListener() throws CSErrorException {
        AContextKeyListener aContextKeyListener = new AContextKeyListener();
        aContextKeyListener.setSource("source");
        AContextKey aContextKey = new AContextKey();
        aContextKey.setKey("flow1.node2");
        aContextKeyListener.setContextKey(aContextKey);
        AContextID aContextID = new AContextID();
        aContextID.setContextId("84716");
        contextKeyListenerPersistence.create(aContextID,aContextKeyListener);
    }


    public void testDeleteContextKeyListener() throws CSErrorException {
        AContextKeyListener aContextKeyListener = new AContextKeyListener();
        AContextID aContextID = new AContextID();
        aContextID.setContextId("84716");
        AContextKey aContextKey = new AContextKey();
        aContextKey.setKey("flow1.node2");
        aContextKeyListener.setContextKey(aContextKey);
        aContextKeyListener.setSource("source");
        contextKeyListenerPersistence.remove(aContextID,aContextKeyListener);
    }


    public void testDeleteAllContextIDListener() throws CSErrorException {
        AContextKeyListener aContextKeyListener = new AContextKeyListener();
        AContextID aContextID = new AContextID();
        aContextID.setContextId("84716");
        AContextKey aContextKey = new AContextKey();
        aContextKey.setKey("flow1.node2");
        aContextKeyListener.setContextKey(aContextKey);
        contextKeyListenerPersistence.removeAll(aContextID);
    }


    public void testGetContextID() throws CSErrorException {
        //ContextID contextID = contextIDPersistence.getContextID(32312579);
        //System.out.println(((AContextID)contextID).getProject());
    }

}
