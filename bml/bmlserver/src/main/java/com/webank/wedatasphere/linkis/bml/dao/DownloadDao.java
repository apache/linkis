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
package com.webank.wedatasphere.linkis.bml.dao;

import com.webank.wedatasphere.linkis.bml.Entity.DownloadModel;

import org.apache.ibatis.annotations.Param;

/**
 * created by cooperyang on 2019/5/30
 * Description:
 */
public interface DownloadDao {

    void insertDownloadModel(@Param("downloadModel")DownloadModel downloadModel);


}
