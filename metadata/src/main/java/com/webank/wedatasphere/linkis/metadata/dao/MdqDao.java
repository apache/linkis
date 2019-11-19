package com.webank.wedatasphere.linkis.metadata.dao;


import com.webank.wedatasphere.linkis.metadata.domain.mdq.po.MdqField;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.po.MdqImport;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.po.MdqLineage;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.po.MdqTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface MdqDao {

    void activateTable(Long tableId);
    
    MdqTable selectTableByName(@Param("database") String database, @Param("tableName") String tableName, @Param("user") String user);

    List<MdqField> listMdqFieldByTableId(Long tableId);

    void insertTable(MdqTable table);

    void insertFields(@Param("mdqFieldList") List<MdqField> mdqFieldList);

    void insertImport(MdqImport mdqImport);

    void insertLineage(MdqLineage mdqLineage);

    MdqTable selectTableForUpdate(@Param("database") String database, @Param("tableName") String tableName);

    void deleteTableBaseInfo(Long id);
}
