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

package com.webank.wedatasphere.linkis.filesystem.util;

import com.webank.wedatasphere.linkis.storage.domain.Column;
import com.webank.wedatasphere.linkis.storage.resultset.table.TableRecord;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Created by johnnwang on 2018/11/6.
 */
@Deprecated
public class CSVUtil {

    public static InputStream createCSV(Column[] columns, List<TableRecord> tableRecords,String seperator){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            if (i == columns.length-1){
                stringBuilder.append(columns[i].columnName());
            }else{
                stringBuilder.append(columns[i].columnName()+",");
            }
        }
        stringBuilder.append("\n");
        for (int i = 0; i < tableRecords.size(); i++) {
            Object[] row = tableRecords.get(i).row();
            for (int j = 0; j < row.length; j++) {
                if (j == row.length-1){
                    stringBuilder.append(row[j]);
                }else{
                    stringBuilder.append(row[j]+",");
                }

            }
            if (i != tableRecords.size()-1){
                stringBuilder.append("\n");
            }
        }
        return new ByteArrayInputStream(stringBuilder.toString().getBytes());
    }
}
