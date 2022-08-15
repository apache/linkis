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

import org.apache.linkis.entrance.conf.EntranceConfiguration

/**
  * Description: 用于websocket方式向前推送日志的
  */
class WebSocketCacheLogReader(logPath:String ,
                              charset:String,
                              sharedCache: Cache,
                              user: String)
  extends CacheLogReader(logPath, EntranceConfiguration.DEFAULT_LOG_CHARSET.getValue,sharedCache, user){

  private var fromLine:Int = 0
  private val size = 100

  override protected def readLog(deal: String => Unit, fL: Int, sz: Int): Int = {

    val readSize:Int = super.readLog(deal, fromLine, size)
    fromLine = fromLine + readSize
    readSize
//    if (! sharedCache.cachedLogs.nonEmpty) return 0
//    val min = sharedCache.cachedLogs.min
//    val max = sharedCache.cachedLogs.max
//    if(fromLine > max) return 0
//    var from = fromLine
//    val to = if(fromLine >= min) {
//      if(size >= 0 && max >= fromLine + size) fromLine + size else max + 1
//    } else {
//      return -1
//    }
//    (from until to) map sharedCache.cachedLogs.get foreach deal
//    fromLine = fromLine + to - from
//    to - from
  }
}
