/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.manager.engineplugin.jdbc.executor;

import org.apache.linkis.storage.domain.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class JDBCHelper {
    protected String helper(ResultSet rs, int dataType, int col)
            throws SQLException {
        String retVal = null;
        Integer intObj;
        switch (dataType) {
            case Types.DATE:
                java.sql.Date date = rs.getDate(col);
                retVal = date.toString();
                break;
            case Types.TIME:
                java.sql.Time time = rs.getTime(col);
                retVal = time.toString();
                break;
            case Types.TIMESTAMP:
                java.sql.Timestamp timestamp = rs.getTimestamp(col);
                retVal = timestamp.toString();
                break;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                retVal = rs.getString(col);
                break;
            case Types.NUMERIC:
            case Types.DECIMAL:
                java.math.BigDecimal numeric = rs.getBigDecimal(col, 10);
                retVal = numeric.toString();
                break;
            case Types.BIT:
                boolean bit = rs.getBoolean(col);
                Boolean boolObj = new Boolean(bit);
                retVal = boolObj.toString();
                break;
            case Types.TINYINT:
                byte tinyint = rs.getByte(col);
                intObj = new Integer(tinyint);
                retVal = intObj.toString();
                break;
            case Types.SMALLINT:
                short smallint = rs.getShort(col);
                intObj = new Integer(smallint);
                retVal = intObj.toString();
                break;
            case Types.INTEGER:
                int integer = rs.getInt(col);
                intObj = new Integer(integer);
                retVal = intObj.toString();
                break;
            case Types.BIGINT:
                long bigint = rs.getLong(col);
                Long longObj = new Long(bigint);
                retVal = longObj.toString();
                break;
            case Types.REAL:
                float real = rs.getFloat(col);
                Float floatObj = new Float(real);
                retVal = floatObj.toString();
                break;
            case Types.FLOAT:
            case Types.DOUBLE:
                double longreal = rs.getDouble(col);
                Double doubleObj = new Double(longreal);
                retVal = doubleObj.toString();
                break;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                byte[] binary = rs.getBytes(col);
                retVal = new String(binary);
                break;
            default:
                break;
        }
        return retVal;
    }

    public static String getTypeStr(int type) {
        String retVal = null;
        Integer intObj;
        switch (type) {
            case Types.NULL:
                retVal = NullType.typeName();
                break;
            case Types.VARCHAR:
                retVal = StringType.typeName();
                break;
            case Types.BOOLEAN:
                retVal = BooleanType.typeName();
                break;
            case Types.TINYINT:
                retVal = TinyIntType.typeName();
                break;
            case Types.SMALLINT:
                retVal = ShortIntType.typeName();
                break;
            case Types.INTEGER:
                retVal = IntType.typeName();
                break;
            case Types.LONGNVARCHAR:
                retVal = LongType.typeName();
                break;
            case Types.LONGVARCHAR:
                retVal = StringType.typeName();
                break;
            case Types.FLOAT:
                retVal = FloatType.typeName();
                break;
            case Types.DOUBLE:
                retVal = DoubleType.typeName();
                break;
            case Types.CHAR:
                retVal = CharType.typeName();
                break;
            case Types.DATE:
                retVal = DateType.typeName();
                break;
            case Types.TIMESTAMP:
                retVal = TimestampType.typeName();
                break;
            case Types.BINARY:
                retVal = BinaryType.typeName();
                break;
            case Types.DECIMAL:
                retVal = DecimalType.typeName();
                break;
            case Types.ARRAY:
                retVal = ArrayType.typeName();
                break;
            case Types.STRUCT:
                retVal = StructType.typeName();
                break;
            case Types.BIGINT:
                retVal = LongType.typeName();
                break;
            case Types.REAL:
                retVal = DoubleType.typeName();
                break;
            default:
                break;
        }
        return retVal;
    }
}
