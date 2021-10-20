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


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class ExcelStorageReader {

    public static List<List<String>> getExcelTitle(InputStream in, File file, Boolean hasHeader, String suffix) throws Exception {

        List<List<String>> res;
        if (".xls".equalsIgnoreCase(suffix)) {
            if(in == null)
                in = new FileInputStream(file);
            res = XlsUtils.getBasicInfo(in);
        } else {
            res = XlsxUtils.getBasicInfo(in, file);
        }
        if(res == null && res.size() < 2){
            throw new Exception("There is a problem with the file format(文件格式有问题)");
        }
        List<String> headerType = new ArrayList<String>();
        List<String> header = res.get(1);
        if (hasHeader) {
            for(int i = 0; i < header.size(); i ++){
                headerType.add("string");
            }
        } else {
            List<String> headerNew = new ArrayList<String>();
            for(int i = 0; i < header.size(); i ++){
                headerNew.add("col_" + (i + 1));
                headerType.add("string");
            }
            res.set(1, headerNew);
        }
        res.add(headerType);
        return  res;
    }

}
