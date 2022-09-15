
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

package org.apache.linkis.manager.common.exception; 
 
import org.apache.linkis.common.exception.ExceptionLevel;
import org.apache.linkis.common.exception.WarnException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 
 * ResourceWarnException Tester
*/ 
public class ResourceWarnExceptionTest { 

 
    @BeforeEach
    @DisplayName("Each unit test method is executed once before execution")
    public void before() throws Exception {
    }
 
    @AfterEach
    @DisplayName("Each unit test method is executed once before execution")
    public void after() throws Exception {
    }
 
 
    @Test
    public void testResourceWarnException() throws Exception {
        String msg = "not supported resource result policy ";
        ResourceWarnException resourceWarnException = new ResourceWarnException(11003, msg);
        assertEquals(11003, resourceWarnException.getErrCode());
        assertEquals(msg, resourceWarnException.getDesc());

        ResourceWarnException warnException2 = new ResourceWarnException(3, "test", "127.0.0.1", 1234, "serviceKind");
        assertEquals(ExceptionLevel.WARN, warnException2.getLevel());
        assertEquals("test", warnException2.getDesc());
        assertEquals("127.0.0.1", warnException2.getIp());
        assertEquals(1234, warnException2.getPort());
        assertEquals("serviceKind", warnException2.getServiceKind());

    }

    @Test
    public void testRMErrorException() throws Exception {
        String msg = "only admin can reset user's resource.";
        RMErrorException rmErrorException = new RMErrorException(120011, msg);
        assertEquals(120011, rmErrorException.getErrCode());
        assertEquals(msg, rmErrorException.getDesc());

        msg = "No suitable ExternalResourceProvider found for cluster: test";
        RMErrorException rmErrorException1 = new RMErrorException(
                110013,
                msg, new RuntimeException());
        assertEquals(110013, rmErrorException1.getErrCode());
        assertEquals(msg, rmErrorException1.getDesc());

        RMErrorException warnException2 = new RMErrorException(3, "test", "127.0.0.1", 1234, "serviceKind");
        assertEquals(ExceptionLevel.ERROR, warnException2.getLevel());
        assertEquals("test", warnException2.getDesc());
        assertEquals("127.0.0.1", warnException2.getIp());
        assertEquals(1234, warnException2.getPort());
        assertEquals("serviceKind", warnException2.getServiceKind());

    }

    @Test
    public void testRMFatalException() throws Exception {
        String msg = "testRMFatalException";
        RMFatalException rmFatalException = new RMFatalException(120011, msg);
        assertEquals(120011, rmFatalException.getErrCode());
        assertEquals(msg, rmFatalException.getDesc());

        RMFatalException rmFatalException1 = new RMFatalException(3, "test", "127.0.0.1", 1234, "serviceKind");
        assertEquals(ExceptionLevel.FATAL, rmFatalException1.getLevel());
        assertEquals("test", rmFatalException1.getDesc());
        assertEquals("127.0.0.1", rmFatalException1.getIp());
        assertEquals(1234, rmFatalException1.getPort());
        assertEquals("serviceKind", rmFatalException1.getServiceKind());

    }

    @Test
    public void testRMWarnException() throws Exception {
        String msg = "testRMWarnException";
        RMWarnException rmWarnException = new RMWarnException(11006, msg);
        assertEquals(11006, rmWarnException.getErrCode());
        assertEquals(msg, rmWarnException.getDesc());

        RMWarnException rmWarnException1 = new RMWarnException(3, "test", "127.0.0.1", 1234, "serviceKind");
        assertEquals(ExceptionLevel.WARN, rmWarnException1.getLevel());
        assertEquals("test", rmWarnException1.getDesc());
        assertEquals("127.0.0.1", rmWarnException1.getIp());
        assertEquals(1234, rmWarnException1.getPort());
        assertEquals("serviceKind", rmWarnException1.getServiceKind());

    }


} 
