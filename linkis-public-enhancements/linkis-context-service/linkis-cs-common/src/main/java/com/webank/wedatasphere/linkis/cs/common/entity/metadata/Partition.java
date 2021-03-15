package com.webank.wedatasphere.linkis.cs.common.entity.metadata;

public interface Partition {

    Integer getLength();

    void setLength(Integer length);

    String getName();

    void setName(String name);

    String getAlias();

    void setAlias(String alias);

    String getType();

    void setType(String type);

    String getComment();

    void setComment(String comment);

    String getExpress();

    void setExpress(String express);

    String getRule();

    void setRule(String rule);

    Boolean getPrimary();

    void setPrimary(Boolean primary);
}
