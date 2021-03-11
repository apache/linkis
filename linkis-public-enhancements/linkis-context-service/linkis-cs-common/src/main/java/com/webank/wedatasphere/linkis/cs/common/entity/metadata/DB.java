package com.webank.wedatasphere.linkis.cs.common.entity.metadata;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.DBType;

public interface DB {

    String getName();

    void setName(String name);

    DBType getDbType();

    void setDbType(DBType dbType);

    String getOwners();

    void setOwners(String owners);

    String getComment();

    void setComment(String comment);

    String[] getLables();

    void setLables(String[] lables);

}
