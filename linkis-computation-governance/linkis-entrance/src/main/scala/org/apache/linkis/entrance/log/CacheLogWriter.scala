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
 
package org.apache.linkis.entrance.log

import org.apache.commons.lang.StringUtils

class CacheLogWriter(logPath:String,
                     charset:String,
                     sharedCache:Cache,
                     user: String)
  extends AbstractLogWriter(logPath, user, charset) {

  def getCache:Option[Cache] = Some(sharedCache)

  private def cache(msg:String): Unit = {
    this synchronized {
      val removed = sharedCache.cachedLogs.add(msg)
      if (removed != null){
        val logs = sharedCache.cachedLogs.toList
        val sb = new StringBuilder
        sb.append(removed).append("\n")
        logs.filter(_ != null).foreach(log => sb.append(log).append("\n"))
        sharedCache.cachedLogs.fakeClear()
        super.write(sb.toString())
      }
    }
  }
  override def write(msg: String): Unit = {
    if (StringUtils.isBlank(msg)){
      cache("")
    }else{
      val rows = msg.split("\n")
      rows.foreach(row => {
        if (row == null) cache("") else cache(row)
      })
    }
  }

  override def flush(): Unit = {
    sharedCache.cachedLogs.toList.filter(StringUtils.isNotEmpty).foreach(super.write)
    sharedCache.cachedLogs.clear()
    super.flush()
  }

}
