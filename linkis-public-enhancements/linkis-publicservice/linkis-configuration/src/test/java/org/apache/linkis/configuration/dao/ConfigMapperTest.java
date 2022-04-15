package org.apache.linkis.configuration.dao;

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

import org.h2.tools.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

public class ConfigMapperTest extends BaseDaoTest{
    @Autowired
    ConfigMapper configMapper;

    @BeforeAll
    @DisplayName("Each unit test method is executed once before execution")
    protected static void beforeAll() throws Exception {
        // Start the console of h2 to facilitate viewing of h2 data
        Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
    }

    @AfterAll
    @DisplayName("Each unit test method is executed once before execution")
    protected static void afterAll() throws Exception {}



}
