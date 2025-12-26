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

package org.apache.linkis.engineconnplugin.flink.resource

import org.apache.linkis.common.utils.Logging

import java.io.File

/**
 * Cleaner for local resource
 */
class FlinkJobLocalResourceCleaner extends FlinkJobResourceCleaner with Logging {

  /**
   * Clean up path array
   *
   * @param resArray
   *   resource array
   */
  override def cleanup(resArray: Array[String]): Unit = {
    Option(resArray).foreach(paths =>
      paths.foreach(path => {
        Option(getClass.getClassLoader.getResource(path)) match {
          case Some(url) =>
            val localFile = new File(url.getPath)
            if (localFile.exists()) {
              logger.info(s"Clean the resource: [${localFile.getPath}]")
              localFile.delete()
            }
          case _ =>
        }

      })
    )
  }

  /**
   * If accept the path
   *
   * @param resource
   *   path
   * @return
   */
  override def accept(resource: String): Boolean = {
    Option(getClass.getClassLoader.getResource(resource)) match {
      case Some(url) =>
        val file = new File(url.getPath)
        // Avoid the linked file/directory
        file.getCanonicalFile.equals(file.getAbsoluteFile)
      case _ => false
    }
  }

}
