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
package com.webank.wedatasphere.linkis.cs.common.entity.data;

import com.webank.wedatasphere.linkis.common.io.FsPath;

/**
 * @author peacewong
 * @date 2020/3/13 19:20
 */
public class CSResultData implements Data {

    private FsPath fsPath;

    @Override
    public FsPath getLocation() {
        return null;
    }

    @Override
    public void setLocation(FsPath fsPath) {

    }
}
