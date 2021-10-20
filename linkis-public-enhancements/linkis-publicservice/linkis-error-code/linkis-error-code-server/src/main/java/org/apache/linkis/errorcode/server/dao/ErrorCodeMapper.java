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
 
package org.apache.linkis.errorcode.server.dao;

import org.apache.linkis.errorcode.common.LinkisErrorCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface ErrorCodeMapper {


    @Select("SELECT * FROM linkis_ps_error_code")
    @Results({
            @Result(property = "errorCode", column = "error_code"),
            @Result(property = "errorDesc", column = "error_desc"),
            @Result(property = "errorRegexStr", column = "error_regex"),
            @Result(property = "errorType", column = "error_type"),
    })
    List<LinkisErrorCode> getAllErrorCodes();

}
