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
 
package org.apache.linkis.engineconn.acessible.executor.log;


import java.util.List;

public interface LogCache {
    /**
     * Store logs in the cache for subsequent delivery
     * 将日志存储到缓存当中，用于后续的发送
     *
     * @param log 日志信息
     */
    void cacheLog(String log);

    /**
     * Get the log of the number of num（获取num数目的日志）
     *
     * @param num The number of logs you want to get（希望获取的日志的数量）
     * @return Log List Collection（日志List集合）
     */
    List<String> getLog(int num);

    /**
     * Take all the logs in the log cache for the engine to take out all the logs after performing a task
     * 将日志缓存中的所有日志都取出，用于engine执行完一个任务之后将日志全部取出
     *
     * @return
     */
    List<String> getRemain();

    int size();
}
