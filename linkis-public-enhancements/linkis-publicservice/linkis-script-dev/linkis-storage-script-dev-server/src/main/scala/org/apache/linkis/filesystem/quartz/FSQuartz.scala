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
 
package org.apache.linkis.filesystem.quartz

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.filesystem.cache.FsCache
import org.quartz.JobExecutionContext
import org.springframework.scheduling.quartz.QuartzJobBean

import scala.collection.JavaConversions._

class FSQuartz extends QuartzJobBean with Logging{
  override def executeInternal(jobExecutionContext: JobExecutionContext): Unit = {
    info("closing fs...")
    FsCache.fsInfo.filter(_._2.exists(_.timeout)).foreach{
      case (_,list) => list synchronized list.filter(_.timeout).foreach{
        f =>{
          info(f.id + "---" +f.fs.fsName()+"---"+ f.lastAccessTime)
          try {
            f.fs.close()
          }catch {
            case e: Exception =>
              info("Requesting IO-Engine call close failed! But still have to continue to clean up the expired fs!", e)
          }
          list -= f
        }
      }
    }
  }
}
