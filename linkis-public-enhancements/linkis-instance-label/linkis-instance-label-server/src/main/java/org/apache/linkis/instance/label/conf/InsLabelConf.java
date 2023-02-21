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

package org.apache.linkis.instance.label.conf;

import org.apache.linkis.common.conf.CommonVars;

public class InsLabelConf {

  /** Limit the size of batch operation */
  public static final CommonVars<Integer> DB_PERSIST_BATCH_SIZE =
      CommonVars.apply("wds.linkis.instance.label.persist.batch.size", 100);

  /** Capacity of async queue */
  public static final CommonVars<Integer> ASYNC_QUEUE_CAPACITY =
      CommonVars.apply("wds.linkis.instance.label.async.queue.capacity", 1000);

  public static final CommonVars<Integer> ASYNC_QUEUE_CONSUME_BATCH_SIZE =
      CommonVars.apply("wds.linkis.instance.label.async.queue.batch.size", 100);

  /** Interval of consuming period */
  public static final CommonVars<Long> ASYNC_QUEUE_CONSUME_INTERVAL =
      CommonVars.apply("wds.linkis.instance.label.async.queue.interval-in-seconds", 10L);

  /** Expire time of cache */
  public static final CommonVars<Integer> CACHE_EXPIRE_TIME =
      CommonVars.apply("wds.linkis.instance.label.cache.expire.time-in-seconds", 10);

  public static final CommonVars<Integer> CACHE_MAX_SIZE =
      CommonVars.apply("wds.linkis.instance.label.cache.maximum.size", 1000);

  public static final CommonVars<String> CACHE_NAMES =
      CommonVars.apply("wds.linkis.instance.label.cache.names", "instance,label,appInstance");

  public static final CommonVars<String> SERVICE_REGISTRY_ADDRESS =
      CommonVars.apply("linkis.discovery.server-address", "http://localhost:20303");
}
