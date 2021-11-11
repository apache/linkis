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
 
package org.apache.linkis.cli.core.presenter.display;

import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.PresenterException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.presenter.display.data.FileOutData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * @description: write data into file
 */
public class PlainTextFileDriver implements DisplayDriver {
    @Override
    public void doOutput(Object data) {
        if (!(data instanceof FileOutData)) {
            throw new PresenterException("PST0004", ErrorLevel.ERROR, CommonErrMsg.PresentDriverErr, "input data is not instance of FileOutData");
        }

        String pathName = ((FileOutData) data).getPathName();
        String fileName = pathName + File.separator + ((FileOutData) data).getFileName();
        String content = ((FileOutData) data).getContent();
        Boolean overWrite = ((FileOutData) data).isOverwrite();

        File dir = new File(pathName);
        File file = new File(fileName);

        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (Exception e) {
                throw new PresenterException("PST0005", ErrorLevel.ERROR, CommonErrMsg.PresentDriverErr, "Cannot mkdir for path: " + dir.getAbsolutePath(), e);
            }
        }

        if (overWrite || !file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                throw new PresenterException("PST0006", ErrorLevel.ERROR, CommonErrMsg.PresentDriverErr, "Cannot create file for path: " + file.getAbsolutePath(), e);
            }
        }

        FileOutputStream fos = null;
        OutputStreamWriter osWritter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fos = new FileOutputStream(file, !overWrite);
            osWritter = new OutputStreamWriter(fos, "UTF-8");
            bufferedWriter = new BufferedWriter(osWritter, 1024);
            bufferedWriter.write(content + "\n");
        } catch (Exception e) {
            throw new PresenterException("PST0007", ErrorLevel.ERROR, CommonErrMsg.PresentDriverErr, "Cannot write: " + file.getAbsolutePath(), e);

        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (Exception e) {
                    //ignore
                }
            }
            if (osWritter != null) {
                try {
                    osWritter.close();
                    ;
                } catch (Exception e) {
                    //ignore
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    //ignore
                }
            }
        }
    }
}