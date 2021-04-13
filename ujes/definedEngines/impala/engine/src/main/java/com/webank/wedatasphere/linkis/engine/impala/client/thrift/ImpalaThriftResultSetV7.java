/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.engine.impala.client.thrift;

import com.google.common.collect.Lists;
import com.webank.wedatasphere.linkis.engine.impala.client.ImpalaResultSet;
import com.webank.wedatasphere.linkis.engine.impala.client.util.ImpalaThriftUtil;
import org.apache.hive.service.cli.thrift.TBinaryColumn;
import org.apache.hive.service.cli.thrift.TBoolColumn;
import org.apache.hive.service.cli.thrift.TByteColumn;
import org.apache.hive.service.cli.thrift.TColumn;
import org.apache.hive.service.cli.thrift.TColumnDesc;
import org.apache.hive.service.cli.thrift.TDoubleColumn;
import org.apache.hive.service.cli.thrift.TFetchResultsReq;
import org.apache.hive.service.cli.thrift.TFetchResultsResp;
import org.apache.hive.service.cli.thrift.TGetResultSetMetadataReq;
import org.apache.hive.service.cli.thrift.TGetResultSetMetadataResp;
import org.apache.hive.service.cli.thrift.TI16Column;
import org.apache.hive.service.cli.thrift.TI32Column;
import org.apache.hive.service.cli.thrift.TI64Column;
import org.apache.hive.service.cli.thrift.TOperationHandle;
import org.apache.hive.service.cli.thrift.TRowSet;
import org.apache.hive.service.cli.thrift.TStatus;
import org.apache.hive.service.cli.thrift.TStatusCode;
import org.apache.hive.service.cli.thrift.TStringColumn;
import org.apache.hive.service.cli.thrift.TTableSchema;
import org.apache.impala.thrift.ImpalaHiveServer2Service;
import org.apache.thrift.TException;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

public class ImpalaThriftResultSetV7 implements ImpalaResultSet {
    private static final byte[] bitMask = {(byte) 0x01, (byte) 0x02, (byte) 0x04, (byte) 0x08, (byte) 0x10,
            (byte) 0x20, (byte) 0x40, (byte) 0x80};

    private ImpalaHiveServer2Service.Client client;
    private TOperationHandle operation;
    private TFetchResultsReq fetchReq;

    private int index;
    private int size;
    private boolean closed;

    /*
     * 默认远端有数据
     */
    private boolean hasMoreRow = true;

    /*
     * 保存数据
     */
    private Iterator<?>[] iterators = null;
    private byte[][] nulls = null;
    private Object[] values = null;
    private Class<?>[] types = null;
    private List<String> columns = null;

    public ImpalaThriftResultSetV7(ImpalaHiveServer2Service.Client client, TOperationHandle operation, int batchSize) {
        this.client = client;
        this.operation = operation;

        this.index = -1;
        this.size = 0;
        this.fetchReq = new TFetchResultsReq();
        this.fetchReq.setMaxRows(batchSize);
        this.fetchReq.setOperationHandle(operation);
    }

    /**
     * 解析字段
     *
     * @param column 字段
     * @param index  顺序
     */
    private void unpackColumn(TColumn column, int index) {
        Iterator<?> result = null;
        byte[] nls = null;
        Class<?> cls = null;
        switch (column.getSetField()) {
            case BINARY_VAL:
                TBinaryColumn binVal = column.getBinaryVal();
                result = binVal.getValuesIterator();
                nls = binVal.getNulls();
                cls = ByteBuffer.class;
                break;
            case BOOL_VAL:
                TBoolColumn boolVal = column.getBoolVal();
                result = boolVal.getValuesIterator();
                nls = boolVal.getNulls();
                cls = Boolean.class;
                break;
            case BYTE_VAL:
                TByteColumn byteVal = column.getByteVal();
                result = byteVal.getValuesIterator();
                nls = byteVal.getNulls();
                cls = Byte.class;
                break;
            case DOUBLE_VAL:
                TDoubleColumn doubleVal = column.getDoubleVal();
                result = doubleVal.getValuesIterator();
                nls = doubleVal.getNulls();
                cls = Double.class;
                break;
            case I16_VAL:
                TI16Column i16Val = column.getI16Val();
                result = i16Val.getValuesIterator();
                nls = i16Val.getNulls();
                cls = Short.class;
                break;
            case I32_VAL:
                TI32Column i32Val = column.getI32Val();
                result = i32Val.getValuesIterator();
                nls = i32Val.getNulls();
                cls = Integer.class;
                break;
            case I64_VAL:
                TI64Column i64Val = column.getI64Val();
                result = i64Val.getValuesIterator();
                nls = i64Val.getNulls();
                cls = Long.class;
                break;
            case STRING_VAL:
                TStringColumn stringVal = column.getStringVal();
                result = stringVal.getValuesIterator();
                nls = stringVal.getNulls();
                cls = String.class;
                break;
        }
        iterators[index] = result;
        nulls[index] = nls;
        types[index] = cls;
    }

    /**
     * 从远端请求数据
     */
    private void fetch() {
        try {
            /*
             * 初始化
             */
            hasMoreRow = false;
            iterators = null;
            nulls = null;
            values = null;
            types = null;

            /*
             * 请求数据
             */
            TFetchResultsResp resp = client.FetchResults(fetchReq);
            TStatus status = resp.getStatus();
            /*
             * 状态异常, 退出
             */
            if (status.getStatusCode().getValue() > TStatusCode.SUCCESS_WITH_INFO_STATUS.getValue()) {
                throw new RuntimeException("Failed to fetch result(s). " + status.toString());
            }

            /*
             * 远端是否还有数据
             */
            hasMoreRow = resp.isHasMoreRows();

            /*
             * 转换返回结果
             */
            TRowSet result = resp.getResults();
            List<TColumn> list = result.getColumns();
            if (list != null) {
                index = -1;

                /*
                 * 初始化本地缓冲区
                 */
                if (iterators == null) {
                    size = list.size();
                    iterators = new Iterator[size];
                    nulls = new byte[size][];
                    values = new Object[size];
                    types = new Class[size];
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
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean next() {
        if (closed) {
            return false;
        }

        /*
         * 本地没有数据，且远端有数据，则请求新数据
         */
        if ((iterators == null || !iterators[0].hasNext()) && hasMoreRow) {
            fetch();
        }

        /*
         * 读取本地数据
         */
        if (iterators != null && iterators[0].hasNext()) {
            for (int i = 0; i < iterators.length; ++i) {
                values[i] = iterators[i].next();
            }
            ++index;
            return true;
        }

        /*
         * 数据读取完毕
         */
        close();
        return false;
    }

    private boolean isNull(int columnIndex) {
        return (nulls[columnIndex][index / 8] & bitMask[index % 8]) != 0;
    }

    @Override
    public Object[] getValues() {
        Object[] result = new Object[values.length];
        for (int i = 0; i < result.length; ++i) {
            result[i] = isNull(i) ? null : values[i];
        }
        return result;
    }

    @Override
    public Object getObject(int columnIndex) {
        --columnIndex;
        if (isNull(columnIndex)) {
            return null;
        }
        return values[columnIndex];
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> clasz) {
        --columnIndex;
        if (isNull(columnIndex)) {
            return null;
        }
        return clasz.cast(values[columnIndex]);
    }

    @Override
    public String getString(int columnIndex) {
        --columnIndex;
        if (isNull(columnIndex)) {
            return null;
        }
        return (String) values[columnIndex];
    }

    @Override
    public Short getShort(int columnIndex) {
        --columnIndex;
        if (isNull(columnIndex)) {
            return null;
        }
        return (Short) values[columnIndex];
    }

    @Override
    public Integer getInteger(int columnIndex) {
        --columnIndex;
        if (isNull(columnIndex)) {
            return null;
        }
        return (Integer) values[columnIndex];
    }

    @Override
    public Long getLong(int columnIndex) {
        --columnIndex;
        if (isNull(columnIndex)) {
            return null;
        }
        return (Long) values[columnIndex];
    }

    @Override
    public int getColumnSize() {
        return getColumns().size();
    }

    @Override
    public Class<?> getType(int columnIndex) {
        --columnIndex;
        return types[columnIndex];
    }

    @Override
    public List<String> getColumns() {
        if (columns == null) {

            try {
                /*
                 * 请求元数据
                 */
                TGetResultSetMetadataReq metadataReq = new TGetResultSetMetadataReq(operation);
                TGetResultSetMetadataResp metadataResp = client.GetResultSetMetadata(metadataReq);

                ImpalaThriftUtil.checkStatus(metadataResp.getStatus());

                TTableSchema tableSchema = metadataResp.getSchema();

                if (tableSchema != null) {
                    List<TColumnDesc> columnDescs = tableSchema.getColumns();
                    columns = Lists.newArrayListWithExpectedSize(columnDescs.size());
                    for (TColumnDesc tColumnDesc : columnDescs) {
                        columns.add(tColumnDesc.getColumnName());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (columns == null) {
                columns = Lists.newArrayListWithExpectedSize(0);
            }
        }
        return columns;
    }

    @Override
    public void close() {
        this.closed = true;
    }
}