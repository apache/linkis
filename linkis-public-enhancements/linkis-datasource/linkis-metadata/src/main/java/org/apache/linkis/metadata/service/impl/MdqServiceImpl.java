/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.metadata.service.impl;

import org.apache.linkis.common.utils.ByteTimeUtils;
import org.apache.linkis.hadoop.common.utils.HDFSUtils;
import org.apache.linkis.hadoop.common.utils.KerberosUtils;
import org.apache.linkis.metadata.conf.MdqConfiguration;
import org.apache.linkis.metadata.dao.MdqDao;
import org.apache.linkis.metadata.domain.mdq.DomainCoversionUtils;
import org.apache.linkis.metadata.domain.mdq.Tunple;
import org.apache.linkis.metadata.domain.mdq.bo.MdqTableBO;
import org.apache.linkis.metadata.domain.mdq.bo.MdqTableBaseInfoBO;
import org.apache.linkis.metadata.domain.mdq.bo.MdqTableFieldsInfoBO;
import org.apache.linkis.metadata.domain.mdq.bo.MdqTableImportInfoBO;
import org.apache.linkis.metadata.domain.mdq.po.MdqField;
import org.apache.linkis.metadata.domain.mdq.po.MdqImport;
import org.apache.linkis.metadata.domain.mdq.po.MdqLineage;
import org.apache.linkis.metadata.domain.mdq.po.MdqTable;
import org.apache.linkis.metadata.domain.mdq.vo.*;
import org.apache.linkis.metadata.hive.config.DSEnum;
import org.apache.linkis.metadata.hive.config.DataSource;
import org.apache.linkis.metadata.hive.dao.HiveMetaDao;
import org.apache.linkis.metadata.hive.dto.MetadataQueryParam;
import org.apache.linkis.metadata.service.HiveMetaWithPermissionService;
import org.apache.linkis.metadata.service.MdqService;
import org.apache.linkis.metadata.type.MdqImportType;
import org.apache.linkis.metadata.util.DWSConfig;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MdqServiceImpl implements MdqService {

  @Autowired private MdqDao mdqDao;

  @Autowired private HiveMetaDao hiveMetaDao;

  @Autowired HiveMetaWithPermissionService hiveMetaWithPermissionService;

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
    // 查询，如果数据库中有这个表，而且是导入创建的，删掉表基本信息
    checkIfNeedDeleteTable(mdqTableBO);
    MdqTableBaseInfoBO tableBaseInfo = mdqTableBO.getTableBaseInfo();
    MdqTable table = DomainCoversionUtils.mdqTableBaseInfoBOToMdqTable(tableBaseInfo);
    table.setImport(mdqTableBO.getImportInfo() != null);
    table.setCreator(userName);
    mdqDao.insertTable(table);
    List<MdqTableFieldsInfoBO> tableFieldsInfo = mdqTableBO.getTableFieldsInfo();
    List<MdqField> mdqFieldList =
        DomainCoversionUtils.mdqTableFieldsInfoBOListToMdqFieldList(tableFieldsInfo, table.getId());
    if (table.getPartitionTable() && table.getImport()) {
      // 创建表是导入,并且是分区表的话,自动去掉最后一个ds列
      List<MdqField> collect =
          mdqFieldList.stream().filter(f -> "ds".equals(f.getName())).collect(Collectors.toList());
      if (collect.size() > 1) {
        mdqFieldList.remove(collect.get(1));
      }
    }
    if (!table.getPartitionTable()) {
      mdqFieldList.stream()
          .filter(MdqField::getPartitionField)
          .forEach(mdqField -> mdqField.setPartitionField(false));
    }
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

  @DataSource(name = DSEnum.SECONDE_DATA_SOURCE)
  public void checkIfNeedDeleteTable(MdqTableBO mdqTableBO) {
    String database = mdqTableBO.getTableBaseInfo().getBase().getDatabase();
    String tableName = mdqTableBO.getTableBaseInfo().getBase().getName();
    MdqTable oldTable = mdqDao.selectTableForUpdate(database, tableName);
    boolean isPartitionsTabble = mdqTableBO.getTableBaseInfo().getBase().getPartitionTable();
    boolean isImport = mdqTableBO.getImportInfo() != null;
    Integer importType = null;
    if (isImport) {
      importType = mdqTableBO.getImportInfo().getImportType();
    }
    logger.info(
        "库名:"
            + database
            + "表名:"
            + tableName
            + "是否是分区:"
            + isPartitionsTabble
            + "是否是导入创建:"
            + isImport
            + "导入类型:"
            + importType);
    if (oldTable != null) {
      if (isImport
          && (importType == MdqImportType.Csv.ordinal()
              || importType == MdqImportType.Excel.ordinal())) {
        String destination = mdqTableBO.getImportInfo().getDestination();
        HashMap hashMap = new Gson().fromJson(destination, HashMap.class);
        if (Boolean.valueOf(hashMap.get("importData").toString())) {
          logger.info(
              "Simply add a partition column without dropping the original table(只是单纯增加分区列，不删除掉原来的表)");
          return;
        }
      }
      logger.info(
          "This will overwrite the tables originally created through the wizard(将覆盖掉原来通过向导建立的表):"
              + oldTable);
      mdqDao.deleteTableBaseInfo(oldTable.getId());
    }
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public MdqTableStatisticInfoVO getTableStatisticInfo(
      MetadataQueryParam queryParam, String partitionSort) throws IOException {
    MdqTableStatisticInfoVO mdqTableStatisticInfoVO =
        getTableStatisticInfoFromHive(queryParam, partitionSort);
    return mdqTableStatisticInfoVO;
  }

  @Override
  public String displaysql(MdqTableBO mdqTableBO) {
    String dbName = mdqTableBO.getTableBaseInfo().getBase().getDatabase();
    String tableName = mdqTableBO.getTableBaseInfo().getBase().getName();
    String displayStr = "//The backend of Linkis is creating a new database table for you";
    return displayStr;
  }

  @DataSource(name = DSEnum.SECONDE_DATA_SOURCE)
  @Override
  public boolean isExistInMdq(String database, String tableName, String user) {
    // 查询mdq表，而且active需要为true
    MdqTable table = mdqDao.selectTableByName(database, tableName, user);
    return table != null;
  }

  @DataSource(name = DSEnum.SECONDE_DATA_SOURCE)
  @Override
  public MdqTableBaseInfoVO getTableBaseInfoFromMdq(
      String database, String tableName, String user) {
    MdqTable table = mdqDao.selectTableByName(database, tableName, user);
    return DomainCoversionUtils.mdqTableToMdqTableBaseInfoVO(table);
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public boolean isExistInHive(MetadataQueryParam queryParam) {
    List<Map<String, Object>> tables =
        hiveMetaWithPermissionService.getTablesByDbNameAndOptionalUserName(queryParam);
    Optional<Map<String, Object>> tableOptional =
        tables
            .parallelStream()
            .filter(f -> queryParam.getTableName().equals(f.get("NAME")))
            .findFirst();
    return tableOptional.isPresent();
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public MdqTableBaseInfoVO getTableBaseInfoFromHive(MetadataQueryParam queryParam) {
    List<Map<String, Object>> tables =
        hiveMetaWithPermissionService.getTablesByDbNameAndOptionalUserName(queryParam);
    List<Map<String, Object>> partitionKeys;
    if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
      partitionKeys = hiveMetaDao.getPartitionKeys(queryParam);
    } else {
      partitionKeys = hiveMetaDao.getPartitionKeysSlave(queryParam);
    }
    Optional<Map<String, Object>> tableOptional =
        tables
            .parallelStream()
            .filter(f -> queryParam.getTableName().equals(f.get("NAME")))
            .findFirst();
    Map<String, Object> table =
        tableOptional.orElseThrow(() -> new IllegalArgumentException("table不存在"));
    MdqTableBaseInfoVO mdqTableBaseInfoVO =
        DomainCoversionUtils.mapToMdqTableBaseInfoVO(table, queryParam.getDbName());
    String tableComment;
    if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
      tableComment = hiveMetaDao.getTableComment(queryParam.getDbName(), queryParam.getTableName());
    } else {
      tableComment =
          hiveMetaDao.getTableCommentSlave(queryParam.getDbName(), queryParam.getTableName());
    }
    mdqTableBaseInfoVO.getBase().setComment(tableComment);
    mdqTableBaseInfoVO.getBase().setPartitionTable(!partitionKeys.isEmpty());
    return mdqTableBaseInfoVO;
  }

  @DataSource(name = DSEnum.SECONDE_DATA_SOURCE)
  @Override
  public List<MdqTableFieldsInfoVO> getTableFieldsInfoFromMdq(
      String database, String tableName, String user) {
    MdqTable table = mdqDao.selectTableByName(database, tableName, user);
    List<MdqField> mdqFieldList = mdqDao.listMdqFieldByTableId(table.getId());
    return DomainCoversionUtils.mdqFieldListToMdqTableFieldsInfoVOList(mdqFieldList);
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public List<MdqTableFieldsInfoVO> getTableFieldsInfoFromHive(MetadataQueryParam queryParam) {
    List<Map<String, Object>> columns;
    List<Map<String, Object>> partitionKeys;
    if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
      columns = hiveMetaDao.getColumns(queryParam);
      partitionKeys = hiveMetaDao.getPartitionKeys(queryParam);
    } else {
      columns = hiveMetaDao.getColumnsSlave(queryParam);
      partitionKeys = hiveMetaDao.getPartitionKeysSlave(queryParam);
    }
    List<MdqTableFieldsInfoVO> normalColumns =
        DomainCoversionUtils.normalColumnListToMdqTableFieldsInfoVOList(columns);
    List<MdqTableFieldsInfoVO> partitions =
        DomainCoversionUtils.partitionColumnListToMdqTableFieldsInfoVOList(partitionKeys);
    normalColumns.addAll(partitions);
    return normalColumns;
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public MdqTableStatisticInfoVO getTableStatisticInfoFromHive(
      MetadataQueryParam queryParam, String partitionSort) throws IOException {
    List<String> partitions;
    if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
      partitions = hiveMetaDao.getPartitions(queryParam);
    } else {
      partitions = hiveMetaDao.getPartitionsSlave(queryParam);
    }
    MdqTableStatisticInfoVO mdqTableStatisticInfoVO = new MdqTableStatisticInfoVO();
    mdqTableStatisticInfoVO.setRowNum(0); // 下个版本
    mdqTableStatisticInfoVO.setTableLastUpdateTime(null);
    mdqTableStatisticInfoVO.setFieldsNum(getTableFieldsInfoFromHive(queryParam).size());

    String tableLocation = getTableLocation(queryParam);
    mdqTableStatisticInfoVO.setTableSize(getTableSize(tableLocation));
    mdqTableStatisticInfoVO.setFileNum(getTableFileNum(tableLocation));
    if (partitions.isEmpty()) {
      // 非分区表
      mdqTableStatisticInfoVO.setPartitionsNum(0);
    } else {
      // 分区表
      mdqTableStatisticInfoVO.setPartitionsNum(getPartitionsNum(tableLocation));
      mdqTableStatisticInfoVO.setPartitions(
          getMdqTablePartitionStatisticInfoVO(partitions, "", partitionSort));
    }
    return mdqTableStatisticInfoVO;
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public MdqTablePartitionStatisticInfoVO getPartitionStatisticInfo(
      MetadataQueryParam queryParam, String partitionPath) throws IOException {
    String tableLocation = getTableLocation(queryParam);
    logger.info("start to get partitionStatisticInfo,path:{}", tableLocation + partitionPath);
    return create(tableLocation + partitionPath);
  }

  public List<MdqTablePartitionStatisticInfoVO> getMdqTablePartitionStatisticInfoVO(
      List<String> partitions, String partitionPath, String partitionSort) {
    // part_name(year=2020/day=0605) => MdqTablePartitionStatisticInfoVO 这里只是返回name，没有相关的分区统计数据
    ArrayList<MdqTablePartitionStatisticInfoVO> statisticInfoVOS = new ArrayList<>();
    Map<String, List<Tunple<String, String>>> partitionsStr =
        partitions.stream()
            .map(this::splitStrByFirstSlanting)
            .filter(Objects::nonNull) // 去掉null的
            .collect(Collectors.groupingBy(Tunple::getKey));
    partitionsStr.forEach(
        (k, v) -> {
          MdqTablePartitionStatisticInfoVO mdqTablePartitionStatisticInfoVO =
              new MdqTablePartitionStatisticInfoVO();
          mdqTablePartitionStatisticInfoVO.setName(k);
          String subPartitionPath = String.format("%s/%s", partitionPath, k);
          mdqTablePartitionStatisticInfoVO.setPartitionPath(subPartitionPath);
          List<String> subPartitions =
              v.stream().map(Tunple::getValue).collect(Collectors.toList());
          List<MdqTablePartitionStatisticInfoVO> childrens =
              getMdqTablePartitionStatisticInfoVO(subPartitions, subPartitionPath, partitionSort);
          // 排序
          if ("asc".equals(partitionSort)) {
            childrens =
                childrens.stream()
                    .sorted(Comparator.comparing(MdqTablePartitionStatisticInfoVO::getName))
                    .collect(Collectors.toList());
          } else {
            childrens =
                childrens.stream()
                    .sorted(
                        Comparator.comparing(MdqTablePartitionStatisticInfoVO::getName).reversed())
                    .collect(Collectors.toList());
          }
          mdqTablePartitionStatisticInfoVO.setChildrens(childrens);
          statisticInfoVOS.add(mdqTablePartitionStatisticInfoVO);
        });
    return statisticInfoVOS;
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  public MdqTableStatisticInfoDTO getTableInfo(MetadataQueryParam queryParam) throws IOException {
    MdqTableStatisticInfoDTO mdqTableStatisticInfoDTO = new MdqTableStatisticInfoDTO();
    mdqTableStatisticInfoDTO.setRowNum(0); // 下个版本
    mdqTableStatisticInfoDTO.setTableLastUpdateTime(null);
    mdqTableStatisticInfoDTO.setFieldsNum(getTableFieldsInfoFromHive(queryParam).size());
    String tableLocation = getTableLocation(queryParam);
    mdqTableStatisticInfoDTO.setTableSize(getTableSize(tableLocation));
    mdqTableStatisticInfoDTO.setFileNum(getTableFileNum(tableLocation));
    return mdqTableStatisticInfoDTO;
  }

  /**
   * 将分区string year=2020/day=0605/time=006 转成Tunple(year=2020,day=0605/time=006) 这种形式
   *
   * @param str
   * @return
   */
  private Tunple<String, String> splitStrByFirstSlanting(String str) {
    if (StringUtils.isBlank(str)) {
      return null;
    }
    int index = str.indexOf("/");
    if (index == -1) {
      return new Tunple<>(str, null);
    } else {
      return new Tunple<>(str.substring(0, index), str.substring(index + 1));
    }
  }

  private MdqTablePartitionStatisticInfoVO create(String path) throws IOException {
    MdqTablePartitionStatisticInfoVO mdqTablePartitionStatisticInfoVO =
        new MdqTablePartitionStatisticInfoVO();
    mdqTablePartitionStatisticInfoVO.setName(new Path(path).getName());
    mdqTablePartitionStatisticInfoVO.setFileNum(getTableFileNum(path));
    mdqTablePartitionStatisticInfoVO.setPartitionSize(getTableSize(path));
    mdqTablePartitionStatisticInfoVO.setModificationTime(getTableModificationTime(path));

    return mdqTablePartitionStatisticInfoVO;
  }

  private Date getTableModificationTime(String tableLocation) throws IOException {
    if (StringUtils.isNotBlank(tableLocation) && getRootHdfs().exists(new Path(tableLocation))) {
      FileStatus tableFile = getFileStatus(tableLocation);
      return new Date(tableFile.getModificationTime());
    }
    return null;
  }

  private int getPartitionsNum(String tableLocation) throws IOException {
    int partitionsNum = 0;
    if (StringUtils.isNotBlank(tableLocation) && getRootHdfs().exists(new Path(tableLocation))) {
      FileStatus tableFile = getFileStatus(tableLocation);
      partitionsNum = getRootHdfs().listStatus(tableFile.getPath()).length;
    }
    return partitionsNum;
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  public String getTableLocation(MetadataQueryParam queryParam) {
    String tableLocation;
    if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
      tableLocation = hiveMetaDao.getLocationByDbAndTable(queryParam);
    } else {
      tableLocation = hiveMetaDao.getLocationByDbAndTableSlave(queryParam);
    }
    logger.info("tableLocation:" + tableLocation);
    return tableLocation;
  }

  private int getTableFileNum(String tableLocation) throws IOException {
    int tableFileNum = 0;
    if (StringUtils.isNotBlank(tableLocation) && getRootHdfs().exists(new Path(tableLocation))) {
      FileStatus tableFile = getFileStatus(tableLocation);
      tableFileNum = (int) getRootHdfs().getContentSummary(tableFile.getPath()).getFileCount();
    }
    return tableFileNum;
  }

  private String getTableSize(String tableLocation) throws IOException {
    return getTableSizeWithRetry(tableLocation, 0);
  }

  private String getTableSizeWithRetry(String tableLocation, int retryCount) throws IOException {
    try {
      String tableSize = "0B";
      if (StringUtils.isNotBlank(tableLocation) && getRootHdfs().exists(new Path(tableLocation))) {
        FileStatus tableFile = getFileStatus(tableLocation);
        tableSize =
            ByteTimeUtils.bytesToString(
                getRootHdfs().getContentSummary(tableFile.getPath()).getLength());
      }
      return tableSize;
    } catch (IOException e) {
      String message = e.getMessage();
      String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
      if (retryCount <= MdqConfiguration.HDFS_INIT_MAX_RETRY_COUNT().getValue()
          && (message != null && message.matches(DWSConfig.HDFS_FILE_SYSTEM_REST_ERRS)
              || rootCauseMessage.matches(DWSConfig.HDFS_FILE_SYSTEM_REST_ERRS))) {
        logger.info("Failed to get tableSize, retry", e);
        resetRootHdfs();
        return getTableSizeWithRetry(tableLocation, retryCount + 1);
      } else {
        throw e;
      }
    }
  }

  private static volatile FileSystem rootHdfs = null;

  private FileStatus getFileStatus(String location) throws IOException {
    try {
      return getRootHdfs().getFileStatus(new Path(location));
    } catch (IOException e) {
      String message = e.getMessage();
      String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
      if (message != null && message.matches(DWSConfig.HDFS_FILE_SYSTEM_REST_ERRS)
          || rootCauseMessage.matches(DWSConfig.HDFS_FILE_SYSTEM_REST_ERRS)) {
        logger.info("Failed to getFileStatus, retry", e);
        resetRootHdfs();
        return getFileStatus(location);
      } else {
        throw e;
      }
    }
  }

  private void resetRootHdfs() {
    if (rootHdfs != null) {
      synchronized (this) {
        if (rootHdfs != null) {
          IOUtils.closeQuietly(rootHdfs);
          logger.info("reset RootHdfs");
          rootHdfs = HDFSUtils.getHDFSRootUserFileSystem();
        }
      }
    }
  }

  private FileSystem getRootHdfs() {
    if (rootHdfs == null) {
      synchronized (this) {
        if (rootHdfs == null) {
          rootHdfs = HDFSUtils.getHDFSRootUserFileSystem();
          KerberosUtils.startKerberosRefreshThread();
        }
      }
    }
    return rootHdfs;
  }
}
