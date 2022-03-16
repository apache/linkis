
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

package org.apache.linkis.common.errorcode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.AccessControlException;
import java.security.Permission;

/**
 * ErrorCodeUtils Tester
 */
public class ErrorCodeUtilsTest {

    @Autowired
    private ErrorCodeUtils errorCodeUtils;

    @BeforeEach
    @DisplayName("Each unit test method is executed once before execution")
    public void before() throws Exception {
        final SecurityManager securityManager = new SecurityManager() {
            public void checkPermission(Permission permission) {
                if (permission.getName().startsWith("exitVM")) {
                    throw new AccessControlException("");
                }
            }
        };
        System.setSecurityManager(securityManager);

    }

    @AfterEach
    @DisplayName("Each unit test method is executed once before execution")
    public void after() throws Exception {
    }


    @Test
    @DisplayName("Method description: ...")
    public void testValidateErrorCode() throws Exception {
        boolean isExited = false;
        try {
            ErrorCodeUtils.validateErrorCode(0,1,2);
        } catch (RuntimeException e) {
            isExited = true;
        }
        Assertions.assertThat(isExited).isEqualTo(true);
        isExited = false;
        try {
            ErrorCodeUtils.validateErrorCode(10000,1,2);
        } catch (RuntimeException e) {
            isExited = true;
        }
        Assertions.assertThat(isExited).isEqualTo(true);
        isExited = false;
        try {
            ErrorCodeUtils.validateErrorCode(20000,20000,24999);
        } catch (RuntimeException e) {
            isExited = true;
        } finally {
            System.setSecurityManager(null);
        }
        Assertions.assertThat(isExited).isEqualTo(false);

    }

} 
