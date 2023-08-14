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

package org.apache.linkis.storage.script.writer;

import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;
import org.apache.linkis.storage.LineRecord;
import org.apache.linkis.storage.script.Compaction;
import org.apache.linkis.storage.script.ScriptFsWriter;
import org.apache.linkis.storage.script.ScriptMetaData;
import org.apache.linkis.storage.script.Variable;
import org.apache.linkis.storage.utils.StorageConfiguration;
import org.apache.linkis.storage.utils.StorageUtils;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.hdfs.client.HdfsDataOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageScriptFsWriter extends ScriptFsWriter {
  private static final Logger log = LoggerFactory.getLogger(StorageScriptFsWriter.class);

  private final FsPath path;
  private final String charset;
  private final OutputStream outputStream;
  private final StringBuilder stringBuilder = new StringBuilder();

  public StorageScriptFsWriter(FsPath path, String charset, OutputStream outputStream) {
    this.path = path;
    this.charset = charset;
    this.outputStream = outputStream;
  }

  @Override
  public void addMetaData(MetaData metaData) throws IOException {
    String suffix = StorageUtils.pathToSuffix(path.getPath());
    List<Compaction> compactions =
        Stream.of(Compaction.listCompactions())
            .filter(compaction -> compaction.belongTo(suffix))
            .collect(Collectors.toList());
    List<String> metadataLine = new ArrayList<>();
    if (!compactions.isEmpty()) {
      Variable[] metaData1 = ((ScriptMetaData) metaData).getMetaData();
      Stream.of(metaData1).map(compactions.get(0)::compact).forEach(metadataLine::add);

      // add annotition symbol
      if (metadataLine.size() > 0) {
        metadataLine.add(compactions.get(0).getAnnotationSymbol());
      }
      if (outputStream != null) {
        IOUtils.writeLines(metadataLine, "\n", outputStream, charset);
      } else {
        metadataLine.forEach(m -> stringBuilder.append(m).append("\n"));
      }
    }
  }

  @Override
  public void addRecord(Record record) throws IOException {
    LineRecord scriptRecord = (LineRecord) record;
    if (outputStream != null) {
      IOUtils.write(scriptRecord.getLine(), outputStream, charset);
    } else {
      stringBuilder.append(scriptRecord.getLine());
    }
  }

  @Override
  public void close() {
    IOUtils.closeQuietly(outputStream);
  }

  @Override
  public void flush() {
    if (outputStream instanceof HdfsDataOutputStream) {
      try {
        ((HdfsDataOutputStream) outputStream).hflush();
      } catch (IOException t) {
        log.warn("Error encountered when flush script", t);
      }
    } else if (outputStream != null) {
      try {
        outputStream.flush();
      } catch (IOException t) {
        log.warn("Error encountered when flush script", t);
      }
    }
  }

  @Override
  public InputStream getInputStream() {
    byte[] bytes = null;
    try {
      bytes =
          stringBuilder.toString().getBytes(StorageConfiguration.STORAGE_RS_FILE_TYPE.getValue());
    } catch (UnsupportedEncodingException e) {
      log.warn("StorageScriptFsWriter getInputStream failed", e);
    }
    return new ByteArrayInputStream(bytes);
  }
}
