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
package com.webank.wedatasphere.linkis.metadata.domain.mdq;

import com.webank.wedatasphere.linkis.metadata.domain.mdq.bo.*;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.po.MdqField;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.po.MdqImport;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.po.MdqLineage;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.po.MdqTable;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.vo.*;
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class DomainCoversionUtils {
    public static MdqTableBaseInfoVO mdqTableToMdqTableBaseInfoVO(MdqTable table){
        MdqTableBaseInfoVO mdqTableBaseInfoVO = new MdqTableBaseInfoVO();
        BaseVO baseVO = new BaseVO();
        ModelVO modelVO = new ModelVO();
        ApplicationVO applicationVO = new ApplicationVO();
        BeanUtils.copyProperties(table,baseVO);
        baseVO.setCreateTime(new Date(table.getCreateTime().getTime()/1000));
        BeanUtils.copyProperties(table,modelVO);
        BeanUtils.copyProperties(table,applicationVO);
        mdqTableBaseInfoVO.setBase(baseVO);
        mdqTableBaseInfoVO.setModel(modelVO);
        mdqTableBaseInfoVO.setApplication(applicationVO);
        return mdqTableBaseInfoVO;
    }

    public static List<MdqTableFieldsInfoVO> mdqFieldListToMdqTableFieldsInfoVOList(List<MdqField> mdqFieldList) {
        return mdqFieldList.stream().map(DomainCoversionUtils::mdqFieldToMdqTableFieldsInfoVO).collect(Collectors.toList());
    }

    public static MdqTableFieldsInfoVO mdqFieldToMdqTableFieldsInfoVO(MdqField mdqField) {
        MdqTableFieldsInfoVO mdqTableFieldsInfoVO = new MdqTableFieldsInfoVO();
        BeanUtils.copyProperties(mdqField,mdqTableFieldsInfoVO);
        return mdqTableFieldsInfoVO;
    }

    public static MdqTable mdqTableBaseInfoBOToMdqTable(MdqTableBaseInfoBO tableBaseInfo) {
        MdqTable table = new MdqTable();
        BaseBO base = tableBaseInfo.getBase();
        ModelBO model = tableBaseInfo.getModel();
        ApplicationBO application = tableBaseInfo.getApplication();
        BeanUtils.copyProperties(base,table);
        BeanUtils.copyProperties(model,table);
        BeanUtils.copyProperties(application,table);
        table.setCreateTime(new Date());// 避免日期非空，其他校验先不搞，由前台传
        table.setAvailable(true);//
        return table;
    }

    public static List<MdqField> mdqTableFieldsInfoBOListToMdqFieldList(List<MdqTableFieldsInfoBO> tableFieldsInfo, Long tableId) {
        return tableFieldsInfo.stream().map(f ->mdqTableFieldsInfoBOToMdqField(f,tableId)).collect(Collectors.toList());
    }

    public static MdqField mdqTableFieldsInfoBOToMdqField(MdqTableFieldsInfoBO tableFieldsInfo, Long tableId) {
        MdqField mdqField = new MdqField();
        mdqField.setTableId(tableId);
        BeanUtils.copyProperties(tableFieldsInfo,mdqField);
        return mdqField;
    }

    public static MdqImport mdqTableImportInfoBOToMdqImport(MdqTableImportInfoBO importInfo) {
        MdqImport mdqImport = new MdqImport();
        mdqImport.setImportType(importInfo.getImportType());
        mdqImport.setArgs(BDPJettyServerHelper.gson().toJson(importInfo.getArgs()));
        return mdqImport;
    }

    public static MdqLineage generateMdaLineage(MdqTableImportInfoBO importInfo) {
        MdqLineage mdqLineage = new MdqLineage();
        mdqLineage.setUpdateTime(new Date());
        String database = importInfo.getArgs().get("database");
        String table = importInfo.getArgs().get("table");
        mdqLineage.setSourceTable(database + "." + table);
        return mdqLineage;
    }

    public static MdqTableBaseInfoVO mapToMdqTableBaseInfoVO(Map<String,Object> table,String database){
        MdqTableBaseInfoVO mdqTableBaseInfoVO = new MdqTableBaseInfoVO();
        BaseVO baseVO = new BaseVO();
        baseVO.setCreateTime(new Date(Long.valueOf(table.get("CREATE_TIME").toString())));
        baseVO.setCreator((String) table.get("OWNER"));
        baseVO.setName((String) table.get("NAME"));
        baseVO.setDatabase(database);
        baseVO.setLatestAccessTime(new Date(Long.valueOf(table.get("LAST_ACCESS_TIME").toString())));
        mdqTableBaseInfoVO.setBase(baseVO);
        mdqTableBaseInfoVO.setModel(new ModelVO());
        mdqTableBaseInfoVO.setApplication(new ApplicationVO());
        return mdqTableBaseInfoVO;
    }

    public static List<MdqTableFieldsInfoVO> normalColumnListToMdqTableFieldsInfoVOList(List<Map<String, Object>> columns) {
        return columns.parallelStream().map(DomainCoversionUtils::normalColumnToMdqTableFieldsInfoVO).collect(Collectors.toList());
    }

    public static MdqTableFieldsInfoVO normalColumnToMdqTableFieldsInfoVO(Map<String, Object> column){
        MdqTableFieldsInfoVO mdqTableFieldsInfoVO = new MdqTableFieldsInfoVO();
        mdqTableFieldsInfoVO.setType((String) column.get("TYPE_NAME"));
        mdqTableFieldsInfoVO.setComment((String)column.get("COMMENT"));
        mdqTableFieldsInfoVO.setPrimary(false);
        mdqTableFieldsInfoVO.setPartitionField(false);
        mdqTableFieldsInfoVO.setName((String)column.get("COLUMN_NAME"));
        return mdqTableFieldsInfoVO;
    }

    public static List<MdqTableFieldsInfoVO> partitionColumnListToMdqTableFieldsInfoVOList(List<Map<String, Object>> partitionKeys) {
        return partitionKeys.parallelStream().map(DomainCoversionUtils::partitionColumnToMdqTableFieldsInfoVO).collect(Collectors.toList());
    }

    public static MdqTableFieldsInfoVO partitionColumnToMdqTableFieldsInfoVO(Map<String, Object> column){
        MdqTableFieldsInfoVO mdqTableFieldsInfoVO = new MdqTableFieldsInfoVO();
        mdqTableFieldsInfoVO.setType((String) column.get("PKEY_TYPE"));
        mdqTableFieldsInfoVO.setComment((String)column.get("PKEY_COMMENT"));
        mdqTableFieldsInfoVO.setPrimary(false);
        mdqTableFieldsInfoVO.setPartitionField(true);
        mdqTableFieldsInfoVO.setName((String)column.get("PKEY_NAME"));
        return mdqTableFieldsInfoVO;
    }
}
