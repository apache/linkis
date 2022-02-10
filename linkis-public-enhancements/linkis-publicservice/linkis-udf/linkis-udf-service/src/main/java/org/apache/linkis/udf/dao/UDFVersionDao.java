package org.apache.linkis.udf.dao;

import org.apache.linkis.udf.entity.UDFVersion;
import org.apache.linkis.udf.vo.UDFVersionVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UDFVersionDao {
    void addUdfVersion(UDFVersion udfVersion);

    UDFVersion selectLatestByUdfId(@Param("udfId") long udfId);

    UDFVersion selectByUdfIdAndVersion(@Param("udfId") long udfId, @Param("version") String version);

    void updatePublishStatus(@Param("udfId") long udfId, @Param("version") String version, @Param("isPublished") boolean isPublished);

    List<UDFVersionVo> getAllVersionByUdfId(@Param("udfId") long udfId);

    List<UDFVersion> getAllVersions(@Param("udfId") long udfId);

    void deleteVersionByUdfId(@Param("udfId") long udfId);

    int getSameJarCount(@Param("userName") String userName, @Param("jarName") String jarName);

    int getOtherSameJarCount(@Param("userName") String userName, @Param("jarName") String jarName, @Param("udfId") long udfId);

    void updateResourceIdByUdfId(@Param("udfId") long udfId, @Param("resourceId") String resourceId,
                                 @Param("oldUser") String oldUser, @Param("newUser") String newUser);

    void updateUDFVersion(UDFVersion udfVersion);
}
