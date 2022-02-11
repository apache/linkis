package org.apache.linkis.publicservice.common.lock.dao;

import org.apache.linkis.publicservice.common.lock.entity.CommonLock;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommonLockMapper {
    @Insert(
            "insert into linkis_ps_common_lock (lock_object, time_out, update_time, create_time)"
                    + "values(#{jsonObject}, #{timeOut}, now(), now())")
    void lock(@Param("jsonObject") String jsonObject, @Param("timeOut") Long timeOut);

    @Delete("delete  from linkis_ps_common_lock where lock_object = #{jsonObject}")
    void unlock(String jsonObject);

    @Select("select * from linkis_ps_common_lock")
    @Results({
        @Result(property = "updateTime", column = "update_time"),
        @Result(property = "createTime", column = "create_time")
    })
    List<CommonLock> getAll();
}
