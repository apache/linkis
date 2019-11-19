package com.webank.wedatasphere.linkis.metadata.domain.mdq.vo;

import java.util.List;


public class MdqTableVO {

    private MdqTableBaseInfoVO tableBaseInfo;
    private List<MdqTableFieldsInfoVO> tableFieldsInfo;
    private MdqTableStatisticInfoVO tableStatisticInfo;

    private MdqImportInfoVO importInfo;

    public MdqTableBaseInfoVO getTableBaseInfo() {
        return tableBaseInfo;
    }

    public void setTableBaseInfo(MdqTableBaseInfoVO tableBaseInfo) {
        this.tableBaseInfo = tableBaseInfo;
    }

    public List<MdqTableFieldsInfoVO> getTableFieldsInfo() {
        return tableFieldsInfo;
    }

    public void setTableFieldsInfo(List<MdqTableFieldsInfoVO> tableFieldsInfo) {
        this.tableFieldsInfo = tableFieldsInfo;
    }

    public MdqTableStatisticInfoVO getTableStatisticInfo() {
        return tableStatisticInfo;
    }

    public void setTableStatisticInfo(MdqTableStatisticInfoVO tableStatisticInfo) {
        this.tableStatisticInfo = tableStatisticInfo;
    }

    public MdqImportInfoVO getImportInfo() {
        return importInfo;
    }

    public void setImportInfo(MdqImportInfoVO importInfo) {
        this.importInfo = importInfo;
    }
}
