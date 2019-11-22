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
package com.webank.wedatasphere.linkis.metadata.service.impl;

import com.google.common.collect.Maps;
import com.google.gson.Gson;

import com.webank.wedatasphere.linkis.common.utils.ByteTimeUtils;
import com.webank.wedatasphere.linkis.common.utils.HDFSUtils;
import com.webank.wedatasphere.linkis.metadata.dao.MdqDao;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.DomainCoversionUtils;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.bo.MdqTableBO;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.bo.MdqTableBaseInfoBO;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.bo.MdqTableFieldsInfoBO;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.bo.MdqTableImportInfoBO;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.po.MdqField;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.po.MdqImport;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.po.MdqLineage;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.po.MdqTable;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.vo.MdqTableBaseInfoVO;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.vo.MdqTableFieldsInfoVO;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.vo.MdqTablePartitionStatisticInfoVO;
import com.webank.wedatasphere.linkis.metadata.domain.mdq.vo.MdqTableStatisticInfoVO;
import com.webank.wedatasphere.linkis.metadata.hive.config.DSEnum;
import com.webank.wedatasphere.linkis.metadata.hive.config.DataSource;
import com.webank.wedatasphere.linkis.metadata.hive.dao.HiveMetaDao;
import com.webank.wedatasphere.linkis.metadata.service.MdqService;
import com.webank.wedatasphere.linkis.metadata.type.MdqImportType;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class MdqServiceImpl implements MdqService {
    @Autowired
    private MdqDao mdqDao;

    @Autowired
    private HiveMetaDao hiveMetaDao;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    @DataSource(name = DSEnum.SECONDE_DATA_SOURCE)
    @Deprecated
    public void activateTable(Long tableId) {
        mdqDao.activateTable(tableId);
    }

    @Override
    @DataSource(name = DSEnum.SECONDE_DATA_SOURCE)
    @Transactional
    public Long persistTable(MdqTableBO mdqTableBO, String userName) {
        //查询，如果数据库中有这个表，而且是导入创建的，删掉表基本信息
        checkIfNeedDeleteTable(mdqTableBO);
        MdqTableBaseInfoBO tableBaseInfo = mdqTableBO.getTableBaseInfo();
        MdqTable table = DomainCoversionUtils.mdqTableBaseInfoBOToMdqTable(tableBaseInfo);
        table.setImport(mdqTableBO.getImportInfo() != null);
        table.setCreator(userName);
        mdqDao.insertTable(table);
        List<MdqTableFieldsInfoBO> tableFieldsInfo = mdqTableBO.getTableFieldsInfo();
        List<MdqField> mdqFieldList = DomainCoversionUtils.mdqTableFieldsInfoBOListToMdqFieldList(tableFieldsInfo, table.getId());
        mdqDao.insertFields(mdqFieldList);
        if (mdqTableBO.getImportInfo() != null) {
            MdqTableImportInfoBO importInfo = mdqTableBO.getImportInfo();
            MdqImport mdqImport = DomainCoversionUtils.mdqTableImportInfoBOToMdqImport(importInfo);
            mdqImport.setTableId(table.getId());
            mdqDao.insertImport(mdqImport);
            if (importInfo.getImportType().equals(MdqImportType.Hive.ordinal())) {
                MdqLineage mdqLineage = DomainCoversionUtils.generateMdaLineage(importInfo);
                mdqLineage.setTableId(table.getId());
                mdqDao.insertLineage(mdqLineage);
            }
        }
        return table.getId();
    }

    private void checkIfNeedDeleteTable(MdqTableBO mdqTableBO) {
        String database = mdqTableBO.getTableBaseInfo().getBase().getDatabase();
        String tableName = mdqTableBO.getTableBaseInfo().getBase().getName();
        MdqTable oldTable = mdqDao.selectTableForUpdate(database, tableName);
        boolean isPartitionsTabble = mdqTableBO.getTableBaseInfo().getBase().getPartitionTable();
        boolean isImport = mdqTableBO.getImportInfo() != null;
        Integer importType = null;
        if (isImport) {
            importType = mdqTableBO.getImportInfo().getImportType();
        }
        logger.info("库名:" + database + "表名:" + tableName + "是否是分区:"
                + isPartitionsTabble + "是否是导入创建:" + isImport + "导入类型:" + importType);
        if (oldTable != null) {
            if (isImport && (importType == MdqImportType.Csv.ordinal() || importType == MdqImportType.Excel.ordinal())) {
                String destination = mdqTableBO.getImportInfo().getArgs().get("destination");
                HashMap hashMap = new Gson().fromJson(destination, HashMap.class);
                if (Boolean.valueOf(hashMap.get("importData").toString())) {
                    logger.info("只是单纯增加分区列，不删除掉原来的表");
                    return;
                }
            }
            logger.info("将覆盖掉原来通过向导建立的表:" + oldTable);
            mdqDao.deleteTableBaseInfo(oldTable.getId());
        }
    }

    @Override
    public MdqTableStatisticInfoVO getTableStatisticInfo(String database, String tableName, String user) throws IOException {
        MdqTableStatisticInfoVO mdqTableStatisticInfoVO = getTableStatisticInfoFromHive(database, tableName, user);
        return mdqTableStatisticInfoVO;
    }

    @Override
    public String displaysql(MdqTableBO mdqTableBO) {
        String dbName = mdqTableBO.getTableBaseInfo().getBase().getDatabase();
        String tableName = mdqTableBO.getTableBaseInfo().getBase().getName();
        //StringBuilder sb = new StringBuilder();
        String displayStr = "//意书后台正在为您创建新的数据库表";
        return displayStr;
    }

    @DataSource(name = DSEnum.SECONDE_DATA_SOURCE)
    @Override
    public boolean isExistInMdq(String database, String tableName, String user) {
        //查询mdq表，而且active需要为true
        MdqTable table = mdqDao.selectTableByName(database, tableName, user);
        return table != null;
    }

    @DataSource(name = DSEnum.SECONDE_DATA_SOURCE)
    @Override
    public MdqTableBaseInfoVO getTableBaseInfoFromMdq(String database, String tableName, String user) {
        MdqTable table = mdqDao.selectTableByName(database, tableName, user);
        return DomainCoversionUtils.mdqTableToMdqTableBaseInfoVO(table);
    }

    @Override
    public MdqTableBaseInfoVO getTableBaseInfoFromHive(String database, String tableName, String user) {
        Map<String, String> map = Maps.newHashMap();
        map.put("dbName", database);
        map.put("userName", user);
        map.put("tableName", tableName);
        List<Map<String, Object>> tables = hiveMetaDao.getTablesByDbNameAndUser(map);
        List<Map<String, Object>> partitionKeys = hiveMetaDao.getPartitionKeys(map);
        Optional<Map<String, Object>> tableOptional = tables.parallelStream()
                .filter(f -> tableName.equals(f.get("NAME"))).findFirst();
        Map<String, Object> talbe = tableOptional.orElseThrow(() -> new IllegalArgumentException("table不存在"));
        MdqTableBaseInfoVO mdqTableBaseInfoVO = DomainCoversionUtils.mapToMdqTableBaseInfoVO(talbe, database);
        String tableComment = hiveMetaDao.getTableComment(database, tableName);
        mdqTableBaseInfoVO.getBase().setComment(tableComment);
        mdqTableBaseInfoVO.getBase().setPartitionTable(!partitionKeys.isEmpty());
        return mdqTableBaseInfoVO;
    }

    @DataSource(name = DSEnum.SECONDE_DATA_SOURCE)
    @Override
    public List<MdqTableFieldsInfoVO> getTableFieldsInfoFromMdq(String database, String tableName, String user) {
        MdqTable table = mdqDao.selectTableByName(database, tableName, user);
        List<MdqField> mdqFieldList = mdqDao.listMdqFieldByTableId(table.getId());
        return DomainCoversionUtils.mdqFieldListToMdqTableFieldsInfoVOList(mdqFieldList);
    }

    @Override
    public List<MdqTableFieldsInfoVO> getTableFieldsInfoFromHive(String database, String tableName, String user) {
        Map<String, String> param = Maps.newHashMap();
        param.put("dbName", database);
        param.put("tableName", tableName);
        List<Map<String, Object>> columns = hiveMetaDao.getColumns(param);
        List<Map<String, Object>> partitionKeys = hiveMetaDao.getPartitionKeys(param);
        List<MdqTableFieldsInfoVO> normalColumns = DomainCoversionUtils.normalColumnListToMdqTableFieldsInfoVOList(columns);
        List<MdqTableFieldsInfoVO> partitions = DomainCoversionUtils.partitionColumnListToMdqTableFieldsInfoVOList(partitionKeys);
        normalColumns.addAll(partitions);
        return normalColumns;
    }

    @Override
    public MdqTableStatisticInfoVO getTableStatisticInfoFromHive(String database, String tableName, String user) throws IOException {
        Map<String, String> map = Maps.newHashMap();
        map.put("dbName", database);
        map.put("tableName", tableName);
        List<String> partitions = hiveMetaDao.getPartitions(map);
        MdqTableStatisticInfoVO mdqTableStatisticInfoVO = new MdqTableStatisticInfoVO();
        mdqTableStatisticInfoVO.setRowNum(0);//下个版本
        mdqTableStatisticInfoVO.setTableLastUpdateTime(null);
        mdqTableStatisticInfoVO.setFieldsNum(getTableFieldsInfoFromHive(database, tableName, user).size());

        String tableLocation = getTableLocation(database, tableName);
        mdqTableStatisticInfoVO.setTableSize(getTableSize(tableLocation));
        mdqTableStatisticInfoVO.setFileNum(getTableFileNum(tableLocation));
        if (partitions.isEmpty()) {
            //非分区表
            mdqTableStatisticInfoVO.setPartitionsNum(0);
        } else {
            //分区表
            mdqTableStatisticInfoVO.setPartitionsNum(getPartitionsNum(tableLocation));
            mdqTableStatisticInfoVO.setPartitions(getMdqTablePartitionStatisticInfoVO(partitions, tableLocation));
        }
        return mdqTableStatisticInfoVO;
    }

    private List<MdqTablePartitionStatisticInfoVO> getMdqTablePartitionStatisticInfoVO(List<String> partitions, String tableLocation) throws IOException {
        //partitions.sort((a,b)-> -a.compareTo(b));
        //partitions
        //partitions.stream().forEach();
        List<MdqTablePartitionStatisticInfoVO> list = new ArrayList<>();
        FileStatus tableFile = getRootHdfs().getFileStatus(new Path(tableLocation));
        FileStatus[] fileStatuses = getRootHdfs().listStatus(tableFile.getPath());
        for (FileStatus fileStatuse : fileStatuses) {
            list.add(create(fileStatuse.getPath().toString()));
        }
        return list;
    }

    private MdqTablePartitionStatisticInfoVO create(String path) throws IOException {
        MdqTablePartitionStatisticInfoVO mdqTablePartitionStatisticInfoVO = new MdqTablePartitionStatisticInfoVO();
        mdqTablePartitionStatisticInfoVO.setName(new Path(path).getName());
        mdqTablePartitionStatisticInfoVO.setFileNum(getTableFileNum(path));
        mdqTablePartitionStatisticInfoVO.setPartitionSize(getTableSize(path));
        FileStatus tableFile = getRootHdfs().getFileStatus(new Path(path));
        FileStatus[] fileStatuses = getRootHdfs().listStatus(tableFile.getPath());
        List<FileStatus> collect = Arrays.stream(fileStatuses).filter(f -> f.isDirectory()).collect(Collectors.toList());
        for (FileStatus fileStatuse : collect) {
            mdqTablePartitionStatisticInfoVO.getChildrens().add(create(fileStatuse.getPath().toString()));
        }
        return mdqTablePartitionStatisticInfoVO;
    }

    private int getPartitionsNum(String tableLocation) throws IOException {
        int partitionsNum = 0;
        if (StringUtils.isNotBlank(tableLocation)) {
            FileStatus tableFile = getRootHdfs().getFileStatus(new Path(tableLocation));
            partitionsNum = getRootHdfs().listStatus(tableFile.getPath()).length;
        }
        return partitionsNum;
    }

    private String getTableLocation(String database, String tableName) {
        Map<String, String> param = Maps.newHashMap();
        param.put("dbName", database);
        param.put("tableName", tableName);
        String tableLocation = hiveMetaDao.getLocationByDbAndTable(param);
        logger.info("tableLocation:" + tableLocation);
        return tableLocation;
    }

    private int getTableFileNum(String tableLocation) throws IOException {
        int tableFileNum = 0;
        if (StringUtils.isNotBlank(tableLocation)) {
            FileStatus tableFile = getRootHdfs().getFileStatus(new Path(tableLocation));
            tableFileNum = (int) getRootHdfs().getContentSummary(tableFile.getPath()).getFileCount();
        }
        return tableFileNum;
    }

    private String getTableSize(String tableLocation) throws IOException {
        String tableSize = "0B";
        if (StringUtils.isNotBlank(tableLocation)) {
            FileStatus tableFile = getRootHdfs().getFileStatus(new Path(tableLocation));
            tableSize = ByteTimeUtils.bytesToString(getRootHdfs().getContentSummary(tableFile.getPath()).getLength());
        }
        return tableSize;
    }

    volatile private static FileSystem rootHdfs = null;

    private FileSystem getRootHdfs() {
        if (rootHdfs == null) {
            synchronized (this) {
                if (rootHdfs == null) {
                    rootHdfs = HDFSUtils.getHDFSRootUserFileSystem();
                }
            }
        }
        return rootHdfs;
    }

}
