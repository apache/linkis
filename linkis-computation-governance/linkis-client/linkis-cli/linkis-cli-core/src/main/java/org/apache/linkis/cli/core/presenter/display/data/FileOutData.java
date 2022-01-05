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
 
package org.apache.linkis.cli.core.presenter.display.data;


public class FileOutData {
    private String pathName;
    private String fileName;
    private String content;
    private Boolean overwrite;

    public FileOutData(String pathName, String fileName, String content, Boolean overwrite) {
        this.pathName = pathName;
        this.fileName = fileName;
        this.content = content;
        this.overwrite = overwrite;
    }

    public String getPathName() {
        return pathName;
    }

    public String getContent() {
        return content;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public String getFileName() {
        return fileName;
    }
}