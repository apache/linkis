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

package com.webank.wedatasphere.linkis.application.entity;


import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by johnnwang on 2019/1/25.
 */
@Data
@Accessors(chain = true)
public class Project {
    private Long id;
    private String name;
    private Boolean isProjectNeedInit;
    private String url;
    private String userInitUrl;
    private Boolean isUserNeedInit;
    private Boolean isProjectInited;
    private String json;
    private Integer level;
}
