package org.apache.linkis.storage.domain;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.regex.Pattern;

import org.apache.linkis.common.utils.Logging;
import org.apache.linkis.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DataType {
    private static Logger logger = LoggerFactory.getLogger(DataType.class);

    public static final String NULL_VALUE = "NULL";
    public static final String LOWCASE_NULL_VALUE = "null";

    // TODO Change to fine-grained regular expressions(改为精细化正则表达式)
    public static final Pattern DECIMAL_REGEX = Pattern.compile("^decimal\\(\\d*\\,\\d*\\)");

    public static final Pattern SHORT_REGEX = Pattern.compile("^short.*");
    public static final Pattern INT_REGEX = Pattern.compile("^int.*");
    public static final Pattern LONG_REGEX = Pattern.compile("^long.*");
    public static final Pattern BIGINT_REGEX = Pattern.compile("^bigint.*");
    public static final Pattern FLOAT_REGEX = Pattern.compile("^float.*");
    public static final Pattern DOUBLE_REGEX = Pattern.compile("^double.*");

    public static final Pattern VARCHAR_REGEX = Pattern.compile("^varchar.*");
    public static final Pattern CHAR_REGEX = Pattern.compile("^char.*");

    public static final Pattern ARRAY_REGEX = Pattern.compile("array.*");

    public static final Pattern MAP_REGEX = Pattern.compile("map.*");

    public static final Pattern LIST_REGEX = Pattern.compile("list.*");

    public static final Pattern STRUCT_REGEX = Pattern.compile("struct.*");

    public String typeName;
    private int javaSQLType;

    public DataType(String typeName, int javaSQLType) {
        this.typeName = typeName;
        this.javaSQLType = javaSQLType;
    }

    @Override
    public String toString() {
        return typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getJavaSQLType() {
        return javaSQLType;
    }


    public static DataType toDataType(String dataType) {
        if (dataType.equals("void") || dataType.equals("null")) {
            return new NullType();
        } else if (dataType.equals("string")) {
            return new StringType();
        } else if (dataType.equals("boolean")) {
            return new BooleanType();
        } else if (SHORT_REGEX.matcher(dataType).matches()) {
            return new ShortIntType();
        } else if (LONG_REGEX.matcher(dataType).matches()) {
            return new LongType();
        } else if (BIGINT_REGEX.matcher(dataType).matches()) {
            return new BigIntType();
        } else if (INT_REGEX.matcher(dataType).matches() || dataType.equals("integer") || dataType.equals("smallint")) {
            return new IntType();
        } else if (FLOAT_REGEX.matcher(dataType).matches()) {
            return new FloatType();
        } else if (DOUBLE_REGEX.matcher(dataType).matches()) {
            return new DoubleType();
        } else if (VARCHAR_REGEX.matcher(dataType).matches()) {
            return new VarcharType();
        } else if (CHAR_REGEX.matcher(dataType).matches()) {
            return new CharType();
        } else if (dataType.equals("date")) {
            return new DateType();
        } else if (dataType.equals("timestamp")) {
            return new TimestampType();
        } else if (dataType.equals("binary")) {
            return new BinaryType();
        } else if (dataType.equals("decimal") || DECIMAL_REGEX.matcher(dataType).matches()) {
            return new DecimalType();
        } else if (ARRAY_REGEX.matcher(dataType).matches()) {
            return new ArrayType();
        } else if (MAP_REGEX.matcher(dataType).matches()) {
            return new MapType();
        } else if (LIST_REGEX.matcher(dataType).matches()) {
            return new ListType();
        } else if (STRUCT_REGEX.matcher(dataType).matches()) {
            return new StructType();
        } else {
            return new StringType();
        }
    }

    public static Object toValue(DataType dataType, String value) {
        try {
            if (dataType instanceof NullType) {
                return null;
            } else if (dataType instanceof StringType || dataType instanceof CharType || dataType instanceof VarcharType || dataType instanceof StructType || dataType instanceof ListType || dataType instanceof ArrayType || dataType instanceof MapType) {
                return value;
            } else if (dataType instanceof BooleanType) {
                if (isNumberNull(value)) {
                    return null;
                } else {
                    return Boolean.valueOf(value);
                }
            } else if (dataType instanceof ShortIntType) {
                if (isNumberNull(value)) {
                    return null;
                } else {
                    return Short.valueOf(value);
                }
            } else if (dataType instanceof IntType) {
                if (isNumberNull(value)) {
                    return null;
                } else {
                    return Integer.valueOf(value);
                }
            } else if (dataType instanceof LongType || dataType instanceof BigIntType) {
                if (isNumberNull(value)) {
                    return null;
                } else {
                    return Long.valueOf(value);
                }
            } else if (dataType instanceof FloatType) {
                if (isNumberNull(value)) {
                    return null;
                } else {
                    return Float.valueOf(value);
                }
            } else if (dataType instanceof DoubleType) {
                if (isNumberNull(value)) {
                    return null;
                } else {
                    return Double.valueOf(value);
                }
            } else if (dataType instanceof DecimalType) {
                if (isNumberNull(value)) {
                    return null;
                } else {
                    return new BigDecimal(value);
                }
            } else if (dataType instanceof DateType) {
                if (isNumberNull(value)) {
                    return null;
                } else {
                    return Date.valueOf(value);
                }
            } else if (dataType instanceof TimestampType) {
                if (isNumberNull(value)) {
                    return null;
                } else {
                    return Timestamp.valueOf(value).toString().replace(".0", "");
                }
            } else if (dataType instanceof BinaryType) {
                if (isNull(value)) {
                    return null;
                } else {
                    return value.getBytes();
                }
            } else {
                return value;
            }
        } catch (Throwable t) {
            logger.debug("Failed to " + value + " switch to dataType:", t);
            return value;
        }
    }

    public static boolean isNull(String value) {
        return value == null || value.equals(NULL_VALUE) || value.trim().equals("");
    }

    public static boolean isNumberNull(String value) {
        return value == null || value.equalsIgnoreCase(NULL_VALUE) || value.trim().equals("");
    }

    public static String valueToString(Object value) {
        if (value == null) {
            return LOWCASE_NULL_VALUE;
        } else if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toPlainString();
        } else {
            return value.toString();
        }
    }

    public final static class NullType extends DataType {
        public NullType() {
            super("void", 0);
        }
    }

    public final static class StringType extends DataType {
        public StringType() {
            super("string", 12);
        }
    }

    public final static class BooleanType extends DataType {
        public BooleanType() {
            super("boolean", 16);
        }
    }

    public final static class TinyIntType extends DataType {
        public TinyIntType() {
            super("tinyint", -6);
        }
    }

    public final static class ShortIntType extends DataType {
        public ShortIntType() {
            super("short", 5);
        }
    }

    public final static class IntType extends DataType {
        public IntType() {
            super("int", 4);
        }
    }

    public final static class LongType extends DataType {
        public LongType() {
            super("long", -5);
        }
    }

    public final static class BigIntType extends DataType {
        public BigIntType() {
            super("bigint", -5);
        }
    }

    public final static class FloatType extends DataType {
        public FloatType() {
            super("float", 6);
        }
    }

    public final static class DoubleType extends DataType {
        public DoubleType() {
            super("double", 8);
        }
    }

    public final static class CharType extends DataType {
        public CharType() {
            super("char", 1);
        }
    }

    public final static class VarcharType extends DataType {
        public VarcharType() {
            super("varchar", 12);
        }
    }

    public final static class DateType extends DataType {
        public DateType() {
            super("date", 91);
        }
    }

    public final static class TimestampType extends DataType {
        public TimestampType() {
            super("timestamp", 93);
        }
    }

    public final static class BinaryType extends DataType {
        public BinaryType() {
            super("binary", -2);
        }
    }

    public final static class DecimalType extends DataType {
        public DecimalType() {
            super("decimal", 3);
        }
    }

    public final static class ArrayType extends DataType {
        public ArrayType() {
            super("array", 2003);
        }
    }

    public final static class MapType extends DataType {
        public MapType() {
            super("map", 2000);
        }
    }

    public final static class ListType extends DataType {
        public ListType() {
            super("list", 2001);
        }
    }

    public final static class StructType extends DataType {
        public StructType() {
            super("struct", 2002);
        }
    }

    public final static class BigDecimalType extends DataType {
        public BigDecimalType() {
            super("bigdecimal", 3);
        }
    }

}
