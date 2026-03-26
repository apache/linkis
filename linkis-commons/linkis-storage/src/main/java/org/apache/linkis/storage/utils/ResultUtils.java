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

package org.apache.linkis.storage.utils;

import org.apache.linkis.common.io.FsWriter;
import org.apache.linkis.storage.conf.LinkisStorageConf;
import org.apache.linkis.storage.entity.FieldTruncationResult;
import org.apache.linkis.storage.entity.OversizedFieldInfo;
import org.apache.linkis.storage.excel.StorageExcelWriter;
import org.apache.linkis.storage.resultset.table.TableMetaData;
import org.apache.linkis.storage.resultset.table.TableRecord;
import org.apache.linkis.storage.source.FileSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultUtils {

  public static final Logger LOGGER = LoggerFactory.getLogger(ResultUtils.class);
  /**
   * 删除指定字段的内容
   *
   * @param metadata 元数据数组，包含字段信息
   * @param contentList 需要处理的二维字符串数组
   * @param fieldsToRemove 需要删除的字段集合
   * @return 处理后的字符串数组，若输入无效返回空集合而非null
   */
  @SuppressWarnings("unchecked")
  public static List<String[]> removeFieldsFromContent(
      Object metadata, List<String[]> contentList, Set<String> fieldsToRemove) {
    // 1. 参数校验
    if (metadata == null
        || fieldsToRemove == null
        || fieldsToRemove.isEmpty()
        || contentList == null
        || !(metadata instanceof Map[])) {
      return contentList;
    }

    // 2. 安全类型转换
    Map<String, Object>[] fieldMetadata = (Map<String, Object>[]) metadata;

    // 3. 收集需要删除的列索引（去重并排序）
    List<Integer> columnsToRemove =
        IntStream.range(0, fieldMetadata.length)
            .filter(
                i -> {
                  Map<String, Object> meta = fieldMetadata[i];
                  Object columnName = meta.get("columnName");
                  return columnName != null
                      && fieldsToRemove.contains(columnName.toString().toLowerCase());
                })
            .distinct()
            .boxed()
            .sorted((a, b) -> Integer.compare(b, a))
            .collect(Collectors.toList());

    // 如果没有需要删除的列，直接返回副本
    if (columnsToRemove.isEmpty()) {
      return new ArrayList<>(contentList);
    }
    // 4. 对每行数据进行处理（删除指定列）
    return contentList.stream()
        .map(
            row -> {
              if (row == null || row.length == 0) {
                return row;
              }
              // 创建可变列表以便删除元素
              List<String> rowList = new ArrayList<>(Arrays.asList(row));
              // 从后向前删除列，避免索引变化问题
              for (int columnIndex : columnsToRemove) {
                if (columnIndex < rowList.size()) {
                  rowList.remove(columnIndex);
                  LOGGER.info("MaskedField  Remove Data columnIndex:" + columnIndex);
                }
              }
              return rowList.toArray(new String[0]);
            })
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  public static Map[] filterMaskedFieldsFromMetadata(Object metadata, Set<String> maskedFields) {
    // 1. 参数校验
    if (metadata == null || maskedFields == null || !(metadata instanceof Map[])) {
      return new Map[0];
    }

    // 2. 类型转换（已通过校验，可安全强转）
    Map<String, Object>[] originalMaps = (Map<String, Object>[]) metadata;

    // 3. 过滤逻辑（提取谓词增强可读性）
    Predicate<Map<String, Object>> isNotMaskedField =
        map -> !maskedFields.contains(map.get("columnName").toString().toLowerCase());

    // 4. 流处理 + 结果转换
    return Arrays.stream(originalMaps)
        .filter(isNotMaskedField)
        .toArray(Map[]::new); // 等价于 toArray(new Map[0])
  }

  /**
   * Convert Map array to TableMetaData
   *
   * @param metadataArray Array of Map containing column information
   * @return TableMetaData object
   */
  @SuppressWarnings("unchecked")
  public static TableMetaData convertMapArrayToTableMetaData(Map[] metadataArray) {
    if (metadataArray == null || metadataArray.length == 0) {
      return new TableMetaData(new org.apache.linkis.storage.domain.Column[0]);
    }

    org.apache.linkis.storage.domain.Column[] columns =
        new org.apache.linkis.storage.domain.Column[metadataArray.length];

    for (int i = 0; i < metadataArray.length; i++) {
      Map<String, Object> columnMap = (Map<String, Object>) metadataArray[i];
      String columnName =
          columnMap.get("columnName") != null ? columnMap.get("columnName").toString() : "";
      String dataType =
          columnMap.get("dataType") != null ? columnMap.get("dataType").toString() : "string";
      String comment = columnMap.get("comment") != null ? columnMap.get("comment").toString() : "";

      // Create Column object
      org.apache.linkis.storage.domain.DataType dtype =
          org.apache.linkis.storage.domain.DataType$.MODULE$.toDataType(dataType);
      columns[i] = new org.apache.linkis.storage.domain.Column(columnName, dtype, comment);
    }

    return new TableMetaData(columns);
  }

  public static void dealMaskedField(
      String maskedFieldNames, FsWriter<?, ?> fsWriter, FileSource fileSource) throws IOException {

    LOGGER.info("Applying field masking for fields: {}", maskedFieldNames);

    // Parse masked field names
    Set<String> maskedFieldsSet =
        Arrays.stream(maskedFieldNames.split(","))
            .map(String::trim)
            .map(String::toLowerCase)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toSet());

    // Collect data from file source
    Pair<Object, ArrayList<String[]>>[] collectedData = fileSource.collect();

    // Process each result set
    for (int i = 0; i < collectedData.length; i++) {
      Pair<Object, ArrayList<String[]>> collectedDataSet = collectedData[i];
      Object metadata = collectedDataSet.getFirst();
      ArrayList<String[]> content = collectedDataSet.getSecond();

      // Filter metadata and content
      Map[] filteredMetadata = filterMaskedFieldsFromMetadata(metadata, maskedFieldsSet);
      List<String[]> filteredContent = removeFieldsFromContent(metadata, content, maskedFieldsSet);

      // Convert Map[] to TableMetaData
      TableMetaData tableMetaData = convertMapArrayToTableMetaData(filteredMetadata);

      // Write filtered data
      fsWriter.addMetaData(tableMetaData);
      for (String[] row : filteredContent) {
        fsWriter.addRecord(new TableRecord(row));
      }
      LOGGER.info(
          "Field masking applied for result set {}. Original columns: {}, Filtered columns: {}",
          i,
          ((Map[]) metadata).length,
          filteredMetadata.length);
    }
  }

  /**
   * Detect and handle oversized fields in result set
   *
   * @param metadata Column names list
   * @param FileContent Data rows list (each row is an ArrayList or Object[])
   * @param truncate Whether to truncate (false means detection only)
   * @return FieldTruncationResult containing detection results and processed data
   */
  public static FieldTruncationResult detectAndHandle(
      Object metadata, List<String[]> FileContent, Integer maxLength, boolean truncate) {
    if (metadata == null || !(metadata instanceof Map[])) {
      return new FieldTruncationResult();
    }

    // 2. 类型转换（已通过校验，可安全强转）
    Map<String, Object>[] originalMaps = (Map<String, Object>[]) metadata;

    // 提取列名
    List<String> columnNames = new ArrayList<>();
    if (metadata != null) {
      for (Map meta : originalMaps) {
        Object columnName = meta.get("columnName");
        columnNames.add(columnName != null ? columnName.toString() : "");
      }
    }

    // 转换 String[] 数组为 ArrayList<String>
    List<ArrayList<String>> dataList = new ArrayList<>();
    for (String[] row : FileContent) {
      ArrayList<String> rowList = new ArrayList<>(Arrays.asList(row));
      dataList.add(rowList);
    }

    int maxCount = LinkisStorageConf.OVERSIZED_FIELD_MAX_COUNT();

    // Detect oversized fields
    List<OversizedFieldInfo> oversizedFields =
        detectOversizedFields(columnNames, dataList, maxLength, maxCount);

    boolean hasOversizedFields = !oversizedFields.isEmpty();

    // Truncate if requested
    List<ArrayList<String>> processedData = dataList;
    LOGGER.info(
        "Staring Field truncation detection function ,truncate is {},hasOversizedFields {}",
        truncate,
        hasOversizedFields);
    if (truncate && hasOversizedFields) {
      processedData = truncateFields(columnNames, dataList, maxLength);
    }
    List<String[]> convertedList =
        processedData.stream()
            .map(row -> row != null ? row.toArray(new String[0]) : null)
            .collect(Collectors.toList());
    return new FieldTruncationResult(hasOversizedFields, oversizedFields, maxCount, convertedList);
  }

  public static void detectAndHandle(
      StorageExcelWriter fsWriter, FileSource fileSource, Integer maxLength) throws IOException {
    // Collect data from file source
    Pair<Object, ArrayList<String[]>>[] collectedData = fileSource.collect();

    // Process each result set
    for (int i = 0; i < collectedData.length; i++) {
      Pair<Object, ArrayList<String[]>> collectedDataSet = collectedData[i];
      Object metadata = collectedDataSet.getFirst();
      ArrayList<String[]> content = collectedDataSet.getSecond();

      FieldTruncationResult fieldTruncationResult =
          detectAndHandle(metadata, content, maxLength, true);

      List<String[]> data = fieldTruncationResult.getData();

      // Convert Map[] to TableMetaData and add truncation markers for oversized fields
      TableMetaData tableMetaData =
          convertMapArrayToTableMetaData((Map<String, Object>[]) metadata);
      // Create a set of oversized field names for quick lookup
      List<String> oversizedFieldNames =
          fieldTruncationResult.getOversizedFields().stream()
              .map(OversizedFieldInfo::getFieldName)
              .distinct()
              .collect(Collectors.toList());
      // If there are oversized fields, add markers to column names in the metadata
      if (fieldTruncationResult.isHasOversizedFields()
          && fieldTruncationResult.getOversizedFields() != null) {
        // Update column names to indicate truncation with max length
        org.apache.linkis.storage.domain.Column[] columns = tableMetaData.columns();
        for (int j = 0; j < columns.length; j++) {
          if (oversizedFieldNames.contains(columns[j].columnName())) {
            // Get the max length for this field
            String truncatedInfo =
                maxLength != null ? "(truncated to " + maxLength + " chars)" : "(truncated)";
            // Create a new column with truncation info suffix to indicate truncation
            columns[j] =
                new org.apache.linkis.storage.domain.Column(
                    columns[j].columnName() + truncatedInfo,
                    columns[j].dataType(),
                    columns[j].comment());
          }
        }

        // Create new TableMetaData with updated column names
        tableMetaData = new TableMetaData(columns);
      }
      // Write filtered data
      if (oversizedFieldNames.isEmpty()) {
        fsWriter.addMetaData(tableMetaData);
      } else {
        StringJoiner joiner = new StringJoiner(",");
        oversizedFieldNames.forEach(joiner::add);
        String note =
            MessageFormat.format(
                "结果集存在字段值超过{0}字符，无法全量下载，以下字段截取前{0}字符展示：{1}",
                LinkisStorageConf.FIELD_EXPORT_DOWNLOAD_LENGTH(), joiner);
        fsWriter.addMetaDataWithNote(tableMetaData, note);
      }
      for (String[] row : data) {
        fsWriter.addRecord(new TableRecord(row));
      }
    }
  }

  /**
   * Detect oversized fields
   *
   * @param metadata Column names
   * @param dataList Data rows
   * @param maxLength Max length threshold
   * @param maxCount Max number of oversized fields to collect
   * @return List of oversized field info
   */
  private static List<OversizedFieldInfo> detectOversizedFields(
      List<String> metadata, List<ArrayList<String>> dataList, int maxLength, int maxCount) {

    List<OversizedFieldInfo> oversizedFields = new ArrayList<>();

    if (metadata == null || dataList == null || dataList.isEmpty()) {
      return oversizedFields;
    }

    // 使用Set来存储已经检查过的超长字段名，避免重复检查
    Set<String> detectedOversizedFields = new HashSet<>();

    // Iterate through data rows
    for (int rowIndex = 0; rowIndex < dataList.size(); rowIndex++) {

      ArrayList<String> row = dataList.get(rowIndex);
      if (row == null) {
        continue;
      }

      // Check each field in the row
      for (int colIndex = 0; colIndex < row.size() && colIndex < metadata.size(); colIndex++) {

        String fieldName = metadata.get(colIndex);

        // 如果该字段已经被检测为超长字段，则跳过检查，提高效率
        if (detectedOversizedFields.contains(fieldName)) {
          continue;
        }

        String fieldValue = row.get(colIndex);
        int fieldLength = getFieldLength(fieldValue);

        if (fieldLength > maxLength) {
          oversizedFields.add(new OversizedFieldInfo(fieldName, rowIndex, fieldLength, maxLength));
          // 将超长字段名加入Set，避免重复检查
          detectedOversizedFields.add(fieldName);
          LOGGER.info(
              "Detected oversized field: field={}, row={}, actualLength={}, maxLength={}",
              fieldName,
              rowIndex,
              fieldLength,
              maxLength);
        }
      }
    }

    return oversizedFields;
  }

  /**
   * Truncate oversized fields
   *
   * @param metadata Column names
   * @param dataList Data rows
   * @param maxLength Max length
   * @return Truncated data list
   */
  private static List<ArrayList<String>> truncateFields(
      List<String> metadata, List<ArrayList<String>> dataList, int maxLength) {

    if (dataList == null || dataList.isEmpty()) {
      return dataList;
    }

    List<ArrayList<String>> truncatedData = new ArrayList<>();

    for (ArrayList<String> row : dataList) {
      if (row == null) {
        truncatedData.add(null);
        continue;
      }

      ArrayList<String> truncatedRow = new ArrayList<>();
      for (String fieldValue : row) {
        String truncatedValue = truncateFieldValue(fieldValue, maxLength);
        truncatedRow.add(truncatedValue);
      }
      truncatedData.add(truncatedRow);
    }

    return truncatedData;
  }

  /**
   * Get field value character length
   *
   * @param value Field value
   * @return Character length
   */
  private static int getFieldLength(Object value) {
    if (value == null) {
      return 0;
    }
    return value.toString().length();
  }

  /**
   * Truncate single field value
   *
   * @param value Field value
   * @param maxLength Max length
   * @return Truncated value
   */
  private static String truncateFieldValue(Object value, int maxLength) {
    if (value == null) {
      return null;
    }
    String str = value.toString();
    if (str.length() <= maxLength) {
      return str;
    }
    return str.substring(0, maxLength);
  }

  /**
   * Apply both field masking and truncation
   *
   * @param maskedFieldNames Comma-separated list of field names to mask
   * @param fsWriter The FsWriter to write results to
   * @param fileSource The FileSource to read data from
   * @throws IOException
   */
  public static void applyFieldMaskingAndTruncation(
      String maskedFieldNames,
      StorageExcelWriter fsWriter,
      FileSource fileSource,
      Integer maxLength)
      throws IOException {

    LOGGER.info("Applying both field masking and truncation");
    // First collect data from file source
    Pair<Object, ArrayList<String[]>>[] collectedData = fileSource.collect();

    // Process each result set
    for (int i = 0; i < collectedData.length; i++) {
      Pair<Object, ArrayList<String[]>> collectedDataSet = collectedData[i];
      Object metadata = collectedDataSet.getFirst();
      ArrayList<String[]> content = collectedDataSet.getSecond();

      // Apply field masking
      Set<String> maskedFieldsSet =
          Arrays.stream(maskedFieldNames.split(","))
              .map(String::trim)
              .map(String::toLowerCase)
              .filter(StringUtils::isNotBlank)
              .collect(Collectors.toSet());

      Map[] filteredMetadata = filterMaskedFieldsFromMetadata(metadata, maskedFieldsSet);
      List<String[]> filteredContent = removeFieldsFromContent(metadata, content, maskedFieldsSet);

      // Then apply field truncation
      FieldTruncationResult fieldTruncationResult =
          detectAndHandle(filteredMetadata, filteredContent, maxLength, true);
      List<String[]> finalData = fieldTruncationResult.getData();

      // Write data
      TableMetaData tableMetaData = convertMapArrayToTableMetaData(filteredMetadata);
      // Create a set of oversized field names for quick lookup
      List<String> oversizedFieldNames =
          fieldTruncationResult.getOversizedFields().stream()
              .map(OversizedFieldInfo::getFieldName)
              .distinct()
              .collect(Collectors.toList());
      // If there are oversized fields, add markers to column names in the metadata
      if (fieldTruncationResult.isHasOversizedFields()
          && fieldTruncationResult.getOversizedFields() != null) {
        // Update column names to indicate truncation with max length
        org.apache.linkis.storage.domain.Column[] columns = tableMetaData.columns();
        for (int j = 0; j < columns.length; j++) {
          if (oversizedFieldNames.contains(columns[j].columnName())) {
            // Get the max length for this field
            String truncatedInfo =
                maxLength != null
                    ? MessageFormat.format(LinkisStorageConf.FIELD_TRUNCATION_NOTE(), maxLength)
                    : LinkisStorageConf.FIELD_NOT_TRUNCATION_NOTE();
            // Create a new column with truncation info suffix to indicate truncation
            columns[j] =
                new org.apache.linkis.storage.domain.Column(
                    columns[j].columnName() + truncatedInfo,
                    columns[j].dataType(),
                    columns[j].comment());
          }
        }

        // Create new TableMetaData with updated column names
        tableMetaData = new TableMetaData(columns);
      }
      if (oversizedFieldNames.isEmpty()) {
        fsWriter.addMetaData(tableMetaData);
      } else {
        StringJoiner joiner = new StringJoiner(",");
        oversizedFieldNames.forEach(joiner::add);
        String note =
            MessageFormat.format(
                LinkisStorageConf.FIELD_OPEN_FILE_TRUNCATION_NOTE(),
                LinkisStorageConf.FIELD_EXPORT_DOWNLOAD_LENGTH(),
                joiner);
        fsWriter.addMetaDataWithNote(tableMetaData, note);
      }
      for (String[] row : finalData) {
        fsWriter.addRecord(new TableRecord(row));
      }
      LOGGER.info(
          "Field masking and truncation applied for result set {}. Original columns: {}, Filtered columns: {}, Truncated fields: {}",
          i,
          ((Map[]) metadata).length,
          filteredMetadata.length,
          fieldTruncationResult.getOversizedFields().size());
    }
  }
}
