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

package org.apache.linkis.storage.csv;

import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;
import org.apache.linkis.storage.domain.Column;
import org.apache.linkis.storage.domain.DataType;
import org.apache.linkis.storage.resultset.table.TableMetaData;
import org.apache.linkis.storage.resultset.table.TableRecord;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageCSVWriter extends CSVFsWriter {
  private static final Logger logger = LoggerFactory.getLogger(StorageCSVWriter.class);

  private final String charset;
  private final String separator;
  private final boolean quoteRetouchEnable;
  private final OutputStream outputStream;

  private final String delimiter;
  private final StringBuilder buffer;

  public StorageCSVWriter(
      String charset, String separator, boolean quoteRetouchEnable, OutputStream outputStream) {
    this.charset = charset;
    this.separator = separator;
    this.quoteRetouchEnable = quoteRetouchEnable;
    this.outputStream = outputStream;

    if (StringUtils.isBlank(separator)) {
      this.delimiter = "\t";
    } else {
      switch (separator) {
        case "t":
          this.delimiter = "\t";
          break;
        default:
          this.delimiter = separator;
          break;
      }
    }
    this.buffer = new StringBuilder(50000);
  }

  @Override
  public String getCharset() {
    return charset;
  }

  @Override
  public String getSeparator() {
    return separator;
  }

  @Override
  public boolean isQuoteRetouchEnable() {
    return quoteRetouchEnable;
  }

  @Override
  public void addMetaData(MetaData metaData) throws IOException {
    Column[] columns = ((TableMetaData) metaData).getColumns();
    String[] head = Stream.of(columns).map(Column::getColumnName).toArray(String[]::new);
    write(head);
  }

  private String compact(String[] row) {
    String quotationMarks = "\"";
    StringBuilder rowBuilder = new StringBuilder();
    for (String value : row) {
      String decoratedValue =
          StringUtils.isBlank(value)
              ? value
              : quoteRetouchEnable
                  ? quotationMarks + value.replaceAll(quotationMarks, "") + quotationMarks
                  : value;
      rowBuilder.append(decoratedValue).append(delimiter);
    }
    if (rowBuilder.length() > 0 && rowBuilder.toString().endsWith(delimiter)) {
      int index = rowBuilder.lastIndexOf(delimiter);
      rowBuilder.delete(index, index + delimiter.length());
    }
    rowBuilder.append("\n");
    if (logger.isDebugEnabled()) {
      logger.debug("delimiter:" + delimiter);
    }
    return rowBuilder.toString();
  }

  private void write(String[] row) throws IOException {
    String content = compact(row);
    if (buffer.length() + content.length() > 49500) {
      IOUtils.write(buffer.toString().getBytes(charset), outputStream);
      buffer.setLength(0);
    }
    buffer.append(content);
  }

  @Override
  public void addRecord(Record record) throws IOException {
    Object[] rows = ((TableRecord) record).row;
    String[] body =
        Stream.of(rows).map(dataType -> DataType.valueToString(dataType)).toArray(String[]::new);
    write(body);
  }

  @Override
  public void flush() throws IOException {
    IOUtils.write(buffer.toString().getBytes(charset), outputStream);
    buffer.setLength(0);
  }

  @Override
  public void close() throws IOException {
    flush();
    IOUtils.closeQuietly(outputStream);
  }
}
