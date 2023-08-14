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

package org.apache.linkis.storage.source;

import org.apache.linkis.common.io.Fs;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.storage.FSFactory;
import org.apache.linkis.storage.csv.CSVFsWriter;

import java.io.IOException;
import java.io.OutputStream;

import org.junit.jupiter.api.Test;

class ResultsetFileSourceTest {

  @Test
  public void testWriter() throws IOException {
    String filePath = this.getClass().getResource("/result-read-test.dolphin").getFile().toString();
    FsPath sourceFsPath = new FsPath(filePath);
    Fs sourceFs = FSFactory.getFs(sourceFsPath);
    sourceFs.init(null);

    FsPath destFsPath = new FsPath(filePath + ".result");
    Fs destFs = FSFactory.getFs(destFsPath);
    destFs.init(null);
    OutputStream outputStream = destFs.write(destFsPath, true);

    CSVFsWriter cSVFsWriter = CSVFsWriter.getCSVFSWriter("UTF-8", ",", false, outputStream);
    FileSource fileSource = FileSource.create(sourceFsPath, sourceFs);
    fileSource.addParams("nullValue", "NULL").write(cSVFsWriter);

    cSVFsWriter.close();
  }
}
