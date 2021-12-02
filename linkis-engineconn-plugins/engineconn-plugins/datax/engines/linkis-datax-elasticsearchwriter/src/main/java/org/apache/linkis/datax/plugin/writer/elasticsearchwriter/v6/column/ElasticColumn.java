/*
 *
 *  Copyright 2020 WeBank
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.datax.plugin.writer.elasticsearchwriter.v6.column;


import com.alibaba.datax.common.element.Column;
import com.alibaba.datax.common.element.Record;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.linkis.datax.plugin.writer.elasticsearchwriter.v6.ElasticWriterErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author davidhua
 * 2019/8/15
 */
public class ElasticColumn {

    private static final String ARRAY_SUFFIX = "]";
    private static final String ARRAY_PREFIX = "[";

    public static final String DEFAULT_NAME_SPLIT = "\\|";

    private String name;

    private String type;

    private String timezone;

    private String format;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public static Map<String, Object> toData(Record record, List<ElasticColumn> colConfs, String columnNameSeparator){
        Map<String, Object> outputData = new HashMap<>(record.getColumnNumber());
        for(int i = 0; i < record.getColumnNumber(); i++){
            Column column = record.getColumn(i);
            ElasticColumn config = colConfs.get(i);
            String columnName = config.getName();
            Map<String, Object> innerOutput = outputData;
            String[] levelColumns = columnName.split(columnNameSeparator);
            if(levelColumns.length > 1) {
                columnName = levelColumns[levelColumns.length - 1];
                for (int j = 0; j < levelColumns.length - 1 ; j++) {
                    Map<String, Object> data = new HashMap<>();
                    innerOutput.put(levelColumns[j], data);
                    innerOutput = data;
                }
            }
            ElasticFieldDataType type = ElasticFieldDataType.valueOf(config.getType().toUpperCase());
            switch(type){
                case IP:
                case IP_RANGE:
                case KEYWORD:
                case TEXT:
                    innerOutput.put(columnName,  column.asString());
                    break;
                case GEO_POINT:
                case GEO_SHAPE:
                case NESTED:
                case OBJECT:
                    innerOutput.put(columnName, parseObject(column.asString()));
                    break;
                case LONG_RANGE:
                case LONG:
                    innerOutput.put(columnName, column.asLong());
                    break;
                case INTEGER:
                case INTEGER_RANGE:
                case SHORT:
                    innerOutput.put(columnName, column.asBigInteger());
                    break;
                case FLOAT:
                case FLOAT_RANGE:
                case HALF_FLOAT:
                case SCALED_FLOAT:
                case DOUBLE_RANGE:
                case DOUBLE:
                    innerOutput.put(columnName, column.asDouble());
                    break;
                case BINARY:
                case BYTE:
                    innerOutput.put(columnName, column.asBytes());
                    break;
                case BOOLEAN:
                    innerOutput.put(columnName, column.asBoolean());
                    break;
                case DATE_RANGE:
                case DATE:
                    innerOutput.put(columnName, parseDate(config, column));
                    break;
                default:
                    throw DataXException.asDataXException(ElasticWriterErrorCode.MAPPING_TYPE_UNSUPPORTED,
                            "unsupported type:[" +config.getType() + "]");
            }
        }
        return outputData;
    }

    private static Object parseObject(String rawData){
        if(rawData.startsWith(ARRAY_PREFIX) &&
                rawData.endsWith(ARRAY_SUFFIX)){
            return JSON.parseObject(rawData, new TypeReference<List<Object>>(){});
        }
        return JSON.parseObject(rawData, Map.class);
    }

    private static String parseDate(ElasticColumn config, Column column){
        DateTimeZone dateTimeZone = DateTimeZone.getDefault();
        if(StringUtils.isNotBlank(config.getTimezone())){
            dateTimeZone = DateTimeZone.forID(config.getTimezone());
        }
        String output;
        if(column.getType() == Column.Type.DATE){
            output = new DateTime(column.asLong(), dateTimeZone).toString();
        }else if(StringUtils.isNotBlank(config.getFormat())){
            DateTimeFormatter formatter = DateTimeFormat.forPattern(config.getFormat());
            output = formatter.withZone(dateTimeZone)
                    .parseDateTime(column.asString()).toString();
        }else{
            output = column.asString();
        }
        return output;
    }

}
