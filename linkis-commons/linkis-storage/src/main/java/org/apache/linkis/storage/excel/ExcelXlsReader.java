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

package org.apache.linkis.storage.excel;

import org.apache.poi.hssf.eventusermodel.*;
import org.apache.poi.hssf.eventusermodel.EventWorkbookBuilder.SheetRecordCollectingListener;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelXlsReader implements HSSFListener {
  private static final Logger logger = LoggerFactory.getLogger(ExcelXlsReader.class);

  private int minColumns = -1;

  private POIFSFileSystem fs;

  private InputStream inputStream;

  private int lastRowNumber;

  private int lastColumnNumber;

  /** Should we output the formula, or the value it has? */
  private boolean outputFormulaValues = true;

  /** For parsing Formulas */
  private SheetRecordCollectingListener workbookBuildingListener;

  // excel2003Workbook(工作薄)
  private HSSFWorkbook stubWorkbook;

  // Records we pick up as we process
  private SSTRecord sstRecord;

  private FormatTrackingHSSFListener formatListener;

  // Table index(表索引)
  private int sheetIndex = -1;

  private BoundSheetRecord[] orderedBSRs;

  @SuppressWarnings("unchecked")
  private ArrayList boundSheetRecords = new ArrayList();

  // For handling formulas with string results
  private int nextRow;

  private int nextColumn;

  private boolean outputNextStringRecord;

  // Current line(当前行)
  private int curRow = 0;

  // a container that stores row records(存储行记录的容器)
  private List<String> rowlist = new ArrayList<String>();

  @SuppressWarnings("unused")
  private String sheetName;

  private IExcelRowDeal excelRowDeal;

  public void init(IExcelRowDeal excelRowDeal, InputStream inputStream) {
    this.excelRowDeal = excelRowDeal;
    this.inputStream = inputStream;
  }

  /**
   * Traverse all the sheets under excel 遍历excel下所有的sheet
   *
   * @throws IOException
   */
  public void process() throws IOException {
    this.fs = new POIFSFileSystem(this.inputStream);
    MissingRecordAwareHSSFListener listener = new MissingRecordAwareHSSFListener(this);
    formatListener = new FormatTrackingHSSFListener(listener);
    HSSFEventFactory factory = new HSSFEventFactory();
    HSSFRequest request = new HSSFRequest();
    if (outputFormulaValues) {
      request.addListenerForAllRecords(formatListener);
    } else {
      workbookBuildingListener = new SheetRecordCollectingListener(formatListener);
      request.addListenerForAllRecords(workbookBuildingListener);
    }
    factory.processWorkbookEvents(request, fs);
  }

  /** HSSFListener listener method, processing Record HSSFListener 监听方法，处理 Record */
  @Override
  @SuppressWarnings("unchecked")
  public void processRecord(Record record) {
    int thisRow = -1;
    int thisColumn = -1;
    String thisStr = null;
    String value = null;
    switch (record.getSid()) {
      case BoundSheetRecord.sid:
        boundSheetRecords.add(record);
        break;
      case BOFRecord.sid:
        BOFRecord br = (BOFRecord) record;
        if (br.getType() == BOFRecord.TYPE_WORKSHEET) {
          // Create a child workbook if needed(如果有需要，则建立子工作薄)
          if (workbookBuildingListener != null && stubWorkbook == null) {
            stubWorkbook = workbookBuildingListener.getStubHSSFWorkbook();
          }

          sheetIndex++;
          if (orderedBSRs == null) {
            orderedBSRs = BoundSheetRecord.orderByBofPosition(boundSheetRecords);
          }
          sheetName = orderedBSRs[sheetIndex].getSheetname();
        }
        break;

      case SSTRecord.sid:
        sstRecord = (SSTRecord) record;
        break;

      case BlankRecord.sid:
        BlankRecord brec = (BlankRecord) record;
        thisRow = brec.getRow();
        thisColumn = brec.getColumn();
        thisStr = "";
        rowlist.add(thisColumn, thisStr);
        break;
      case BoolErrRecord.sid: // Cell is boolean(单元格为布尔类型)
        BoolErrRecord berec = (BoolErrRecord) record;
        thisRow = berec.getRow();
        thisColumn = berec.getColumn();
        thisStr = berec.getBooleanValue() + "";
        rowlist.add(thisColumn, thisStr);
        break;

      case FormulaRecord.sid: // Cell is a formula type(单元格为公式类型)
        FormulaRecord frec = (FormulaRecord) record;
        thisRow = frec.getRow();
        thisColumn = frec.getColumn();
        if (outputFormulaValues) {
          if (Double.isNaN(frec.getValue())) {
            // Formula result is a string
            // This is stored in the next record
            outputNextStringRecord = true;
            nextRow = frec.getRow();
            nextColumn = frec.getColumn();
          } else {
            thisStr = formatListener.formatNumberDateCell(frec);
          }
        } else {
          thisStr =
              '"'
                  + HSSFFormulaParser.toFormulaString(stubWorkbook, frec.getParsedExpression())
                  + '"';
        }
        rowlist.add(thisColumn, thisStr);
        break;
      case StringRecord.sid: // a string of formulas in a cell(单元格中公式的字符串)
        if (outputNextStringRecord) {
          // String for formula
          StringRecord srec = (StringRecord) record;
          thisStr = srec.getString();
          thisRow = nextRow;
          thisColumn = nextColumn;
          outputNextStringRecord = false;
        }
        break;
      case LabelRecord.sid:
        LabelRecord lrec = (LabelRecord) record;
        curRow = thisRow = lrec.getRow();
        thisColumn = lrec.getColumn();
        value = lrec.getValue().trim();
        value = value.equals("") ? " " : value;
        this.rowlist.add(thisColumn, value);
        break;
      case LabelSSTRecord.sid: // Cell is a string type(单元格为字符串类型)
        LabelSSTRecord lsrec = (LabelSSTRecord) record;
        curRow = thisRow = lsrec.getRow();
        thisColumn = lsrec.getColumn();
        if (sstRecord == null) {
          rowlist.add(thisColumn, " ");
        } else {
          value = sstRecord.getString(lsrec.getSSTIndex()).toString().trim();
          value = value.equals("") ? " " : value;
          rowlist.add(thisColumn, value);
        }
        break;
      case NumberRecord.sid: // Cell is a numeric type(单元格为数字类型)
        NumberRecord numrec = (NumberRecord) record;
        curRow = thisRow = numrec.getRow();
        thisColumn = numrec.getColumn();
        value = formatListener.formatNumberDateCell(numrec).trim();
        value = value.equals("") ? "0" : value;
        // Add column values to the container(向容器加入列值)
        rowlist.add(thisColumn, value);
        break;
      default:
        break;
    }

    // Encountered a new line of operations(遇到新行的操作)(
    if (thisRow != -1 && thisRow != lastRowNumber) {
      lastColumnNumber = -1;
    }

    // Null operation(空值的操作)
    if (record instanceof MissingCellDummyRecord) {
      MissingCellDummyRecord mc = (MissingCellDummyRecord) record;
      curRow = thisRow = mc.getRow();
      thisColumn = mc.getColumn();
      rowlist.add(thisColumn, " ");
    }

    // Update row and column values(更新行和列的值)
    if (thisRow > -1) lastRowNumber = thisRow;
    if (thisColumn > -1) lastColumnNumber = thisColumn;

    // End of line operation(行结束时的操作)
    if (record instanceof LastCellOfRowDummyRecord) {
      if (minColumns > 0) {
        // Column value is re-empted(列值重新置空)
        if (lastColumnNumber == -1) {
          lastColumnNumber = 0;
        }
      }
      lastColumnNumber = -1;

      // At the end of each line, the dealRow() method(每行结束时， dealRow() 方法)
      excelRowDeal.dealRow(orderedBSRs, sheetIndex, curRow, rowlist);
      // Empty container(清空容器)
      rowlist.clear();
    }
  }

  public void close() {
    try {
      if (fs != null) {
        fs.close();
      }
    } catch (Exception e) {
      logger.info("ExcelXlsReader fs closed failed", e);
    }

    try {
      if (inputStream != null) {
        inputStream.close();
      }
    } catch (IOException e) {
      logger.info("ExcelXlsReader inputStream closed failed", e);
    }
  }
}
