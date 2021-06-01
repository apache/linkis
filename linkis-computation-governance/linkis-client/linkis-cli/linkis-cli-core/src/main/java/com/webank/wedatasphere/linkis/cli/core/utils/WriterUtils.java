/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.cli.core.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: linkis-cli
 * @description: write output to file
 * @create: 2021/01/05 21:08
 */
public class WriterUtils {
    private static Map<String, BufferedWriter> writerMap = new ConcurrentHashMap<>();

    private static BufferedWriter getWriter(String filename) throws Exception {
        if (!writerMap.containsKey(filename)) {
            synchronized (WriterUtils.class) {
                if (!writerMap.containsKey(filename)) {
                    File file = new File(filename);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream fos = new FileOutputStream(file);
                    OutputStreamWriter osWritter = new OutputStreamWriter(fos, "UTF-8");//设置字符编码
                    BufferedWriter bufferedWriter = new BufferedWriter(osWritter, 1024);//设置缓冲区大小
                    writerMap.put(filename, new BufferedWriter(bufferedWriter));
                }
            }
        }
        return writerMap.get(filename);
    }

    public static void closeWriter(String filename) {
        if (StringUtils.isNotBlank(filename) && writerMap.containsKey(filename)) {
            try {
                writerMap.get(filename).flush();
                writerMap.get(filename).close();
                writerMap.remove(filename);
            } catch (Exception e) {
                //ignore
            }
        }
    }

    public static void writeToFile(String filename, String content) throws Exception {
        BufferedWriter writer = getWriter(filename);
        writer.write(content + "\n");
    }
}