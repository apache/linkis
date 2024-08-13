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

import org.apache.linkis.cs.common.entity.enumeration.ExpireType;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.persistence.persistence.ContextIDPersistence;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Date;

public class ContextIDTest {
  AnnotationConfigApplicationContext context = null;
  ContextIDPersistence contextIDPersistence = null;

  public void before() {
    context = new AnnotationConfigApplicationContext(Scan.class);
    contextIDPersistence = context.getBean(ContextIDPersistence.class);
  }

  public void testcreateContextID() throws CSErrorException {
    AContextID aContextID = new AContextID();
    aContextID.setContextId(null);
    ContextID contextID = contextIDPersistence.createContextID(aContextID);
    System.out.println(contextID.getContextId());
  }

  public void testDeleteContextID() throws CSErrorException {
    contextIDPersistence.deleteContextID("3");
  }

  public void testGetContextID() throws CSErrorException {
    ContextID contextID = contextIDPersistence.getContextID("2");
    System.out.println(((AContextID) contextID).getProject());
  }

  public void testUpdateContextID() throws CSErrorException {
    AContextID aContextID = new AContextID();
    aContextID.setContextId("84695");
    aContextID.setUser("hadoop");
    aContextID.setExpireTime(new Date());
    aContextID.setExpireType(ExpireType.TODAY);
    aContextID.setInstance("updateInstance");
    aContextID.setBackupInstance("updatebackup");
    aContextID.setApplication("hive");
    contextIDPersistence.updateContextID(aContextID);
  }
}
