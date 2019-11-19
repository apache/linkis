package com.webank.wedatasphere.linkis.metadata.domain.mdq.bo;

import java.util.List;


public class MdqTableBO {

    private MdqTableImportInfoBO importInfo;
    private MdqTableBaseInfoBO tableBaseInfo;
    private List<MdqTableFieldsInfoBO> tableFieldsInfo;

    public MdqTableImportInfoBO getImportInfo() {
        return importInfo;
    }

    public void setImportInfo(MdqTableImportInfoBO importInfo) {
        this.importInfo = importInfo;
    }

    public MdqTableBaseInfoBO getTableBaseInfo() {
        return tableBaseInfo;
    }

    public void setTableBaseInfo(MdqTableBaseInfoBO tableBaseInfo) {
        this.tableBaseInfo = tableBaseInfo;
    }

    public List<MdqTableFieldsInfoBO> getTableFieldsInfo() {
        return tableFieldsInfo;
    }

    public void setTableFieldsInfo(List<MdqTableFieldsInfoBO> tableFieldsInfo) {
        this.tableFieldsInfo = tableFieldsInfo;
    }
}
