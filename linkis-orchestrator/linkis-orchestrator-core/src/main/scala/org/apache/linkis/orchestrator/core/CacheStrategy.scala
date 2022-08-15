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

package org.apache.linkis.orchestrator.core

/**
 */
trait CacheStrategy {

  val scope: Int = 0 // TODO: enum: ONLY_SESSION, ALL_SESSIONS

  val cacheLevel: Int = 0 // TODO: enum: CS_CACHE, CS_AND_ENGINE_TYPES_CACHE

  val expireTime: Long = -1 // never expired by default

}

object CacheStrategy {

  val ONLY_SESSION_AND_CS_TERM_CACHE = new CacheStrategy {}

}
