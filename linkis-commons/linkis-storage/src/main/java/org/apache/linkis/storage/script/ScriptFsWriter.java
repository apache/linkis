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

package org.apache.linkis.storage.script;

import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.common.io.FsWriter;
import org.apache.linkis.storage.script.writer.StorageScriptFsWriter;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class ScriptFsWriter extends FsWriter {
  FsPath path;
  String charset;

  public abstract InputStream getInputStream();

  public static ScriptFsWriter getScriptFsWriter(
      FsPath path, String charset, OutputStream outputStream) {
    return new StorageScriptFsWriter(path, charset, outputStream);
  }
}
