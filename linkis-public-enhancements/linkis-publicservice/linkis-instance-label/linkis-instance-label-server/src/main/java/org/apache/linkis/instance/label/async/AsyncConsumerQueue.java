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
 
package org.apache.linkis.instance.label.async;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public interface AsyncConsumerQueue<T> {

    /**
     * Size of queue
     * @return value
     */
    int size();

    /**
     * Inserts the specified entity
     * @param entity entity
     * @return if the queue is full, return false
     */
    boolean offer(T entity);

    /**
     * Inserts the specified entity
     * @param entity entity
     * @param timeout timeout
     * @param timeUnit unit
     * @return if the queue is full, return false
     */
    boolean offer(T entity, long timeout, TimeUnit timeUnit) throws InterruptedException;

    /**
     * Define the consumer
     * @param batchSize batch size for each consuming
     * @param interval consume interval
     * @param timeUnit time unit of interval
     * @param consumer consume function
     */
    void consumer(int batchSize, long interval, TimeUnit timeUnit, Consumer<List<T>> consumer);

}
