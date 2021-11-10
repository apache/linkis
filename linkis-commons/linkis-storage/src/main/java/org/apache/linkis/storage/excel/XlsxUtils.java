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
 
package org.apache.linkis.storage.excel;

import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class XlsxUtils {

    public static List<List<String>> getBasicInfo(InputStream inputStream, File file) throws Exception{
        List<List<String>> res = new ArrayList<>();
        Workbook wb = null;
        if(inputStream != null){
            wb = StreamingReader.builder()
                    .sstCacheSize(100)
                    .rowCacheSize(2)    // number of rows to keep in memory (defaults to 10)
                    .open(inputStream);
        } else {
            wb = StreamingReader.builder()
                    .sstCacheSize(100)
                    .rowCacheSize(2)    // number of rows to keep in memory (defaults to 10)
                    .open(file);
        }
        List<String> sheetNames = new ArrayList<>();
        for(Sheet sheet : wb){
            sheetNames.add(sheet.getSheetName());
        }

        Sheet sheet = wb.getSheetAt(0);
        Iterator<Row> iterator = sheet.iterator();
        Row row = null;
        while(iterator.hasNext() && row == null){
            row = iterator.next();
        }

        if(row == null){
            throw new Exception("The incoming Excel file is empty(传入的Excel文件为空)");
        }

        List<String> values = new ArrayList<>();
        for(Cell cell:row){
            values.add(cell.getStringCellValue());
        }
        if(inputStream != null){
            inputStream.close();
        }
        res.add(sheetNames);
        res.add(values);
        return res;

    }
}
