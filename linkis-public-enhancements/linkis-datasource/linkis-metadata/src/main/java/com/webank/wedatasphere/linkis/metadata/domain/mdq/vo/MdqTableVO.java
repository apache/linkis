/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
