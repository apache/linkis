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

import java.util.Date;

/**
 * Created by johnnwang on 2019/1/23.
 */
@Data
@Accessors(chain = true)
public class ApplicationUser {
    private Long id;
    private String email;
    private String userName;
    private String password;
    private Boolean admin;
    private Boolean active;
    private String name;
    private String description;
    private String department;
    private String avatar;
    private Date createTime;
    private Long createBy;
    private Date updateTime;
    private Long updateBy;
    private Boolean isFirstLogin;
}
