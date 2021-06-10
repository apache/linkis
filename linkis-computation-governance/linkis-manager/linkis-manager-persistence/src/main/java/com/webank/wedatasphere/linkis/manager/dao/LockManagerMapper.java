package com.webank.wedatasphere.linkis.manager.dao;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface LockManagerMapper {
    @Insert("insert into linkis_cg_manager_lock (lock_object, time_out, update_time, create_time)" +
            "values(#{jsonObject}, #{timeOut}, now(), now())")
    void lock(@Param("jsonObject") String jsonObject, @Param("timeOut") Long timeOut);

    @Delete("delete  from linkis_cg_manager_lock where lock_object = #{jsonObject}")
    void unlock(String jsonObject);
}
