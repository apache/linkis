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

package org.apache.linkis.monitor.bml.cleaner.service;

import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.monitor.bml.cleaner.entity.CleanedResourceVersion;
import org.apache.linkis.storage.fs.FileSystem;

import java.io.IOException;

public interface VersionService {

  void doMove(
      FileSystem fs,
      FsPath srcPath,
      FsPath destPath,
      CleanedResourceVersion insertVersion,
      long delVersionId)
      throws IOException;

  void moveOnDb(CleanedResourceVersion insertVersion, long delVersionId);
}
