
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

package org.apache.linkis.manager.exception; 
 
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 
 * PersistenceWarnException Tester
*/ 
public class PersistenceExceptionTest {
 
    @BeforeEach
    @DisplayName("Each unit test method is executed once before execution")
    public void before() throws Exception {
    }
 
    @AfterEach
    @DisplayName("Each unit test method is executed once before execution")
    public void after() throws Exception {
    }

    @Test
    public void testNodeInstanceDuplicateException(){
        NodeInstanceDuplicateException ne = new NodeInstanceDuplicateException(41001, "Node实例已存在");
        assertEquals(41001, ne.getErrCode());
        assertEquals("Node实例已存在", ne.getDesc());
    }

    @Test
    public void testNodeInstanceNotFoundException(){
        NodeInstanceNotFoundException ne = new NodeInstanceNotFoundException(41002, "Node实例不存在");
        assertEquals(41002, ne.getErrCode());
        assertEquals("Node实例不存在", ne.getDesc());
    }

    @Test
    public void testPersistenceErrorException(){
        PersistenceErrorException pe = new PersistenceErrorException(410002, " The emNode is null test instance");
        assertEquals(410002, pe.getErrCode());
        assertEquals(" The emNode is null test instance", pe.getDesc());
        PersistenceErrorException pe1 = new PersistenceErrorException(410002, " The emNode is null test instance", new RuntimeException());
        assertEquals(410002, pe1.getErrCode());
        assertEquals(" The emNode is null test instance", pe1.getDesc());
    }

    @Test
    public void testPersistenceWarnException(){
        String msg = "beanutils populate failed";
        PersistenceWarnException pe0 = new PersistenceWarnException(10000, msg);
        assertEquals(10000, pe0.getErrCode());
        assertEquals(msg, pe0.getDesc());

        PersistenceWarnException pe = new PersistenceWarnException(10000, msg, new RuntimeException());
        assertEquals(10000, pe.getErrCode());
        assertEquals(msg, pe.getDesc());
    }

} 
