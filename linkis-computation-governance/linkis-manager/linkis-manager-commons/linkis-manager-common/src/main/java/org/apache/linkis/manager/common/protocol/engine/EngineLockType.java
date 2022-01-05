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
 
package org.apache.linkis.manager.common.protocol.engine;


public enum EngineLockType {

    /**
     * Always - 除非主动释放，否则不释放 release unless one release positively
     * Once - 加锁后访问一次便释放，且超时也会释放 released when visited at once
     * Timed - 加锁后，超时或主动释放都会释放  released when timeout
     */
    Always,
    Once,
    Timed

}
