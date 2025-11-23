/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.cs.persistence;

import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.persistence.persistence.ContextIDListenerPersistence;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ContextIDListenerTest {
  AnnotationConfigApplicationContext context = null;
  ContextIDListenerPersistence contextIDListenerPersistence = null;

  public void before() {
    context = new AnnotationConfigApplicationContext(Scan.class);
    contextIDListenerPersistence = context.getBean(ContextIDListenerPersistence.class);
  }

  public void testcreateContextIDListener() throws CSErrorException {
    AContextIDListener aContextIDListener = new AContextIDListener();
    aContextIDListener.setSource("source");
    AContextID aContextID = new AContextID();
    aContextID.setContextId("84716");
    aContextIDListener.setContextID(aContextID);
    contextIDListenerPersistence.create(aContextID, aContextIDListener);
  }

  public void testDeleteContextIDListener() throws CSErrorException {
    AContextIDListener aContextIDListener = new AContextIDListener();
    AContextID aContextID = new AContextID();
    aContextID.setContextId("84716");
    aContextIDListener.setSource("source");
    aContextIDListener.setContextID(aContextID);
    contextIDListenerPersistence.remove(aContextIDListener);
  }

  public void testDeleteAllContextIDListener() throws CSErrorException {
    AContextID aContextID = new AContextID();
    aContextID.setContextId("84716");
    contextIDListenerPersistence.removeAll(aContextID);
  }

  public void testGetContextID() throws CSErrorException {
    // ContextID contextID = contextIDPersistence.getContextID(32312579);
    // System.out.println(((AContextID)contextID).getProject());
  }
}
