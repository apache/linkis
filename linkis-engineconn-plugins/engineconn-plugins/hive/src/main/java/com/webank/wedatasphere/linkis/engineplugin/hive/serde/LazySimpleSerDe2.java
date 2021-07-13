package com.webank.wedatasphere.linkis.engineplugin.hive.serde;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.common.classification.InterfaceAudience.Public;
import org.apache.hadoop.hive.common.classification.InterfaceStability.Stable;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.AbstractEncodingAwareSerDe;
import org.apache.hadoop.hive.serde2.ByteStream;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.SerDeSpec;
import org.apache.hadoop.hive.serde2.SerDeStats;
import org.apache.hadoop.hive.serde2.SerDeUtils;
import org.apache.hadoop.hive.serde2.io.*;
import org.apache.hadoop.hive.serde2.lazy.*;
import org.apache.hadoop.hive.serde2.lazy.objectinspector.primitive.LazyObjectInspectorParametersImpl;
import org.apache.hadoop.hive.serde2.objectinspector.ListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.MapObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector.Category;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.UnionObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.*;
import org.apache.hadoop.hive.serde2.typeinfo.StructTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoUtils;
import org.apache.hadoop.io.BinaryComparable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

/**
 * LazySimpleSerDe can be used to read the same data format as
 * MetadataTypedColumnsetSerDe and TCTLSeparatedProtocol.
 *
 * However, LazySimpleSerDe creates Objects in a lazy way, to provide better
 * performance.
 *
 * Also LazySimpleSerDe outputs typed columns instead of treating all columns as
 * String like MetadataTypedColumnsetSerDe.
 */
@Public
@Stable
@SerDeSpec(schemaProps = {
        serdeConstants.LIST_COLUMNS, serdeConstants.LIST_COLUMN_TYPES,
        serdeConstants.FIELD_DELIM, serdeConstants.COLLECTION_DELIM, serdeConstants.MAPKEY_DELIM,
        serdeConstants.SERIALIZATION_FORMAT, serdeConstants.SERIALIZATION_NULL_FORMAT,
        serdeConstants.SERIALIZATION_ESCAPE_CRLF,
        serdeConstants.SERIALIZATION_LAST_COLUMN_TAKES_REST,
        serdeConstants.ESCAPE_CHAR,
        serdeConstants.SERIALIZATION_ENCODING,
        LazySerDeParameters.SERIALIZATION_EXTEND_NESTING_LEVELS,
        LazySerDeParameters.SERIALIZATION_EXTEND_ADDITIONAL_NESTING_LEVELS
})
public class LazySimpleSerDe2 extends AbstractEncodingAwareSerDe {

    private LazySerDeParameters serdeParams = null;

    private ObjectInspector cachedObjectInspector;

    private long serializedSize;
    private SerDeStats stats;
    private boolean lastOperationSerialize;
    private boolean lastOperationDeserialize;
    public static byte[] trueBytes = {(byte) 't', 'r', 'u', 'e'};
    public static byte[] falseBytes = {(byte) 'f', 'a', 'l', 's', 'e'};

    @Override
    public String toString() {
        return getClass().toString()
                + "["
                + Arrays.asList(serdeParams.getSeparators())
                + ":"
                + ((StructTypeInfo) serdeParams.getRowTypeInfo()).getAllStructFieldNames()
                + ":"
                + ((StructTypeInfo) serdeParams.getRowTypeInfo())
                .getAllStructFieldTypeInfos() + "]";
    }

    public LazySimpleSerDe2() throws SerDeException {
    }

    /**
     * Initialize the SerDe given the parameters. serialization.format: separator
     * char or byte code (only supports byte-value up to 127) columns:
     * ","-separated column names columns.types: ",", ":", or ";"-separated column
     * types
     *
     * @see  org.apache.hadoop.hive.serde2.AbstractSerDe#initialize(Configuration, Properties)
     */
    @Override
    public void initialize(Configuration job, Properties tbl)
            throws SerDeException {

        super.initialize(job, tbl);

        serdeParams = new LazySerDeParameters(job, tbl, getClass().getName());

        // Create the ObjectInspectors for the fields
        cachedObjectInspector = LazyFactory.createLazyStructInspector(serdeParams
                        .getColumnNames(), serdeParams.getColumnTypes(),
                new LazyObjectInspectorParametersImpl(serdeParams));

        cachedLazyStruct = (LazyStruct) LazyFactory
                .createLazyObject(cachedObjectInspector);

        serializedSize = 0;
        stats = new SerDeStats();
        lastOperationSerialize = false;
        lastOperationDeserialize = false;
    }

    // The object for storing row data
    LazyStruct cachedLazyStruct;

    // The wrapper for byte array
    ByteArrayRef byteArrayRef;

    /**
     * Deserialize a row from the Writable to a LazyObject.
     *
     * @param field
     *          the Writable that contains the data
     * @return The deserialized row Object.
     * @see  org.apache.hadoop.hive.serde2.AbstractSerDe#deserialize(Writable)
     */
    @Override
    public Object doDeserialize(Writable field) throws SerDeException {
        if (byteArrayRef == null) {
            byteArrayRef = new ByteArrayRef();
        }
        BinaryComparable b = (BinaryComparable) field;
        byteArrayRef.setData(b.getBytes());
        cachedLazyStruct.init(byteArrayRef, 0, b.getLength());
        lastOperationSerialize = false;
        lastOperationDeserialize = true;
        return cachedLazyStruct;
    }

    /**
     * Returns the ObjectInspector for the row.
     */
    @Override
    public ObjectInspector getObjectInspector() throws SerDeException {
        return cachedObjectInspector;
    }

    /**
     * Returns the Writable Class after serialization.
     *
     * @see org.apache.hadoop.hive.serde2.AbstractSerDe#getSerializedClass()
     */
    @Override
    public Class<? extends Writable> getSerializedClass() {
        return Text.class;
    }

    Text serializeCache = new Text();
    ByteStream.Output serializeStream = new ByteStream.Output();

    /**
     * Serialize a row of data.
     *
     * @param obj
     *          The row object
     * @param objInspector
     *          The ObjectInspector for the row object
     * @return The serialized Writable object
     * @throws SerDeException
     * @see org.apache.hadoop.hive.serde2.AbstractSerDe#serialize(Object, ObjectInspector)
     */
    @Override
    public Writable doSerialize(Object obj, ObjectInspector objInspector)
            throws SerDeException {

        if (objInspector.getCategory() != Category.STRUCT) {
            throw new SerDeException(getClass().toString()
                    + " can only serialize struct types, but we got: "
                    + objInspector.getTypeName());
        }

        // Prepare the field ObjectInspectors
        StructObjectInspector soi = (StructObjectInspector) objInspector;
        List<? extends StructField> fields = soi.getAllStructFieldRefs();
        List<Object> list = soi.getStructFieldsDataAsList(obj);
        List<? extends StructField> declaredFields = (serdeParams.getRowTypeInfo() != null && ((StructTypeInfo) serdeParams.getRowTypeInfo())
                .getAllStructFieldNames().size() > 0) ? ((StructObjectInspector) getObjectInspector())
                .getAllStructFieldRefs()
                : null;

        serializeStream.reset();
        serializedSize = 0;

        // Serialize each field
        for (int i = 0; i < fields.size(); i++) {
            // Append the separator if needed.
            if (i > 0) {
                try {
                    serializeStream.write("\u0001".getBytes());
                } catch (IOException e) {
                    throw new RuntimeException("serializeStream write delimiter IOException" );
                }
            }
            // Get the field objectInspector and the field object.
            ObjectInspector foi = fields.get(i).getFieldObjectInspector();
            Object f = (list == null ? null : list.get(i));

            if (declaredFields != null && i >= declaredFields.size()) {
                throw new SerDeException("Error: expecting " + declaredFields.size()
                        + " but asking for field " + i + "\n" + "data=" + obj + "\n"
                        + "tableType=" + serdeParams.getRowTypeInfo().toString() + "\n"
                        + "dataType="
                        + TypeInfoUtils.getTypeInfoFromObjectInspector(objInspector));
            }

            serializeField(serializeStream, f, foi, serdeParams);
        }

        // TODO: The copy of data is unnecessary, but there is no work-around
        // since we cannot directly set the private byte[] field inside Text.
        serializeCache
                .set(serializeStream.getData(), 0, serializeStream.getLength());
        serializedSize = serializeStream.getLength();
        lastOperationSerialize = true;
        lastOperationDeserialize = false;
        return serializeCache;
    }

    protected void serializeField(ByteStream.Output out, Object obj, ObjectInspector objInspector,
                                  LazySerDeParameters serdeParams) throws SerDeException {
        try {
            serialize(out, obj, objInspector, serdeParams.getSeparators(), 1, serdeParams.getNullSequence(),
                    serdeParams.isEscaped(), serdeParams.getEscapeChar(), serdeParams.getNeedsEscape());
        } catch (IOException e) {
            throw new SerDeException(e);
        }
    }

    /**
     * Serialize the row into the StringBuilder.
     *
     * @param out
     *          The StringBuilder to store the serialized data.
     * @param obj
     *          The object for the current field.
     * @param objInspector
     *          The ObjectInspector for the current Object.
     * @param separators
     *          The separators array.
     * @param level
     *          The current level of separator.
     * @param nullSequence
     *          The byte sequence representing the NULL value.
     * @param escaped
     *          Whether we need to escape the data when writing out
     * @param escapeChar
     *          Which char to use as the escape char, e.g. '\\'
     * @param needsEscape
     *          Which byte needs to be escaped for 256 bytes.
     * @throws IOException
     * @throws SerDeException
     */
    public static void serialize(ByteStream.Output out, Object obj,
                                 ObjectInspector objInspector, byte[] separators, int level,
                                 Text nullSequence, boolean escaped, byte escapeChar, boolean[] needsEscape)
            throws IOException, SerDeException {

        if (obj == null) {
            out.write(nullSequence.getBytes(), 0, nullSequence.getLength());
            return;
        }

        char separator;
        List<?> list;
        switch (objInspector.getCategory()) {
            case PRIMITIVE:
                writePrimitiveUTF8(out, obj,
                        (PrimitiveObjectInspector) objInspector, escaped, escapeChar,
                        needsEscape);
                return;
            case LIST:
                separator = (char) getSeparator(separators, level);
                ListObjectInspector loi = (ListObjectInspector) objInspector;
                list = loi.getList(obj);
                ObjectInspector eoi = loi.getListElementObjectInspector();
                if (list == null) {
                    out.write(nullSequence.getBytes(), 0, nullSequence.getLength());
                } else {
                    for (int i = 0; i < list.size(); i++) {
                        if (i > 0) {
                            out.write(separator);
                        }
                        serialize(out, list.get(i), eoi, separators, level + 1, nullSequence,
                                escaped, escapeChar, needsEscape);
                    }
                }
                return;
            case MAP:
                separator = (char) getSeparator(separators, level);
                char keyValueSeparator =
                        (char) getSeparator(separators, level + 1);

                MapObjectInspector moi = (MapObjectInspector) objInspector;
                ObjectInspector koi = moi.getMapKeyObjectInspector();
                ObjectInspector voi = moi.getMapValueObjectInspector();
                Map<?, ?> map = moi.getMap(obj);
                if (map == null) {
                    out.write(nullSequence.getBytes(), 0, nullSequence.getLength());
                } else {
                    boolean first = true;
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        if (first) {
                            first = false;
                        } else {
                            out.write(separator);
                        }
                        serialize(out, entry.getKey(), koi, separators, level + 2,
                                nullSequence, escaped, escapeChar, needsEscape);
                        out.write(keyValueSeparator);
                        serialize(out, entry.getValue(), voi, separators, level + 2,
                                nullSequence, escaped, escapeChar, needsEscape);
                    }
                }
                return;
            case STRUCT:
                separator = (char) getSeparator(separators, level);
                StructObjectInspector soi = (StructObjectInspector) objInspector;
                List<? extends StructField> fields = soi.getAllStructFieldRefs();
                list = soi.getStructFieldsDataAsList(obj);
                if (list == null) {
                    out.write(nullSequence.getBytes(), 0, nullSequence.getLength());
                } else {
                    for (int i = 0; i < list.size(); i++) {
                        if (i > 0) {
                            out.write(separator);
                        }
                        serialize(out, list.get(i), fields.get(i).getFieldObjectInspector(),
                                separators, level + 1, nullSequence, escaped, escapeChar,
                                needsEscape);
                    }
                }
                return;
            case UNION:
                separator = (char)getSeparator(separators, level);
                UnionObjectInspector uoi = (UnionObjectInspector) objInspector;
                List<? extends ObjectInspector> ois = uoi.getObjectInspectors();
                if (ois == null) {
                    out.write(nullSequence.getBytes(), 0, nullSequence.getLength());
                } else {
                    writePrimitiveUTF8(out, new Byte(uoi.getTag(obj)),
                            PrimitiveObjectInspectorFactory.javaByteObjectInspector,
                            escaped, escapeChar, needsEscape);
                    out.write(separator);
                    serialize(out, uoi.getField(obj), ois.get(uoi.getTag(obj)),
                            separators, level + 1, nullSequence, escaped, escapeChar,
                            needsEscape);
                }
                return;
            default:
                break;
        }

        throw new RuntimeException("Unknown category type: "
                + objInspector.getCategory());
    }

    /**
     * Returns the statistics after (de)serialization)
     */

    @Override
    public SerDeStats getSerDeStats() {
        // must be different
        assert (lastOperationSerialize != lastOperationDeserialize);

        if (lastOperationSerialize) {
            stats.setRawDataSize(serializedSize);
        } else {
            stats.setRawDataSize(cachedLazyStruct.getRawDataSerializedSize());
        }
        return stats;

    }

    @Override
    protected Writable transformFromUTF8(Writable blob) {
        Text text = (Text)blob;
        return SerDeUtils.transformTextFromUTF8(text, this.charset);
    }

    @Override
    protected Writable transformToUTF8(Writable blob) {
        Text text = (Text)blob;
        return SerDeUtils.transformTextToUTF8(text, this.charset);
    }

    /**
     * This method is deprecated and is only used for backward compatibility.
     * Replaced by @see org.apache.hadoop.hive.serde2.lazy.LazySerDeParameters#LazySerDeParameters(Configuration, Properties, String)
     */
    @Deprecated
    public static SerDeParameters initSerdeParams(Configuration job,
                                                  Properties tbl,
                                                  String serdeName) throws SerDeException {
        return new SerDeParameters(job, tbl, serdeName);
    }

    /**
     * This class is deprecated and is only used for backward compatibility. Replace by
     * @see org.apache.hadoop.hive.serde2.lazy.LazySerDeParameters .
     */
    @Deprecated
    public static class SerDeParameters extends org.apache.hadoop.hive.serde2.lazy.LazySerDeParameters {

        public SerDeParameters(Configuration job,
                               Properties tbl,
                               String serdeName) throws SerDeException {
            super(job, tbl, serdeName);
        }
    }

    public static void writePrimitiveUTF8(OutputStream out, Object o,
                                          PrimitiveObjectInspector oi, boolean escaped, byte escapeChar,
                                          boolean[] needsEscape) throws IOException {

        PrimitiveObjectInspector.PrimitiveCategory category = oi.getPrimitiveCategory();
        byte[] binaryData = null;
        switch (category) {
            case BOOLEAN: {
                boolean b = ((BooleanObjectInspector) oi).get(o);
                if (b) {
                    binaryData = Base64.encodeBase64(trueBytes);
                } else {
                    binaryData = Base64.encodeBase64(falseBytes);
                }
                break;
            }
            case BYTE: {
                binaryData = Base64.encodeBase64(String.valueOf(((ByteObjectInspector) oi).get(o)).getBytes());
                break;
            }
            case SHORT: {
                binaryData = Base64.encodeBase64(String.valueOf(((ShortObjectInspector) oi).get(o)).getBytes());
                break;
            }
            case INT: {
                binaryData = Base64.encodeBase64(String.valueOf(((IntObjectInspector) oi).get(o)).getBytes());
                break;
            }
            case LONG: {
                binaryData = Base64.encodeBase64(String.valueOf(((LongObjectInspector) oi).get(o)).getBytes());
                break;
            }
            case FLOAT: {
                binaryData = Base64.encodeBase64(String.valueOf(((FloatObjectInspector) oi).get(o)).getBytes());
                break;
            }
            case DOUBLE: {
                binaryData = Base64.encodeBase64(String.valueOf(((DoubleObjectInspector) oi).get(o)).getBytes());
                break;
            }
            case STRING: {
                binaryData = Base64.encodeBase64(((StringObjectInspector) oi).getPrimitiveWritableObject(o).getBytes());
                break;
            }
            case CHAR: {
                HiveCharWritable hc = ((HiveCharObjectInspector) oi).getPrimitiveWritableObject(o);
                binaryData = Base64.encodeBase64(String.valueOf(hc).getBytes());
                break;
            }
            case VARCHAR: {
                HiveVarcharWritable hc = ((HiveVarcharObjectInspector)oi).getPrimitiveWritableObject(o);
                binaryData = Base64.encodeBase64(String.valueOf(hc).getBytes());
                break;
            }
            case BINARY: {
                BytesWritable bw = ((BinaryObjectInspector) oi).getPrimitiveWritableObject(o);
                byte[] toEncode = new byte[bw.getLength()];
                System.arraycopy(bw.getBytes(), 0,toEncode, 0, bw.getLength());
                byte[] toWrite = Base64.encodeBase64(toEncode);
                out.write(toWrite, 0, toWrite.length);
                break;
            }
            case DATE: {
                DateWritableV2 dw = ((DateObjectInspector) oi).getPrimitiveWritableObject(o);
                binaryData = Base64.encodeBase64(String.valueOf(dw).getBytes());
                break;
            }
            case TIMESTAMP: {
                TimestampWritableV2 tw = ((TimestampObjectInspector) oi).getPrimitiveWritableObject(o);
                binaryData = Base64.encodeBase64(String.valueOf(tw).getBytes());
                break;
            }
            case TIMESTAMPLOCALTZ: {
                TimestampLocalTZWritable tw2 = ((TimestampLocalTZObjectInspector) oi).getPrimitiveWritableObject(o);
                binaryData = Base64.encodeBase64(String.valueOf(tw2).getBytes());
                break;
            }
            case INTERVAL_YEAR_MONTH: {
                HiveIntervalYearMonthWritable hw = ((HiveIntervalYearMonthObjectInspector) oi).getPrimitiveWritableObject(o);
                binaryData = Base64.encodeBase64(String.valueOf(hw).getBytes());
                break;
            }
            case INTERVAL_DAY_TIME: {
                HiveIntervalDayTimeWritable ht = ((HiveIntervalDayTimeObjectInspector) oi).getPrimitiveWritableObject(o);
                binaryData = Base64.encodeBase64(String.valueOf(ht).getBytes());
                break;
            }
            case DECIMAL: {
                HiveDecimalObjectInspector decimalOI = (HiveDecimalObjectInspector) oi;
                binaryData = Base64.encodeBase64(String.valueOf(decimalOI).getBytes());
                break;
            }
            default: {
                throw new RuntimeException("Unknown primitive type: " + category);
            }
        }
        if(binaryData == null){
            throw new RuntimeException("get primitive type is null: " + category);
        }
        out.write(binaryData,0,binaryData.length);
    }

    public static void writeEscaped(OutputStream out, byte[] bytes, int start,
                                    int len, boolean escaped, byte escapeChar, boolean[] needsEscape)
            throws IOException {
        if (escaped) {
            int end = start + len;
            for (int i = start; i <= end; i++) {
                if (i == end || needsEscape[bytes[i] & 0xFF]) {  // Converts negative byte to positive index
                    if (i > start) {
                        out.write(bytes, start, i - start);
                    }

                    if (i == end) break;

                    out.write(escapeChar);
                    if (bytes[i] == '\r') {
                        out.write('r');
                        start = i + 1;
                    } else if (bytes[i] == '\n') {
                        out.write('n');
                        start = i + 1;
                    } else {
                        // the current char will be written out later.
                        start = i;
                    }
                }
            }
        } else {
            out.write(bytes, start, len);
        }
    }
    static byte getSeparator(byte[] separators, int level) throws SerDeException {
        try {
            return separators[level];
        } catch (ArrayIndexOutOfBoundsException var5) {
            String msg = "Number of levels of nesting supported for LazySimpleSerde is " + (separators.length - 1) + " Unable to work with level " + level;
            String txt = ". Use %s serde property for tables using LazySimpleSerde.";
            if (separators.length < 9) {
                msg = msg + String.format(txt, "hive.serialization.extend.nesting.levels");
            } else if (separators.length < 25) {
                msg = msg + String.format(txt, "hive.serialization.extend.additional.nesting.levels");
            }

            throw new SerDeException(msg, var5);
        }
    }
}