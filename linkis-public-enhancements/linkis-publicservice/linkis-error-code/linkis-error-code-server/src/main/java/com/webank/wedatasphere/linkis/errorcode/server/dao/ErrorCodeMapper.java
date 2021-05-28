package com.webank.wedatasphere.linkis.errorcode.server.dao;

import com.webank.wedatasphere.linkis.errorcode.common.LinkisErrorCode;
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
