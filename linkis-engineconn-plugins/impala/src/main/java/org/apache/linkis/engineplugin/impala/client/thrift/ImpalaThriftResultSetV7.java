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

package org.apache.linkis.engineplugin.impala.client.thrift;

import org.apache.linkis.engineplugin.impala.client.ImpalaResultSet;
import org.apache.linkis.engineplugin.impala.client.util.ThriftUtil;

import org.apache.hive.service.rpc.thrift.TBinaryColumn;
import org.apache.hive.service.rpc.thrift.TBoolColumn;
import org.apache.hive.service.rpc.thrift.TByteColumn;
import org.apache.hive.service.rpc.thrift.TColumn;
import org.apache.hive.service.rpc.thrift.TColumnDesc;
import org.apache.hive.service.rpc.thrift.TDoubleColumn;
import org.apache.hive.service.rpc.thrift.TFetchResultsReq;
import org.apache.hive.service.rpc.thrift.TFetchResultsResp;
import org.apache.hive.service.rpc.thrift.TGetResultSetMetadataReq;
import org.apache.hive.service.rpc.thrift.TGetResultSetMetadataResp;
import org.apache.hive.service.rpc.thrift.TI16Column;
import org.apache.hive.service.rpc.thrift.TI32Column;
import org.apache.hive.service.rpc.thrift.TI64Column;
import org.apache.hive.service.rpc.thrift.TOperationHandle;
import org.apache.hive.service.rpc.thrift.TRowSet;
import org.apache.hive.service.rpc.thrift.TStatus;
import org.apache.hive.service.rpc.thrift.TStatusCode;
import org.apache.hive.service.rpc.thrift.TStringColumn;
import org.apache.hive.service.rpc.thrift.TTableSchema;
import org.apache.hive.service.rpc.thrift.TTypeDesc;
import org.apache.impala.thrift.ImpalaHiveServer2Service;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ImpalaThriftResultSetV7 implements ImpalaResultSet {

  /*
   * bit mask, used to mask the byte value for in position bit
   */
  private static final byte[] bitMask = {
    (byte) 0x01,
    (byte) 0x02,
    (byte) 0x04,
    (byte) 0x08,
    (byte) 0x10,
    (byte) 0x20,
    (byte) 0x40,
    (byte) 0x80
  };

  private final ImpalaHiveServer2Service.Client client;
  private final TOperationHandle operation;
  private final TFetchResultsReq fetchReq;

  private volatile int rowIndex;
  private volatile int cols;
  private volatile boolean closed;

  private volatile boolean hasMoreRow;

  private volatile Iterator<?>[] iterators = null;
  private volatile byte[][] nulls = null;
  private volatile int[] lengths = null;
  private volatile List<Column> columns = null;

  public ImpalaThriftResultSetV7(
      ImpalaHiveServer2Service.Client client, TOperationHandle operation, int batchSize) {
    this.client = client;
    this.operation = operation;

    this.rowIndex = -1;
    this.cols = 0;
    this.fetchReq = new TFetchResultsReq();
    this.fetchReq.setMaxRows(batchSize);
    this.fetchReq.setOperationHandle(operation);
    this.hasMoreRow = true;
  }

  private void unpackColumn(TColumn column, int index) {
    Iterator<?> iterator = null;
    byte[] nulls = null;
    int length = 0;
    switch (column.getSetField()) {
      case BINARY_VAL:
        TBinaryColumn binVal = column.getBinaryVal();
        iterator = binVal.getValuesIterator();
        nulls = binVal.getNulls();
        binVal.getValuesSize();
        length = binVal.getValuesSize();
        break;
      case BOOL_VAL:
        TBoolColumn boolVal = column.getBoolVal();
        iterator = boolVal.getValuesIterator();
        nulls = boolVal.getNulls();
        length = boolVal.getValuesSize();
        break;
      case BYTE_VAL:
        TByteColumn byteVal = column.getByteVal();
        iterator = byteVal.getValuesIterator();
        nulls = byteVal.getNulls();
        length = byteVal.getValuesSize();
        break;
      case DOUBLE_VAL:
        TDoubleColumn doubleVal = column.getDoubleVal();
        iterator = doubleVal.getValuesIterator();
        nulls = doubleVal.getNulls();
        length = doubleVal.getValuesSize();
        break;
      case I16_VAL:
        TI16Column i16Val = column.getI16Val();
        iterator = i16Val.getValuesIterator();
        nulls = i16Val.getNulls();
        length = i16Val.getValuesSize();
        break;
      case I32_VAL:
        TI32Column i32Val = column.getI32Val();
        iterator = i32Val.getValuesIterator();
        nulls = i32Val.getNulls();
        length = i32Val.getValuesSize();
        break;
      case I64_VAL:
        TI64Column i64Val = column.getI64Val();
        iterator = i64Val.getValuesIterator();
        nulls = i64Val.getNulls();
        length = i64Val.getValuesSize();
        break;
      case STRING_VAL:
        TStringColumn stringVal = column.getStringVal();
        iterator = stringVal.getValuesIterator();
        nulls = stringVal.getNulls();
        length = stringVal.getValuesSize();
        break;
    }

    this.iterators[index] = iterator;
    this.nulls[index] = nulls;
    this.lengths[index] = length;
  }

  /** fetch data from remote server */
  private void fetch() {
    try {
      hasMoreRow = false;
      iterators = null;
      nulls = null;
      lengths = null;

      do {
        /*
         * request data
         */
        TFetchResultsResp resp = client.FetchResults(fetchReq);
        TStatus status = resp.getStatus();

        /*
         * request error
         */
        if (status.getStatusCode().getValue() > TStatusCode.SUCCESS_WITH_INFO_STATUS.getValue()) {
          throw new RuntimeException("Failed to fetch result(s). " + status);
        }

        hasMoreRow = resp.isHasMoreRows();

        TRowSet result = resp.getResults();
        List<TColumn> list = result.getColumns();
        if (list != null) {
          rowIndex = -1;

          if (iterators == null) {
            cols = list.size();
            iterators = new Iterator[cols];
            /*
             * nulls matrix, indicate which [cols][row] is null value
             */
            nulls = new byte[cols][];
            lengths = new int[cols];
          }

          /*
           * 填充缓冲区
           */
          int i = 0;
          for (TColumn item : list) {
            unpackColumn(item, i);
            ++i;
          }
        }

        if (lengths != null && lengths[0] > 0) {
          break;
        }
      } while (hasMoreRow);

    } catch (TException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public synchronized Row next() {
    if (closed) {
      return null;
    }

    /*
     * fetch remote data
     */
    if ((iterators == null || !iterators[0].hasNext()) && hasMoreRow) {
      fetch();
    }

    /*
     * build local row
     */
    if (iterators != null && iterators[0].hasNext()) {
      ++rowIndex;
      Object[] values = new Object[iterators.length];
      for (int i = 0; i < iterators.length; ++i) {
        values[i] = iterators[i].next();

        /* use nulls matrix for recognize null value */
        if ((nulls[i][rowIndex / 8] & bitMask[rowIndex % 8]) != 0) {
          values[i] = null;
        }
      }
      return new Row(values);
    }

    close();
    return null;
  }

  @Override
  public int getColumnSize() {
    return getColumns().size();
  }

  @Override
  public synchronized List<Column> getColumns() {
    if (columns == null) {
      try {
        TGetResultSetMetadataReq metadataReq = new TGetResultSetMetadataReq(operation);
        TGetResultSetMetadataResp metadataResp = client.GetResultSetMetadata(metadataReq);

        ThriftUtil.checkStatus(metadataResp.getStatus());

        TTableSchema tableSchema = metadataResp.getSchema();

        if (tableSchema != null) {
          List<TColumnDesc> columnList = tableSchema.getColumns();
          List<Column> columns = new ArrayList<>(columnList.size());

          int index = 0;
          for (TColumnDesc tColumnDesc : columnList) {
            columns.add(
                new Column(++index, tColumnDesc.getColumnName(), tColumnDesc.getTypeDesc()));
          }
          this.columns = Collections.unmodifiableList(columns);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      if (columns == null) {
        this.columns = Collections.emptyList();
      }
    }
    return columns;
  }

  @Override
  public void close() {
    this.closed = true;
  }

  public static class Column implements ImpalaResultSet.Column {
    private final int index;
    private final String name;
    private final TTypeDesc type;

    protected Column(int index, String name, TTypeDesc type) {
      this.index = index;
      this.name = name;
      this.type = type;
    }

    @Override
    public int getIndex() {
      return index;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public TTypeDesc getType() {
      return type;
    }
  }

  public static class Row implements ImpalaResultSet.Row {
    private final Object[] values;

    protected Row(Object[] values) {
      this.values = values;
    }

    @Override
    public Object[] getValues() {
      return values;
    }

    @Override
    public Object getObject(int columnIndex) {
      return values[columnIndex - 1];
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) {
      return type.cast(values[columnIndex - 1]);
    }

    @Override
    public String getString(int columnIndex) {
      return (String) values[columnIndex - 1];
    }

    @Override
    public Short getShort(int columnIndex) {
      return (Short) values[columnIndex - 1];
    }

    @Override
    public Integer getInteger(int columnIndex) {
      return (Integer) values[columnIndex - 1];
    }

    @Override
    public Long getLong(int columnIndex) {
      return (Long) values[columnIndex - 1];
    }
  }
}
